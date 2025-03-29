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

        String originalUserDir = System.getProperty("user.dir");
        System.setProperty("user.dir", tempDir.toString());

        try {
            Thread mainThread = new Thread(() -> {
                try {
                    Main.main(new String[]{});
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            mainThread.start();

            Thread.sleep(2000);

            mainThread.interrupt();

        } finally {
            System.setProperty("user.dir", originalUserDir);
        }
    }

    @Test
    @DisplayName("Test the main execution, by showing a proper error message, when loading an invalid config")
    void testMainWithInvalidConfig(@TempDir Path tempDir) throws InterruptedException {

        ByteArrayOutputStream errContent = new ByteArrayOutputStream();
        PrintStream originalErr = System.err;
        System.setErr(new PrintStream(errContent));

        String originalUserDir = System.getProperty("user.dir");
        System.setProperty("user.dir", tempDir.toString());

        try {
            Main.main(new String[]{});

            Thread.sleep(1000);

            String errorOutput = errContent.toString();
            System.out.println(errorOutput);

            assertFalse(errorOutput.isEmpty(), "Should have generated an error message");
            assertTrue(errorOutput.contains("server.config"));

        } finally {
            System.setErr(originalErr);
            System.setProperty("user.dir", originalUserDir);
        }
    }
}