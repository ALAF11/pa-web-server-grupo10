import org.junit.jupiter.api.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import static org.junit.jupiter.api.Assertions.*;

class LogConsumerTest {
    private static final String TEST_LOG_FILE = "test-server.log";
    private BlockingQueue<LogEntry> logQueue;
    private LogConsumer logConsumer;
    private Thread consumerThread;

    @BeforeEach
    void setUp() {
        logQueue = new LinkedBlockingQueue<>();
        logConsumer = new LogConsumer(logQueue, TEST_LOG_FILE);
        consumerThread = new Thread(logConsumer);
        consumerThread.start();
    }

    @AfterEach
    void tearDown() throws IOException {
        //Interrupt the thread
        consumerThread.interrupt();
        try {
            consumerThread.join(100); //Wait up to 100ms for the thread to finish
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        Files.deleteIfExists(Path.of(TEST_LOG_FILE));
    }

    @Test
    @DisplayName("Should correctly write a single log entry to the log file")
    void testLogEntryIsWrittenToFile() throws InterruptedException, IOException {
        //Creates a test log entry
        LogEntry testEntry = new LogEntry(
                "2024-03-28 12:00:00.000",
                "GET",
                "/index.html",
                "127.0.0.1",
                200
        );

        //Add the entry to the queue
        logQueue.put(testEntry);

        //Gives consumers time to process
        Thread.sleep(100);

        // Checks that the file has been created and contains the entry
        Path logPath = Path.of(TEST_LOG_FILE);
        assertTrue(Files.exists(logPath), "The log file should exist");

        String logContent = Files.readString(logPath);
        assertTrue(logContent.contains(testEntry.toJSON()), "The log file should contain the JSON entry");
    }

    @Test
    @DisplayName("Should correctly write multiple log entries in sequence to the log file")
    void testMultipleLogEntriesAreWritten() throws InterruptedException, IOException {
        //Creates several log entries
        LogEntry entry1 = new LogEntry("2024-03-28 12:00:00.000", "GET", "/", "127.0.0.1", 200);
        LogEntry entry2 = new LogEntry("2024-03-28 12:00:01.000", "GET", "/index.html", "192.168.1.1", 200);
        LogEntry entry3 = new LogEntry("2024-03-28 12:00:02.000", "GET", "/nonexistent.html", "10.0.0.1", 404);

        //Add the entrys to the queue
        logQueue.put(entry1);
        logQueue.put(entry2);
        logQueue.put(entry3);

        //Gives consumers time to process
        Thread.sleep(100);

        //Checks the contents of the file
        String logContent = Files.readString(Path.of(TEST_LOG_FILE));

        assertTrue(logContent.contains(entry1.toJSON()), "It should contain the first entry");
        assertTrue(logContent.contains(entry2.toJSON()), "It should contain the second entry");
        assertTrue(logContent.contains(entry3.toJSON()), "It should contain the third entry");
    }
}