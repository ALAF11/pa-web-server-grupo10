import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

class MainTest {

    @Test
    void testMainWithValidConfig(@TempDir Path tempDir) throws IOException {
        //

        Path configFile = tempDir.resolve("server.config");
        String configContent = "server.port=8080\n" +
                "server.root=./html\n" +
                "server.document.root=./html\n" +
                "server.log.file=server.log\n" +
                "server.maximum.requests=5\n" +
                "server.default.page=index\n" +
                "server.default.page.extension=html\n" +
                "server.page.404=404.html";

        Files.writeString(configFile, configContent);

        //
        String originalUserDir = System.getProperty("user.dir");
        System.setProperty("user.dir", tempDir.toString());

        try {
            //
            Thread mainThread = new Thread(() -> {
                try {
                    Main.main(new String[]{});
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            mainThread.start();

            //
            Thread.sleep(1000);

            //
            mainThread.interrupt();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            //
            System.setProperty("user.dir", originalUserDir);
        }
    }

    @Test
    void testMainWithInvalidConfig(@TempDir Path tempDir){
        //
        ByteArrayOutputStream errContent = new ByteArrayOutputStream();
        PrintStream originalErr = System.err;
        System.setErr(new PrintStream(errContent));

        String originalUserDir = System.getProperty("user.dir");
        System.setProperty("user.dir", tempDir.toString());

        try {
            Main.main(new String[]{});

            //
            String errorOutput = errContent.toString();
            assertTrue(errorOutput.contains("Error loading the configuration file"));
        } finally {
            System.setErr(originalErr);
            System.setProperty("user.dir", originalUserDir);
        }
    }
}