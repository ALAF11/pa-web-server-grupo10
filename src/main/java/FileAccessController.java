import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock; //com locks, trancas

public class FileAccessController {
    private static final ConcurrentHashMap<String, ReentrantLock> fileLocks = new ConcurrentHashMap<>();
    private final String serverRoot;

    public FileAccessController(String serverRoot) {
        this.serverRoot = serverRoot;
    }

    public byte[] readFile(String route) throws IOException, InterruptedException {
        String filePath = serverRoot + route;
        File file = new File(filePath);

        ReentrantLock fileLock = fileLocks.computeIfAbsent(filePath, k -> new ReentrantLock());
        fileLock.lock();

        try {
            if (file.exists() && !file.isDirectory()) {
                return Files.readAllBytes(Paths.get(filePath));
            } else {
                throw new IOException("File not found: " + filePath);
                //return Files.readAllBytes(Paths.get(serverRoot + "/404.html"));
            }
        } finally {
            fileLock.unlock();

            if (fileLock.getHoldCount() == 0 && !fileLock.hasQueuedThreads()) {
                fileLocks.remove(filePath, fileLock);
            }
        }
    }
}