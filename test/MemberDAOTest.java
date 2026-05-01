package test;

import dao.MemberDAO;
import db.DatabaseManager;
import model.Member;
import org.junit.jupiter.api.*;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.sql.*;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * MemberDAOTest – unit tests for {@link MemberDAO}.
 *
 * Uses Mockito to mock {@link DatabaseManager} and the JDBC layer so no real
 * database connection is required. Each test verifies a single unit of behaviour.
 */
@DisplayName("MemberDAO Tests")
class MemberDAOTest {

    private static final Member SAMPLE_MEMBER =
        new Member(1, "Alice Smith", "alice@example.com", "Student");

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
            MemberDAO dao = new MemberDAO();
            assertTrue(dao.create(SAMPLE_MEMBER));

            verify(ps).setString(1, SAMPLE_MEMBER.getMemberName());
            verify(ps).setString(2, SAMPLE_MEMBER.getEmail());
            verify(ps).setString(3, SAMPLE_MEMBER.getMembershipType());
        }
    }

    @Test
    @DisplayName("create() returns false when SQLException is thrown")
    void create_returnsFalseOnSQLException() throws Exception {
        Connection conn = mock(Connection.class);
        when(conn.prepareStatement(anyString())).thenThrow(new SQLException("DB failure"));

        DatabaseManager dm = mock(DatabaseManager.class);
        when(dm.getConnection()).thenReturn(conn);

        try (MockedStatic<DatabaseManager> staticDm = Mockito.mockStatic(DatabaseManager.class)) {
            staticDm.when(DatabaseManager::getInstance).thenReturn(dm);
            MemberDAO dao = new MemberDAO();
            assertFalse(dao.create(SAMPLE_MEMBER));
        }
    }

    // ── findById() ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("findById() returns correctly mapped Member")
    void findById_returnsMappedMember() throws Exception {
        ResultSet rs = mock(ResultSet.class);
        when(rs.next()).thenReturn(true);
        when(rs.getInt("member_id")).thenReturn(1);
        when(rs.getString("member_name")).thenReturn("Alice Smith");
        when(rs.getString("email")).thenReturn("alice@example.com");
        when(rs.getString("membership_type")).thenReturn("Student");

        PreparedStatement ps = mock(PreparedStatement.class);
        when(ps.executeQuery()).thenReturn(rs);

        Connection conn = mock(Connection.class);
        when(conn.prepareStatement(anyString())).thenReturn(ps);

        DatabaseManager dm = mock(DatabaseManager.class);
        when(dm.getConnection()).thenReturn(conn);

        try (MockedStatic<DatabaseManager> staticDm = Mockito.mockStatic(DatabaseManager.class)) {
            staticDm.when(DatabaseManager::getInstance).thenReturn(dm);
            MemberDAO dao = new MemberDAO();
            Member found = dao.findById(1);

            assertNotNull(found);
            assertEquals(1, found.getMemberId());
            assertEquals("Alice Smith", found.getMemberName());
            assertEquals("alice@example.com", found.getEmail());
            assertEquals("Student", found.getMembershipType());
        }
    }

    @Test
    @DisplayName("findById() returns null when member does not exist")
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
            MemberDAO dao = new MemberDAO();
            assertNull(dao.findById(42));
        }
    }

    // ── update() ──────────────────────────────────────────────────────────────

    @Test
    @DisplayName("update() binds all fields and returns true on success")
    void update_bindsAllFieldsAndReturnsTrue() throws Exception {
        Connection conn = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);
        when(conn.prepareStatement(anyString())).thenReturn(ps);
        when(ps.executeUpdate()).thenReturn(1);

        DatabaseManager dm = mock(DatabaseManager.class);
        when(dm.getConnection()).thenReturn(conn);

        try (MockedStatic<DatabaseManager> staticDm = Mockito.mockStatic(DatabaseManager.class)) {
            staticDm.when(DatabaseManager::getInstance).thenReturn(dm);
            MemberDAO dao = new MemberDAO();
            assertTrue(dao.update(SAMPLE_MEMBER));

            verify(ps).setString(1, SAMPLE_MEMBER.getMemberName());
            verify(ps).setString(2, SAMPLE_MEMBER.getEmail());
            verify(ps).setString(3, SAMPLE_MEMBER.getMembershipType());
            verify(ps).setInt(4, SAMPLE_MEMBER.getMemberId());
        }
    }

    // ── delete() ──────────────────────────────────────────────────────────────

    @Test
    @DisplayName("delete() returns true when row is removed")
    void delete_returnsTrueOnSuccess() throws Exception {
        Connection conn = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);
        when(conn.prepareStatement(anyString())).thenReturn(ps);
        when(ps.executeUpdate()).thenReturn(1);

        DatabaseManager dm = mock(DatabaseManager.class);
        when(dm.getConnection()).thenReturn(conn);

        try (MockedStatic<DatabaseManager> staticDm = Mockito.mockStatic(DatabaseManager.class)) {
            staticDm.when(DatabaseManager::getInstance).thenReturn(dm);
            MemberDAO dao = new MemberDAO();
            assertTrue(dao.delete(1));
        }
    }

    @Test
    @DisplayName("delete() returns false when member ID does not exist")
    void delete_returnsFalseWhenNotFound() throws Exception {
        Connection conn = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);
        when(conn.prepareStatement(anyString())).thenReturn(ps);
        when(ps.executeUpdate()).thenReturn(0);

        DatabaseManager dm = mock(DatabaseManager.class);
        when(dm.getConnection()).thenReturn(conn);

        try (MockedStatic<DatabaseManager> staticDm = Mockito.mockStatic(DatabaseManager.class)) {
            staticDm.when(DatabaseManager::getInstance).thenReturn(dm);
            MemberDAO dao = new MemberDAO();
            assertFalse(dao.delete(999));
        }
    }

    // ── search() ──────────────────────────────────────────────────────────────

    @Test
    @DisplayName("search() binds LIKE pattern for name and email columns")
    void search_bindsCorrectPatterns() throws Exception {
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
            MemberDAO dao = new MemberDAO();
            List<Member> results = dao.search("alice");

            verify(ps).setString(1, "%alice%");
            verify(ps).setString(2, "%alice%");
            assertTrue(results.isEmpty());
        }
    }

    // ── Member model ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("Member.getSummary() contains ID, name, email and type")
    void member_getSummaryContainsAllFields() {
        String summary = SAMPLE_MEMBER.getSummary();
        assertTrue(summary.contains("Alice Smith"));
        assertTrue(summary.contains("alice@example.com"));
        assertTrue(summary.contains("Student"));
    }

    @Test
    @DisplayName("Member.getId() returns memberId")
    void member_getIdReturnsMemberId() {
        assertEquals(1, SAMPLE_MEMBER.getId());
    }
}
