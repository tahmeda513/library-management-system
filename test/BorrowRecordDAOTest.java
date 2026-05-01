package test;

import dao.BorrowRecordDAO;
import db.DatabaseManager;
import model.BorrowRecord;
import org.junit.jupiter.api.*;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.sql.*;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * BorrowRecordDAOTest – unit tests for {@link BorrowRecordDAO} and the
 * {@link BorrowRecord} domain model.
 *
 * Mocks the JDBC layer via Mockito so tests run without a real database.
 */
@DisplayName("BorrowRecordDAO & BorrowRecord Tests")
class BorrowRecordDAOTest {

    // ── Fixtures ──────────────────────────────────────────────────────────────

    private static final String TODAY        = LocalDate.now().toString();
    private static final String YESTERDAY    = LocalDate.now().minusDays(1).toString();
    private static final String TOMORROW     = LocalDate.now().plusDays(1).toString();
    private static final String NEXT_WEEK    = LocalDate.now().plusDays(7).toString();

    /** A valid, non-overdue, borrowed record. */
    private static final BorrowRecord ACTIVE_RECORD =
        new BorrowRecord(1, 10, 20, TODAY, NEXT_WEEK, "Borrowed");

    /** An overdue record (due date in the past, not yet returned). */
    private static final BorrowRecord OVERDUE_RECORD =
        new BorrowRecord(2, 11, 21, YESTERDAY, YESTERDAY, "Borrowed");

    /** A returned record. */
    private static final BorrowRecord RETURNED_RECORD =
        new BorrowRecord(3, 12, 22, YESTERDAY, TODAY, "Returned");

    // ── Helpers ───────────────────────────────────────────────────────────────

    /**
     * Builds a mock ResultSet pre-loaded with the fields of {@code record}.
     * Also stubs the JOIN-populated display columns.
     */
    private ResultSet mockResultSet(BorrowRecord record, boolean hasRow) throws SQLException {
        ResultSet rs = mock(ResultSet.class);
        when(rs.next()).thenReturn(hasRow);
        if (hasRow) {
            when(rs.getInt("record_id")).thenReturn(record.getRecordId());
            when(rs.getInt("book_id")).thenReturn(record.getBookId());
            when(rs.getInt("member_id")).thenReturn(record.getMemberId());
            when(rs.getString("borrow_date")).thenReturn(record.getBorrowDate());
            when(rs.getString("due_date")).thenReturn(record.getDueDate());
            when(rs.getString("return_status")).thenReturn(record.getReturnStatus());
            when(rs.getString("book_title")).thenReturn("Test Book");
            when(rs.getString("member_name")).thenReturn("Test Member");
        }
        return rs;
    }

    // ── create() ──────────────────────────────────────────────────────────────

    @Test
    @DisplayName("create() returns true when INSERT affects 1 row")
    void create_returnsTrueOnSuccess() throws Exception {
        Connection conn = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);
        when(conn.prepareStatement(anyString())).thenReturn(ps);
        when(ps.executeUpdate()).thenReturn(1);

        DatabaseManager dm = mock(DatabaseManager.class);
        when(dm.getConnection()).thenReturn(conn);

