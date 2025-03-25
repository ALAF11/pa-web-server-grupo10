import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import static org.junit.jupiter.api.Assertions.*;

public class ConfigLoaderTest {
    @Test
    public void testLoadValidConfig() throws IOException {

        Path tempConfig = Files.createTempFile("test_config",".properties");
        Files.write(tempConfig, "test_config".getBytes(), StandardOpenOption.APPEND);
        ServerConfig config= ConfigLoader.loadConfig(tempConfig.toString());

        assertEquals("8080", config.getConfig("server.port"));
        assertEquals("/html", config.getConfig("server.root"));
    }

    @Test
    public void testLoadNonExistentConfig() {
        assertThrows(IOException.class, () -> {
            ConfigLoader.loadConfig("non_existent_file.properties");
        });
    }
}