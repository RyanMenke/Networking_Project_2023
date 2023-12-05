import java.io.IOException;
import java.time.LocalDateTime;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class LogWriter2 extends Thread {
    private static LogWriter2 instance;
    private Peer peer;
    private LinkedBlockingQueue<String> queue;
    private Logger logger;

    private LogWriter2(Peer peer) throws IOException {
        String currentWorkingDirectory = System.getProperty("user.dir");

        this.peer = peer;
        this.queue = new LinkedBlockingQueue<>();
        FileHandler fileHandler = new FileHandler(
                currentWorkingDirectory + "/log_" + peer.getPeerId() + ".log",
                0,
                1,
                true
        );
        fileHandler.setFormatter(new SimpleFormatter());
        this.logger = Logger.getLogger("peer_" + peer.getPeerId());
        this.logger.addHandler(fileHandler);
        this.logger.setLevel(Level.INFO);

    }

    public static LogWriter2 getInstance(Peer peer) throws IOException {
        if (instance == null) {
            instance = new LogWriter2(peer);
            instance.start();
        }

        return instance;
    }

    interface Loggable {
        String makeLog();
    }

    public void writeHandshakeLog(int peer1) {

    }

    public void writeLog(String log) {
        LocalDateTime timeOfConnection = LocalDateTime.now();
        String finalLog = ("" + timeOfConnection + ": " + log);
        this.queue.add(finalLog);
    }

    @Override
    public void run() {
        while (true) {
            try {
                String log = queue.take();
                //LocalDateTime timeOfConnection = LocalDateTime.now();
                this.logger.info( log);
            } catch (InterruptedException e) {
                break;
            }
        }
    }
}

