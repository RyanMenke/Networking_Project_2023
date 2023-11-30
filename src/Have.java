import java.nio.ByteBuffer;

public class Have {
    private int index;

    public Have(int index) {
        this.index = index;
    }

    public static Have fromRawData(byte[] data) {
        ByteBuffer buffer = ByteBuffer.wrap(data);

        byte[] indexBytes = new byte[4];
        buffer.get(indexBytes, 0, 4);
        int index = ByteBuffer.wrap(indexBytes).getInt();

        return new Have(index);
    }

    public int getIndex() {
        return index;
    }
}
