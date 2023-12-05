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
    private ServerSocket currentServerSocket;
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
        //System.out.println("RUN");
        ServerSocket listener = new ServerSocket(peer.getPortNumber());
        try {
            //System.out.println("INSIDE TRY");
            // This is where the unchoking interval is calculated
            pollForPreferredNeighbors();
            checkForOptimisticUnchoke();

            while(true) {
                //System.out.println("in the while");

                currentServerSocket = listener;
                if (checkIfAllPeersAndSelfHaveFile()){
                    closeConnections();
                    //System.out.println("Made it to closing the server");
                    listener.close();
                    break;
                }


                Socket client = listener.accept();
                //System.out.println("Socket accepted");
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

                    @Override
                    public void addToDownloadRate(int peerId, int rate) {
                        peerStateMap.get(peerId).addToNumberOfBitsInterval(rate);
                    }

                    @Override
                    public void setHasCompleteFile(int peerId) {
                        peerStateMap.get(peerId).setHasCompleteFile(true);
                    }

                    @Override
                    public boolean doAllPeersHaveCompleteFile() {
                        for (Map.Entry<Integer, PeerState> entry: peerStateMap.entrySet()) {
                            if (!entry.getValue().hasCompleteFile()) {
                                return false;
                            }
                        }
                        return true;
                    }


                });
                //System.out.println("Connection instantiated");
                connection.start();
                //System.out.println("Client "  + peer.getPeerId() + " is connected!");
                connections.add(connection);
                //System.out.println("Made it JUST BEFORE closing the server");
            }
            //System.out.println("Floating cloud");
        }
        catch (Exception e) {
            //System.out.println("MADE IT TO CATCH");
            closeConnections();
            listener.close();
        }
//        catch (IOException e) {
//        }
        //This is the finally statement I am Making it into where listener is closed
            finally
         {
            //System.out.println("Made it to closing the server");
            //System.out.println(listener.isClosed());
            return;
            //listener.close();
        }

    }

    public void closeConnections() throws IOException {
        for (ServerSocketConnection connection: connections) {
            connection.closeServer();
        }
    }

//    public boolean checkIfAllPeersAndSelfHaveFile() {
//        boolean allPeersHaveFile = false;
//        boolean selfHasFile = peer.hasFile();
//        if (peerStateMap.size() >= peer.getNumberOfClientsThatShouldConnect() && selfHasFile) {
//            for (ServerSocketConnection connection: connections) {
//                //System.out.println();
//                if (!connection.clientHasCompleteFile()) {
//                    return false;
//                }
//            }
//            return true;
//        }
//        return false;
//    }

