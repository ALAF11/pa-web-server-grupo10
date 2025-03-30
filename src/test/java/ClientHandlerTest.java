//import org.junit.jupiter.api.*;
//import static org.junit.jupiter.api.Assertions.*;
//import java.io.*;
//import java.net.ServerSocket;
//import java.net.Socket;
//import java.nio.file.Files;
//import java.nio.file.Path;
//import java.util.concurrent.BlockingQueue;
//import java.util.concurrent.LinkedBlockingQueue;
//
//class ClientHandlerTest {
//
//    private static Path tempDir;
//    private ServerConfig config;
//    private FileAccessController fileAccessController;
//    private BlockingQueue<LogEntry> logQueue;
//
//    @BeforeAll
//    static void setUpBeforeAll() throws IOException {
//        System.out.println("[SETUP] Creating temporary test directory...");
//        // Create temporary directory for server files
//        tempDir = Files.createTempDirectory("test_server_root");
//        System.out.println("[SETUP] Temporary directory created at: " + tempDir);
//
//        // Create test files
//        System.out.println("[SETUP] Creating test files (index.html, 404.html)...");
//        Files.write(tempDir.resolve("index.html"),
//                "<html><body><h1>Test Page</h1></body></html>".getBytes());
//        Files.write(tempDir.resolve("404.html"),
//                "<html><body><h1>404 Not Found</h1></body></html>".getBytes());
//    }
//
//    @AfterAll
//    static void tearDownAfterAll() throws IOException {
//        System.out.println("[TEARDOWN] Cleaning up temporary directory...");
//        // Clean up temporary directory
//        Files.walk(tempDir)
//                .sorted(java.util.Comparator.reverseOrder())
//                .forEach(path -> {
//                    try { Files.delete(path); }
//                    catch (IOException e) {
//                        System.err.println("[WARNING] Failed to delete: " + path);
//                    }
//                });
//        System.out.println("[TEARDOWN] Cleanup complete");
//    }
//
//    @BeforeEach
//    void setUp() {
//        System.out.println("\n[TEST SETUP] Configuring test environment...");
//        // Configure server settings for testing
//        config = new ServerConfig();
//        config.setConfig("server.root", tempDir.toString());
//        config.setConfig("server.document.root", tempDir.toString());
//        config.setConfig("server.default.page", "index");
//        config.setConfig("server.default.page.extension", "html");
//        config.setConfig("server.page.404", "404.html");
//
//        fileAccessController = new FileAccessController(config);
//        logQueue = new LinkedBlockingQueue<>();
//        System.out.println("[TEST SETUP] Configuration complete");
//    }
//
//    @Test
//    @DisplayName("Test valid GET request")
//    void testHandleValidGetRequest() throws Exception {
//        System.out.println("\n[TEST] Starting valid GET request test...");
//        try (ServerSocket testServer = new ServerSocket(0)) {
//            int port = testServer.getLocalPort();
//            System.out.println("[TEST] Test server started on port: " + port);
//
//            // Start handler in separate thread
//            Thread handlerThread = new Thread(() -> {
//                try (Socket clientSocket = testServer.accept()) {
//                    System.out.println("[HANDLER] Connection accepted, starting handler...");
//                    new ClientHandler(clientSocket, config, fileAccessController, logQueue).run();
//                    System.out.println("[HANDLER] Handler completed");
//                } catch (Exception e) {
//                    System.err.println("[HANDLER ERROR] " + e.getMessage());
//                    fail("Handler error: " + e.getMessage());
//                }
//            });
//            handlerThread.start();
//
//            // Simulate client making valid GET request
//            try (Socket client = new Socket("localhost", port)) {
//                 client.setSoTimeout(3000);
//                 PrintWriter out = new PrintWriter(client.getOutputStream(), true);
//                 BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
//
//                System.out.println("[CLIENT] Sending GET /index.html request...");
//                out.println("GET /index.html HTTP/1.1");
//                out.println("Host: localhost");
//                out.println();
//
//                // Read and verify response
//                String fullResponse = readFullResponse(in);
//                System.out.println("[CLIENT] Received response:\n" + fullResponse);
//
//                assertTrue(fullResponse.contains("HTTP/1.1 200 OK"), "Should return 200 status");
//
//                String responseBody = extractResponseBody(fullResponse);
//                assertEquals("<html><body><h1>Test Page</h1></body></html>", responseBody, "Should return exact index.html content");
//            }
//            handlerThread.join(3000);
//            assertFalse(handlerThread.isAlive(), "Handler thread should have completed");
//        }
//    }
//
//    @Test
//    @DisplayName("Test invalid GET request (file not found)")
//    void testHandleInvalidGetRequest() throws Exception {
//        System.out.println("\n[TEST] Starting invalid GET request test...");
//        try (ServerSocket testServer = new ServerSocket(0)) {
//            int port = testServer.getLocalPort();
//            System.out.println("[TEST] Test server started on port: " + port);
//
//            // Start handler in separate thread
//            Thread handlerThread = new Thread(() -> {
//                try (Socket clientSocket = testServer.accept()) {
//                    clientSocket.setSoTimeout(3000);
//                    System.out.println("[HANDLER] Connection accepted, starting handler...");
//                    new ClientHandler(clientSocket, config, fileAccessController, logQueue).run();
//                    System.out.println("[HANDLER] Handler completed");
//                } catch (Exception e) {
//                    System.err.println("[HANDLER ERROR] " + e.getMessage());
//                    fail("Handler error: " + e.getMessage());
//                }
//            });
//            handlerThread.start();
//
//            // Simulate client making invalid GET request
//            try (Socket client = new Socket("localhost", port);
//                 PrintWriter out = new PrintWriter(client.getOutputStream(), true);
//                 BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()))) {
//
//                System.out.println("[CLIENT] Sending GET /nonexistent.html request...");
//                out.println("GET /nonexistent.html HTTP/1.1");
//                out.println("Host: localhost");
//                out.println();
//
//                // Read and verify response
//                String fullResponse = readFullResponse(in);
//                System.out.println("[CLIENT] Received response:\n" + fullResponse);
//
//                assertTrue(fullResponse.contains("HTTP/1.1 404 Not Found"), "Should return 404 status");
//
//                String responseBody = extractResponseBody(fullResponse);
//                assertEquals("<html><body><h1>404 Not Found</h1></body></html>", responseBody, "Should return exact 404.html content");
//            }
//            handlerThread.join(3000);
//            assertFalse(handlerThread.isAlive(), "Handler thread should have completed");
//        }
//    }
//
//    private String readFullResponse(BufferedReader in) throws IOException {
//        StringBuilder response = new StringBuilder();
//        String line;
//
//        while ((line = in.readLine()) != null) {
//            response.append(line).append("\n");
//            if (line.isEmpty()) {
//                // Read content if available
//                while (in.ready()) {
//                    response.append((char) in.read());
//                }
//                break;
//            }
//        }
//        return response.toString().trim();
//    }
//
//    private String extractResponseBody(String fullResponse) {
//        String[] parts = fullResponse.split("\n\n", 2);
//        return parts.length > 1 ? parts[1].trim() : "";
//    }
//}
