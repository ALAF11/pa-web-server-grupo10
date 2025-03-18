public class Main {
    public static void main(String[] args) {
        String configFilePath = System.getProperty("user.dir") + "/html/server.config";
        ServerConfig config = ConfigLoader.loadConfig(configFilePath);

        ThreadPool threadPool = new ThreadPool(config.getMaximumRequests(), config.getMaximumRequests());
        MainHTTPServerThread s = new MainHTTPServerThread(config, threadPool);
        s.start();
        try {
            s.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
