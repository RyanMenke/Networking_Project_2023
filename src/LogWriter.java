import java.time.LocalDateTime;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class LogWriter {

    //Message for when a peer makes a connection

    public static void TCPMakeConnection(int peer1, int peer2) {
        LocalDateTime timeOfConnection = LocalDateTime.now();

        String peer_1_name = "peer_100" + peer1;
        String peer_2_name = "peer_100" + peer2;



        Logger logger = Logger.getLogger("my logger");

        try {

            // I need to create a FileHandler to be able to write log files

            FileHandler fileHandler = new FileHandler("../project/log_" + peer_1_name + ".log");
            fileHandler.setFormatter(new SimpleFormatter());
            logger.addHandler(fileHandler);

            // Set the logging level (e.g., INFO, WARNING, SEVERE)

            logger.setLevel(Level.INFO);


            //Log message


            logger.info(timeOfConnection + ": Peer " + peer_1_name + " makes a connection to Peer " + peer_2_name + ".");
        } catch (Exception e) {
            e.printStackTrace();
        }


        }

        //Message for when a TCP connection is received

    public static void TCPReceiveConnection(int peer1, int peer2) {
        LocalDateTime timeOfConnection = LocalDateTime.now();

        String peer_1_name = "peer_100" + peer1;
        String peer_2_name = "peer_100" + peer2;



        Logger logger = Logger.getLogger("my logger");

        try {

            // I need to create a FileHandler to be able to write log files

            FileHandler fileHandler = new FileHandler("../project/log_" + peer_1_name + ".log");
            fileHandler.setFormatter(new SimpleFormatter());
            logger.addHandler(fileHandler);

            // Set the logging level (e.g., INFO, WARNING, SEVERE)

            logger.setLevel(Level.INFO);


            //Log message


            logger.info(timeOfConnection +": Peer "+peer_1_name+" is connected from Peer "+peer_2_name+".");
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    //Log for when preferred neighbor is changed

    public static void ChangePreferredNeighbor(int peer1, String preferredNeighborList) {
        LocalDateTime timeOfConnection = LocalDateTime.now();

        String peer_1_name = "peer_100" + peer1;




        Logger logger = Logger.getLogger("my logger");

        try {

            // I need to create a FileHandler to be able to write log files

            FileHandler fileHandler = new FileHandler("../project/log_" + peer_1_name + ".log");
            fileHandler.setFormatter(new SimpleFormatter());
            logger.addHandler(fileHandler);

            // Set the logging level (e.g., INFO, WARNING, SEVERE)

            logger.setLevel(Level.INFO);


            //Log message


            logger.info(timeOfConnection+": Peer "+peer_1_name+" has the preferred neighbors "+preferredNeighborList+".");
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    //message for when a neighbor is optomistically unchoked

    public static void ChangeOptimisticallyUnchokedNeighbor(int peer1, int peer2) {
        LocalDateTime timeOfConnection = LocalDateTime.now();

        String peer_1_name = "peer_100" + peer1;
        String peer_2_name = "peer_100" + peer2;



        Logger logger = Logger.getLogger("my logger");

        try {

            // I need to create a FileHandler to be able to write log files

            FileHandler fileHandler = new FileHandler("../project/log_" + peer_1_name + ".log");
            fileHandler.setFormatter(new SimpleFormatter());
            logger.addHandler(fileHandler);

            // Set the logging level (e.g., INFO, WARNING, SEVERE)

            logger.setLevel(Level.INFO);


            //Log message


            logger.info(timeOfConnection +": Peer "+peer_1_name+" has the optimistically unchoked neighbor "+peer_2_name+".");
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    //Log message for when a peer unchokes another peer

    public static void Unchoking(int peer1, int peer2) {
        LocalDateTime timeOfConnection = LocalDateTime.now();

        String peer_1_name = "peer_100" + peer1;
        String peer_2_name = "peer_100" + peer2;



        Logger logger = Logger.getLogger("my logger");

        try {

            // I need to create a FileHandler to be able to write log files

            FileHandler fileHandler = new FileHandler("../project/log_" + peer_1_name + ".log");
            fileHandler.setFormatter(new SimpleFormatter());
            logger.addHandler(fileHandler);

            // Set the logging level (e.g., INFO, WARNING, SEVERE)

            logger.setLevel(Level.INFO);


            //Log message


            logger.info(timeOfConnection +": Peer "+peer_1_name+" is unchoked by "+peer_2_name+".");
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    //message for when peer chokes another peer

    public static void Choking(int peer1, int peer2) {
        LocalDateTime timeOfConnection = LocalDateTime.now();

        String peer_1_name = "peer_100" + peer1;
        String peer_2_name = "peer_100" + peer2;



        Logger logger = Logger.getLogger("my logger");

        try {

            // I need to create a FileHandler to be able to write log files

            FileHandler fileHandler = new FileHandler("../project/log_" + peer_1_name + ".log");
            fileHandler.setFormatter(new SimpleFormatter());
            logger.addHandler(fileHandler);

            // Set the logging level (e.g., INFO, WARNING, SEVERE)

            logger.setLevel(Level.INFO);


            //Log message


            logger.info(timeOfConnection +": Peer "+peer_1_name+" is choked by "+peer_2_name+".");
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    //message for when a have message is received

    public static void ReceiveHaveMessage(int peer1, int peer2, int pieceIndex) {
        LocalDateTime timeOfConnection = LocalDateTime.now();

        String peer_1_name = "peer_100" + peer1;
        String peer_2_name = "peer_100" + peer2;



        Logger logger = Logger.getLogger("my logger");

        try {

            // I need to create a FileHandler to be able to write log files

            FileHandler fileHandler = new FileHandler("../project/log_" + peer_1_name + ".log");
            fileHandler.setFormatter(new SimpleFormatter());
            logger.addHandler(fileHandler);

            // Set the logging level (e.g., INFO, WARNING, SEVERE)

            logger.setLevel(Level.INFO);


            //Log message


            logger.info(timeOfConnection +": Peer "+peer_1_name+" received the ‘have’ message from "+peer_2_name+" for the piece "+pieceIndex+".");
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    //message for when an "Interested" message is received

    public static void ReceiveInterestedMessage(int peer1, int peer2) {
        LocalDateTime timeOfConnection = LocalDateTime.now();

        String peer_1_name = "peer_100" + peer1;
        String peer_2_name = "peer_100" + peer2;



        Logger logger = Logger.getLogger("my logger");

        try {

            // I need to create a FileHandler to be able to write log files

            FileHandler fileHandler = new FileHandler("../project/log_" + peer_1_name + ".log");
            fileHandler.setFormatter(new SimpleFormatter());
            logger.addHandler(fileHandler);

            // Set the logging level (e.g., INFO, WARNING, SEVERE)

            logger.setLevel(Level.INFO);


            //Log message


            logger.info(timeOfConnection +": Peer "+peer_1_name+" received the ‘interested’ message from "+peer_2_name+".");
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    //Log message for when a "Not interested" message is received

    public static void ReceiveNotInterestedMessage(int peer1, int peer2) {
        LocalDateTime timeOfConnection = LocalDateTime.now();

        String peer_1_name = "peer_100" + peer1;
        String peer_2_name = "peer_100" + peer2;



        Logger logger = Logger.getLogger("my logger");

        try {

            // I need to create a FileHandler to be able to write log files

            FileHandler fileHandler = new FileHandler("../project/log_" + peer_1_name + ".log");
            fileHandler.setFormatter(new SimpleFormatter());
            logger.addHandler(fileHandler);

            // Set the logging level (e.g., INFO, WARNING, SEVERE)

            logger.setLevel(Level.INFO);


            //Log message


            logger.info(timeOfConnection +": Peer "+peer_1_name+" received the ‘not interested’ message from "+peer_2_name+".");
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    //message for when a piece is being downloaded

    public static void DownloadingPiece(int peer1, int peer2, int pieceIndex, int numberOfPieces) {
        LocalDateTime timeOfConnection = LocalDateTime.now();

        String peer_1_name = "peer_100" + peer1;
        String peer_2_name = "peer_100" + peer2;



        Logger logger = Logger.getLogger("my logger");

        try {

            // I need to create a FileHandler to be able to write log files

            FileHandler fileHandler = new FileHandler("../project/log_" + peer_1_name + ".log");
            fileHandler.setFormatter(new SimpleFormatter());
            logger.addHandler(fileHandler);

            // Set the logging level (e.g., INFO, WARNING, SEVERE)

            logger.setLevel(Level.INFO);


            //Log message


            logger.info(timeOfConnection +": Peer "+peer_1_name+" has downloaded the piece "+pieceIndex+" from " +
                    peer_2_name+". Now the number of pieces it has is "+numberOfPieces+".");
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    //Log message for when a peer completes a download

    public static void CompletionOfDownload(int peer1, int peer2) {
        LocalDateTime timeOfConnection = LocalDateTime.now();

        String peer_1_name = "peer_100" + peer1;
        String peer_2_name = "peer_100" + peer2;



        Logger logger = Logger.getLogger("my logger");

        try {

            // I need to create a FileHandler to be able to write log files

            FileHandler fileHandler = new FileHandler("../project/log_" + peer_1_name + ".log");
            fileHandler.setFormatter(new SimpleFormatter());
            logger.addHandler(fileHandler);

            // Set the logging level (e.g., INFO, WARNING, SEVERE)

            logger.setLevel(Level.INFO);


            //Log message


            logger.info(timeOfConnection +": Peer "+peer_1_name+" has downloaded the complete file.");
        } catch (Exception e) {
            e.printStackTrace();
        }


    }
}


