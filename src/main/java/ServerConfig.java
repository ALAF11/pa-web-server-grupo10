public class ServerConfig {
    private String serverRoot;
    private int port;
    private String documentRoot;
    private String defaultPage;
    private String defaultPageExtension;
    private String page404;
    private int maximumRequests;


    public String getServerRoot(){
        return serverRoot;
    }

    public void setServerRoot(String serverRoot) {
        this.serverRoot = serverRoot;
    }

    public int getPort(){
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getDocumentRoot(){
        return documentRoot;
    }

    public void setDocumentRoot(String documentRoot) {
        this.documentRoot = documentRoot;
    }

    public String getDefaultPage(){
        return defaultPage;
    }

    public void setDefaultPage(String defaultPage) {
        this.defaultPage = defaultPage;
    }

    public String getDefaultPageExtension(){
        return defaultPageExtension;
    }

    public void setDefaultPageExtension(String defaultPageExtension) {
        this.defaultPageExtension = defaultPageExtension;
    }

    public String getPage404(){
        return page404;
    }

    public void setPage404(String page404) {
        this.page404 = page404;
    }

    public int getMaximumRequests(){
        return maximumRequests;
    }

    public void setMaximumRequests(int maximumRequests) {
        this.maximumRequests = maximumRequests;
    }
}
