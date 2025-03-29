import java.util.HashMap;
import java.util.Map;

/**
 * The ServerConfig class manager configuration settings for the HTTP server.
 * <p>
 *      This class provides a centralized way to store and retrieved configuration
 *      parameters using key-value pairs. It supports both string and integer values.
 */

public class ServerConfig {

    private final Map<String, String> configMap;

    /**
     * Constructs a new empty ServerConfig object.
     */

    public ServerConfig() {
        this.configMap = new HashMap<>();
    }

    /**
     * Sets a configuration value.
     *
     * @param key the configuration key.
     * @param value the configuration value.
     */

    public void setConfig(String key, String value) {
        configMap.put(key, value);
    }

    /**
     * Retrieves a string configuration value.
     *
     * @param key the configuration key to retrieve
     * @return the configuration value, or null if not found
     */

    public String getConfig(String key) {
        return configMap.get(key);
    }

    /**
     * Retrieves an integer configuration value.
     *
     * @param key the configuration key to retrieve
     * @return the configuration value as an integer
     * @throws NumberFormatException If the value cannot be parsed as an integer
     */

    public int getIntConfig(String key) {
        return Integer.parseInt(configMap.get(key));
    }
}