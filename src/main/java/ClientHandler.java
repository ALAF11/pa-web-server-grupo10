import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;

public class ClientHandler implements Runnable {

    private final Socket client;
    private final ServerConfig config;
    private final FileAccessController fileAccessController;

    public ClientHandler(Socket client, ServerConfig config, FileAccessController fileAccessController) {

        this.client = client;
        this.config = config;
        this.fileAccessController = fileAccessController;
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

            byte[] content;
            try {
                content = fileAccessController.readFile(route);
                clientOutput.write("HTTP/1.1 200 OK\r\n".getBytes());
            } catch (IOException | InterruptedException e) {
                clientOutput.write("Content-Type: text/html\r\n".getBytes());
                try {
                    content = Files.readAllBytes(Paths.get(config.getConfig("server.root") + "/" + config.getConfig("server.page.404")));
                } catch (IOException ex) {
                    content = "<html><body><h1>404 Not Found</h1></body></html>".getBytes();
                }
                clientOutput.write("HTTP/1.1 404 Not Found\r\n".getBytes());
            }

            // Send response body
            clientOutput.write(content);
            clientOutput.write("\r\n\r\n".getBytes());
            clientOutput.flush();

        } catch (IOException e) {
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
