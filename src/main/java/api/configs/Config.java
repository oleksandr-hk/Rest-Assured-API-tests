package api.configs;

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
        //first priority it's system property baseApiUrl =..
        String systemValue = System.getProperty(key);

        if (systemValue != null) {
            return systemValue;
        }
        //second priority it's env variable - BASEAPIURL
        //admin.username -> ADMIN_USERNAME
        String envKey = key.toUpperCase().replace(".", "_");
        String envValue = System.getenv(envKey);
        if (envValue != null) {
            return envValue;
        }
        //third priority it's config property
        return INSTANCE.properties.getProperty(key);
    }

}
