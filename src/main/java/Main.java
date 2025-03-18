import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        try {
            // Caminho para o ficheiro de configuração
            String configFilePath = System.getProperty("user.dir") + "/html/server.config";

            // Carregar as configurações do ficheiro
            ServerConfig config = ConfigLoader.loadConfig(configFilePath);  // Esse método pode lançar IOException
            System.out.println("Configuração carregada com sucesso!");

            // Exibir as configurações carregadas
            System.out.println("Server Root: " + config.getServerRoot());
            System.out.println("Port: " + config.getPort());
            System.out.println("Document Root: " + config.getDocumentRoot());
            System.out.println("Default Page: " + config.getDefaultPage());
            System.out.println("Default Page Extension: " + config.getDefaultPageExtension());
            System.out.println("404 Page: " + config.getPage404());
            System.out.println("Maximum Requests: " + config.getMaximumRequests());

            // Iniciar o servidor HTTP
            MainHTTPServerThread s = new MainHTTPServerThread(config);
            s.start();
            // Esperar a thread do servidor HTTP finalizar
            s.join();

        } catch (IOException e) {
            System.err.println("Erro ao carregar o ficheiro de configuração: " + e.getMessage());
            e.printStackTrace();  // Exibe o erro detalhado para depuração
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
