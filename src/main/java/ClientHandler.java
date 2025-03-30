import java.io.*;
import java.net.Socket;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.BlockingQueue;

/**
 * The ClientHandler class represents a thread that handles individual client connections to the HTTP server.
 * <p>
 * It processes HTTP requests, serves files from the server's root directory, and logs all requests
 * to a centralized logging system.
 * The class implements proper file access control, error handling,
 * and concurrent request management via {@link BlockingQueue}.
 */

public class ClientHandler implements Runnable {

    private final Socket client;
    private final ServerConfig config;
    private final FileAccessController fileAccessController;
    private final BlockingQueue<LogEntry> logQueue;

    /**
     * Constructs a new ClientHandler with the specified client connection and dependencies.
     *
     * @param client the Socket representing the client connection
     * @param config the ServerConfig containing server configuration parameters
     * @param fileAccessController the FileAccessController for thread-safe file operations
     * @param logQueue the BlockingQueue for asynchronous log processing
     */

    public ClientHandler(Socket client, ServerConfig config, FileAccessController fileAccessController, BlockingQueue<LogEntry> logQueue ) {

        this.client = client;
        this.config = config;
        this.fileAccessController = fileAccessController;
        this.logQueue = logQueue;
    }

    /**
     * The main execution method for handling client requests.
     * <p>
     * This method reads the HTTP request, processes it, serves the appropriate file (or error page),
     * and logs the transaction. It implements proper resource management by ensuring all streams
     * and sockets are closed after processing.
     */

    @Override
    public void run() {

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(client.getInputStream()));

            OutputStream clientOutput = client.getOutputStream()) {

            StringBuilder requestBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null && !line.isBlank()) {
                requestBuilder.append(line).append("\r\n");
            }

            String request = requestBuilder.toString();
            String[] tokens = request.split(" ");
            if (tokens.length < 2) {
                System.err.println("Invalid request received.");
                return;
            }
            String route = tokens[1];
            route = URLDecoder.decode(route, StandardCharsets.UTF_8);
            System.out.println("Request received: " + request);

            if (route.equals("/")) {
                route = "/" + config.getConfig("server.default.page") + "." + config.getConfig("server.default.page.extension");
            }

            byte[] content;
            int httpStatus;

            try {
                content = fileAccessController.readFile(route);
                httpStatus = 200;
            } catch (IOException | InterruptedException e) {
                try {
                    content = Files.readAllBytes(Paths.get(config.getConfig("server.root") + "/" + config.getConfig("server.page.404")));
                } catch (IOException ex) {
                    content = "<html><body><h1>404 Not Found</h1></body></html>".getBytes();

                }
                httpStatus = 404;
            }

            clientOutput.write(("HTTP/1.1 " + (httpStatus == 200 ? "200 OK" : "404 Not Found") + "\r\n").getBytes());

            clientOutput.write("Content-Type: text/html\r\n".getBytes());;
            clientOutput.write("\r\n".getBytes());

            clientOutput.write(content);
            clientOutput.write("\r\n\r\n".getBytes());
            clientOutput.flush();

            LogEntry logEntry = new LogEntry(
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")),
                    "GET",
                    route,
                    client.getInetAddress().toString(),
                    httpStatus
            );

            logQueue.put(logEntry);

        } catch (IOException | InterruptedException e) {
            System.err.println("Error handling client request.");
            e.printStackTrace();

        } finally {
            try {
                client.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