        try (MockedStatic<DatabaseManager> staticDm = Mockito.mockStatic(DatabaseManager.class)) {
            staticDm.when(DatabaseManager::getInstance).thenReturn(dm);
            BorrowRecordDAO dao = new BorrowRecordDAO();
            boolean result = dao.create(ACTIVE_RECORD);

            assertTrue(result);
            verify(ps).setInt(1, ACTIVE_RECORD.getBookId());
            verify(ps).setInt(2, ACTIVE_RECORD.getMemberId());
            verify(ps).setString(3, ACTIVE_RECORD.getBorrowDate());
            verify(ps).setString(4, ACTIVE_RECORD.getDueDate());
            verify(ps).setString(5, ACTIVE_RECORD.getReturnStatus());
        }
    }

    @Test
    @DisplayName("create() returns false on SQLException")
    void create_returnsFalseOnSQLException() throws Exception {
        Connection conn = mock(Connection.class);
        when(conn.prepareStatement(anyString())).thenThrow(new SQLException("Fail"));

        DatabaseManager dm = mock(DatabaseManager.class);
        when(dm.getConnection()).thenReturn(conn);

        try (MockedStatic<DatabaseManager> staticDm = Mockito.mockStatic(DatabaseManager.class)) {
            staticDm.when(DatabaseManager::getInstance).thenReturn(dm);
            BorrowRecordDAO dao = new BorrowRecordDAO();
            assertFalse(dao.create(ACTIVE_RECORD));
        }
    }

    // ── findById() ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("findById() returns fully populated BorrowRecord")
    void findById_returnsMappedRecord() throws Exception {
        ResultSet rs = mockResultSet(ACTIVE_RECORD, true);

        PreparedStatement ps = mock(PreparedStatement.class);
        when(ps.executeQuery()).thenReturn(rs);

        Connection conn = mock(Connection.class);
        when(conn.prepareStatement(anyString())).thenReturn(ps);

        DatabaseManager dm = mock(DatabaseManager.class);
        when(dm.getConnection()).thenReturn(conn);

        try (MockedStatic<DatabaseManager> staticDm = Mockito.mockStatic(DatabaseManager.class)) {
            staticDm.when(DatabaseManager::getInstance).thenReturn(dm);
            BorrowRecordDAO dao = new BorrowRecordDAO();
            BorrowRecord found = dao.findById(1);

            assertNotNull(found);
            assertEquals(1,   found.getRecordId());
            assertEquals(10,  found.getBookId());
            assertEquals(20,  found.getMemberId());
            assertEquals("Borrowed", found.getReturnStatus());
            assertEquals("Test Book",   found.getBookTitle());
            assertEquals("Test Member", found.getMemberName());
        }
    }

    @Test
    @DisplayName("findById() returns null when record not found")
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
            BorrowRecordDAO dao = new BorrowRecordDAO();
            assertNull(dao.findById(999));
        }
    }

    // ── update() ──────────────────────────────────────────────────────────────

    @Test
    @DisplayName("update() binds all six parameters and returns true on success")
    void update_bindsAllParamsAndReturnsTrue() throws Exception {
        Connection conn = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);
        when(conn.prepareStatement(anyString())).thenReturn(ps);
        when(ps.executeUpdate()).thenReturn(1);

        DatabaseManager dm = mock(DatabaseManager.class);
        when(dm.getConnection()).thenReturn(conn);

        try (MockedStatic<DatabaseManager> staticDm = Mockito.mockStatic(DatabaseManager.class)) {
            staticDm.when(DatabaseManager::getInstance).thenReturn(dm);
            BorrowRecordDAO dao = new BorrowRecordDAO();
            assertTrue(dao.update(ACTIVE_RECORD));

            verify(ps).setInt(1, ACTIVE_RECORD.getBookId());
            verify(ps).setInt(2, ACTIVE_RECORD.getMemberId());
            verify(ps).setString(5, ACTIVE_RECORD.getReturnStatus());
            verify(ps).setInt(6, ACTIVE_RECORD.getRecordId());
        }
    }

    // ── delete() ──────────────────────────────────────────────────────────────

    @Test
    @DisplayName("delete() returns true when DELETE affects 1 row")
    void delete_returnsTrueOnSuccess() throws Exception {
        Connection conn = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);
        when(conn.prepareStatement(anyString())).thenReturn(ps);
        when(ps.executeUpdate()).thenReturn(1);

        DatabaseManager dm = mock(DatabaseManager.class);
        when(dm.getConnection()).thenReturn(conn);

        try (MockedStatic<DatabaseManager> staticDm = Mockito.mockStatic(DatabaseManager.class)) {
            staticDm.when(DatabaseManager::getInstance).thenReturn(dm);
            BorrowRecordDAO dao = new BorrowRecordDAO();
            assertTrue(dao.delete(1));
        }
    }

    // ── BorrowRecord model: isOverdue() ───────────────────────────────────────

    @Test
    @DisplayName("isOverdue() returns false for a future due date")
    void isOverdue_returnsFalseForFutureDueDate() {
        assertFalse(ACTIVE_RECORD.isOverdue());
    }

    @Test
    @DisplayName("isOverdue() returns true for a past due date with Borrowed status")
    void isOverdue_returnsTrueForPastDueDateAndBorrowedStatus() {
        assertTrue(OVERDUE_RECORD.isOverdue());
    }

    @Test
    @DisplayName("isOverdue() returns false for Returned records even if past due date")
    void isOverdue_returnsFalseForReturnedRecord() {
        // RETURNED_RECORD has due date of today (borderline), but status = Returned
        assertFalse(RETURNED_RECORD.isOverdue());
    }

    // ── BorrowRecord model: daysOverdue() ─────────────────────────────────────

    @Test
    @DisplayName("daysOverdue() is positive for an overdue record")
    void daysOverdue_isPositiveForOverdueRecord() {
        assertTrue(OVERDUE_RECORD.daysOverdue() >= 1,
            "Should be at least 1 day overdue for a record due yesterday");
    }

    @Test
    @DisplayName("daysOverdue() is zero or negative for a future due date")
    void daysOverdue_isNonPositiveForFutureRecord() {
        assertTrue(ACTIVE_RECORD.daysOverdue() <= 0);
    }

    // ── BorrowRecord model: getSummary() ──────────────────────────────────────

    @Test
    @DisplayName("getSummary() contains record ID, status, and dates")
    void getSummary_containsKeyFields() {
        String summary = ACTIVE_RECORD.getSummary();
        assertTrue(summary.contains("Borrowed"));
        assertTrue(summary.contains(TODAY));
        assertTrue(summary.contains(NEXT_WEEK));
    }

    @Test
    @DisplayName("getSummary() appends overdue warning for overdue records")
    void getSummary_appendsOverdueWarning() {
        String summary = OVERDUE_RECORD.getSummary();
        assertTrue(summary.contains("OVERDUE"), "Expected OVERDUE flag in summary");
    }

    @Test
    @DisplayName("getSummary() does not contain overdue warning for returned records")
    void getSummary_noOverdueWarningForReturnedRecord() {
        String summary = RETURNED_RECORD.getSummary();
        assertFalse(summary.contains("OVERDUE"), "Returned records should not show OVERDUE flag");
    }
}