//    public boolean checkIfAllPeersAndSelfHaveFile() {
//        boolean allPeersHaveFile = false;
//        boolean selfHasFile = peer.hasFile();
//        if (peerStateMap.size() >= peer.getNumberOfClientsThatShouldConnect() && selfHasFile) {
//            for (Map.Entry<Integer, PeerState> entry: peerStateMap.entrySet()) {
//                //System.out.println();
//                if (!entry.getValue().hasCompleteFile()) {
//                    return false;
//                }
//            }
//            return true;
//        }
//        return false;
//    }

    public boolean checkIfAllPeersAndSelfHaveFile() {
        boolean allPeersHaveFile = false;
        boolean selfHasFile = peer.hasFile();
        if (connections.size() >= peer.getNumberOfClientsThatShouldConnect() && selfHasFile) {
            for (ServerSocketConnection connection: connections) {
                //System.out.println();
                if (!connection.clientHasCompleteFile()) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    private void checkForOptimisticUnchoke() {
        int interval = peer.getPeerConfig().getOptimisticUnchokingInterval();
        PeerConfiguration config = peer.getPeerConfig();
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);

        Runnable task = () -> {
            // Optimistic Unchoke code
            synchronized (peerStateMap) {
                //System.out.println("We should have this many total items: " + peer.getNumberOfClientsThatShouldConnect());
                for (Map.Entry<Integer, PeerState> entry: peerStateMap.entrySet()) {
                    //System.out.println("This Id: " + entry.getKey() + ", do they have the file: " + entry.getValue().hasCompleteFile());
                }
                if (checkIfAllPeersAndSelfHaveFile()) {
                    //System.out.println("WE ALL HAVE THE FILE:" + checkIfAllPeersAndSelfHaveFile());
                    try {
                        //System.out.println(executor.shutdownNow());
                        this.currentServerSocket.close();
                        //System.out.println(currentServerSocket.isClosed());
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }
                    finally {
                        executor.shutdownNow();
                    }
                    return;
                }
                if (peerStateMap.isEmpty()) {
                    return;
                }
                //System.out.println("Am I even optimistically unchoking");

                List<Integer> interestedNeighborKeys = new ArrayList<>();
                for (Map.Entry<Integer, PeerState> entry: peerStateMap.entrySet()) {

                    //System.out.println("Optimistic Unchoking For loop PeerId: "  + entry.getKey());
                    //System.out.println("Is "  + entry.getKey() + " interested? " + entry.getValue().isInterested());
                    if (entry.getValue().isOptimisticallyUnchoked()) {
                        //entry.getValue().setChoked(true);
                        entry.getValue().setOptimisticallyUnchoked(false);
                    }

                    if (entry.getValue().isInterested() && !entry.getValue().isChoked()) {
                        //System.out.println("I adding to the optimistic choking list with this peerID: " + entry.getKey());
                        interestedNeighborKeys.add(entry.getKey());
                    }


                }
                if (interestedNeighborKeys.size() > 0) {
                    Collections.shuffle(interestedNeighborKeys);
                    //System.out.println("Optimistic Unchoking winner! Is this peerID: " + interestedNeighborKeys.get(0));
                    //peerStateMap.get(interestedNeighborKeys.get(0)).setChoked(false);
                    peerStateMap.get(interestedNeighborKeys.get(0)).setOptimisticallyUnchoked(true);

                    try {
                        LogWriter2.getInstance(peer).writeLog("Peer " + peer.getPeerId() + " has the optimistically unchoked neighbor " + interestedNeighborKeys.get(0) + ".");
                    }
                    catch (IOException e) {

                    }

                }
                //System.out.println("Interested Neighbor Keys: " + interestedNeighborKeys.size());
                sendChokeAndUnchokeMessages();
                //System.out.println("Running at intervals... (optimistic)");
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
                //System.out.println("We should have this many total items: " + peer.getNumberOfClientsThatShouldConnect());
                for (Map.Entry<Integer, PeerState> entry: peerStateMap.entrySet()) {
                    //System.out.println("This Id: " + entry.getKey() + ", do they have the file: " + entry.getValue().hasCompleteFile());
                }
                if (checkIfAllPeersAndSelfHaveFile()) {
                    //System.out.println("RETURN");
                    //System.out.println("WE ALL HAVE THE FILE:" + checkIfAllPeersAndSelfHaveFile());
                    try {
                        //System.out.println(executor.shutdownNow());
                        this.currentServerSocket.close();
                        //System.out.println(currentServerSocket.isClosed());
                        //System.out.println("PAST EXECUTOR");
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }
                    finally {
                        executor.shutdownNow();
                    }
                    return;
                }
                if (peerStateMap.isEmpty()) {
                    return;
                }

                int preferredNeighbors = config.getNumberOfPreferredNeighbors();
                //System.out.println("Right before ReAssessNeighbors");
                //if (true) {
                if (this.peer.hasFile()) {
                    reAssessRandomNeighbors();
                }
                else {
                    reAssessNeighborsByThroughput();
                }
                sendChokeAndUnchokeMessages();

                // Neighbors need to receive unchoke or choke message every interval.
                // Only unchoke if interested.
                // Only optimistically unchoke if Peer has the entire file.

                //System.out.println("Running at intervals... (Regular)");
            }
        };


        executor.scheduleAtFixedRate(task, intervalInSeconds, intervalInSeconds, TimeUnit.SECONDS);
    }

    private void reAssessRandomNeighbors() {
        List<Integer> interestedNeighborKeys = new ArrayList<>();
        //System.out.println("I am in reAssessNeighbors");
        for (Map.Entry<Integer, PeerState> entry: peerStateMap.entrySet()) {
            //System.out.println("This is a peerID: " + entry.getKey());
            //System.out.println("I am choking Temporarily this peerID: " + entry.getKey());
            entry.getValue().setChoked(true);
            if (entry.getValue().isInterested()) {
                //System.out.println("I made it inside the interested key zone with this peerID: " + entry.getKey());
                interestedNeighborKeys.add(entry.getKey());
                //System.out.println("Adding interested Neighbor key to list: " + entry.getKey());
            }
        }

        ArrayList<Integer> listForLog = new ArrayList<>();
        if (interestedNeighborKeys.size() <= peer.getPeerConfig().getNumberOfPreferredNeighbors()) {
            //System.out.println("Am I reaching the code for SMALL neighbors");
            for (int key: interestedNeighborKeys) {
                if (peerStateMap.get(key).isInterested()) {
                    listForLog.add(key);
                    peerStateMap.get(key).setChoked(false);
                    //System.out.println(peerStateMap.get(key).isChoked());
                }
            }
        }
        else {
            Collections.shuffle(interestedNeighborKeys);
            for (int i = 0; i < peer.getPeerConfig().getNumberOfPreferredNeighbors(); i++) {
                listForLog.add(interestedNeighborKeys.get(i));
                peerStateMap.get(interestedNeighborKeys.get(i)).setChoked(false);
            }
        }

        if (listForLog.size() > 0) {
            try {
                LogWriter2.getInstance(peer).writeLog("Peer " + peer.getPeerId() + " has the preferred neighbors " + listForLog.toString() + ".");
            } catch (IOException e) {

            }
        }

    }

    private void reAssessNeighborsByThroughput() {
        List<Integer> interestedNeighborKeys = new ArrayList<>();
        ArrayList<Map.Entry<Integer, Integer>> peerIDAndBytesReceivedEntrySet = createSortedThroughputMap();
        ArrayList<Integer> listForLog = new ArrayList<>();

        for (Map.Entry<Integer, PeerState> entry: peerStateMap.entrySet()) {
            //System.out.println("THROUGHPUT");
            entry.getValue().setChoked(true);
            if (entry.getValue().isInterested()) {
                //System.out.println("I made it inside the interested key zone with this peerID: " + entry.getKey());
                interestedNeighborKeys.add(entry.getKey());

                //System.out.println("Adding interested Neighbor key to list: " + entry.getKey());
            }
        }

        if (interestedNeighborKeys.size() <= peer.getPeerConfig().getNumberOfPreferredNeighbors()) {
            //System.out.println("Am I reaching the code for SMALL neighbors");
            for (int key: interestedNeighborKeys) {
                if (peerStateMap.get(key).isInterested()) {
                    peerStateMap.get(key).setChoked(false);
                    //System.out.println(peerStateMap.get(key).isChoked());
                    listForLog.add(key);
                }
            }
        }
        else {
            for (int i = 0; i < peer.getPeerConfig().getNumberOfPreferredNeighbors(); i++) {
//                peerStateMap.get(interestedNeighborKeys.get(i)).setChoked(false);
                peerStateMap.get(peerIDAndBytesReceivedEntrySet.get(i).getKey()).setChoked(false);
                listForLog.add(peerIDAndBytesReceivedEntrySet.get(i).getKey());
            }
        }

        for (Map.Entry<Integer, PeerState> entry: peerStateMap.entrySet()) {
            entry.getValue().setNumberOfBitsInterval(0);
        }

        if (listForLog.size() > 0) {
            try {
                LogWriter2.getInstance(peer).writeLog("Peer " + peer.getPeerId() + " has the preferred neighbors " + listForLog.toString() + ".");
            } catch (IOException e) {

            }
        }
    }

    private ArrayList<Map.Entry<Integer, Integer>> createSortedThroughputMap() {
        Map<Integer, Integer> throughputMap = new HashMap<>();
        for (Map.Entry<Integer, PeerState> entry: peerStateMap.entrySet()) {
            if (!entry.getValue().isOptimisticallyUnchoked()) {
                throughputMap.put(entry.getKey(), entry.getValue().getNumberOfBitsInterval());
            }
        }
        ArrayList<Map.Entry<Integer, Integer>> sortedThroughputList = new ArrayList<>(throughputMap.entrySet());

        //System.out.println("Unsorted THROUGHPUT: ");
        printList(sortedThroughputList);
        Collections.sort(sortedThroughputList, (e1, e2) -> e2.getValue().compareTo(e1.getValue()));
        //System.out.println("Sorted THROUGHPUT: ");
        printList(sortedThroughputList);
        //Collections.sort(sortedThroughputList, Map.Entry.comparingByValue());
        return sortedThroughputList;
    }

    private void printList(ArrayList<Map.Entry<Integer, Integer>> list) {
        for (Map.Entry<Integer, Integer> l: list) {
            //System.out.println(": " + l.getValue());
        }
    }

    private void sendChokeAndUnchokeMessages() {
        for (ServerSocketConnection connection: connections) {

            PeerState state = peerStateMap.get(connection.getClientPeerId());

            if (state != null) {
                //System.out.println("This is the state of interest: " + state.isInterested());
                //System.out.println("This is the state of Choking: " + state.isChoked());
                if (state.isChoked() && !state.isOptimisticallyUnchoked()) {
                    //System.out.println("Am I reaching this choking message code");
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
                //System.out.println("Am I Adding all indexes: " + index);
                //connection.sendMessage(Message.makeHave(indexAsByte).toBytes());

            }
        }
    }
}
