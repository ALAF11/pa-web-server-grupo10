import java.util.HashMap;
import java.util.Map;

public class ServerConfig {

    private Map<String, String> configMap;

    public ServerConfig() {
        this.configMap = new HashMap<>();
    }

    public void setConfig(String key, String value) {
        configMap.put(key, value);
    }

    public String getConfig(String key) {
        return configMap.get(key);
    }

    public int getIntConfig(String key) {
        return Integer.parseInt(configMap.get(key));
    }
}