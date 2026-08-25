package ch.bzz;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public record DatabaseConfig(String url, String user, String password) {

    static DatabaseConfig load(String path) {
        Properties properties = new Properties();
        try (FileInputStream in = new FileInputStream(path)) {
            properties.load(in);
        } catch (IOException e) {
            throw new RuntimeException(
                    "Could not read " + path
                            + ". Copy config.properties.template to config.properties and fill in the DB connection details.",
                    e);
        }
        return new DatabaseConfig(
                properties.getProperty("DB_URL"),
                properties.getProperty("DB_USER"),
                properties.getProperty("DB_PASSWORD"));
    }
}
