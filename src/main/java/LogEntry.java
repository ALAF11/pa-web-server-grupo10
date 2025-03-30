/**
 * The LogEntry class represents a single log entry for HTTP requests in JSON format.
 * <p>
 * This immutable class stores information about HTTP requests including
 * timestamp, method, route, client origin, and status code.
 * The class provides conversion to JSON format for logging purposes.
 * </p>
 * <p>
 * Instances of this class are thread-safe as they are immutable after construction.
 * </p>
 */

public class LogEntry {
    private final String timestamp;
    private final String method;
    private final String route;
    private final String origin;
    private final int httpStatus;

    /**
     * Constructs a new LogEntry with all required fields.
     * <p>
     * @param timestamp the exact time when the request was received in format
     *                  "YYYY-MM-DD HH:MM:SS.SSS"
     * @param method the HTTP method used in the request (GET, POST, etc.)
     * @param route the requested route/path.
     * @param origin the IP address of the client
     * @param httpStatus the HTTP status code returned to the client.
     */

    public LogEntry(String timestamp, String method, String route, String origin, int httpStatus){
        this.timestamp = timestamp;
        this.method = method;
        this.route = route;
        this.origin = origin;
        this.httpStatus = httpStatus;
    }

    /**
     * Converts the log entry to JSON format.
     *
     * @return a JSON string representation of the log entry.
     */

    public String toJSON(){
        return String.format(
                "{\n"+
                        "\"timestamp\": \"%s\",\n" +
                        "\"method\": \"%s\", \n" +
                        "\"route\": \"%s\", \n" +
                        "\"origin\": \"%s\", \n" +
                        "\"httpStatus\": %d\n" +
                "}", timestamp, method, route, origin, httpStatus

        );
    }
}
