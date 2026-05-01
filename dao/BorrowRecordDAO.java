package dao;

import db.DatabaseManager;
import model.BorrowRecord;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * BorrowRecordDAO – Data Access Object for the borrow_records table.
 * Uses JOIN queries to populate display fields (bookTitle, memberName).
 */
public class BorrowRecordDAO implements GenericDAO<BorrowRecord, Integer> {

    private static final Logger LOGGER = Logger.getLogger(BorrowRecordDAO.class.getName());

    private Connection getConn() {
        return DatabaseManager.getInstance().getConnection();
    }

    private static final String SELECT_WITH_JOINS =
        "SELECT r.*, b.title AS book_title, m.member_name " +
        "FROM borrow_records r " +
        "JOIN books b ON r.book_id = b.book_id " +
        "JOIN members m ON r.member_id = m.member_id ";

    // ── CREATE ─────────────────────────────────────────────────────────────────
    @Override
    public boolean create(BorrowRecord record) {
        String sql = "INSERT INTO borrow_records (book_id, member_id, borrow_date, due_date, return_status) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, record.getBookId());
            ps.setInt(2, record.getMemberId());
            ps.setString(3, record.getBorrowDate());
            ps.setString(4, record.getDueDate());
            ps.setString(5, record.getReturnStatus());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error creating borrow record", e);
            return false;
        }
    }

    // ── READ (by ID) ────────────────────────────────────────────────────────────
    @Override
    public BorrowRecord findById(Integer id) {
        String sql = SELECT_WITH_JOINS + "WHERE r.record_id = ?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapRow(rs);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding borrow record", e);
        }
        return null;
    }

    // ── READ (all) ──────────────────────────────────────────────────────────────
    @Override
    public List<BorrowRecord> findAll() {
        List<BorrowRecord> records = new ArrayList<>();
        String sql = SELECT_WITH_JOINS + "ORDER BY r.record_id ASC";
        try (Statement st = getConn().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) records.add(mapRow(rs));
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error fetching borrow records", e);
        }
        return records;
    }

    // ── UPDATE ─────────────────────────────────────────────────────────────────
    @Override
    public boolean update(BorrowRecord record) {
        String sql = "UPDATE borrow_records SET book_id=?, member_id=?, borrow_date=?, due_date=?, return_status=? WHERE record_id=?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, record.getBookId());
            ps.setInt(2, record.getMemberId());
            ps.setString(3, record.getBorrowDate());
            ps.setString(4, record.getDueDate());
            ps.setString(5, record.getReturnStatus());
            ps.setInt(6, record.getRecordId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error updating borrow record", e);
            return false;
        }
    }

    // ── DELETE ─────────────────────────────────────────────────────────────────
    @Override
    public boolean delete(Integer id) {
        String sql = "DELETE FROM borrow_records WHERE record_id = ?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error deleting borrow record", e);
            return false;
        }
    }

    // ── ADVANCED QUERIES ────────────────────────────────────────────────────────

    /** Returns all records for a specific member. */
    public List<BorrowRecord> findByMember(int memberId) {
        List<BorrowRecord> records = new ArrayList<>();
        String sql = SELECT_WITH_JOINS + "WHERE r.member_id = ?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, memberId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) records.add(mapRow(rs));
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding records by member", e);
        }
        return records;
    }

    /** Returns all records for a specific book. */
    public List<BorrowRecord> findByBook(int bookId) {
        List<BorrowRecord> records = new ArrayList<>();
        String sql = SELECT_WITH_JOINS + "WHERE r.book_id = ?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, bookId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) records.add(mapRow(rs));
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding records by book", e);
        }
        return records;
    }

    /** Returns all records where today's date is past the due date and status != Returned. */
    public List<BorrowRecord> findOverdue() {
        List<BorrowRecord> records = new ArrayList<>();
        String today = LocalDate.now().toString();
        String sql = SELECT_WITH_JOINS +
            "WHERE r.return_status != 'Returned' AND r.due_date < ?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, today);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) records.add(mapRow(rs));
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding overdue records", e);
        }
        return records;
    }

    /** Filters records by borrow date range. */
    public List<BorrowRecord> findByDateRange(String fromDate, String toDate) {
        List<BorrowRecord> records = new ArrayList<>();
        String sql = SELECT_WITH_JOINS + "WHERE r.borrow_date BETWEEN ? AND ? ORDER BY r.borrow_date";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, fromDate);
            ps.setString(2, toDate);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) records.add(mapRow(rs));
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error filtering by date range", e);
        }
        return records;
    }

    /** Filters records by status. */
    public List<BorrowRecord> findByStatus(String status) {
        List<BorrowRecord> records = new ArrayList<>();
        String sql = SELECT_WITH_JOINS + "WHERE r.return_status = ?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, status);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) records.add(mapRow(rs));
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error filtering records by status", e);
        }
        return records;
    }

    // ── Helper ─────────────────────────────────────────────────────────────────
    private BorrowRecord mapRow(ResultSet rs) throws SQLException {
        BorrowRecord record = new BorrowRecord(
            rs.getInt("record_id"),
            rs.getInt("book_id"),
            rs.getInt("member_id"),
            rs.getString("borrow_date"),
            rs.getString("due_date"),
            rs.getString("return_status")
        );
        try { record.setBookTitle(rs.getString("book_title")); } catch (SQLException ignored) {}
        try { record.setMemberName(rs.getString("member_name")); } catch (SQLException ignored) {}
        return record;
    }
}
