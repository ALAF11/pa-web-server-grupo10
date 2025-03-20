import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;

public class ClientHandler implements Runnable {

    private final Socket client;
    private final ServerConfig config;

    public ClientHandler(Socket client, ServerConfig config) {

        this.client = client;
        this.config = config;
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

            // Serve the requested file
            String filePath = config.getConfig("server.root") + route;
            File file = new File(filePath);

            byte[] content;
            if (file.exists() && !file.isDirectory()) {
                content = Files.readAllBytes(Paths.get(filePath));
                clientOutput.write("HTTP/1.1 200 OK\r\n".getBytes());
            } else {
                content = Files.readAllBytes(Paths.get(config.getConfig("server.root") + "/" + config.getConfig("server.page.404")));
                clientOutput.write("HTTP/1.1 404 Not Found\r\n".getBytes());
            }

            // Send HTTP response headers
            clientOutput.write("Content-Type: text/html\r\n".getBytes());
            clientOutput.write("\r\n".getBytes());

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
