import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;


import static org.junit.jupiter.api.Assertions.*;

public class FileAccessControllerTest {

    private static ServerConfig config;
    private static final String testFile = "test.html";

    @BeforeAll
    public static void setup() throws Exception {
        config = new ServerConfig();
        String rootPath = Paths.get(System.getProperty("user.dir"), "html").toString();
        config.setConfig("server.root", rootPath);

        // Ensure the html directory exists
        Path htmlDir = Paths.get(rootPath);
        if (!Files.exists(htmlDir)) {
            Files.createDirectories(htmlDir);
        }

        Path testFilePath = Paths.get(rootPath, testFile);
        Files.write(testFilePath, "Test content".getBytes());
    }

    @AfterAll
    public static void cleanup() throws IOException {
        Path testFilePath = Paths.get(config.getConfig("server.root"), testFile);
        Files.deleteIfExists(testFilePath);
    }

    @Test
    @DisplayName("Must allow controlled concurrent access to the same file")
    public void testConcurrentReads() throws InterruptedException, IOException {
        FileAccessController controller = new FileAccessController(config);
        Path testFilePath = Paths.get(config.getConfig("server.root"), testFile);
        byte[] expectedContent = Files.readAllBytes(testFilePath);

        int threadCount = 5;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            executor.submit(createTestTask(controller, expectedContent, startLatch, endLatch, i));
        }

        startLatch.countDown(); // unlocks all threads
        assertTrue(endLatch.await(10, TimeUnit.SECONDS), "Time exceeded");
        executor.shutdownNow();
    }

    private Runnable createTestTask(FileAccessController controller, byte[] expectedContent, CountDownLatch startLatch, CountDownLatch endLatch, int threadNumber) {
        return () -> {
            try {
                startLatch.await();
                Thread.sleep(threadNumber * 10);

                long startTime = System.nanoTime();
                byte[] content = controller.readFile(testFile);
                long duration = System.nanoTime() - startTime;

                assertArrayEquals(expectedContent, content, "Read content differs from what is expected in the thread" + threadNumber);

                System.out.printf("Thread %d completed at %d ms%n", threadNumber + 1, TimeUnit.NANOSECONDS.toMillis(duration));

            } catch (InterruptedException e) {
                fail("Thread failed " + threadNumber, e);

            } catch (IOException e) {
                throw new RuntimeException(e);

            } finally {
                endLatch.countDown();
            }
        };
    }
}