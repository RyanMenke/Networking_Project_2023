import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

public class Message {
    private int length;
//    private int messageType;
    private byte messageType;
    private byte[] content;

    public static final int CHOKE = 0;
    public static final int UN_CHOKE = 1;
    public static final int INTERESTED = 2;
    public static final int NOT_INTERESTED = 3;
    public static final int HAVE = 4;
    public static final int BIT_FIELD = 5;
    public static final int REQUEST = 6;
    public static final int PIECE = 7;

    public static final int HAS_COMPLETE_FILE = 8;

    public Message(int length, int messageType, byte[] content) {
        this.length = length;
        this.messageType = (byte) messageType;
        this.content = content;
    }

    public static byte[] parsePieceMessage(byte[] messageContent) {
        int messageLength = messageContent.length;
        int messagePieceLength = messageLength - 4;
        byte[] onlyPieceContent = new byte[messagePieceLength];
        System.arraycopy(messageContent, 3, onlyPieceContent, 0, messagePieceLength);

        return onlyPieceContent;
    }

    public static Message makeBitfield(byte[] content) {
        return new Message(content.length, BIT_FIELD, content);
    }

    public static Message makeInterested() {
        byte[] a = new byte[0];
        return new Message(1, INTERESTED, a);
    }

    public static Message makeNotInterested() {
        byte[] a = new byte[0];
        return new Message(1, NOT_INTERESTED, a);
    }

    public static Message makeChoke() {
        byte[] a = new byte[0];
        return new Message(1, CHOKE, a);
    }

    public static Message makeUnchoke() {
        byte[] a = new byte[0];
        return new Message(1, UN_CHOKE, a);
    }

    public static Message makeRequest(byte[] content) {
        return new Message(content.length, REQUEST, content);
    }

    public static Message makePiece(byte[] content) {
        return new Message(content.length, PIECE, content);
    }

    public static Message makeHave(byte[] content) {
        return new Message(content.length, HAVE, content);
    }

    public static Message makeHasEntireFile() {
        byte[] a = new byte[0];
        return new Message(1, HAS_COMPLETE_FILE, a);
    }

    /**
     * Given an input stream, construct a Message
     *
     * @param input
     * @return
     * @throws IOException
     */
    public static Message fromInputStream(InputStream input) throws IOException {
        //System.out.println("Received message of length " + input.available());
        byte[] length = new byte[4];
        input.read(length, 0, 4);
        int messageLength = ByteBuffer.wrap(length).getInt();
        //System.out.println("The content of the message is " + messageLength + " bytes");

        byte[] messageType = new byte[1];

        //System.out.println("About to read message type - input length is " + input.available());
        input.read(messageType, 0, 1);
        int type = messageType[0] & 0xFF;
        //System.out.println("message type " + type);

        byte[] content = new byte[messageLength];
        input.read(content, 0, messageLength);

        return new Message(messageLength, type, content);
    }

    public int getMessageType() {
        return messageType;
    }

    public byte[] getContent() {
        return content;
    }

    public byte[] toBytes() {
        int length = content.length;
        byte[] lengthByteArray = ByteBuffer.allocate(4).putInt(length).array();
        byte[] messageType = ByteBuffer.allocate(1).put((byte)getMessageType()).array();
        ByteBuffer combined = ByteBuffer.allocate(4 + 1 + length);
        combined.put(lengthByteArray);
        combined.put(messageType);
        combined.put(content);

        return combined.array();
    }
}
