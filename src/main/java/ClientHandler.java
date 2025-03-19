import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;

public class ClientHandler implements Runnable {

    private final Socket client;
    private final String serverRoot;

    public ClientHandler(Socket client, String serverRoot) {

        this.client = client;
        this.serverRoot = serverRoot;
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

            Thread.sleep(3000);
            String request = requestBuilder.toString();
            String[] tokens = request.split(" ");
            if (tokens.length < 2) {
                System.err.println("Invalid request received.");
                return;
            }
            String route = tokens[1];
            System.out.println("Request received: " + request);

            if (route.equals("/")) {
                route = "/index.html";
            }

            String filePath = serverRoot + route;
            File file = new File(filePath);

                byte[] content;
                if (file.exists() && !file.isDirectory()) {
                    content = Files.readAllBytes(Paths.get(filePath));
                    clientOutput.write("HTTP/1.1 200 OK\r\n".getBytes());
                } else {
                    content = Files.readAllBytes(Paths.get(serverRoot + "/404.html"));
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
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                client.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
