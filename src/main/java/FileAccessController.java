import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/**
 * The FileAccessController class manager thread-safe file access operations for the HTTP server.
 * <p>
 *     This class implements a file locking mechanism to prevent concurrent modifications and
 *     pah transversal attacks. It maintains a map of file locks to ensure exclusive access
 *     to files during read operations.
 *
 * @see ReentrantLock
 * @see ConcurrentHashMap
 */

public class FileAccessController {

    private static final ConcurrentHashMap<String, ReentrantLock> fileLocks = new ConcurrentHashMap<>();
    private final ServerConfig config;
    private static final long LOCK_TIMEOUT_SECONDS = 5;

    /**
     *  Constructs a new FileAccessController with the specified server configuration.
     *
     * @param config the ServerConfig object containing server configuration parameters
     */

    public FileAccessController(ServerConfig config) {
        this.config = config;
    }

    /**
     * Reads a file from the server's root directory with a thread-safe access control.
     * <p>
     *     This method performs several security checks and operations:
     *     <ol>
     *         <li> Validates the requested path against the server root to prevent path traversal </li>
     *         <li> Acquires an exclusive lock for the life with timeout </li>
     *         <li> Verifies file existence and type </li>
     *         <li> Reads file contents </li>
     *         <li> Releases the lock and cleans up </li>
     *     </ol>
     * @param route the relative path of the file to read from server root
     * @return byte array containing the file contents
     * @throws IOException if:
     * <ul>
     *     <li> Path traversak attempt is detected </li>
     *     <li> File is not found </li>
     *     <li> Path is a directory </li>
     *     <li> Timeout occurs while waiting for file lock </li>
     *     <li> General I/O error occurs during reading </li>
     * </ul>
     * @throws InterruptedException if the thread is interrupted while waiting for the lock
     */

    public byte[] readFile(String route) throws IOException, InterruptedException {
        Path rootPath = Paths.get(config.getConfig("server.root")).toAbsolutePath().normalize();

        if (route == null || route.isEmpty()) {
            route = "/";
        }

        Path requestedPath = route.equals("/") ? Paths.get("") : Paths.get(route).normalize();
        Path filePath = rootPath.resolve(requestedPath).toAbsolutePath().normalize();

        if (!filePath.startsWith(rootPath)) {
            String errorMsg = String.format(
                    "Access denied: Path traversal attempt. Requested '%s' is outside server root '%s'",
                    filePath, rootPath);
            System.err.println("Security violation: " + errorMsg);
            throw new IOException(errorMsg);
        }

        ReentrantLock fileLock = fileLocks.computeIfAbsent(
                filePath.toString(),
                k -> new ReentrantLock(true)
        );

        System.out.printf("[%s] Trying to acquire lock for: %s (Queued threads: %d)%n",
                Thread.currentThread().getName(),
                filePath,
                fileLock.getQueueLength());

        if (!fileLock.tryLock(LOCK_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
            System.err.printf("[%s] Timeout when waiting for lock: %s%n",
                    Thread.currentThread().getName(),
                    filePath);
            throw new IOException("Timeout waiting for file access: " + filePath);
        }

        try {
            System.out.printf("[%s] Lock acquired for: %s (Time on the system: %dns)%n",
                    Thread.currentThread().getName(),
                    filePath,
                    System.nanoTime());

            if (!Files.exists(filePath)) {
                throw new IOException("File not found: " + filePath);
            }
            if (Files.isDirectory(filePath)) {
                throw new IOException("Path is a directory: " + filePath);
            }

            Thread.sleep(100);

            return Files.readAllBytes(filePath);
        } finally {
            System.out.printf("[%s] Releasing lock: %s (Tempo no sistema: %dns)%n",
                    Thread.currentThread().getName(),
                    filePath,
                    System.nanoTime());

            if (!fileLock.hasQueuedThreads()) {
                fileLocks.remove(filePath.toString(), fileLock);
            }
            fileLock.unlock();
        }
    }
}