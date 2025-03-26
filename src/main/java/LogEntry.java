import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class LogEntry {
    private final String timestamp;
    private final String method;
    private final String route;
    private final String origin;
    private final int httpStatus;

    public LogEntry(String timestamp, String method, String route, String origin, int httpStatus){
        this.timestamp = timestamp;
        this.method = method;
        this.route = route;
        this.origin = origin;
        this.httpStatus = httpStatus;
    }

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
