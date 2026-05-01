package test;

import dao.BookDAO;
import db.DatabaseManager;
import model.Book;
import org.junit.jupiter.api.*;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.sql.*;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * BookDAOTest – unit tests for {@link BookDAO}.
 *
 * Uses Mockito to mock the database layer so no real SQLite file is required.
 * Each test verifies one unit of behaviour in isolation.
 */
@DisplayName("BookDAO Tests")
class BookDAOTest {

    // ── Fixtures ──────────────────────────────────────────────────────────────

    private static final Book SAMPLE_BOOK =
        new Book(1, "Clean Code", "Robert C. Martin", "Programming", "Available");

    // ── create() ──────────────────────────────────────────────────────────────

    @Test
    @DisplayName("create() returns true when INSERT succeeds")
    void create_returnsTrueOnSuccess() throws Exception {
        Connection conn = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);
        when(conn.prepareStatement(anyString())).thenReturn(ps);
        when(ps.executeUpdate()).thenReturn(1);

        DatabaseManager dm = mock(DatabaseManager.class);
        when(dm.getConnection()).thenReturn(conn);

        try (MockedStatic<DatabaseManager> staticDm = Mockito.mockStatic(DatabaseManager.class)) {
            staticDm.when(DatabaseManager::getInstance).thenReturn(dm);

            BookDAO dao = new BookDAO();
            boolean result = dao.create(SAMPLE_BOOK);

            assertTrue(result, "create() should return true when one row is affected");
            verify(ps).setString(1, SAMPLE_BOOK.getTitle());
            verify(ps).setString(2, SAMPLE_BOOK.getAuthor());
            verify(ps).setString(3, SAMPLE_BOOK.getCategory());
            verify(ps).setString(4, SAMPLE_BOOK.getAvailabilityStatus());
        }
    }

    @Test
    @DisplayName("create() returns false when INSERT affects 0 rows")
    void create_returnsFalseWhenNoRowAffected() throws Exception {
        Connection conn = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);
        when(conn.prepareStatement(anyString())).thenReturn(ps);
        when(ps.executeUpdate()).thenReturn(0);

        DatabaseManager dm = mock(DatabaseManager.class);
        when(dm.getConnection()).thenReturn(conn);

        try (MockedStatic<DatabaseManager> staticDm = Mockito.mockStatic(DatabaseManager.class)) {
            staticDm.when(DatabaseManager::getInstance).thenReturn(dm);
            BookDAO dao = new BookDAO();
            assertFalse(dao.create(SAMPLE_BOOK));
        }
    }

    @Test
    @DisplayName("create() returns false on SQLException")
    void create_returnsFalseOnSQLException() throws Exception {
        Connection conn = mock(Connection.class);
        when(conn.prepareStatement(anyString())).thenThrow(new SQLException("DB error"));

        DatabaseManager dm = mock(DatabaseManager.class);
        when(dm.getConnection()).thenReturn(conn);

        try (MockedStatic<DatabaseManager> staticDm = Mockito.mockStatic(DatabaseManager.class)) {
            staticDm.when(DatabaseManager::getInstance).thenReturn(dm);
            BookDAO dao = new BookDAO();
            assertFalse(dao.create(SAMPLE_BOOK), "create() should swallow SQLException and return false");
        }
    }

    // ── findById() ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("findById() returns mapped Book when row exists")
    void findById_returnsMappedBook() throws Exception {
        ResultSet rs = mock(ResultSet.class);
        when(rs.next()).thenReturn(true);
        when(rs.getInt("book_id")).thenReturn(1);
        when(rs.getString("title")).thenReturn("Clean Code");
        when(rs.getString("author")).thenReturn("Robert C. Martin");
        when(rs.getString("category")).thenReturn("Programming");
        when(rs.getString("availability_status")).thenReturn("Available");

        PreparedStatement ps = mock(PreparedStatement.class);
        when(ps.executeQuery()).thenReturn(rs);

        Connection conn = mock(Connection.class);
        when(conn.prepareStatement(anyString())).thenReturn(ps);

        DatabaseManager dm = mock(DatabaseManager.class);
        when(dm.getConnection()).thenReturn(conn);

        try (MockedStatic<DatabaseManager> staticDm = Mockito.mockStatic(DatabaseManager.class)) {
            staticDm.when(DatabaseManager::getInstance).thenReturn(dm);
            BookDAO dao = new BookDAO();
            Book found = dao.findById(1);

            assertNotNull(found);
            assertEquals(1, found.getBookId());
            assertEquals("Clean Code", found.getTitle());
            assertEquals("Robert C. Martin", found.getAuthor());
            assertEquals("Available", found.getAvailabilityStatus());
        }
    }

    @Test
    @DisplayName("findById() returns null when no row found")
    void findById_returnsNullWhenNotFound() throws Exception {
        ResultSet rs = mock(ResultSet.class);
        when(rs.next()).thenReturn(false);

        PreparedStatement ps = mock(PreparedStatement.class);
        when(ps.executeQuery()).thenReturn(rs);

        Connection conn = mock(Connection.class);
        when(conn.prepareStatement(anyString())).thenReturn(ps);

        DatabaseManager dm = mock(DatabaseManager.class);
        when(dm.getConnection()).thenReturn(conn);

        try (MockedStatic<DatabaseManager> staticDm = Mockito.mockStatic(DatabaseManager.class)) {
            staticDm.when(DatabaseManager::getInstance).thenReturn(dm);
            BookDAO dao = new BookDAO();
            assertNull(dao.findById(999));
        }
    }

    // ── update() ──────────────────────────────────────────────────────────────

    @Test
    @DisplayName("update() returns true when UPDATE succeeds")
    void update_returnsTrueOnSuccess() throws Exception {
        Connection conn = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);
        when(conn.prepareStatement(anyString())).thenReturn(ps);
        when(ps.executeUpdate()).thenReturn(1);

        DatabaseManager dm = mock(DatabaseManager.class);
        when(dm.getConnection()).thenReturn(conn);

        try (MockedStatic<DatabaseManager> staticDm = Mockito.mockStatic(DatabaseManager.class)) {
            staticDm.when(DatabaseManager::getInstance).thenReturn(dm);
            BookDAO dao = new BookDAO();
            assertTrue(dao.update(SAMPLE_BOOK));
            verify(ps).setInt(5, SAMPLE_BOOK.getBookId());
        }
    }

    // ── delete() ──────────────────────────────────────────────────────────────

    @Test
    @DisplayName("delete() returns true when DELETE succeeds")
    void delete_returnsTrueOnSuccess() throws Exception {
        Connection conn = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);
        when(conn.prepareStatement(anyString())).thenReturn(ps);
        when(ps.executeUpdate()).thenReturn(1);

        DatabaseManager dm = mock(DatabaseManager.class);
        when(dm.getConnection()).thenReturn(conn);

        try (MockedStatic<DatabaseManager> staticDm = Mockito.mockStatic(DatabaseManager.class)) {
            staticDm.when(DatabaseManager::getInstance).thenReturn(dm);
            BookDAO dao = new BookDAO();
            assertTrue(dao.delete(1));
        }
    }

    @Test
    @DisplayName("delete() returns false when no row matched")
    void delete_returnsFalseWhenNotFound() throws Exception {
        Connection conn = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);
        when(conn.prepareStatement(anyString())).thenReturn(ps);
        when(ps.executeUpdate()).thenReturn(0);

        DatabaseManager dm = mock(DatabaseManager.class);
        when(dm.getConnection()).thenReturn(conn);

        try (MockedStatic<DatabaseManager> staticDm = Mockito.mockStatic(DatabaseManager.class)) {
            staticDm.when(DatabaseManager::getInstance).thenReturn(dm);
            BookDAO dao = new BookDAO();
            assertFalse(dao.delete(999));
        }
    }

    // ── search() ──────────────────────────────────────────────────────────────

    @Test
    @DisplayName("search() passes correct LIKE pattern for keyword")
    void search_passesCorrectPattern() throws Exception {
        ResultSet rs = mock(ResultSet.class);
        when(rs.next()).thenReturn(false); // empty result is fine for this test

        PreparedStatement ps = mock(PreparedStatement.class);
        when(ps.executeQuery()).thenReturn(rs);

        Connection conn = mock(Connection.class);
        when(conn.prepareStatement(anyString())).thenReturn(ps);

        DatabaseManager dm = mock(DatabaseManager.class);
        when(dm.getConnection()).thenReturn(conn);

        try (MockedStatic<DatabaseManager> staticDm = Mockito.mockStatic(DatabaseManager.class)) {
            staticDm.when(DatabaseManager::getInstance).thenReturn(dm);
            BookDAO dao = new BookDAO();
            List<Book> results = dao.search("clean");

            // Verify the LIKE pattern was bound for title, author, category
            verify(ps).setString(1, "%clean%");
            verify(ps).setString(2, "%clean%");
            verify(ps).setString(3, "%clean%");
            // Verify exact ID param was bound
            verify(ps).setString(4, "clean");
            assertTrue(results.isEmpty());
        }
    }

    // ── Book model ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Book.isAvailable() returns true for 'Available' status")
    void book_isAvailableReturnsTrueForAvailableStatus() {
        Book book = new Book(1, "Title", "Author", "Cat", "Available");
        assertTrue(book.isAvailable());
    }

    @Test
    @DisplayName("Book.isAvailable() returns false for 'Borrowed' status")
    void book_isAvailableReturnsFalseForBorrowedStatus() {
        Book book = new Book(1, "Title", "Author", "Cat", "Borrowed");
        assertFalse(book.isAvailable());
    }

    @Test
    @DisplayName("Book.getSummary() contains all key fields")
    void book_getSummaryContainsAllFields() {
        String summary = SAMPLE_BOOK.getSummary();
        assertTrue(summary.contains("Clean Code"));
        assertTrue(summary.contains("Robert C. Martin"));
        assertTrue(summary.contains("Programming"));
        assertTrue(summary.contains("Available"));
    }
}
