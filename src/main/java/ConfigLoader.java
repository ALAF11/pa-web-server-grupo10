import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class ConfigLoader {
    public static ServerConfig loadConfig(String configFilePath) throws IOException {
        ServerConfig config = new ServerConfig();
        Properties properties = new Properties();

        try (FileInputStream file = new FileInputStream(configFilePath)) {
            properties.load(file);

            for (String key : properties.stringPropertyNames()) {
                config.setConfig(key, properties.getProperty(key));
            }
        }

        return config;
    }
}