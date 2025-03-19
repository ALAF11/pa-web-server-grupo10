import java.io.FileWriter;
import java.time.LocalDateTime;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class LogConsumer {
    private static final BlockingQueue<LogEntry> logQueue = new LinkedBlockingQueue<>();
    private static volatile boolean running = true;

    public static void startLogThread() {
        Thread logThread = new Thread(() -> {
            while (running || !logQueue.isEmpty()) {
                try {
                    LogEntry entry = logQueue.take();
                    writeToFile(entry);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } catch (Exception e) {
                    System.err.println("Log error: " + e.getMessage());
                }
            }
        });
        logThread.start();
    }
    private static synchronized void writeToFile(LogEntry entry){
        try(FileWriter writer =new FileWriter("server.log",true)){
            writer.write(entry.toJSON()+ "\n");
        } catch (Exception e) {
            System.err.println("Failed to write log: " + e.getMessage());
        }
    }
    public static void logRequest(LocalDateTime timestamp, String method,
                                  String route, String origin, int status) {
        logQueue.add(new LogEntry(timestamp, method, route, origin, status));
    }
    public static void shutdown() {
        running = false;
    }
}
