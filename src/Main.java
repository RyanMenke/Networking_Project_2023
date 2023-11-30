import java.io.IOException;
import java.util.ArrayList;

public class Main {

    public static Peer getPeerById(int id, ArrayList<Peer> peers) {
        for (Peer peer: peers) {
            if (peer.getPeerId() == id) {
                return peer;
            }
        }
        return null;
    }

    /**
     * We're supposed to connect to any server that has an id less than our id. From our list of peers,
     * establish a list of Clients where we will connect as a client.
     *
     * @param id
     * @param peers
     * @return
     */
    public static ArrayList<Client> getServersWeShouldConnectTo(Peer self, int id, ArrayList<Peer> peers, Server server) {
        ArrayList<Client> serversToConnectTo = new ArrayList<>();

        for (Peer peer: peers) {
            if (peer.getPeerId() < id) {
                serversToConnectTo.add(new Client(self, peer, id, new Client.ClientEvent() {
                    @Override
                    public void pieceReceived(int index) {
                        server.notifyNeighborsOfNewPiece(index);
                    }
                }));
            }
        }

        return serversToConnectTo;
    }

    public static void main(String[] args) {



        ///////////////////////////////////////////////////
        //first I did some basic testing in my code///////
        //this will not be in the final in the same form//
        //////////////////////////////////////////////////




        //This is an array of "peers"
        //they will be the ones sharing file information
        ArrayList<Peer> peers = new ArrayList<>();

        FileReader.readPeerInfoFile(peers);




        for (int i = 0; i < peers.size(); i++) {
            peers.get(i).printContents();
        }

        PeerConfiguration config = new PeerConfiguration();
        FileReader.readConfigInfo(config);
        config.printConstants();

        String currentWorkingDirectory = System.getProperty("user.dir");
        System.out.println("Current working directory: " + currentWorkingDirectory);

        //First, you need to get the current working directory, which is the directory where you
        //start the Java program, as follows:
        // String workingDir = System.getProperty("user.dir");
        //Second, you invoke exec() method as in the following:
        // Runtime.getRuntime().exec("ssh " + hostname + " cd " + workingDir + " ; " +
        //peerProcessName + " " + peerProcessArguments );

        int peerId = Integer.parseInt(args[0]);
        Peer p1 = getPeerById(peerId, peers);
        if (p1 == null) {
            System.out.println("Could not find peer of specified ID");
            return;
        }

        // Gives the peer the number of pieces for the file size so that it can maintain a bitfield
        int fileSize = config.getFileSize();
        int pieceSize = config.getPieceSize();
        int numberOfPieces = fileSize/pieceSize;
        if (fileSize % pieceSize != 0) {
            numberOfPieces = (fileSize/pieceSize) + 1;
        }

        p1.setNumberOfPieces(numberOfPieces);
        p1.setPeerConfig(config);
        if (p1.hasFile()) {
            FileReader.readEntireFile(p1);
        }
        else {
            p1.setImageFileDataToBlankByteArray();
        }
        System.out.println("THE NUMBER OF PIECES IS: " + p1.getNumberOfPieces());
        System.out.println("SIZE OF THE FINAL PIECE: " + p1.getPeerConfig().getFileSize() % p1.getPeerConfig().getPieceSize());
        System.out.println("the size of my bitset is: " + p1.getBitfield().length());

        // First, begin communicating with any other servers (peers) that we're supposed
        // to connect to
        Server server = new Server(p1);
        ArrayList<Client> peersToConnectTo = getServersWeShouldConnectTo(p1, peerId, peers, server);
        System.out.println("Should attempt to connect to " + peersToConnectTo.size() + " peers");
        // For each client, start a thread that will connect to the peer that it's supposed to
        // communicate with and begin exchanging messages
        for (Client client: peersToConnectTo) {
            client.start();
        }

        // After we have connected to other servers (acting as a client), start our
        // own server, so that clients can connect to us.

        try {
            server.run();
        } catch (IOException e) {
            System.out.println("Server failed with an IOException " + e.getMessage());
        }
    }
}
