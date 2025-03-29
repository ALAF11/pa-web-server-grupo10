import java.io.*;
import java.util.concurrent.BlockingQueue;

/**
 * The LogConsumer class is responsible for asynchronously writing log entries to a file.
 * <p>
 * This class implements the Runnable interface to operate in a separate thread, consuming
 * log entries from a shared blocking queue and writing them to a specified log file.
 * It ensures thread-safe log writing and proper resource management.
 *
 * @see BlockingQueue
 * @see LogEntry
 */

public class LogConsumer implements Runnable {
    private BlockingQueue<LogEntry> logQueue;
    private final String logFilePath;

    public LogConsumer(BlockingQueue<LogEntry> logQueue, String logFilePath){
        this.logQueue = logQueue;
        this.logFilePath = logFilePath;

    }

    /**
     * The main execution method that continuously consumes log entries from the queue.
     * <p>
     * This method runs in a loop until the thread is interrupted, writing each log entry
     * to the log file in JSON format. It handles interruptions gracefully and ensures
     * proper cleanup of resources.
     */

    @Override
    public void run() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(logFilePath, true))){
           while(!Thread.currentThread().isInterrupted()){
               try {
                   LogEntry logEntry = logQueue.take();

                   writer.write(logEntry.toJSON());
                   writer.newLine();
                   writer.flush();
               }
               catch (InterruptedException e) {
                   Thread.currentThread().interrupt();
                   break;
               }
           }
        }
        catch (IOException e){
            if(!Thread.currentThread().isInterrupted()) {
                e.printStackTrace();
            }
        }
    }

}
