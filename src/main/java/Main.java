import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;

public class Main {

    public static void main(String[] args) {
        try {
            String configFilePath = System.getProperty("user.dir") + "/html/server.config";
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
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
