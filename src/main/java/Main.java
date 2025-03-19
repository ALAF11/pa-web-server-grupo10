import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        try {
            String configFilePath = System.getProperty("user.dir") + "/html/server.config";
            ServerConfig config = ConfigLoader.loadConfig(configFilePath);

            ThreadPool threadPool = new ThreadPool(config.getMaximumRequests(), config.getMaximumRequests());
            MainHTTPServerThread serverThread = new MainHTTPServerThread(config, threadPool);
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
