/**
 * The LogEntry class represents a single log entry for HTTP requests.
 * <p>
 *     This immutable class stores information about HTTP requests including
 *     Timestamp, method, route, origin, and status code, and provides
 *     functionality to convert the entry to JSON format.
 */

public class LogEntry {
    private final String timestamp;
    private final String method;
    private final String route;
    private final String origin;
    private final int httpStatus;

    /**
     * Constructs a new LogEntry with all required fields.
     *
     * @param timestamp the time when the request was received
     * @param method the HTTP method used (get, post, etc.)
     * @param route the requested route/path
     * @param origin the IP adress of the client
     * @param httpStatus the HTTP status code returned
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
     * @return a JSON string representation of the log entry
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
