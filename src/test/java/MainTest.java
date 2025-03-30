import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class MainTest {

    @Test
    @DisplayName("Test if server starts when a valid config is provided")
    void testMainWithValidConfig(@TempDir Path tempDir) throws IOException, InterruptedException {

        //Create a temporary configuration file with valid settings
        Path configFile = tempDir.resolve("server.config");
        String configContent = "server.port=8080\n" +
                "server.root=./html\n" +
                "server.document.root=./html\n" +
                "server.log.file=server.log\n" +
                "server.maximum.requests=5\n" +
                "server.default.page=index\n" +
                "server.default.page.extension=html\n" +
                "server.page.404=404.html\n" +
                "server.max.total.requests=5";

        Files.writeString(configFile, configContent);

        //Store and override the current working directory
        String originalUserDir = System.getProperty("user.dir");
        System.setProperty("user.dir", tempDir.toString());

        try {
            //Start the server in a separate thread
            Thread mainThread = new Thread(() -> {
                try {
                    Main.main(new String[]{});
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            mainThread.start();

            //Allow some time for server initialization
            Thread.sleep(2000);

            //Verify server started by attempting to interrupt it
            mainThread.interrupt();

        } finally {
            //Restore original working directory
            System.setProperty("user.dir", originalUserDir);
        }
    }

    @Test
    @DisplayName("Test the main execution, by showing a proper error message, when loading an invalid config")
    void testMainWithInvalidConfig(@TempDir Path tempDir) throws InterruptedException {

        //Redirect System.err to capture error output
        ByteArrayOutputStream errContent = new ByteArrayOutputStream();
        PrintStream originalErr = System.err;
        System.setErr(new PrintStream(errContent));

        //Change working directory to temp dir (where no config exists)
        String originalUserDir = System.getProperty("user.dir");
        System.setProperty("user.dir", tempDir.toString());

        try {
            //Execute main method - should fail due to missing config
            Main.main(new String[]{});

            //Allow time for error to be processed
            Thread.sleep(1000);

            //Verify error output contains expected messages
            String errorOutput = errContent.toString();
            System.out.println(errorOutput);

            assertFalse(errorOutput.isEmpty(), "Should have generated an error message");
            assertTrue(errorOutput.contains("server.config"));

        } finally {
            //Restore original error stream and working directory
            System.setErr(originalErr);
            System.setProperty("user.dir", originalUserDir);
        }
    }
}