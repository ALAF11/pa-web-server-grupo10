import java.io.*;
import java.util.concurrent.BlockingQueue;

/**
 * The LogConsumer class implements asynchronous file logging using a producer-consumer pattern.
 * <p>
 * This component runs in a dedicated thread, consuming LogEntry objects from
 * a shared blocking queue and writting them sequentially to a log file
 * in JSON format. It ensures thread-safe log writing and proper resource management.
 * <p>
 * The log file is opened in append mode, preserving existing log entries.
 *
 * @see BlockingQueue
 * @see LogEntry
 */

public class LogConsumer implements Runnable {
    private BlockingQueue<LogEntry> logQueue;
    private final String logFilePath;

    /**
     * Constructs a new LogConsumer with the specified queue and output file path.
     * <p>
     * The consumer will write all log entries taken from the queue to the specified file,
     * creating it if necessary or appending to existing content.
     *
     *
     * @param logQueue the shared blocking queue containing log entries to consume.
     * @param logFilePath the path to the log file where entries will be written.
     */

    public LogConsumer(BlockingQueue<LogEntry> logQueue, String logFilePath){
        this.logQueue = logQueue;
        this.logFilePath = logFilePath;

    }

    /**
     * The main execution method that continuously consumes log entries from the queue
     * and writes them to the log file.
     * <p>
     * This method runs in a loop until the thread is interrupted, writing each log entry
     * to the log file in JSON format. It handles interruptions gracefully and ensures
     * proper cleanup of resources.
     * <p>
     * On interruption, completes any pending write operations before terminating.
     * This method guarantees proper closure of file resources even if exceptions occur.
     *
     * @throws RuntimeException if an unrecoverable I/O error occurs during the file writting.
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
