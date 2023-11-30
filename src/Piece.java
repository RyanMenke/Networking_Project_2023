import java.nio.ByteBuffer;

public class Piece {
    private int index;
    private byte[] content;

    public Piece(int index, byte[] content) {
        this.index = index;
        this.content = content;
    }

    /**
     * ‘piece’ messages have a payload which consists of a 4-byte piece index field and the
     * content of the piece.
     *
     * @param data
     * @return
     */
    public static Piece fromRawData(byte[] data) {
        ByteBuffer buffer = ByteBuffer.wrap(data);

        byte[] indexBytes = new byte[4];
        buffer.get(indexBytes, 0, 4);
        int index = ByteBuffer.wrap(indexBytes).getInt();

        byte[] piece = new byte[data.length - 4];
        buffer.get(piece, 4, data.length - 4);

        return new Piece(index, piece);
    }

    public int getIndex() {
        return index;
    }

    public byte[] getContent() {
        return content;
    }
}
