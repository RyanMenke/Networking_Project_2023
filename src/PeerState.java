import java.util.BitSet;

public class PeerState {
    private BitSet bitSet;
    private boolean isChoked;
    private boolean isInterested;
    private boolean isOptimisticallyUnchoked;

    public PeerState() {
        isChoked = false;
        isInterested = false;
        isOptimisticallyUnchoked = false;
    }

    public BitSet getBitSet() {
        return bitSet;
    }

    public boolean isChoked() {
        return isChoked;
    }

    public boolean isOptimisticallyUnchoked() {
        return isOptimisticallyUnchoked;
    }

    public boolean isInterested() {
        return isInterested;
    }

    public void setBitSet(BitSet bitSet) {
        this.bitSet = bitSet;
    }

    public void setChoked(boolean choked) {
        this.isChoked = choked;
    }

    public void setOptimisticallyUnchoked (boolean optimisticallyUnchoked) {
        this.isOptimisticallyUnchoked = optimisticallyUnchoked;
    }

    public void setInterested(boolean interested) {
        this.isInterested = interested;
    }
}
