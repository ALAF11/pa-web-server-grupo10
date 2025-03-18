import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class LogEntry {
    private final String timestamp;
    private final String method;
    private final String route;
    private final String origin;
    private final int httpStatus;

    public LogEntry(LocalDateTime timestamp, String method, String route, String origin, int httpStatus){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        this.timestamp = timestamp.format(formatter);
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
                        "\"httpStatus\": \"%d\", \n" +
                "}", timestamp, method, route, origin, httpStatus

        );
    }
}
