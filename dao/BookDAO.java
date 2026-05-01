package dao;

import db.DatabaseManager;
import model.Book;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * BookDAO – Data Access Object for the books table.
 *
 * OOP concepts: Encapsulation (hides SQL details), Polymorphism (implements GenericDAO).
 * Uses PreparedStatement to prevent SQL injection.
 */
public class BookDAO implements GenericDAO<Book, Integer> {

    private static final Logger LOGGER = Logger.getLogger(BookDAO.class.getName());

    private Connection getConn() {
        return DatabaseManager.getInstance().getConnection();
    }

    // ── CREATE ─────────────────────────────────────────────────────────────────
    @Override
    public boolean create(Book book) {
        String sql = "INSERT INTO books (title, author, category, availability_status) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, book.getTitle());
            ps.setString(2, book.getAuthor());
            ps.setString(3, book.getCategory());
            ps.setString(4, book.getAvailabilityStatus());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error creating book", e);
            return false;
        }
    }

    // ── READ (by ID) ────────────────────────────────────────────────────────────
    @Override
    public Book findById(Integer id) {
        String sql = "SELECT * FROM books WHERE book_id = ?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapRow(rs);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding book by ID", e);
        }
        return null;
    }

    // ── READ (all) ──────────────────────────────────────────────────────────────
    @Override
    public List<Book> findAll() {
        return findAll("book_id", true);
    }

    public List<Book> findAll(String sortColumn, boolean ascending) {
        List<Book> books = new ArrayList<>();
        String dir = ascending ? "ASC" : "DESC";
        // Whitelist sort columns to prevent SQL injection
        String safeCol = switch (sortColumn) {
            case "title", "author", "category", "availability_status" -> sortColumn;
            default -> "book_id";
        };
        String sql = "SELECT * FROM books ORDER BY " + safeCol + " " + dir;
        try (Statement st = getConn().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) books.add(mapRow(rs));
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error fetching books", e);
        }
        return books;
    }

    // ── UPDATE ─────────────────────────────────────────────────────────────────
    @Override
    public boolean update(Book book) {
        String sql = "UPDATE books SET title=?, author=?, category=?, availability_status=? WHERE book_id=?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, book.getTitle());
            ps.setString(2, book.getAuthor());
            ps.setString(3, book.getCategory());
            ps.setString(4, book.getAvailabilityStatus());
            ps.setInt(5, book.getBookId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error updating book", e);
            return false;
        }
    }

    // ── DELETE ─────────────────────────────────────────────────────────────────
    @Override
    public boolean delete(Integer id) {
        String sql = "DELETE FROM books WHERE book_id = ?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error deleting book", e);
            return false;
        }
    }

    // ── SEARCH ─────────────────────────────────────────────────────────────────

    /** Searches books by title, author, category (case-insensitive LIKE), or by exact book ID. */
    public List<Book> search(String keyword) {
        List<Book> books = new ArrayList<>();
        // If the keyword is a pure integer, also match by book_id
        String sql = "SELECT * FROM books WHERE LOWER(title) LIKE ? OR LOWER(author) LIKE ? " +
                     "OR LOWER(category) LIKE ? OR CAST(book_id AS TEXT) = ?";
        String pattern = "%" + keyword.toLowerCase() + "%";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, pattern);
            ps.setString(2, pattern);
            ps.setString(3, pattern);
            ps.setString(4, keyword.trim()); // exact ID match
            ResultSet rs = ps.executeQuery();
            while (rs.next()) books.add(mapRow(rs));
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error searching books", e);
        }
        return books;
    }

    /** Returns only books with the given availability status. */
    public List<Book> filterByStatus(String status) {
        List<Book> books = new ArrayList<>();
        String sql = "SELECT * FROM books WHERE availability_status = ?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, status);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) books.add(mapRow(rs));
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error filtering books", e);
        }
        return books;
    }

    /** Returns books by category. */
    public List<Book> filterByCategory(String category) {
        List<Book> books = new ArrayList<>();
        String sql = "SELECT * FROM books WHERE LOWER(category) = LOWER(?)";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, category);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) books.add(mapRow(rs));
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error filtering books by category", e);
        }
        return books;
    }

    // ── Helper ─────────────────────────────────────────────────────────────────
    private Book mapRow(ResultSet rs) throws SQLException {
        return new Book(
            rs.getInt("book_id"),
            rs.getString("title"),
            rs.getString("author"),
            rs.getString("category"),
            rs.getString("availability_status")
        );
    }
}
