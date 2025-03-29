import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class LogEntryTest {

    @Test
    @DisplayName("Test JSON serialization of LogEntry with complete data")
    public void testToJSONFormat(){
        LogEntry entry = new LogEntry(
                "2025-03-27 14:30:00.123",
                "GET",
                "/index.html",
                "127.0.0.1",
                200
        );

        String actualJSON = entry.toJSON();
        System.out.println("=== JSON Gerado ===\n" + actualJSON);

        String expectedJSON =
                "{\n"+
                        "\"timestamp\": \"2025-03-27 14:30:00.123\",\n" +
                        "\"method\": \"GET\", \n" +
                        "\"route\": \"/index.html\", \n" +
                        "\"origin\": \"127.0.0.1\", \n" +
                        "\"httpStatus\": 200\n" +
                        "}";

        assertEquals(expectedJSON, entry.toJSON());
    }

}