import java.util.BitSet;

public class PeerState {
    private BitSet bitSet;
    private boolean isChoked;
    private boolean isInterested;
    private boolean isOptimisticallyUnchoked;

    private boolean hasCompleteFile;

    private int numberOfBitsInterval;


    public PeerState() {
        isChoked = false;
        isInterested = false;
        isOptimisticallyUnchoked = false;

        //this.numberOfBitsInterval = Integer.MAX_VALUE;
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

    public void setNumberOfBitsInterval(int rate) {
        this.numberOfBitsInterval = rate;
    }

    public void addToNumberOfBitsInterval(int rate) {
        this.numberOfBitsInterval += rate;
    }

    public int getNumberOfBitsInterval() {
        return this.numberOfBitsInterval;
    }

    public void setHasCompleteFile(boolean hasFile) {
        this.hasCompleteFile = hasFile;
    }

    public boolean hasCompleteFile() {
        return this.hasCompleteFile;
    }
}
