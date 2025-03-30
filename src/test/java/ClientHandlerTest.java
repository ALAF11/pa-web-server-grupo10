import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class ClientHandlerTest {

    public static void main(String[] args) throws Exception {
        System.out.println("=== Iniciando teste do ClientHandler ===");

        // Create a temporary directory for server files
        Path tempDir = Files.createTempDirectory("test_server_root");
        System.out.println("Diretório temporário criado: " + tempDir);

        // Create a test file (index.html)
        Path indexFile = tempDir.resolve("index.html");
        Files.write(indexFile, "<html><body><h1>Test Page</h1></body></html>".getBytes());
        System.out.println("Arquivo index.html criado");

        // Create 404.html file
        Path notFoundFile = tempDir.resolve("404.html");
        Files.write(notFoundFile, "<html><body><h1>404 Not Found</h1></body></html>".getBytes());
        System.out.println("Arquivo 404.html criado");

        // Configure ServerConfig for testing
        ServerConfig config = new ServerConfig();
        config.setConfig("server.root", tempDir.toString());
        config.setConfig("server.document.root", tempDir.toString());
        config.setConfig("server.default.page", "index");
        config.setConfig("server.default.page.extension", "html");
        config.setConfig("server.page.404", "404.html");
        System.out.println("Configurações do servidor definidas");

        // Create necessary dependencies
        FileAccessController fileAccessController = new FileAccessController(config);
        BlockingQueue<LogEntry> logQueue = new LinkedBlockingQueue<>();

        // Test valid request
        System.out.println("\n=== Testando requisição válida ===");
        testValidRequest(config, fileAccessController, logQueue);

        // Test invalid request
        System.out.println("\n=== Testando requisição inválida ===");
        testInvalidRequest(config, fileAccessController, logQueue);

        // Verify logs
        System.out.println("\n=== Verificando logs ===");
        while (!logQueue.isEmpty()) {
            LogEntry log = logQueue.take();
            System.out.println("Log entry: " + log.toJSON());
        }

        System.out.println("\n=== Teste concluído com sucesso ===");
    }

    private static void testValidRequest(ServerConfig config, FileAccessController fac, BlockingQueue<LogEntry> logQueue) throws Exception {

        try (ServerSocket testServer = new ServerSocket(0)) {
            System.out.println("Servidor de teste iniciado na porta: " + testServer.getLocalPort());

            // Start thread to simulate the handler
            new Thread(() -> {
                try (Socket clientSocket = testServer.accept()) {
                    System.out.println("Conexão de teste aceita");
                    ClientHandler handler = new ClientHandler(clientSocket, config, fac, logQueue);
                    handler.run();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();

            // Simulate client making a valid GET request
            try (Socket client = new Socket("localhost", testServer.getLocalPort());
                 PrintWriter out = new PrintWriter(client.getOutputStream(), true);
                 BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()))) {

                System.out.println("Enviando requisição GET /index.html");
                out.println("GET /index.html HTTP/1.1");
                out.println("Host: localhost");
                out.println();

                // Read response
                System.out.println("Resposta do servidor:");
                String line;
                while ((line = in.readLine()) != null) {
                    System.out.println(line);
                    if (line.isEmpty()) {
                        break;
                    }
                }

                // Read Content
                StringBuilder content = new StringBuilder();
                while (in.ready()) {
                    content.append((char) in.read());
                }
                System.out.println("Conteúdo recebido:\n" + content);
            }
        }
    }

    private static void testInvalidRequest(ServerConfig config, FileAccessController fac, BlockingQueue<LogEntry> logQueue) throws Exception {
        // Start thread to simulate the handler
        try (ServerSocket testServer = new ServerSocket(0)) {
            System.out.println("Servidor de teste iniciado na porta: " + testServer.getLocalPort());

            new Thread(() -> {
                try (Socket clientSocket = testServer.accept()) {
                    System.out.println("Conexão de teste aceita");
                    ClientHandler handler = new ClientHandler(clientSocket, config, fac, logQueue);
                    handler.run();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();

            // Simulate client making an invalid GET request
            try (Socket client = new Socket("localhost", testServer.getLocalPort());
                 PrintWriter out = new PrintWriter(client.getOutputStream(), true);
                 BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()))) {

                System.out.println("Enviando requisição GET /arquivo_inexistente.html");
                out.println("GET /arquivo_inexistente.html HTTP/1.1");
                out.println("Host: localhost");
                out.println();

                // Read response
                System.out.println("Resposta do servidor:");
                String line;
                while ((line = in.readLine()) != null) {
                    System.out.println(line);
                    if (line.isEmpty()) {
                        break;
                    }
                }

                // Read content
                StringBuilder content = new StringBuilder();
                while (in.ready()) {
                    content.append((char) in.read());
                }
                System.out.println("Conteúdo recebido:\n" + content);
            }
        }
    }
}