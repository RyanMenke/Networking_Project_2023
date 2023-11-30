import java.io.BufferedReader;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.BitSet;

public class Peer {

    //peer variables
    private int peerId;
    private String hostName;
    private int portNumber;
    private boolean hasFile;

    private boolean remainOpen;

    private int numberOfPieces;

    private PeerConfiguration peerConfig;

    private byte[] imageFileData;


    // memory stored internally in each peer
    // gives a representation of the pieces stored, used to determine what pieces are missing
    private BitSet bitfield;


    //connection variables
    Socket socket;
    InputStream inputStream;
    OutputStream outputStream;
    BufferedReader bufferedReader;
    PrintWriter printWriter;


    public Peer(int Id, String hName, int pNumber, boolean hFile) {
        peerId = Id;
        hostName = hName;
        portNumber = pNumber;
        hasFile = hFile;

        remainOpen = true;
    }

    // TODO: Left off here
    // TODO: ensure that the bitfield is divisible by 8, as it is measured in bytes
    public void setNumberOfPieces(int numberOfPieces) {


        this.numberOfPieces = numberOfPieces;
        int bitfieldSize = numberOfPieces;
        if (numberOfPieces % 8 != 0) {
            int remainder = numberOfPieces % 8;
            int ammountToAdd = 8 - remainder;
            bitfieldSize += ammountToAdd;
            System.out.println(bitfieldSize);
        }

        // Initialize a bitset where all bits are set to 0
        bitfield = new BitSet(bitfieldSize);

        if (hasFile) {
            for (int i = 0; i < numberOfPieces; i++) {
                bitfield.set(i);
            }
        }
    }

    public void setPeerConfig(PeerConfiguration config) {
        this.peerConfig = config;
    }

    public PeerConfiguration getPeerConfig() {
        return this.peerConfig;
    }

    public void setImageFileData(byte[] data) {
        this.imageFileData = data;
    }

    public void setImageFileDataToBlankByteArray() {
        this.imageFileData = new byte[peerConfig.getFileSize()];
    }

    public int getNumberOfPieces() {
        return numberOfPieces;
    }

    public byte[] getImageFileData() {
        return this.imageFileData;
    }

    public BitSet getBitfield() {
        return bitfield;
    }

    public void printContents() {
        System.out.println("");
        System.out.println(peerId);
        System.out.println(hostName);
        System.out.println(portNumber);
        System.out.println(hasFile);
        System.out.println("");
    }

    public int getPeerId() {

        return peerId;

    }

    public String getHostName() {

        return hostName;

    }

    public int getPortNumber() {

        return portNumber;

    }

    public Boolean hasFile() {

        return hasFile;

    }

    public void runProcess(PeerConfiguration config) {
        if (peerId == 1001) {
        }

        while (remainOpen) {

        }
    }

    /**
     * We're interested in this peer if the peer has any pieces we don't have
     *
     * @param peerBitset
     * @return
     */
    public boolean isInterestedInPeer(BitSet peerBitset) {
        int[] zeroBitIndexes = zeroBitIndexes();

        for (int index: zeroBitIndexes) {
            if (peerBitset.get(index)) {
                return true;
            }
        }

        return false;
    }

    public ArrayList<Integer> getListOfInterestedIndexesFromBitset(BitSet foreignBitset) {
        ArrayList<Integer> indexList = new ArrayList<>();
        for (int i = 0; i < foreignBitset.length(); i++) {
            if (foreignBitset.get(i) && !this.bitfield.get(i)) {
                //System.out.println("AAAAAAAAAAAAAAAAAAAAAAAAAA");
                indexList.add(i);
            }
        }
        System.out.println("Index list length: " + indexList.size());
        return indexList;
    }

    private int[] zeroBitIndexes() {
        int numZeroBits = bitfield.size() - bitfield.cardinality();
        int[] zeroBits = new int[numZeroBits];

        int currentIndex = 0;

        for (int i = 0; i < bitfield.size(); i++) {
            if (!bitfield.get(i)) {
                zeroBits[currentIndex++] = i;
            }
        }

        return zeroBits;
    }

    public byte[] getPieceFromIndex(int index) {
        if (imageFileData.length == 0) {
            return new byte[0];
        }
        int pieceSize = peerConfig.getPieceSize();
        int offset = pieceSize * index;
        //index = index - 1;

        boolean isFirstBool = index == (numberOfPieces);
        boolean isSecondBool = peerConfig.getFileSize() % pieceSize != 0;
        System.out.println("Inside getPieceFromIndex the base index calculated is: " + offset);
        System.out.println("Inside getPieceFromIndex the index is: " + index);
        System.out.println("Inside getPieceFromIndex is the first bool true?: " + isFirstBool);
        System.out.println("Inside getPieceFromIndex is the second bool true?: " + isSecondBool);

        if (index == (numberOfPieces - 1) && peerConfig.getFileSize() % pieceSize != 0) {
            pieceSize = peerConfig.getFileSize() % pieceSize;
            System.out.println("THIS IS THE FINAL INDEX PIECE SIZE: " + pieceSize);
            System.out.println("THIS IS THE FINAL INDEX OFFSET SIZE: " + offset);
        }
        byte[] pieceArray = new byte[pieceSize];


        System.arraycopy(imageFileData, offset, pieceArray, 0, pieceSize);
        return pieceArray;
    }

    public static void printByteArray(byte[] input) {
        System.out.println("====== PRINTING BYTE ARRAY =======");
        for (byte b: input) {
            System.out.print(b + " ");
        }
        System.out.println("====== END PRINTING BYTE ARRAY =======");
    }

    public void updateBitfieldWithNewIndex(int index) {
        this.bitfield.set(index);
    }

    public boolean doesPeerHaveIndex(int index) {
        if (this.bitfield.get(index)) {
            return true;
        }
        return false;
    }

    public void updateImageFileData(int index, byte[] data) {
        int newIndex = index * peerConfig.getPieceSize();
        System.out.println("IMAGE DATA BEFORE UPDATE: " + imageFileData[index]);
        System.arraycopy(data, 0 , imageFileData, newIndex, data.length);
        System.out.println("IMAGE DATA AFTER UPDATE: " + imageFileData[index]);
    }

    public boolean checkIfHasCompleteFile() {
        for (int i = 0; i < numberOfPieces; i++) {
            if (!bitfield.get(i)) {
                return false;
            }
        }
        hasFile = true;
        return true;
    }
    //First Peer:
    //This peer also finds out that it is the first
    //peer; it will just listen on the port 6008 as specified in the file. Being the first peer, there
    //are no other peers to make connections to.


    //handshake:
    //32 bytes
    //header | zero bits | Peer Id
    //Header: 18 byte string "P2PFILESHARINGPROJ"
    //zero bits: 10 bytes
    //Peer ID: 4 bytes, integer ID


    //Actual Message
    //Length | Type | Payload
    //Length: 4 bytes, conveys length of message (minus the actual length field)
    //Type: 1 byte (0-7) is the type of message
    //Payload:a payload of variable length

    //Message Types:
    //0 | Choke
    //1 | Unchoke
    //2 | Interested
    //3 | Not Interested
    //4 | Have
    //5 | Bitfield
    //6 | request
    //7 | piece
}
