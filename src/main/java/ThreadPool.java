import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ThreadPool extends ThreadPoolExecutor {

    public ThreadPool(int corePoolSize, int maximumPoolSize) {
        super (corePoolSize, maximumPoolSize, 1, TimeUnit.MINUTES, new LinkedBlockingDeque<>());
    }

    @Override
    protected void beforeExecute(Thread thread, Runnable runnable) {
        super.beforeExecute(thread, runnable);
        System.out.println("Executing task: " + runnable);
    }

    protected void afterExecute(Runnable runnable, Throwable throwable) {
        super.afterExecute(runnable, throwable);
        System.out.println("Task completed: " + runnable);
    }

    protected void terminated() {
        super.terminated();
        System.out.println("Thread pool terminated.");
    }
}
