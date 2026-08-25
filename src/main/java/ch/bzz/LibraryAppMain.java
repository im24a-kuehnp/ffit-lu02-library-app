package ch.bzz;

import java.io.IOException;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class LibraryAppMain {

    private static final Map<String, String> COMMANDS = new LinkedHashMap<>();
    static {
        COMMANDS.put("help", "shows this help text");
        COMMANDS.put("listBooks", "lists all books in the library");
        COMMANDS.put("importBooks", "imports books from a .xlsx, .tsv or .csv file, e.g. importBooks data\\books.tsv");
        COMMANDS.put("quit", "exits the application");
    }

    public static void main(String[] args) {
        DatabaseConfig config = DatabaseConfig.load("config.properties");
        BookRepository bookRepository = new BookRepository(config);
        BookFileReader bookFileReader = new BookFileReader();

        Scanner scanner = new Scanner(System.in);
        while (true) {
            String line = scanner.nextLine().trim();
            String[] parts = line.split("\\s+", 2);
            String command = parts[0];
            String argument = parts.length > 1 ? parts[1] : null;

            if (command.equals("quit")) {
                break;
            }
            else if (command.equals("help")) {
                printHelp();
            }
            else if (command.equals("listBooks")) {
                listBooks(bookRepository);
            }
            else if (command.equals("importBooks")) {
                importBooks(bookFileReader, bookRepository, argument);
            }
            else {
                System.out.println("Unknown command: " + command);
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

    private static void listBooks(BookRepository bookRepository) {
        try {
            List<Book> books = bookRepository.findAll();
            for (Book book : books) {
                System.out.printf("id: %d, isbn: %s, title: %s, author: %s, publication_year: %d%n",
                        book.id(), book.isbn(), book.title(), book.author(), book.publicationYear());
            }
        } catch (SQLException e) {
            System.out.println("Could not load books: " + e.getMessage());
        }
    }

    private static void importBooks(BookFileReader bookFileReader, BookRepository bookRepository, String filePath) {
        if (filePath == null || filePath.isBlank()) {
            System.out.println("Usage: importBooks <path-to-file> (.xlsx, .tsv or .csv)");
            return;
        }

        try {
            List<Book> books = bookFileReader.read(filePath);
            int imported = bookRepository.insertAll(books);
            System.out.println("Imported " + imported + " book(s) from " + filePath);
        } catch (IOException e) {
            System.out.println("Could not read file '" + filePath + "': " + e.getMessage());
        } catch (SQLException e) {
            System.out.println("Could not import books: " + e.getMessage());
        }
    }
}
