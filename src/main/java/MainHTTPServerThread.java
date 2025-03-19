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



    private static String SERVER_ROOT;

    private ServerSocket server;
    private final ServerConfig config;
    private final ThreadPool threadPool;

    static {
        String configFilePath = System.getProperty("user.dir") + "/html/server.config";
        ServerConfig config = null;
        try {
            config = ConfigLoader.loadConfig(configFilePath);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        SERVER_ROOT = config.getServerRoot();
    }

    public MainHTTPServerThread(ServerConfig config, ThreadPool threadPool){
        this.config = config;
        this.threadPool = threadPool;
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
    public void run() { //teste

        try {
            server = new ServerSocket(config.getPort());
            System.out.println("Server started on port: " + config.getPort());
            //System.out.println("Working Directory: " + System.getProperty("user.dir"));
            System.out.println("Server Root: " + SERVER_ROOT);
            System.out.println("Document Root: " + config.getDocumentRoot());

            while (true) {
                Socket client = server.accept();
                System.out.println("New client connected: " + client);
                threadPool.execute(new ClientHandler(client, SERVER_ROOT));
            }

        } catch (IOException e) {
            System.err.println("Server error: Unable to start on port " + config.getPort());
            e.printStackTrace();
        } finally {
            threadPool.shutdown();
        }
    }
}