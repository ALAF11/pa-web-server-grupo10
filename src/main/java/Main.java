import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;

/**
 * The Main class is the entry point for the HTTP server application.
 * <p> This class initializes all server component including configuration,
 * thread pool, acess controller, and logging system.
 */

public class Main {

    /**
     * The main method that starts the HTTP server.
     *
     * @param args command line argumments
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
