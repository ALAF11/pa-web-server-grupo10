import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.BlockingQueue;

public class ClientHandler implements Runnable {

    private final Socket client;
    private final ServerConfig config;
    private final FileAccessController fileAccessController;
    private final BlockingQueue<LogEntry> logQueue;

    public ClientHandler(Socket client, ServerConfig config, FileAccessController fileAccessController, BlockingQueue<LogEntry> logQueue ) {

        this.client = client;
        this.config = config;
        this.fileAccessController = fileAccessController;
        this.logQueue = logQueue;
    }

    @Override
    public void run() {

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(client.getInputStream()));

            OutputStream clientOutput = client.getOutputStream()) {

            // Read and parse the HTTP request
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
            System.out.println("Request received: " + request);

            if (route.equals("/")) {
                route = "/" + config.getConfig("server.default.page") + "." + config.getConfig("server.default.page.extension");
            }

            //Serve the request file
            String filePath = config.getConfig("server.root") + route;
            File file = new File(filePath);

            byte[] content;
            int httpStatus;

            try {
                content = fileAccessController.readFile(route);
                httpStatus = 200;
            } catch (IOException | InterruptedException e) {
                try {
                    content = Files.readAllBytes(Paths.get(config.getConfig("server.root") + "/" + config.getConfig("server.page.404")));
                    httpStatus = 404;
                } catch (IOException ex) {
                    content = "<html><body><h1>404 Not Found</h1></body></html>".getBytes();
                    httpStatus = 404;
                }
            }

            clientOutput.write(("HTTP/1.1 " + (httpStatus == 200 ? "200 OK" : "404 Not Found") + "\r\n").getBytes());

            //Send HTTP response headers
            clientOutput.write("Content-Type: text/html\r\n".getBytes());;
            clientOutput.write("\r\n".getBytes());

            // Send response body
            clientOutput.write(content);
            clientOutput.write("\r\n\r\n".getBytes());
            clientOutput.flush();

            //Create log entry
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
            String method = "GET";
            String origin = client.getInetAddress().toString();

            LogEntry logEntry = new LogEntry(timestamp, method, route, origin, httpStatus);

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
