import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;

public class FileAcessController {
    private static final ConcurrentHashMap<String, Semaphore> fileLocks = new ConcurrentHashMap<>();
    private final String serverRoot;

    public FileAcessController(String serverRoot) {
        this.serverRoot = serverRoot;
    }

    public byte [] readFile(String route) throws IOException, InterruptedException {
        String filePath = serverRoot + route;
        File file = new File(filePath);

        Semaphore fileLock = fileLocks.computeIfAbsent(filePath, k -> new Semaphore(1, true));

        fileLock.acquire();

        try{
            if (file.exists() && !file.isDirectory()) {
                return Files.readAllBytes(Paths.get(filePath));
            }   else {
                return Files.readAllBytes(Paths.get(serverRoot + "/404.html"));
            }

            } finally {
            fileLock.release();
            fileLocks.computeIfPresent(filePath, (key,sem) -> sem.availablePermits() == 1 ? null : sem);
        }
    }
}
