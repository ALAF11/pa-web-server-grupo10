import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Semaphore;

/**
 * The MainHTTPServerThread class implements the core HTTP server that listens on a specified port.
 * <p>
 * This thread handles incoming HTTP requests, manages concurrent connections using
 * a configured thread pool and enforces requests limits through a semaphore. It
 * coordinates with other components to serve files securely
 * and maintain request logs in JSON format.
 */

public class MainHTTPServerThread extends Thread {

    private final ServerConfig config;
    private final ThreadPool threadPool;
    private final FileAccessController fileAccessController;
    private BlockingQueue<LogEntry> logQueue;
    private final Semaphore requestLimiter;

    /**
     * Constructs a new MainHTTPServerThreads with the specified configuration,
     * thread pool, file access controller, log queue, and request limiter.
     *
     * @param config The server configuration containing settings such as port and root directories.
     * @param threadPool The thread pool used to handle client requests.
     * @param fileAccessController The controller managing file access permissions.
     * @param logQueue The queue for logging server activities.
     * @param requestLimiter The semaphore to limit the number of concurrent requests limit.
     */

    public MainHTTPServerThread(ServerConfig config, ThreadPool threadPool, FileAccessController fileAccessController, BlockingQueue<LogEntry> logQueue, Semaphore requestLimiter) {
        this.config = config;
        this.threadPool = threadPool;
        this.fileAccessController = fileAccessController;
        this.logQueue = logQueue;
        this.requestLimiter = requestLimiter;
    }

    /**
     * Starts the HTTP server and listens for incoming client requests.
     * Processes HTTP GET requests and serves files from the defined server root directory.
     * <p>
     * The server runs indefinitely until interrupted, performing the following operarions:
     * 1. Listen on the configured port for incoming connections
     * 2. Acquire a permit from the semaphore for each new request
     * 3. Delegates request processing to the thread pool
     * 4. Releases the semaphore permit when processing completes
     * </p>
     * <p>
     * On interruption or IO error, the server shuts down by:
     * 1. Interrupting the current thread
     * 2. Shutting down the thread pool
     * 3. Closing all resources
     * </p>
     *
     */

    @Override
    public void run() {
        try {
            ServerSocket server = new ServerSocket(config.getIntConfig("server.port"));
            System.out.println("Server started on port: " + config.getIntConfig("server.port"));
            System.out.println("Server Root: " + config.getConfig("server.root"));
            System.out.println("Document Root: " + config.getConfig("server.document.root"));

            while (true) {
                Socket client = server.accept();
                requestLimiter.acquire();

                threadPool.execute(() -> {
                    try {
                        new ClientHandler(client, config, fileAccessController, logQueue).run();
                    } finally {
                        requestLimiter.release();
                    }
                });
            }
        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            threadPool.shutdown();
        }
    }
}