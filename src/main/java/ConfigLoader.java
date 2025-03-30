import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * The ConfigLoader class provides functionality to load server configuration from a properties file.
 * <p>
 * This utility class reads a configuration file in Java Properties format and creates
 * a {@link ServerConfig} object populated with the configuration values.
 * <p>
 * It handles the file loading and parsing operations, converting the flat properties structure
 * into a configuration object for the server
 *     
 * @see ServerConfig
 * @see Properties
 */

public class ConfigLoader {

    /**
     * Loads server configuration from the specified file path.
     * <p>
     * The method opens the configuration file, parses the properties format,
     * creates and populates a ServerConfig object, and ensures proper resource clean up.
     * <p>
     * @param configFilePath the absolute or relative path to the configuration file
     * @return a fully populated ServerConfig object
     * @throws IOException if the configuration file cannot be found or there are
     * permission issues reading the file or the file format is invalid or any
     * other I/O error occurs during reading
     * 
     * @see ServerConfig#setConfig(String, String)
     */
    
    public static ServerConfig loadConfig(String configFilePath) throws IOException {
        ServerConfig config = new ServerConfig();
        Properties properties = new Properties();

        try (FileInputStream file = new FileInputStream(configFilePath)) {
            properties.load(file);

            for (String key : properties.stringPropertyNames()) {
                config.setConfig(key, properties.getProperty(key));
            }
        } catch (IOException e) {
            throw new IOException("Failed to load configuration from: " + configFilePath, e);
        }

        return config;
    }
}