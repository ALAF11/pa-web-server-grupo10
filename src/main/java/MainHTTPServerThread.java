import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Semaphore;

/**
 * The MainHTTPServerThread class represents an HTTP server that listens on a specified port.
 * It serves files from a predefined server root directory and manages client requests using a thread pool.
 * <p>
 *     The server supports concurrent requests, limits the number of active requests using a semaphore,
 *     and logs activities throught a shared log queue.
 */

public class MainHTTPServerThread extends Thread {

    private final ServerConfig config;
    private final ThreadPool threadPool;
    private final FileAccessController fileAccessController;
    private BlockingQueue<LogEntry> logQueue;
    private final Semaphore requestLimiter;

    /**
     * Constructs a new MainHTTPServerThreads with the specified configuration, thread pool, file acess controller, log queue, and request limiter.
     *
     * @param config The server configuration containing settings such as port and root directories.
     * @param threadPool The thread pool used to handle client requests.
     * @param fileAccessController The controller managing file access permissions.
     * @param logQueue The queue for logging server activities.
     * @param requestLimiter The semaphore to limit the number of concurrent
     */

    public MainHTTPServerThread(ServerConfig config, ThreadPool threadPool, FileAccessController fileAccessController, BlockingQueue<LogEntry> logQueue, Semaphore requestLimiter) {
        this.config = config;
        this.threadPool = threadPool;
        this.fileAccessController = fileAccessController;
        this.logQueue = logQueue;
        this.requestLimiter = requestLimiter;
    }

    /**
     * Reads a binary file and returns its contents as a byte array.
     *
     * @param path The file path to read.
     * @return A byte array containing the file's contents, or an empty array if an error occurs.
     */

    private byte[] readBinaryFile(String path) {
        try {
            return Files.readAllBytes(Paths.get(path));
        } catch (IOException e) {
            System.err.println("Error reading file: " + path);
            e.printStackTrace();
            return new byte[0];
        }
    }

    /**
     * Reads a text file and returns its contents as a string.
     *
     * @param path The file path to read.
     * @return A string containing the file's contents, or an empty string if an error occurs.
     */

    private String readFile(String path) {
        StringBuilder content = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(path))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
        } catch (IOException e) {
            System.err.println("Error reading file: " + path);
            e.printStackTrace();
        }
        return content.toString();
    }

    /**
     * Starts the HTTP server and listens for incoming client requests.
     * Processes HTTP GET requests and serves files from the defined server root directory.
     * <p>
     * The server runs indefinitely until interrupted. Each client request is handled by a thread
     * From the thread pool, and the number of concurrent requests is limited by the semaphore.
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