import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * A simple HTTP server that listens on a specified port.
 * It serves files from a predefined server root directory.
 */
public class MainHTTPServerThread extends Thread {

    private static String SERVER_ROOT; // Define by user
    // private final int port;
    private ServerSocket server;
    private final ServerConfig config;

    static {
        String configFilePath = System.getProperty("user.dir") + "/html/server.config";
        ServerConfig config = ConfigLoader.loadConfig(configFilePath);
        SERVER_ROOT = config.getServerRoot();
    }

    public MainHTTPServerThread(ServerConfig config){
        this.config = config;
    }

    /**
     * Constructor to initialize the HTTP server thread with a specified port.
     *
     * @param port The port number on which the server will listen.

    public MainHTTPServerThread(int port) {
        this.port = port;
    }
     */

    /**
     * Reads a binary file and returns its contents as a byte array.
     *
     * @param path The file path to read.
     * @return A byte array containing the file's contents, or an empty array if an error occurs.
     */

    private byte[] readBinaryFile(String path) {
        try {
            return Files.readAllBytes(Paths.get(path));
        } catch (IOException e) {
            System.err.println("Error reading file: " + path);
            e.printStackTrace();
            return new byte[0];
        }
    }

    /**
     * Reads a text file and returns its contents as a string.
     *
     * @param path The file path to read.
     * @return A string containing the file's contents, or an empty string if an error occurs.
     */
    private String readFile(String path) {
        StringBuilder content = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(path))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
        } catch (IOException e) {
            System.err.println("Error reading file: " + path);
            e.printStackTrace();
        }
        return content.toString();
    }

    /**
     * Starts the HTTP server and listens for incoming client requests.
     * Processes HTTP GET requests and serves files from the defined server root directory.
     */
    @Override
    public void run() {

        try {
            server = new ServerSocket(config.getPort());
            System.out.println("Server started on port: " + config.getPort());
            //System.out.println("Working Directory: " + System.getProperty("user.dir"));
            System.out.println("Server Root: " + SERVER_ROOT);
            System.out.println("Document Root: " + config.getDocumentRoot());

            while (true) {
                Socket client = server.accept();
                System.out.println("New client connected: " + client);
                new Thread(() -> handleClientRequest(client)).start();
            }

        } catch (IOException e) {
            System.err.println("Server error: Unable to start on port " + config.getPort());
            e.printStackTrace();
        }
    }

    public void handleClientRequest(Socket client){

        try(BufferedReader br = new BufferedReader(new InputStreamReader(client.getInputStream()));

            OutputStream clientOutput = client.getOutputStream()){
            // Read and parse the HTTP request
            StringBuilder requestBuilder = new StringBuilder();
            String line;
            while (!(line = br.readLine()).isBlank()) {
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

            if(route.equals("/")){
                route = "/index.html";
            }

            // Serve the requested file
            String filePath = SERVER_ROOT + route;
            File file = new File(filePath);

            byte[] content;
            if(file.exists() && !file.isDirectory()){
                content = readBinaryFile(filePath);
                clientOutput.write("HTTP/1.1 200 OK\r\n".getBytes());
            } else {
                content = readBinaryFile(SERVER_ROOT + "/404.html");
            }   clientOutput.write("HTTP/1.1 404 Not Found\r\n".getBytes());

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
        }
    }
}