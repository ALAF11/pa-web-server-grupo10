import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * The threadPool class extends ThreadPoolExecutor to provide custom thread pool functionality.
 * <p>
 *     This implementation adds logging before and after task execution, and maintains
 *     a fixed pool size with an unbounded task queue.
 *
 * @see ThreadPoolExecutor
 */

public class ThreadPool extends ThreadPoolExecutor {

    /**
     * Constructs a new ThreadPool with fixed size.
     *
     * @param corePoolSize the number of threads to keep in the pool
     * @param maximumPoolSize the maximum number of threads in the pool
     */

    public ThreadPool(int corePoolSize, int maximumPoolSize) {
        super (corePoolSize, maximumPoolSize, 1, TimeUnit.MINUTES, new LinkedBlockingQueue<>());
    }

    /**
     * Logs task execution start.
     *
     * @param thread the thread that will run task
     * @param runnable the task that will be executed
     */

    @Override
    protected void beforeExecute(Thread thread, Runnable runnable) {
        super.beforeExecute(thread, runnable);
        System.out.println("Executing task: " + runnable);
    }

    /**
     * Logs task completion.
     *
     * @param runnable the task that was executed
     * @param throwable any exception that occurred, or null if none
     */

    protected void afterExecute(Runnable runnable, Throwable throwable) {
        super.afterExecute(runnable, throwable);
        System.out.println("Task completed: " + runnable);
    }

    /**
     * Logs thread pool termination
     */

    protected void terminated() {
        super.terminated();
        System.out.println("Thread pool terminated.");
    }
}
