import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class Main {

    public static void main(String[] args) {
        try {
            String configFilePath = System.getProperty("user.dir") + "/html/server.config";
            ServerConfig config = ConfigLoader.loadConfig(configFilePath);

            BlockingQueue<LogEntry> logQueue = new LinkedBlockingQueue<>();

            Thread logConsumerThread = new Thread(new LogConsumer(logQueue, config.getConfig("server.log.file")));
            logConsumerThread.start();

            FileAccessController FileAccessController = new FileAccessController(config.getConfig("server.root"));
            ThreadPool threadPool = new ThreadPool(config.getIntConfig("server.maximum.requests"), config.getIntConfig("server.maximum.requests"));
            MainHTTPServerThread serverThread = new MainHTTPServerThread(config, threadPool, FileAccessController, logQueue);
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
