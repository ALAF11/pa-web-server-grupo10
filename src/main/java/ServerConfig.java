import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

public class ServerConfig {
    private final ReentrantLock lock = new ReentrantLock();
    private final Map<String, String> configMap;

    public ServerConfig() {
        this.configMap = new HashMap<>();
    }

    public void setConfig(String key, String value) {
        lock.lock();
        try {
            configMap.put(key, value);
        } finally {
            lock.unlock();
        }
    }

    public String getConfig(String key) {
        lock.lock();
        try {
            return configMap.get(key);
        } finally {
            lock.unlock();
        }
    }


    public int getIntConfig(String key) {
        lock.lock();
        try {
            return Integer.parseInt(configMap.get(key));
        } finally {
            lock.unlock();
        }
    }
}