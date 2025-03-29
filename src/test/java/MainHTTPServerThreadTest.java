import org.junit.jupiter.api.Test;
import java.util.concurrent.*;

public class MainHTTPServerThreadTest {

    @Test
    public void testVisualizacaoFluxo() throws InterruptedException {
        int MAX_TOTAL = 5;
        int MAX_POOL = 2;

        System.out.println("[CONFIG] Semáforo = " + MAX_TOTAL + " permits | ThreadPool = " + MAX_POOL + " threads");

        Semaphore semaforo = new Semaphore(MAX_TOTAL, true);
        ThreadPoolExecutor threadPool = (ThreadPoolExecutor) Executors.newFixedThreadPool(MAX_POOL);

        CountDownLatch latch = new CountDownLatch(30);

        for (int i = 1; i <= 30; i++) {
            final int taskId = i;
            new Thread(() -> {
                try {
                    System.out.printf("[ENTRADA] Task %d quer entrar | Semáforo: %d/%d | ThreadPool: %d/%d%n",
                            taskId,
                            semaforo.availablePermits(), MAX_TOTAL,
                            threadPool.getActiveCount(), MAX_POOL);

                    semaforo.acquire();

                    System.out.printf("[SEMAFORO] Task %d adquiriu permit | Livres: %d%n",
                            taskId, semaforo.availablePermits());

                    threadPool.execute(() -> {
                        try {
                            System.out.printf("[THREADPOOL] Task %d começou | Threads ativas: %d%n",
                                    taskId, threadPool.getActiveCount());

                            Thread.sleep(300);

                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        } finally {
                            semaforo.release();
                            latch.countDown();
                            System.out.printf("[FIM] Task %d completou | Semáforo liberado: %d/%d%n",
                                    taskId, semaforo.availablePermits(), MAX_TOTAL);
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