import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class ThreadPoolTest {

    private ThreadPool pool;

    @AfterEach
    void shutdownPool() {
        if (pool != null) {
            pool.shutdown();
        }
    }

    @Test
    @DisplayName("Runs concurrent tasks up to pool size")
    void concurrentTasks() throws InterruptedException {
        int poolSize = 3;
        pool = new ThreadPool(poolSize, poolSize);
        AtomicInteger running = new AtomicInteger(0);
        CountDownLatch allDone = new CountDownLatch(poolSize);

        for (int i = 0; i < poolSize; i++) {
            pool.execute(() -> {
               running.incrementAndGet();
               try { Thread.sleep(100); } catch (InterruptedException e) {}
               running.decrementAndGet();
               allDone.countDown();
            });
        }

        assertTrue(allDone.await(1, TimeUnit.SECONDS));
        assertEquals(0, running.get());
    }

    @Test
    @DisplayName("Queues extra tasks when pool is full")
    void queuesExtraTasks() throws InterruptedException {
        pool = new ThreadPool(1, 1);
        CountDownLatch firstDone = new CountDownLatch(1);
        CountDownLatch secondDone = new CountDownLatch(1);
        AtomicInteger secondStarted = new AtomicInteger(0);

        pool.execute(() -> {
            try { Thread.sleep(100); } catch (InterruptedException e) {}
            firstDone.countDown();
        });

        pool.execute(() -> {
            secondStarted.set(1);
            secondDone.countDown();
        });

        assertEquals(0, secondStarted.get(), "Second task should still be queued");
        assertTrue(firstDone.await(1, TimeUnit.SECONDS), "First task should complete");
        assertTrue(secondDone.await(1, TimeUnit.SECONDS), "Second task should execute after first");
        assertEquals(1, secondStarted.get(), "Second task should have executed");
    }
}