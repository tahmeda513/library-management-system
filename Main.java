import db.DatabaseManager;
import ui.ConsoleUI;
import ui.MainWindow;

import javax.swing.*;
import java.util.Arrays;
import java.util.logging.*;

/**
 * Main – application entry point.
 *
 * <p>Launches the console UI by default (Basic Requirement).
 * Pass {@code --gui} as a command-line argument to start the Swing GUI instead
 * (Medium Requirement).
 *
 * <p>OOP concepts: Demonstrates separation of concerns — database initialisation
 * is decoupled from both UI layers.
 */
public class Main {

    private static final Logger LOGGER = Logger.getLogger(Main.class.getName());

    public static void main(String[] args) {
        // Configure basic logging format
        System.setProperty("java.util.logging.SimpleFormatter.format",
            "[%1$tF %1$tT] [%4$s] %5$s%6$s%n");

        LOGGER.info("Starting St Mary's Digital Library Management System...");

        // Initialise DB eagerly so any connection error surfaces before UI loads
        try {
            DatabaseManager.getInstance();
        } catch (RuntimeException e) {
            System.err.println("ERROR: Failed to connect to the database: " + e.getMessage());
            System.err.println("Ensure sqlite-jdbc.jar is on the classpath.");
            System.exit(1);
        }

        boolean useGui = Arrays.asList(args).contains("--gui");

        if (useGui) {
            launchGui();
        } else {
            launchConsole();
        }
    }

    // ── Console UI (Basic Requirement) ────────────────────────────────────────

    private static void launchConsole() {
        LOGGER.info("Launching console interface.");
        ConsoleUI console = new ConsoleUI();
        console.start();
    }

    // ── Swing GUI (Medium Requirement) ────────────────────────────────────────

    private static void launchGui() {
        LOGGER.info("Launching Swing GUI.");
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                LOGGER.warning("Could not set system look and feel: " + e.getMessage());
            }
            MainWindow window = new MainWindow();
            window.setVisible(true);
            LOGGER.info("GUI launched successfully.");
        });
    }
}
