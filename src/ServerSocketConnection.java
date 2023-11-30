import java.io.*;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.BitSet;
import java.util.HashMap;
import java.util.Map;

/**
 * Given that a client has connected to your server, handle the communication with the client
 */
public class ServerSocketConnection extends Thread {
    private Socket connection;
    private InputStream in;
    private OutputStream out;
    private Peer self;
    private int clientPeerId;
    // This is an interface that an individual socket connection can use to communicate
    // with the server, whose job it is to maintain the state of all connected clients.
    private ConnectedClientInfo serverInterface;

    private Map<Integer, PeerState> peerStateMap = new HashMap<>();

    public ServerSocketConnection(Socket connection, Peer self, ConnectedClientInfo serverInterface) {
        this.connection = connection;
        this.self = self;
        this.serverInterface = serverInterface;
    }

    public int getClientPeerId() {
        return clientPeerId;
    }

    public void run() {
        try {
            System.out.println("RUNNING Bbbbbbbbbbbbbbbbbbb");
            //initialize Input and Output streams
            out = connection.getOutputStream();
            out.flush();
            in = connection.getInputStream();

            System.out.println("Attempting handshake with client");
            if (!doHandshake(in)) {
                System.out.println("Failed to complete handshake (ServerSocketConnection)");
                return;
            }

            while (true) {
                while (in.available() == 0) {}
                Message message = Message.fromInputStream(in);
                switch (message.getMessageType()) {
                    case Message.CHOKE:
                        System.out.println("Received CHOKE");
                        break;
                    case Message.UN_CHOKE:
                        System.out.println("Received UN_CHOKE");
                        break;
                    case Message.INTERESTED:
                        System.out.println("Received INTERESTED");
                        serverInterface.clientIsInterested(clientPeerId);
                        break;
                    case Message.NOT_INTERESTED:
                        System.out.println("Received NOT_INTERESTED");
                        serverInterface.clientIsNotInterested(clientPeerId);
                        break;
                    case Message.HAVE:
                        System.out.println("Received HAVE");
                        break;
                    case Message.BIT_FIELD:
                        BitSet bitfield = self.getBitfield();
                        if (bitfield.nextSetBit(0) != -1) {
                            Message bitfieldToSend = Message.makeBitfield(self.getBitfield().toByteArray());
                            sendMessage(bitfieldToSend.toBytes());
                        }
                        // handle the interested/not_interested case
                        Bitfield clientsBitfield = Bitfield.fromMessage(message);
                        BitSet clientBitset = ByteConverter.byteArrayToBitSet(message.getContent());
                        boolean isInterested = self.isInterestedInPeer(clientBitset);

                        System.out.println("Am I interested on the server socket Side? " + isInterested);
                        if (isInterested) {
                            // handling the interested case.
                            sendMessage(Message.makeInterested().toBytes());
                            // Send the interested message
                            System.out.print("Interested sent (server Socket)");
                        }
                        else {
                            // handling the "not interested" case
                            sendMessage(Message.makeNotInterested().toBytes());
                            System.out.print("notInterested sent (server Socket)");
                        }

                        System.out.println("Received BIT_FIELD");
                        break;
                    case Message.REQUEST:

                        System.out.println("Received REQUEST");
                        byte[] index = message.getContent();
                        // byte[] piece = Message.parsePieceMessage(message.getContent());
                        int indexForPiece = ByteBuffer.wrap(index, 0, Integer.BYTES).getInt();
                        System.out.println("When receiving a request the index is: " + indexForPiece);
                        byte[] pieceArray = self.getPieceFromIndex(indexForPiece);

                        byte[] combinedArray = new byte[index.length + pieceArray.length];

                        // Copy byteArray1 into combinedArray
                        System.arraycopy(index, 0, combinedArray, 0, index.length);

                        // Copy byteArray2 into combinedArray starting from the end of byteArray1
                        System.arraycopy(pieceArray, 0, combinedArray, index.length, pieceArray.length);
                        sendMessage(Message.makePiece(combinedArray).toBytes());

                        break;
                    case Message.PIECE:
                        System.out.println("Received PIECE");
                        break;
                }
            }
        } catch (IOException ioException) {
//            System.out.println("Disconnect with Client " + clientNumber);
        } finally {
            //Close connections
            try {
                in.close();
                out.close();
                connection.close();
            } catch (IOException ioException) {
//                System.out.println("Disconnect with Client " + clientNumber);
            }

            serverInterface.onDisconnected(this);
        }
    }

    interface ConnectedClientInfo {
        void onClientConnected(Handshake handshake);

        void clientIsInterested(int peerId);

        void clientIsNotInterested(int peerId);

        boolean isInterested(int peerId);

        boolean isChoked(int peerId);

        void onDisconnected(ServerSocketConnection connection);
    }

    //send a message to the output stream
    public void sendMessage(byte[] msg) {
        try {
            out.write(msg);
            out.flush();
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }

    public void sendUnchoke() {
        sendMessage(Message.makeUnchoke().toBytes());
    }

    public void sendChoke() {
        sendMessage(Message.makeChoke().toBytes());
    }

    private boolean doHandshake(InputStream input) {
        try {
            Handshake clientHandshake = Handshake.fromInputStream(input);
            System.out.println("Received client handshake");
            clientPeerId = clientHandshake.getPeerID();
            serverInterface.onClientConnected(clientHandshake);

            Handshake serverHandshake = new Handshake(self.getPeerId());
            sendMessage(serverHandshake.toBytes());

            return true;
        } catch (Exception e) {
            System.out.println("Handshake failed " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}

