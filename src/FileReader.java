import java.io.*;
import java.util.ArrayList;
import java.util.BitSet;

public class FileReader {
    static void readPeerInfoFile(ArrayList<Peer> peers) {

        // Path of file to read
        String filePath = "../project_config_file/PeerInfo.test.cfg";

        try {
            // Create a File object
            File file = new File(filePath);

            // Check if the file exists
            if (!file.exists()) {
                System.out.println("The file does not exist.");
                return;
            }

            // Create a FileInputStream to read the file
            FileInputStream fileInputStream = new FileInputStream(file);

            // Create a BufferedReader to efficiently read the file
            BufferedReader reader = new BufferedReader(new InputStreamReader(fileInputStream));

            String line;


            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(" ");
                //System.out.println(parts[0]);  // Access the first part
                //System.out.println(parts[1]);  // Access the second part
                //System.out.println(parts[2]);
                boolean b = (Integer.parseInt(parts[3]) == 1);
                Peer p = new Peer(Integer.parseInt(parts[0]), parts[1], Integer.parseInt(parts[2]), b);
                peers.add(p);

                //System.out.println(line);
            }

            // Close the resources when done
            reader.close();
            fileInputStream.close();
            return;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static void readConfigInfo(PeerConfiguration config) {
        // Specify the path to the file you want to read
        String filePath = "../project_config_file/Common.cfg";

        try {
            // Create a File object
            File file = new File(filePath);

            // Check if the file exists
            if (!file.exists()) {
                System.out.println("The file does not exist.");
                return;
            }

            // Create a FileInputStream to read the file
            FileInputStream fileInputStream = new FileInputStream(file);

            // Create a BufferedReader to efficiently read the file
            BufferedReader reader = new BufferedReader(new InputStreamReader(fileInputStream));

            String line;

            ArrayList<String> temp = new ArrayList<>();
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(" ");
                //System.out.println(parts[0]);  // Access the first part
                System.out.println(parts[1]);  // Access the second part
                //System.out.println(parts[2]);
                temp.add(parts[1]);

                System.out.println(line);
            }
            System.out.println(Integer.parseInt(temp.get(0)));
            System.out.println(temp.get(1));
            System.out.println(temp.get(2));
            System.out.println(temp.get(3));
            System.out.println(temp.get(4));
            config.setValues(
                    Integer.parseInt(temp.get(0)),
                    Integer.parseInt(temp.get(1)),
                    Integer.parseInt(temp.get(2)),
                    temp.get(3),
                    Integer.parseInt(temp.get(4)),
                    Integer.parseInt(temp.get(5))
            );

            // Close the resources when done
            reader.close();
            fileInputStream.close();
            return;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void readEntireFile(Peer peer) {
        String filePath = "../project_config_file/" + peer.getPeerId() + "/" + peer.getPeerConfig().getFileName();

        try (InputStream inputStream = new FileInputStream(filePath);
             ByteArrayOutputStream byteStream = new ByteArrayOutputStream()) {

            byte[] buffer = new byte[peer.getPeerConfig().getFileSize()];
            int bytesRead;

            // Read from the input stream and write to the byte array stream
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                byteStream.write(buffer, 0, bytesRead);
            }

            byte[] jpegBytes = byteStream.toByteArray();

            // Display the size of the byte array
            System.out.println("Size of JPEG byte array: " + jpegBytes.length);

            // put it into peer
            peer.setImageFileData(jpegBytes);

            // Use the 'jpegBytes' byte array as needed (e.g., send over network, process, etc.)
        } catch (IOException e) {
            e.printStackTrace();
        }
        //writeTestFile(peer);
        //checkDifferences();
    }

    public static void writeFile(Peer peer) {

        byte[] byteArray = peer.getImageFileData(); /* Your byte array containing JPEG data */;
//        BitSet conversion = ByteConverter.byteArrayToBitSet(peer.getImageFileData());
//        byte[] byteConvert = ByteConverter.bitSetToByteArraySameOrder(conversion);
//        byte[] byteArray = new byte[byteConvert.length];
//        System.arraycopy(byteConvert, 0, byteArray, 0, byteArray.length);

        String filePath = "../project_config_file/" + peer.getPeerId() + "/" + peer.getPeerConfig().getFileName();

        try (FileOutputStream fos = new FileOutputStream(filePath)) {
            fos.write(byteArray);
            System.out.println("Byte array successfully written to a JPEG file.");
        } catch (IOException e) {
            System.err.println("Error writing the byte array to a JPEG file: " + e.getMessage());
        }
    }

    public static void writeTestFile(Peer peer) {

        BitSet conversion = ByteConverter.byteArrayToBitSet(peer.getImageFileData());
        byte[] byteConvert = ByteConverter.bitSetToByteArraySameOrder(conversion);
        byte[] byteArray = new byte[byteConvert.length];
        System.arraycopy(byteConvert, 0, byteArray, 0, byteArray.length);/* Your byte array containing JPEG data */;

        String filePath = "../project_config_file/" + peer.getPeerId() + "/testTree.jpg";

        try (FileOutputStream fos = new FileOutputStream(filePath)) {
            fos.write(byteArray);
            System.out.println("Byte array successfully written to a JPEG file.");
        } catch (IOException e) {
            System.err.println("Error writing the byte array to a JPEG file: " + e.getMessage());
        }
    }

    public static void checkDifferences() {
        String file1Path = "../project_config_file/1001/tree.jpg"; // Replace with the path to your first file
        String file2Path = "../project_config_file/1002/tree.jpg"; // Replace with the path to your second file

        try (FileInputStream fis1 = new FileInputStream(file1Path);
             FileInputStream fis2 = new FileInputStream(file2Path)) {

            boolean areEqual = true;
            int differenceCount = 0;
            int byteReadFromFirst, byteReadFromSecond;
            long position = 0;

            do {
                byteReadFromFirst = fis1.read();
                byteReadFromSecond = fis2.read();
                position++;

                if (byteReadFromFirst != byteReadFromSecond) {
                    System.out.println("Files differ at position: " + position);
                    areEqual = false;
                    break;
                }
            } while (byteReadFromFirst != -1 && byteReadFromSecond != -1);

            if (areEqual) {
                System.out.println("Files are identical.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
