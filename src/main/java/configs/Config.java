package configs;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Config {
    private static Config INSTANCE = new Config();
    private final Properties properties = new Properties();

    private Config() {
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("config.properties")) {
            if(input == null) {
                throw new RuntimeException("config.properties not found in resources");
            }
            properties.load(input);
        } catch (IOException ioException) {
            throw new RuntimeException("Filed to load config properties");
        }
    };

    public static String getProperty(String key) {
        return INSTANCE.properties.getProperty(key);
    }

}
