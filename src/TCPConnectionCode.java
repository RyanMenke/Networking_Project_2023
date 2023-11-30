import java.io.*;
import java.net.*;

public class TCPConnectionCode {


    //Beginning Implementation of TCP code

    public static void Listen(int portNumber) {

        try {
            ServerSocket serverSocket = new ServerSocket(portNumber); // Create a server socket listening on port 12345

            Socket clientSocket = serverSocket.accept(); // Accept a connection from a client

        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }




    ////////////////////////////////////////////////////////////////////////////////////
    //This is not my code, this is example code I am using to write my own Socket code//
    ////////////////////////////////////////////////////////////////////////////////////

//            final int port = 12345;
//            final String sharedFileName = "shared.txt";
//
//            // Create a shared file (simulating a file to share)
//            File sharedFile = new File(sharedFileName);
//            try {
//                FileWriter fileWriter = new FileWriter(sharedFile);
//                fileWriter.write("This is the shared file content.");
//                fileWriter.close();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//
//            try (ServerSocket serverSocket = new ServerSocket(port)) {
//                System.out.println("Peer is waiting for a connection on port " + port);
//
//                // Listen for incoming connections from other peers
//                Socket clientSocket = serverSocket.accept();
//                System.out.println("Connection established with the other peer.");
//
//                // Create an input stream to read the file
//                FileInputStream fileInputStream = new FileInputStream(sharedFile);
//                BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);
//
//                // Create an output stream to send the file
//                OutputStream outputStream = clientSocket.getOutputStream();
//                byte[] buffer = new byte[1024];
//                int bytesRead;
//
//                while ((bytesRead = bufferedInputStream.read(buffer)) != -1) {
//                    outputStream.write(buffer, 0, bytesRead);
//                }
//
//                System.out.println("File sent successfully.");
//
//                // Close the connection
//                clientSocket.close();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
        }
