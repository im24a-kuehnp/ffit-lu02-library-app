package ch.bzz;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Scanner;

public class LibraryAppMain {

    private static final Map<String, String> COMMANDS = new LinkedHashMap<>();
    static {
        COMMANDS.put("help", "shows this help text");
        COMMANDS.put("listBooks", "lists all books in the library");
        COMMANDS.put("quit", "exits the application");
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            String s = scanner.next();
            if (s.equals("quit")) {
                break;
            }
            else if (s.equals("help")) {
                printHelp();
            }
            else if (s.equals("listBooks")) {
                listBooks();
            }
            else {
                System.out.println("Unknown command: " + s);
            }

        }
        System.out.println("Exiting ...");
        scanner.close();
    }

    private static void printHelp() {
        System.out.println("Available commands:");
        for (Map.Entry<String, String> command : COMMANDS.entrySet()) {
            System.out.println("  " + command.getKey() + " - " + command.getValue());
        }
    }

    private static void listBooks() {
        Properties config = loadConfig();
        String url = config.getProperty("DB_URL");
        String user = config.getProperty("DB_USER");
        String password = config.getProperty("DB_PASSWORD");

        String sql = "SELECT id, isbn, title, author, publication_year FROM books ORDER BY id";
        try (Connection connection = DriverManager.getConnection(url, user, password);
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {

            while (resultSet.next()) {
                System.out.printf("%d: %s by %s (%s) [%d]%n",
                        resultSet.getInt("id"),
                        resultSet.getString("title"),
                        resultSet.getString("author"),
                        resultSet.getString("isbn"),
                        resultSet.getInt("publication_year"));
            }
        } catch (SQLException e) {
            System.out.println("Could not load books: " + e.getMessage());
        }
    }

    private static Properties loadConfig() {
        Properties properties = new Properties();
        try (FileInputStream in = new FileInputStream("config.properties")) {
            properties.load(in);
        } catch (IOException e) {
            throw new RuntimeException(
                    "Could not read config.properties. Copy config.properties.template to config.properties and fill in the DB connection details.",
                    e);
        }
        return properties;
    }
}
