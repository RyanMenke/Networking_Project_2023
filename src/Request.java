import java.nio.ByteBuffer;

public class Request {
    private int index;

    public Request(int index) {
        this.index = index;
    }

    /**
     * request’ messages have a payload which consists of a 4-byte piece index field. Note
     * that ‘request’ message payload defined here is different from that of BitTorrent. We don’t
     * divide a piece into smaller subpieces.
     *
     * @param data
     * @return
     */
    public static Request fromRawData(byte[] data) {
        ByteBuffer buffer = ByteBuffer.wrap(data);

        byte[] indexBytes = new byte[4];
        buffer.get(indexBytes, 0, 4);
        int index = ByteBuffer.wrap(indexBytes).getInt();

        return new Request(index);
    }
}
