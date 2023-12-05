import java.io.*;
import java.util.ArrayList;
import java.util.BitSet;

public class PeerFileReader {
    static void readPeerInfoFile(ArrayList<Peer> peers) {
        String currentWorkingDirectory = System.getProperty("user.dir");

        // Path of file to read
        String filePath = currentWorkingDirectory + "/PeerInfo.cfg";

        try {
            // Create a File object
            File file = new File(filePath);

            // Check if the file exists
            if (!file.exists()) {
                //System.out.println("The file does not exist.");
                return;
            }

            // Create a FileInputStream to read the file
            FileInputStream fileInputStream = new FileInputStream(file);

            // Create a BufferedReader to efficiently read the file
            BufferedReader reader = new BufferedReader(new InputStreamReader(fileInputStream));

            String line;


            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(" ");
                ////System.out.println(parts[0]);  // Access the first part
                ////System.out.println(parts[1]);  // Access the second part
                ////System.out.println(parts[2]);
                boolean b = (Integer.parseInt(parts[3]) == 1);
                Peer p = new Peer(Integer.parseInt(parts[0]), parts[1], Integer.parseInt(parts[2]), b);
                peers.add(p);

                ////System.out.println(line);
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
        String currentWorkingDirectory = System.getProperty("user.dir");
        // Specify the path to the file you want to read
        String filePath = currentWorkingDirectory + "/Common.cfg";

        try {
            // Create a File object
            File file = new File(filePath);

            // Check if the file exists
            if (!file.exists()) {
                //System.out.println("The file does not exist.");
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
                ////System.out.println(parts[0]);  // Access the first part
                //System.out.println(parts[1]);  // Access the second part
                ////System.out.println(parts[2]);
                temp.add(parts[1]);

                //System.out.println(line);
            }
            //System.out.println(Integer.parseInt(temp.get(0)));
            //System.out.println(temp.get(1));
            //System.out.println(temp.get(2));
            //System.out.println(temp.get(3));
            //System.out.println(temp.get(4));
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
        String currentWorkingDirectory = System.getProperty("user.dir");

        String filePath = currentWorkingDirectory + "/" + peer.getPeerId() + "/" + peer.getPeerConfig().getFileName();

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
            //System.out.println("Size of JPEG byte array: " + jpegBytes.length);

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
        String currentWorkingDirectory = System.getProperty("user.dir");

        byte[] byteArray = peer.getImageFileData(); /* Your byte array containing JPEG data */;
//        BitSet conversion = ByteConverter.byteArrayToBitSet(peer.getImageFileData());
//        byte[] byteConvert = ByteConverter.bitSetToByteArraySameOrder(conversion);
//        byte[] byteArray = new byte[byteConvert.length];
//        System.arraycopy(byteConvert, 0, byteArray, 0, byteArray.length);

        String directoryName = currentWorkingDirectory + "/" + peer.getPeerId();
        File directory = new File(directoryName);

        // Check if the directory doesn't exist, then create it
        if (!directory.exists()) {
            boolean created = directory.mkdirs();
            if (created) {
                System.out.println("Directory created successfully.");
            } else {
                System.out.println("Failed to create directory.");
            }
        } else {
            System.out.println("Directory already exists.");
        }

        String filePath = currentWorkingDirectory + "/" + peer.getPeerId() + "/" + peer.getPeerConfig().getFileName();

        try (FileOutputStream fos = new FileOutputStream(filePath)) {
            fos.write(byteArray);
            //System.out.println("Byte array successfully written to a JPEG file.");
        } catch (IOException e) {
            System.err.println("Error writing the byte array to a JPEG file: " + e.getMessage());
        }
    }

    public static void writeTestFile(Peer peer) {

        BitSet conversion = ByteConverter.byteArrayToBitSet(peer.getImageFileData());
        byte[] byteConvert = ByteConverter.bitSetToByteArraySameOrder(conversion);
        byte[] byteArray = new byte[byteConvert.length];
        System.arraycopy(byteConvert, 0, byteArray, 0, byteArray.length);/* Your byte array containing JPEG data */;

        String filePath = "~/" + peer.getPeerId() + "/testTree.jpg";

        try (FileOutputStream fos = new FileOutputStream(filePath)) {
            fos.write(byteArray);
            //System.out.println("Byte array successfully written to a JPEG file.");
        } catch (IOException e) {
            System.err.println("Error writing the byte array to a JPEG file: " + e.getMessage());
        }
    }

    public static void checkDifferences() {
        String file1Path = "~/1001/tree.jpg"; // Replace with the path to your first file
        String file2Path = "~/1002/tree.jpg"; // Replace with the path to your second file

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
                    //System.out.println("Files differ at position: " + position);
                    areEqual = false;
                    break;
                }
            } while (byteReadFromFirst != -1 && byteReadFromSecond != -1);

            if (areEqual) {
                //System.out.println("Files are identical.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
