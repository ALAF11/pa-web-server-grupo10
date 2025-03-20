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
           while(true){
               LogEntry logEntry = logQueue.take(); //this call blocks until an entry is available

               writer.write(logEntry.toJSON());
               writer.newLine(); //
               writer.flush();
           }
        }
        catch (IOException | InterruptedException e){
            e.printStackTrace();
        }
    }

//    public static void startLogThread() {
//        Thread logThread = new Thread(() -> {
//            while (running || !logQueue.isEmpty()) {
//                try {
//                    LogEntry entry = logQueue.take();
//                    writeToFile(entry);
//                } catch (InterruptedException e) {
//                    Thread.currentThread().interrupt();
//                } catch (Exception e) {
//                    System.err.println("Log error: " + e.getMessage());
//                }
//            }
//        });
//        logThread.start();
//    }
//    private static synchronized void writeToFile(LogEntry entry){
//        try(FileWriter writer =new FileWriter("server.log",true)){
//            writer.write(entry.toJSON()+ "\n");
//        } catch (Exception e) {
//            System.err.println("Failed to write log: " + e.getMessage());
//        }
//    }
//    public static void logRequest(LocalDateTime timestamp, String method,
//                                  String route, String origin, int status) {
//        logQueue.add(new LogEntry(timestamp, method, route, origin, status));
//    }
//    public static void shutdown() {
//
//        running = false;
//    }

}
