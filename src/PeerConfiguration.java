public class PeerConfiguration {

    private int numberOfPreferredNeighbors;
    private int unchokingInterval;
    private int optimisticUnchokingInterval;
    private String fileName;
    private int fileSize;
    private int pieceSize;

    public PeerConfiguration(int preferredNeighbors, int uInterval, int ouInterval, String fName, int fSize, int pSize) {

        numberOfPreferredNeighbors = preferredNeighbors;
        unchokingInterval = uInterval;
        optimisticUnchokingInterval = ouInterval;
        fileName = fName;
        fileSize = fSize;
        pieceSize = pSize;

    }

    public void setValues(int preferredNeighbors, int uInterval, int ouInterval, String fName, int fSize, int pSize) {

        numberOfPreferredNeighbors = preferredNeighbors;
        unchokingInterval = uInterval;
        optimisticUnchokingInterval = ouInterval;
        fileName = fName;
        fileSize = fSize;
        pieceSize = pSize;

    }

    public PeerConfiguration() {

    }

    public void printConstants() {

        //System.out.println(numberOfPreferredNeighbors);

        //System.out.println(unchokingInterval);

        //System.out.println(optimisticUnchokingInterval);

        //System.out.println(fileName);

        //System.out.println(fileSize);

        //System.out.println(pieceSize);

    }

    public int getNumberOfPreferredNeighbors() {

        return numberOfPreferredNeighbors;

    }

    public int getUnchokingInterval() {

        return unchokingInterval;

    }

    public int getOptimisticUnchokingInterval() {

        return optimisticUnchokingInterval;

    }

    public String getFileName() {

        return fileName;

    }

    public int getFileSize() {

        return fileSize;

    }

    public int getPieceSize() {

        return pieceSize;

    }
}
