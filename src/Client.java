import java.io.*;
import java.net.ConnectException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;

public class Client extends Thread {

    private Peer self;
    // This peer represents the server we're connecting to
    private Peer peer;
    private OutputStream out;
    private InputStream in;
    private Socket requestSocket;
    private int clientId;

    private boolean selfIsInterested;
    private ArrayList<Integer> indexList;
    private BitSet serverBitSet;

    private boolean isChokedByServer;

    private ClientEvent eventEmitter;

    private boolean iHaveShakenHands;

    public Client(Peer self, Peer peer, int clientId, ClientEvent eventEmitter) {
        this.self = self;
        this.peer = peer;
        this.clientId = clientId;

        this.selfIsInterested = false;
        this.isChokedByServer = false;
        this.eventEmitter = eventEmitter;
        this.iHaveShakenHands = false;
    }

    public boolean getIHaveShakenHands() {
        return this.iHaveShakenHands;
    }

    interface ClientEvent {
        void pieceReceived(int index);
    }

    public void run() {
        try{
            System.out.println("RUNNING Aaaaaaaaaaaaaaaaaaaaa");
            //create a socket to connect to the server
            requestSocket = new Socket(peer.getHostName(), peer.getPortNumber());
            System.out.println("Connected to " + peer.getHostName() + " on port " + peer.getPortNumber());
            //initialize inputStream and outputStream
            out = requestSocket.getOutputStream();
            out.flush();
            in = requestSocket.getInputStream();

            if (!doHandshake(in)) {
                System.out.println("Failed to complete handshake (Client)");
                return;
            }

            // After the handshake is complete, the client sends its bitfield to the server
            Message bitfield = Message.makeBitfield(self.getBitfield().toByteArray());
            sendMessage(bitfield.toBytes());

//            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
            while(true)
            {
                while (in.available() == 0) {}
                Message message = Message.fromInputStream(in);
                switch (message.getMessageType()) {
                    case Message.CHOKE:
                        isChokedByServer = true;
                        System.out.println("Received CHOKE");
                        break;
                    case Message.UN_CHOKE:
                        this.isChokedByServer = false;
                        System.out.println("Received UN_CHOKE");
                        if (selfIsInterested) {
                            indexList = self.getListOfInterestedIndexesFromBitset(serverBitSet);
                            Collections.shuffle(indexList);
                            System.out.println("Inside UNCHOKE the chosen base index is: " + indexList.get(0));
                            byte[] byteArray = ByteBuffer.allocate(Integer.BYTES).putInt(indexList.get(0)).array();
                            sendMessage(Message.makeRequest(byteArray).toBytes());
                        }
                        else {
                            sendMessage(Message.makeNotInterested().toBytes());
                        }
                        break;
                    case Message.INTERESTED:
                        System.out.println("Received INTERESTED");
                        break;
                    case Message.NOT_INTERESTED:
                        System.out.println("Received NOT_INTERESTED");
                        break;
                    case Message.HAVE:
                        System.out.println("Received HAVE");
                        byte[] haveIndex = message.getContent();
                        int indexForHave = ByteBuffer.wrap(haveIndex, 0, Integer.BYTES).getInt();
                        System.out.println("When receiving a request the index is: " + indexForHave);
//                        boolean doesPeerHaveIndex = self.doesPeerHaveIndex(indexForHave);
                        serverBitSet.set(indexForHave);
                        indexList = self.getListOfInterestedIndexesFromBitset(serverBitSet);
                        if (indexList.size() > 0) {
                            sendMessage(Message.makeInterested().toBytes());
                        }
                        checkClientInterestAfterStateAssignmentAndSendResponse();

                        break;
                    case Message.BIT_FIELD:
                        System.out.println("Received BIT_FIELD");
                        Bitfield serversBitfield = Bitfield.fromMessage(message);
                        serverBitSet = serversBitfield.getBitset();
                        System.out.println("SERVER BITSET FINAL INDEX: " + serverBitSet.get(1487));
                        //serverBitSet = ByteConverter.byteArrayToBitSet(message.getContent());
                        System.out.println("SERVER BITSET LENGTH: " + serverBitSet.length());
                        System.out.println("SERVER BITSET FINAL INDEX: " + serverBitSet.get(1487));
                        this.iHaveShakenHands = true;
                        if (self.isInterestedInPeer(serversBitfield.getBitset())) {
                            // handling the interested case.
                            indexList = self.getListOfInterestedIndexesFromBitset(serversBitfield.getBitset());
                            this.serverBitSet = serversBitfield.getBitset();
                            System.out.println("This is the length of the Index List for requests: " + indexList.size());
                            selfIsInterested = true;
                            sendMessage(Message.makeInterested().toBytes());
                            // Send the interested message
                            System.out.print("Interested sent (Client)");
                        }
                        else {
                            // handling the "not interested" case
                            selfIsInterested = false;
                            sendMessage(Message.makeNotInterested().toBytes());
                            System.out.print("notInterested sent (Client)");
                        }
                        break;
                    case Message.REQUEST:
                        System.out.println("Received REQUEST");
                        break;
                    case Message.PIECE:
                        System.out.println("Received PIECE");
                        ByteBuffer byteBuffer = ByteBuffer.wrap(message.getContent(), 0, Integer.BYTES);

                        // Read the int value from the ByteBuffer
                        int index = byteBuffer.getInt();
                        byte[] pieceData = new byte[message.getContent().length - 4];
                        System.arraycopy(message.getContent(), 4, pieceData, 0, pieceData.length);
                        System.out.println("The length of the Piece I am receiving is: " + pieceData.length);
                        System.out.println("The index of the Piece I am receiving is: " + index);

                        // Updating Clients internally stored data
                        self.updateBitfieldWithNewIndex(index);
                        self.updateImageFileData(index, pieceData);
                        if (self.checkIfHasCompleteFile()) {
                            FileReader.writeFile(self);
                        }
                        System.out.println("MY IMAGE DATA: " + self.getImageFileData());

                        // Sending message to server that piece has been received
                        eventEmitter.pieceReceived(index);


                        // Checking if after that update sets match up, in which case Client is no longer interested.
                        indexList = self.getListOfInterestedIndexesFromBitset(serverBitSet);
                        checkClientInterestAfterStateAssignmentAndSendResponse();

                        // TODO: I need to send a "have" message to ALL neighbors

                        break;
                }
            }
        }
        catch (ConnectException e) {
            System.err.println("Connection refused. You need to initiate a server first.");
        }
        catch(UnknownHostException unknownHost){
            System.err.println("You are trying to connect to an unknown host!");
        }
        catch(IOException ioException){
            ioException.printStackTrace();
        }
        finally{
            //Close connections
//            try{
//                // TODO: THIS MUST CLOSE AT SOME POINT
//                System.out.println("CLIENT CLOSING SERVER CONNECTION");
//                in.close();
//                out.close();
//                requestSocket.close();
//            }
//            catch(IOException ioException){
//                ioException.printStackTrace();
//            }
        }
    }
    public void checkClientInterestAfterStateAssignmentAndSendResponse() {
        if (indexList.size() < 1) {
            sendMessage(Message.makeNotInterested().toBytes());
            selfIsInterested = false;
            System.out.println("The final size of the image data is: " + self.getImageFileData().length);
        }
        else if (!isChokedByServer){
            Collections.shuffle(indexList);
            System.out.println("Inside UNCHOKE the chosen base index is: " + indexList.get(0));
            byte[] byteArray = ByteBuffer.allocate(Integer.BYTES).putInt(indexList.get(0)).array();
            sendMessage(Message.makeRequest(byteArray).toBytes());
        }
    }

    //send a message to the output stream
    void sendMessage(byte[] msg)
    {
        try{
            //stream write the message
            out.write(msg);
            out.flush();
        }
        catch(IOException ioException){
//            ioException.printStackTrace();
        }
    }

    private boolean doHandshake(InputStream input) {
        try {
            Handshake clientHandshake = new Handshake(clientId);
            System.out.println("Attempting to send handshake to server");
            byte[] handshake = clientHandshake.toBytes();
            System.out.println("Sending " + handshake.length + " handshake bytes");
            sendMessage(handshake);
            System.out.println("sent message to server");

            // Verify the handshake from the server.
            // TODO: Do we need to do any additional verification from the server?
            Handshake serverHandshake = Handshake.fromInputStream(input);
            System.out.println("Received server handshake");

            LogWriter2.getInstance(self).writeLog("Peer "+ self.getPeerId() + " is connected from Peer "+peer.getPeerId()+".");
            //LogWriter.TCPMakeConnection(self.getPeerId(), peer.getPeerId());
            return true;
        } catch (Exception e) {
            System.out.println("Handshake failed ");
            e.printStackTrace();
            return false;
        }

    }
}
