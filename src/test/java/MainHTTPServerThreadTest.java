import org.junit.jupiter.api.Test;
import java.util.concurrent.*;

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
}