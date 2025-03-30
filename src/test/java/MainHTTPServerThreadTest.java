import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.*;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MainHTTPServerThreadTest {

    private static final int TEST_PORT = findAvailablePort();
    private MainHTTPServerThread serverThread;

    @AfterEach
    void tearDown() {
        //Check if server thread exists and is running
        if (serverThread != null && serverThread.isAlive()) {
            serverThread.interrupt();
            try {
                serverThread.join(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    @Test
    @DisplayName("Should correctly manage concurrent requests with semaphore and thread pool")
    public void testConcurrencyFlow() throws InterruptedException {
        //Configuration for testing concurrency limits
        int MAX_TOTAL_REQUESTS = 5;
        int MAX_POOL_THREADS = 2;

        System.out.println("[CONFIG] semaphore = " + MAX_TOTAL_REQUESTS + " permits | ThreadPool = " + MAX_POOL_THREADS + " threads");

        //Create synchronization objects
        Semaphore sem = new Semaphore(MAX_TOTAL_REQUESTS, true);
        ThreadPoolExecutor threadPool = (ThreadPoolExecutor) Executors.newFixedThreadPool(MAX_POOL_THREADS);

        CountDownLatch latch = new CountDownLatch(10);


        for (int i = 1; i <= 10; i++) {
            final int taskId = i;
            new Thread(() -> {
                try {
                    //Log request attempt
                    System.out.printf("[ENTRY] Task %d requesting acess | Semaphore: %d/%d | Active threads: %d/%d%n",
                            taskId,
                            sem.availablePermits(), MAX_TOTAL_REQUESTS,
                            threadPool.getActiveCount(), MAX_POOL_THREADS);

                    //Acquire semaphore permit (blocks if none available)
                    sem.acquire();

                    System.out.printf("[SEMAPHORE] Task %d acquired permit | Available: %d%n",
                            taskId, sem.availablePermits());

                    //Submit task to thread pool
                    threadPool.execute(() -> {
                        try {
                            System.out.printf("[THREADPOOL] Task %d started | Active Threads: %d%n",
                                    taskId, threadPool.getActiveCount());

                            //Simulate request processing time
                            Thread.sleep(300);

                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        } finally {

                            sem.release();
                            latch.countDown();
                            System.out.printf("[EXIT] Task %d completed | Semaphore released: %d/%d%n",
                                    taskId, sem.availablePermits(), MAX_TOTAL_REQUESTS);
                        }
                    });

                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }).start();

            //Space out request arrivals
            Thread.sleep(50);
        }

        latch.await();
        threadPool.shutdown();
    }

    @Test
    @DisplayName("Should initialize server with valid configuration")
    void testServerInitialization() throws IOException, InterruptedException {
        // Setup test configuration with dynamic port
        ServerConfig config = new ServerConfig();
        config.setConfig("server.port", String.valueOf(TEST_PORT));
        config.setConfig("server.max.total.requests", "5");
        config.setConfig("server.root", "./html");
        config.setConfig("server.document.root", "./html");
        config.setConfig("server.default.page", "index");
        config.setConfig("server.default.page.extension", "html");

        // Create dependencies
        ThreadPool threadPool = new ThreadPool(2, 2);
        BlockingQueue<LogEntry> logQueue = new LinkedBlockingQueue<>();
        FileAccessController fileAccessController = new FileAccessController(config);
        Semaphore requestLimiter = new Semaphore(20);

        // Create and start server thread
        serverThread = new MainHTTPServerThread(config, threadPool, fileAccessController, logQueue, requestLimiter);
        serverThread.start();

        // Wait for server to start
        Thread.sleep(500);

        // Verify server is running by attempting to connect
        try (Socket clientSocket = new Socket("localhost", TEST_PORT)) {
            assertTrue(clientSocket.isConnected(), "Server should accept connections");
        }

        assertTrue(serverThread.isAlive(), "Server thread should be running");
    }

    //Helper method to find an available port
    private static int findAvailablePort() {
        try (ServerSocket socket = new ServerSocket(0)) {
            return socket.getLocalPort();
        } catch (IOException e) {
            throw new RuntimeException("Could not find available port", e);
        }
    }

}