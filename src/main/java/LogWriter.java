import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LogWriter {
    private static final String LOG_FILE = "server.log";
    private static final BlockingQueue<String> logQueue = new LinkedBlockingQueue<>();
    private static final ExecutorService executorService = Executors.newSingleThreadExecutor();

    static {
        executorService.submit(new LogWriterTask());
    }

    public static void logRequest(String method, String route, String origin, int httpStatus) {
        String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        String logEntry = String.format(
                "{\"timestamp\": \"%s\", \"method\": \"%s\", \"route\": \"%s\", \"origin\": \"%s\", \"http_response_status\": %d}",
                timestamp, method, route, origin, httpStatus
        );

        try {
            logQueue.put(logEntry);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private static class LogWriterTask implements Runnable {
        @Override
        public void run() {
            try (FileWriter fileWriter = new FileWriter(LOG_FILE, true)) {
                while (true) {
                    // Pega um log da fila e escreve no arquivo
                    String logEntry = logQueue.take();
                    fileWriter.write(logEntry + "\n");
                    fileWriter.flush();
                }
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public static void close() {
        executorService.shutdown();
    }
}

