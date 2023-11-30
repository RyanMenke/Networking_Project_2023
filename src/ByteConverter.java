import java.util.BitSet;

public class ByteConverter {

    public static byte[] bitSetToByteArraySameOrder(BitSet bitSet) {
        int bitSetLength = bitSet.length();
        byte[] byteArray = new byte[(bitSetLength + 7) / 8]; // Calculate the number of bytes needed

        for (int i = 0; i < bitSetLength; i++) {
            if (bitSet.get(i)) {
                int byteIndex = i / 8;
                int bitIndex = 7 - (i % 8); // Invert the bit index to maintain order

                byteArray[byteIndex] |= (1 << bitIndex);
            }
        }
        return byteArray;
    }

    public static BitSet byteArrayToBitSet(byte[] byteArray) {
        BitSet bitSet = new BitSet(byteArray.length * 8); // Initialize BitSet with enough bits

        int bitIndex = 0;
        for (byte b : byteArray) {
            for (int i = 7; i >= 0; i--) {
                bitSet.set(bitIndex++, (b & (1 << i)) != 0);
            }
        }
        return bitSet;
    }
}
