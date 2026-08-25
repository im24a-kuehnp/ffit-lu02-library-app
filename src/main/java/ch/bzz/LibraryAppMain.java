package ch.bzz;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Scanner;

public class LibraryAppMain {

    private static final Map<String, String> COMMANDS = new LinkedHashMap<>();
    static {
        COMMANDS.put("help", "shows this help text");
        COMMANDS.put("quit", "exits the application");
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            String s = scanner.next();
            if (s.equals("quit")) {
                break;
            }
            if (s.equals("help")) {
                printHelp();
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
}
