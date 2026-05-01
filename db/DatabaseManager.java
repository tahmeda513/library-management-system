package db;

import java.sql.*;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * DatabaseManager – Singleton class responsible for managing the SQLite
 * connection and initialising the schema on first run.
 *
 * OOP concepts: Singleton pattern (single shared instance), encapsulation.
 */
public class DatabaseManager {

    private static final Logger LOGGER = Logger.getLogger(DatabaseManager.class.getName());
    private static final String DB_URL = "jdbc:sqlite:library.db";
    private static DatabaseManager instance;
    private Connection connection;

    // Private constructor – enforces Singleton
    private DatabaseManager() {
        connect();
        initialiseSchema();
    }

    /** Returns the single shared instance. Thread-safe via synchronised. */
    public static synchronized DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }

    /** Returns the live connection, reconnecting if closed. */
    public Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                connect();
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to check connection state", e);
        }
        return connection;
    }

    private void connect() {
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection(DB_URL);
            // Enable foreign-key enforcement
            try (Statement st = connection.createStatement()) {
                st.execute("PRAGMA foreign_keys = ON");
            }
            LOGGER.info("Connected to SQLite database.");
        } catch (ClassNotFoundException e) {
            LOGGER.log(Level.SEVERE, "SQLite JDBC driver not found. Add sqlite-jdbc.jar to the classpath.", e);
            throw new RuntimeException("SQLite JDBC driver not found", e);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database connection failed", e);
            throw new RuntimeException("Database connection failed", e);
        }
    }

    /** Creates tables if they do not already exist, then inserts sample data. */
    private void initialiseSchema() {
        String createBooks = """
            CREATE TABLE IF NOT EXISTS books (
                book_id INTEGER PRIMARY KEY AUTOINCREMENT,
                title TEXT NOT NULL,
                author TEXT NOT NULL,
                category TEXT NOT NULL,
                availability_status TEXT NOT NULL DEFAULT 'Available'
            )""";

        String createMembers = """
            CREATE TABLE IF NOT EXISTS members (
                member_id INTEGER PRIMARY KEY AUTOINCREMENT,
                member_name TEXT NOT NULL,
                email TEXT NOT NULL UNIQUE,
                membership_type TEXT NOT NULL
            )""";

        String createBorrowRecords = """
            CREATE TABLE IF NOT EXISTS borrow_records (
                record_id INTEGER PRIMARY KEY AUTOINCREMENT,
                book_id INTEGER NOT NULL,
                member_id INTEGER NOT NULL,
                borrow_date DATE NOT NULL,
                due_date DATE NOT NULL,
                return_status TEXT NOT NULL DEFAULT 'Borrowed',
                FOREIGN KEY (book_id) REFERENCES books(book_id),
                FOREIGN KEY (member_id) REFERENCES members(member_id)
            )""";

        try (Statement st = connection.createStatement()) {
            st.execute(createBooks);
            st.execute(createMembers);
            st.execute(createBorrowRecords);
            insertSampleDataIfEmpty(st);
            LOGGER.info("Database schema initialised.");
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Schema initialisation failed", e);
        }
    }

    private void insertSampleDataIfEmpty(Statement st) throws SQLException {
        ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM books");
        if (rs.getInt(1) == 0) {
            st.executeUpdate("""
                INSERT INTO books (title, author, category, availability_status) VALUES
                ('Introduction to Java', 'John Smith', 'Programming', 'Available'),
                ('Database Systems', 'Maria Garcia', 'Computer Science', 'Borrowed'),
                ('Software Engineering Principles', 'Alan Brown', 'Engineering', 'Available')
            """);
            st.executeUpdate("""
                INSERT INTO members (member_name, email, membership_type) VALUES
                ('Alice Johnson', 'alice.johnson@stmarys.ac.uk', 'Student'),
                ('Michael Lee', 'michael.lee@stmarys.ac.uk', 'Staff'),
                ('Sara Ahmed', 'sara.ahmed@stmarys.ac.uk', 'Student')
            """);
            st.executeUpdate("""
                INSERT INTO borrow_records (book_id, member_id, borrow_date, due_date, return_status) VALUES
                (2, 1, '2025-03-01', '2025-03-15', 'Borrowed'),
                (1, 2, '2025-03-02', '2025-03-16', 'Returned'),
                (3, 3, '2025-03-05', '2025-03-19', 'Borrowed')
            """);
            LOGGER.info("Sample data inserted.");
        }
    }

    /** Closes the database connection gracefully. */
    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                LOGGER.info("Database connection closed.");
            }
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Error closing connection", e);
        }
    }
}
