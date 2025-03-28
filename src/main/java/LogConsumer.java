import java.io.*;
import java.rmi.server.LoaderHandler;
import java.time.LocalDateTime;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class LogConsumer implements Runnable {
    private BlockingQueue<LogEntry> logQueue;
    private final String logFilePath;

    public LogConsumer(BlockingQueue<LogEntry> logQueue, String logFilePath){
        this.logQueue = logQueue;
        this.logFilePath = logFilePath;

    }

    @Override
    public void run() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(logFilePath, true))){
           while(!Thread.currentThread().isInterrupted()){
               try {
                   LogEntry logEntry = logQueue.take(); //this call blocks until an entry is available

                   writer.write(logEntry.toJSON());
                   writer.newLine(); //
                   writer.flush();
               }
               catch (InterruptedException e) {
                   Thread.currentThread().interrupt(); // Restores interrupt status
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
