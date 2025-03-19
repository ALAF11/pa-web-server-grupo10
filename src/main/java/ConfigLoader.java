import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class ConfigLoader {
    public static ServerConfig loadConfig(String configFilePath) throws IOException {
        ServerConfig config = new ServerConfig();
        Properties properties = new Properties();

        try (FileInputStream file = new FileInputStream(configFilePath)) {

            properties.load(file);

            config.setServerRoot(getRequiredProperty(properties, "server.root"));
            config.setPort(Integer.parseInt(getRequiredProperty(properties, "server.port")));
            config.setDocumentRoot(getRequiredProperty(properties, "server.document.root"));
            config.setDefaultPage(getRequiredProperty(properties, "server.default.page"));
            config.setDefaultPageExtension(getRequiredProperty(properties, "server.default.page.extension"));
            config.setPage404(getRequiredProperty(properties, "server.page.404"));
            config.setMaximumRequests(Integer.parseInt(getRequiredProperty(properties, "server.maximum.requests")));
        }

        return config;
    }

    private static String getRequiredProperty(Properties properties, String key) {
        String value = properties.getProperty(key);
        if (value == null) {
            throw new IllegalArgumentException("Missing required configuration property: " + key);
        }
        return value;
    }
}