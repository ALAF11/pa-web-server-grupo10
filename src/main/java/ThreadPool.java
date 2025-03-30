import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * The threadPool class extends ThreadPoolExecutor to provide custom thread pool functionality.
 * <p>
 * This thread pool provides detailed logging of task lifecycle events including:
 * task start, completion and pool termination. It maintains a fixed-size pool
 * with an unbounded task queue.
 *
 * @see ThreadPoolExecutor
 */

public class ThreadPool extends ThreadPoolExecutor {

    /**
     * Constructs a new ThreadPool with fixed size configuration.
     * <p>
     * Creates a thread pool where both core and maximum pool sizes are equal. Uses
     * an unbounded queue and 1-minute keep-alive time for thread termination policy.
     *
     * @param corePoolSize the number of threads to keep in the pool
     * @param maximumPoolSize the maximum number of threads in the pool
     */

    public ThreadPool(int corePoolSize, int maximumPoolSize) {
        super (corePoolSize, maximumPoolSize, 1, TimeUnit.MINUTES, new LinkedBlockingQueue<>());
    }

    /**
     * Logs task execution start.
     * <p>
     * Called immediately before executing the given task in the specified thread.
     *
     * @param thread the thread that will run the task
     * @param runnable the task that will be executed
     */

    @Override
    protected void beforeExecute(Thread thread, Runnable runnable) {
        super.beforeExecute(thread, runnable);
        System.out.println("Executing task: " + runnable);
    }

    /**
     * Logs task completion.
     * <p>
     * Called after the given task has completed execution.
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
     * <p>
     * Called when the executor has terminated all threads and is being shut down.
     */

    protected void terminated() {
        super.terminated();
        System.out.println("Thread pool terminated.");
    }
}
