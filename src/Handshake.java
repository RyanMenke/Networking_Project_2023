import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class Handshake {
    private static final String HANDSHAKE_HEADER = "P2PFILESHARINGPROJ";
    private byte[] padding = new byte[10];
    private int peerID;
    private static final int TOTAL_BYTES = 32;

    public Handshake(int peerID) {
        this.peerID = peerID;
    }



    /**
     * The handshake consists of three parts: handshake header, zero bits, and peer ID.
     *
     * The length of the handshake message is 32 bytes.
     * The handshake header is 18-byte string ‘P2PFILESHARINGPROJ’, which is followed by 10-byte zero bits,
     * which is followed by 4-byte peer ID which is the integer representation of the peer ID.
     *
     * @param input
     * @return
     * @throws IllegalArgumentException
     */
    public static Handshake fromBytes(byte[] input) throws IllegalArgumentException {
        //System.out.println("Handshake received input of length " + input.length);
        ByteBuffer message = ByteBuffer.wrap(input);

        byte[] header = new byte[18];
        message.get(header, 0, 18);
        String handshakeHeader = new String(header, StandardCharsets.UTF_8);
        //System.out.println(handshakeHeader);

        if (!handshakeHeader.equals(HANDSHAKE_HEADER)) {
            throw new IllegalArgumentException("Invalid header passed to handshake " + handshakeHeader + " here");
        }

        byte[] padding = new byte[10];
        message.position(18);
        message.get(padding, 0, 10);
        //System.out.println("Padding " + new String(padding, StandardCharsets.UTF_8));

        byte[] peerId = new byte[4];
        message.position(28);
        message.get(peerId, 0, 4);
        int peerIdentifier = ByteBuffer.wrap(peerId).getInt();
        //System.out.println("Peer id " + peerIdentifier);

        return new Handshake(peerIdentifier);
    }

    public static Handshake fromInputStream(InputStream input) throws IOException {
        //System.out.println("About to read handshake bytes");
        while (input.available() < TOTAL_BYTES) {}
        //System.out.println("Bytes available " + input.available());
        byte[] bytes = new byte[TOTAL_BYTES];
        input.read(bytes);

        return fromBytes(bytes);
    }

    public byte[] toBytes() {
        byte[] header = HANDSHAKE_HEADER.getBytes(StandardCharsets.UTF_8);
        byte[] peerIdentifier = ByteBuffer.allocate(Integer.BYTES).putInt(peerID).array();
        ByteBuffer combined = ByteBuffer.allocate(TOTAL_BYTES);
        combined.put(header);
        combined.put(ByteBuffer.allocate(10).array());
        combined.put(peerIdentifier);

        return combined.array();
    }

    public String getHandshakeHeader() {
        return HANDSHAKE_HEADER;
    }

    public byte[] getPadding() {
        return padding;
    }

    public int getPeerID() {
        return peerID;
    }
}

