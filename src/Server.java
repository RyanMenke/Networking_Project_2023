import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Server {

    private Peer peer;
    // Represents connected clients
    private Map<Integer, PeerState> peerStateMap = new HashMap<>();
    private List<ServerSocketConnection> connections = new ArrayList<>();

    /**
     * The configuration for our server is contained in a Peer
     *
     * @param peer
     */
    public Server(Peer peer) {
        this.peer = peer;
    }

    public void run() throws IOException {
        ServerSocket listener = new ServerSocket(peer.getPortNumber());
        try {
            // This is where the unchoking interval is calculated
            pollForPreferredNeighbors();
            if (peer.hasFile()) {
                checkForOptimisticUnchoke();
            }

            while(true) {
                Socket client = listener.accept();
                System.out.println("Socket accepted");
                ServerSocketConnection connection = new ServerSocketConnection(client, peer, new ServerSocketConnection.ConnectedClientInfo() {
                    @Override
                    public void onClientConnected(Handshake handshake) {
                        PeerState state = new PeerState();
                        peerStateMap.put(handshake.getPeerID(), state);
                    }

                    @Override
                    public void clientIsInterested(int peerId) {
                        PeerState state = peerStateMap.get(peerId);
                        if (state != null) {
                            state.setInterested(true);
                        }
                    }

                    @Override
                    public void clientIsNotInterested(int peerId) {
                        PeerState state = peerStateMap.get(peerId);
                        if (state != null) {
                            state.setInterested(false);
                        }
                    }

                    @Override
                    public boolean isInterested(int peerId) {
                        PeerState state = peerStateMap.get(peerId);

                        if (state == null) {
                            return false;
                        }

                        return state.isInterested();
                    }

                    @Override
                    public boolean isChoked(int peerId) {
                        return peerStateMap.get(peerId).isChoked();
                    }

                    @Override
                    public void onDisconnected(ServerSocketConnection connection) {
                        connections.remove(connection);
                    }
                });
                System.out.println("Connection instantiated");
                connection.start();
                System.out.println("Client "  + peer.getPeerId() + " is connected!");
                connections.add(connection);
            }
        } finally {
            listener.close();
        }
    }

    private void checkForOptimisticUnchoke() {
        int interval = peer.getPeerConfig().getOptimisticUnchokingInterval();
        PeerConfiguration config = peer.getPeerConfig();
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);

        Runnable task = () -> {
            // Optimistic Unchoke code
            synchronized (peerStateMap) {
                if (peerStateMap.isEmpty()) {
                    return;
                }
                System.out.println("Am I even optimistically unchoking");

                List<Integer> interestedNeighborKeys = new ArrayList<>();
                for (Map.Entry<Integer, PeerState> entry: peerStateMap.entrySet()) {
                    System.out.println("Optimistic Unchoking For loop PeerId: "  + entry.getKey());
                    System.out.println("Is "  + entry.getKey() + " interested? " + entry.getValue().isInterested());
                    if (entry.getValue().isOptimisticallyUnchoked()) {
                        peerStateMap.get(interestedNeighborKeys.get(0)).setChoked(true);
                        peerStateMap.get(interestedNeighborKeys.get(0)).setOptimisticallyUnchoked(false);
                    }

                    if (entry.getValue().isInterested() && !entry.getValue().isChoked()) {
                        System.out.println("I adding to the optimistic choking list with this peerID: " + entry.getKey());
                        interestedNeighborKeys.add(entry.getKey());
                    }
                }
                if (interestedNeighborKeys.size() > 0) {
                    Collections.shuffle(interestedNeighborKeys);
                    System.out.println("Optimistic Unchoking winner! Is this peerID: " + interestedNeighborKeys.get(0));
                    peerStateMap.get(interestedNeighborKeys.get(0)).setChoked(false);
                    peerStateMap.get(interestedNeighborKeys.get(0)).setOptimisticallyUnchoked(true);
                }
                sendChokeAndUnchokeMessages();
                System.out.println("Running at intervals... (optimistic)");
            }
            // Only optimistically unchoke if Peer has the entire file.
        };


        executor.scheduleAtFixedRate(task, interval, interval, TimeUnit.SECONDS);
    }

    private void pollForPreferredNeighbors() {
        int intervalInSeconds = peer.getPeerConfig().getUnchokingInterval();
        PeerConfiguration config = peer.getPeerConfig();

        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);

        Runnable task = () -> {
            synchronized (peerStateMap) {
                if (peerStateMap.isEmpty()) {
                    return;
                }

                int preferredNeighbors = config.getNumberOfPreferredNeighbors();
                System.out.println("Right before ReAssessNeighbors");
                reAssessNeighbors();
                sendChokeAndUnchokeMessages();

                // Neighbors need to receive unchoke or choke message every interval.
                // Only unchoke if interested.
                // Only optimistically unchoke if Peer has the entire file.

                System.out.println("Running at intervals... (Regular)");
            }
        };


        executor.scheduleAtFixedRate(task, intervalInSeconds, intervalInSeconds, TimeUnit.SECONDS);
    }

    private void reAssessNeighbors() {
        List<Integer> interestedNeighborKeys = new ArrayList<>();
        System.out.println("I am in reAssessNeighbors");
        for (Map.Entry<Integer, PeerState> entry: peerStateMap.entrySet()) {
            System.out.println("This is a peerID: " + entry.getKey());
            if (!entry.getValue().isOptimisticallyUnchoked()) {
                System.out.println("I am choking Temporarily this peerID: " + entry.getKey());
                entry.getValue().setChoked(true);
            }
            if (entry.getValue().isInterested() && !entry.getValue().isOptimisticallyUnchoked()) {
                System.out.println("I made it inside the interested key zone with this peerID: " + entry.getKey());
                interestedNeighborKeys.add(entry.getKey());
                System.out.println("Adding interested Neighbor key to list: " + entry.getKey());
            }
        }

        if (interestedNeighborKeys.size() <= peer.getPeerConfig().getNumberOfPreferredNeighbors()) {
            System.out.println("Am I reaching the code for SMALL neighbors");
            for (int key: interestedNeighborKeys) {
                if (peerStateMap.get(key).isInterested()) {
                    peerStateMap.get(key).setChoked(false);
                    System.out.println(peerStateMap.get(key).isChoked());
                }
            }
        }
        else {
            Collections.shuffle(interestedNeighborKeys);
            for (int i = 0; i < peer.getPeerConfig().getNumberOfPreferredNeighbors(); i++) {
                peerStateMap.get(interestedNeighborKeys.get(i)).setChoked(false);
            }
        }
    }

    private void sendChokeAndUnchokeMessages() {
        for (ServerSocketConnection connection: connections) {
            PeerState state = peerStateMap.get(connection.getClientPeerId());

            if (state != null) {
                System.out.println("This is the state of interest: " + state.isInterested());
                System.out.println("This is the state of Choking: " + state.isChoked());
                if (state.isChoked()) {
                    System.out.println("Am I reaching this choking message code");
                    connection.sendChoke();
                } else {
                    connection.sendUnchoke();
                }
            }
        }
    }

    public void notifyNeighborsOfNewPiece(int index) {
        if (connections.size() > 0) {
            ByteBuffer byteBuffer = ByteBuffer.allocate(Integer.BYTES);

            // Put the int value into the ByteBuffer
            byteBuffer.putInt(index);

            // Retrieve the bytes from the ByteBuffer into a byte array
            byte[] indexAsByte = byteBuffer.array();
            for (ServerSocketConnection connection : connections) {


                connection.addToHaveQueue(index);
                //connection.sendMessage(Message.makeHave(indexAsByte).toBytes());

            }
        }
    }
}
