import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * The configLoader class provides functionality to load server configuration from a properties file.
 * <p>
 *     This utility class reads a configuration file in Java Properties format and creates
 *     a {@link ServerConfig} object populated with the configuration values. It handles
 *     the file loading and parsing operations, converting the flat properties structure
 *     into a configuration object for the server
 *     
 * @see ServerConfig
 * @see Properties
 */

public class ConfigLoader {

    /**
     * Loads server configuration from the specified file path.
     * <p>
     *     The method performs the following operations:
     *     <ol>
     *         <li> Opens configuration file for reading </li>
     *         <li> Parses the properties format </li>
     *         <li> Creates and populates a ServerConfig object </li>
     *         <li> Ensures proper resource cleanup </li>
     *     </ol>
     * @param configFilePath the absolute or relative path to the configuration file
     * @return a fully populated ServerConfig object
     * @throws IOException if any of the following occurs:
     * <ul>
     *     <li> The configuration file cannot be found </li>
     *     <li> There are permission issues reading the file </li>
     *     <li> The file format is invalid </li>
     *     <li> Any other I/O error occurs during reading </li>
     * </ul>
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
        }

        return config;
    }
}