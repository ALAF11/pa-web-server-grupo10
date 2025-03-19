import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class ConfigLoader {
    public static ServerConfig loadConfig(String configFilePath) {
        ServerConfig config = new ServerConfig();
        Properties properties = new Properties();
        try (FileInputStream file = new FileInputStream(configFilePath)) {

            properties.load(file);
            config.setServerRoot(properties.getProperty("server.root", ""));
            config.setPort(Integer.parseInt(properties.getProperty("server.port", "8888")));
            config.setDocumentRoot (properties.getProperty("server.document.root", "/"));
            config.setDefaultPage (properties.getProperty("server.default.page", "index"));
            config.setDefaultPageExtension (properties.getProperty("server.default.page.extension", "html"));
            config.setPage404 (properties.getProperty("server.page.404", "404.html"));
            config.setMaximumRequests (Integer.parseInt(properties.getProperty("server.maximum.requests", "5")));

        } catch (IOException e) {
            System.err.println("Error reading configuration file: " + configFilePath);
            e.printStackTrace();

        }

        return config;
    }
}
