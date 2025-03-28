import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ConfigLoaderTest{

    @Test
    public void testLoadConfig() throws IOException{
        //Create a temporary configuration file for testing
        String testConfigContent =
                "server.port=8080\n" +
                        "server.root=/var/www\n" +
                        "server.document.root=html\n";

        Path tempConfigFile = Files.createTempFile("test-config", "config");
        Files.writeString(tempConfigFile,testConfigContent);

        try {
            //Test the loading of the configuration file
            ServerConfig config = ConfigLoader.loadConfig(tempConfigFile.toString());

            //Checks that the properties have been loaded correctly
            assertEquals("8080", config.getConfig("server.port"));
            assertEquals("/var/www", config.getConfig("server.root"));
            assertEquals("html", config.getConfig("server.document.root"));

        } finally {
            //Clean - deletes the temporary file
            Files.deleteIfExists(tempConfigFile);
        }

    }

    @Test
    public void testLoadConfigWithNonExistentFile(){
        //Test with a file that doesn't exist
        assertThrows(IOException.class, () -> {
            ConfigLoader.loadConfig("non-existent-file.config");
        });
    }
}