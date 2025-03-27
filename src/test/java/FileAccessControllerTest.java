import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.AfterAll;
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
    private static String testFile = "teste.html";

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
    public void testConcurrentReads() throws InterruptedException, IOException {
        FileAccessController controller = new FileAccessController(config);
        Path testFilePath = Paths.get(config.getConfig("server.root"), testFile);
        byte[] expectedContent = Files.readAllBytes(testFilePath);

        int threadCount = 5;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(threadCount);

        System.out.println("Iniciando teste com " + threadCount + " threads...");

        for (int i = 0; i < threadCount; i++) {
            final int threadNumber = i + 1;
            executor.submit(() -> {
                try {
                    System.out.printf("Thread %d aguardando sinal para iniciar (%s)%n",
                            threadNumber,
                            Thread.currentThread().getName());

                    startLatch.await();

                    System.out.printf("Thread %d iniciando leitura (%s)%n",
                            threadNumber,
                            Thread.currentThread().getName());

                    long startTime = System.nanoTime();
                    byte[] content = controller.readFile(testFile);
                    long endTime = System.nanoTime();

                    assertArrayEquals(expectedContent, content);

                    System.out.printf("Thread %d concluída em %d ms%n",
                            threadNumber,
                            TimeUnit.NANOSECONDS.toMillis(endTime - startTime));
                } catch (Exception e) {
                    System.err.printf("Thread %d falhou: %s%n",
                            threadNumber,
                            e.getMessage());
                    fail("Thread " + threadNumber + " failed: " + e.getMessage());
                } finally {
                    endLatch.countDown();
                }
            });
        }

        System.out.println("Liberando todas as threads simultaneamente...");
        startLatch.countDown();

        assertTrue(endLatch.await(10, TimeUnit.SECONDS), "Test timed out");
        System.out.println("Todas as threads concluíram o processamento.");

        executor.shutdown();
    }}