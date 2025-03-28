import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ServerConfigTest {

    @Test
    public void testSetAndGetConfig(){
        //arrange
        ServerConfig config = new ServerConfig();
        String key = "test.key";
        String value = "test.value";

        //Act
        config.setConfig(key,value);
        String retrievedValue = config.getConfig(key);

        //Assert
        assertEquals(value, retrievedValue, "The recovered value must be equal to the defined value");
    }

    @Test
    public void testGetIntConfig(){
        //arrange
        ServerConfig config = new ServerConfig();
        String key = "numeric.key";
        String value = "42";

        //act
        config.setConfig(key, value);
        int numericValue = config.getIntConfig(key);

        //assert
        assertEquals(42, numericValue, "The numeric value retrieved must be equal to the value set");

    }

    @Test
    public void testGetNonExistentConfig(){
        //arrange
        ServerConfig config = new ServerConfig();
        String key = "non.existent.key";

        //act
        String value = config.getConfig(key);

        //assert
        assertNull(value, "Must return null for a non-existent key");
    }

    @Test
    public void testGetIntConfigWithInvalidNumber(){
        //arrange
        ServerConfig config = new ServerConfig();
        String key = "invalid.number.key";
        String value = "not.a.number";
        config.setConfig(key, value);

        //act and assert
        assertThrows(NumberFormatException.class, () -> {
            config.getIntConfig(key);
                }, "Should throw NumberFormatException for a non-numeric value");
    }


}