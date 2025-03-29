import org.junit.jupiter.api.Test;

import java.net.ServerSocket;
import java.util.concurrent.*;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MainHTTPServerThreadTest {

    @Test
    public void testConcurrencyFlow() throws InterruptedException {
        int MAX_TOTAL_REQUESTS = 5;
        int MAX_POOL_THREADS = 2;

        System.out.println("[CONFIG] semaphore = " + MAX_TOTAL_REQUESTS + " permits | ThreadPool = " + MAX_POOL_THREADS + " threads");

        Semaphore sem = new Semaphore(MAX_TOTAL_REQUESTS, true);
        ThreadPoolExecutor threadPool = (ThreadPoolExecutor) Executors.newFixedThreadPool(MAX_POOL_THREADS);

        CountDownLatch latch = new CountDownLatch(30);

        for (int i = 1; i <= 30; i++) {
            final int taskId = i;
            new Thread(() -> {
                try {
                    System.out.printf("[ENTRY] Task %d requesting acess | Semaphore: %d/%d | Active threads: %d/%d%n",
                            taskId,
                            sem.availablePermits(), MAX_TOTAL_REQUESTS,
                            threadPool.getActiveCount(), MAX_POOL_THREADS);

                    sem.acquire();

                    System.out.printf("[SEMAPHORE] Task %d acquired permit | Available: %d%n",
                            taskId, sem.availablePermits());

                    threadPool.execute(() -> {
                        try {
                            System.out.printf("[THREADPOOL] Task %d started | Active Threads: %d%n",
                                    taskId, threadPool.getActiveCount());

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

            Thread.sleep(50);
        }

        latch.await();
        threadPool.shutdown();
    }

//    @Test
//    public void testServerInitialization() throws IOException, InterruptedException {
//        ServerConfig config = new ServerConfig();
//        // Configurações OBRIGATÓRIAS para o MainHTTPServerThread
//        config.setConfig("server.port", "8080");
//        config.setConfig("server.max.total.requests", "5"); // Adicione esta linha
//        config.setConfig("server.root", "./html");
//        config.setConfig("server.document.root", "./html");
//        config.setConfig("server.default.page", "index");
//        config.setConfig("server.default.page.extension", "html");
//
//        ThreadPool threadPool = new ThreadPool(2, 2);
//        BlockingQueue<LogEntry> logQueue = new LinkedBlockingQueue<>();
//        FileAccessController fac = new FileAccessController(config);
//
//        MainHTTPServerThread server = new MainHTTPServerThread(
//                config, threadPool, fac, logQueue
//        );
//
//        server.start();
//        Thread.sleep(500); // Tempo para inicialização
//        assertTrue(server.isAlive());
//        server.interrupt();
//
//    }

}