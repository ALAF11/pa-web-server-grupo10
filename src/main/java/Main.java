import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;

/**
 * The Main class is the entry point for the HTTP server application.
 * <p>
 * This class initializes all server component including configuration loading,
 * thread pool setup, access controller, logging system and the main
 * server thread. It establishes the foundation for the server´s operation.
 */

public class Main {

    /**
     * The main method that starts the HTTP server.
     * <p>
     * The method performs the following initialization sequence:
     * 1. Loads server configuration from server.config file
     * 2. Sets up asynchronous logging system
     * 3. Initializes file access controller
     * 4. Creates thread pool with configured size
     * 5. Configures request limiter semaphore
     * 6. Starts main server thread
     * <p>
     * The server runs until interrupted, handling all client
     * requests within the configured concurrency limits.
     *
     * @param args command line arguments (currently unused)
     * @throws IOException if the configuration file cannot be loaded or is invalid.
     * @throws InterruptedException if the server thread is interrupted during execution
     */

    public static void main(String[] args) {
        try {
            String configFilePath = System.getProperty("user.dir") + "/server.config";
            ServerConfig config = ConfigLoader.loadConfig(configFilePath);

            BlockingQueue<LogEntry> logQueue = new LinkedBlockingQueue<>();

            Thread logConsumerThread = new Thread(new LogConsumer(logQueue, config.getConfig("server.log.file")));
            logConsumerThread.start();

            FileAccessController fileAccessController = new FileAccessController(config);

            ThreadPool threadPool = new ThreadPool(config.getIntConfig("server.maximum.requests"), config.getIntConfig("server.maximum.requests"));

            Semaphore requestLimiter = new Semaphore(config.getIntConfig("server.max.total.requests"), true);

            MainHTTPServerThread serverThread = new MainHTTPServerThread(config, threadPool, fileAccessController, logQueue, requestLimiter);

            serverThread.start();
            serverThread.join();

        } catch (IOException e) {
            System.err.println("Error loading the configuration file: " + e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Server was interrupted");
        }
    }
}
