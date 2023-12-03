import java.io.*;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.*;

/**
 * Given that a client has connected to your server, handle the communication with the client
 */
public class ServerSocketConnection extends Thread {
    private Socket connection;
    private InputStream in;
    private OutputStream out;
    private Peer self;
    private int clientPeerId;
    private boolean shouldClose = false;
    // This is an interface that an individual socket connection can use to communicate
    // with the server, whose job it is to maintain the state of all connected clients.
    private ConnectedClientInfo serverInterface;

    private boolean canAcceptHave;

    private boolean clientIsInterested;

    private Date startingTime;
    private Date endingTime;
    private double downloadSpeed;

    private Map<Integer, PeerState> peerStateMap = new HashMap<>();

    private Queue<Integer> haveQueue = new LinkedList<>();

    public ServerSocketConnection(Socket connection, Peer self, ConnectedClientInfo serverInterface) {
        this.connection = connection;
        this.self = self;
        this.serverInterface = serverInterface;

        this.canAcceptHave = false;

        this.downloadSpeed = Double.MAX_VALUE;
    }

    public int getClientPeerId() {
        return clientPeerId;
    }

    public boolean getCanAcceptHave() {
        return this.canAcceptHave;
    }

    public boolean getClientIsInterested() {
        return this.clientIsInterested;
    }

    public void setShouldClose(boolean shouldClose) { this.shouldClose = shouldClose; }

    public boolean getShouldClose() { return this.shouldClose; }

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
                if (haveQueue.size() <= 0) {
                    while (!haveQueue.isEmpty()) {
                        ByteBuffer byteBuffer = ByteBuffer.allocate(Integer.BYTES);

                        // Put the int value into the ByteBuffer
                        byteBuffer.putInt(haveQueue.poll());

                        // Retrieve the bytes from the ByteBuffer into a byte array
                        byte[] indexAsByte = byteBuffer.array();
                        sendMessage(Message.makeHave(indexAsByte).toBytes());
                    }
                }
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
                        canAcceptHave = true;
                        break;
                    case Message.NOT_INTERESTED:
                        System.out.println("Received NOT_INTERESTED");
                        serverInterface.clientIsNotInterested(clientPeerId);
                        canAcceptHave = true;
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

                        //this.startingTime = new Date();
                        // Copy byteArray2 into combinedArray starting from the end of byteArray1
                        System.arraycopy(pieceArray, 0, combinedArray, index.length, pieceArray.length);
                        serverInterface.addToDownloadRate(clientPeerId, combinedArray.length);
                        sendMessage(Message.makePiece(combinedArray).toBytes());

                        break;
                    case Message.PIECE:
                        System.out.println("Received PIECE");
                        break;

                    case Message.HAS_COMPLETE_FILE:
                        serverInterface.setHasCompleteFile(clientPeerId);
                        if (serverInterface.doAllPeersHaveCompleteFile()) {
                            setShouldClose(true);
                        }
                        System.out.println("HAS COMPLETE FILE");
                        break;
                }
            }
        } catch (IOException ioException) {
//            System.out.println("Disconnect with Client " + clientNumber);
        } finally {
            //Close connections
            try {
                System.out.println("CLOSING SERVER CONNECTION");
                if (shouldClose) {
                    in.close();
                    out.close();
                    connection.close();
                }
//                in.close();
//                out.close();
//                connection.close();
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

        void addToDownloadRate(int peerId, int rate);

        void setHasCompleteFile(int peerId);

        boolean doAllPeersHaveCompleteFile();
    }

    //send a message to the output stream
    public void sendMessage(byte[] msg) {
        try {
            out.write(msg);
            out.flush();
        } catch (IOException ioException) {
            System.out.println("Failed to send message to client ID " + clientPeerId + " socket state is closed " + connection.isClosed());
//            ioException.printStackTrace();
        }
    }

    public void closeServer() throws IOException {
        connection.close();
    }

    public void addToHaveQueue(int index) {
        haveQueue.offer(index);
    }

    public Queue<Integer> getHaveQueue() {
        return haveQueue;
    }

    public int popHaveQueue() {
        return haveQueue.poll();
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

            LogWriter2.getInstance(self).writeLog("Peer " + self.getPeerId() + " makes a connection to Peer " + clientPeerId + ".");
            //LogWriter.TCPReceiveConnection(self.getPeerId(), clientPeerId);
            return true;
        } catch (Exception e) {
            System.out.println("Handshake failed " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}

