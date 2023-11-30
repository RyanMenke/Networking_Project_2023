import java.util.BitSet;

public class Bitfield {

    private BitSet bitset;

    public Bitfield(BitSet bitset) {
        this.bitset = bitset;
    }

    /**
     * bitfield’ messages is only sent as the first message right after handshaking is done when
     * a connection is established. ‘bitfield’ messages have a bitfield as its payload. Each bit in
     * the bitfield payload represents whether the peer has the corresponding piece or not. The
     * first byte of the bitfield corresponds to piece indices 0 – 7 from high bit to low bit,
     * respectively. The next one corresponds to piece indices 8 – 15, etc. Spare bits at the end
     * are set to zero. Peers that don’t have anything yet may skip a ‘bitfield’ message.
     *
     * @param message
     * @return
     */

    // TODO: I believe this function might be broken. I don't think the message is being parsed
    public static Bitfield fromMessage(Message message) {
        return new Bitfield(BitSet.valueOf(message.getContent()));
    }

    public BitSet getBitset() {
        return bitset;
    }
}
