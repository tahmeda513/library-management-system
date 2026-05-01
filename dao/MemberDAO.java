package dao;

import db.DatabaseManager;
import model.Member;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * MemberDAO – Data Access Object for the members table.
 */
public class MemberDAO implements GenericDAO<Member, Integer> {

    private static final Logger LOGGER = Logger.getLogger(MemberDAO.class.getName());

    private Connection getConn() {
        return DatabaseManager.getInstance().getConnection();
    }

    // ── CREATE ─────────────────────────────────────────────────────────────────
    @Override
    public boolean create(Member member) {
        String sql = "INSERT INTO members (member_name, email, membership_type) VALUES (?, ?, ?)";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, member.getMemberName());
            ps.setString(2, member.getEmail());
            ps.setString(3, member.getMembershipType());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error creating member", e);
            return false;
        }
    }

    // ── READ (by ID) ────────────────────────────────────────────────────────────
    @Override
    public Member findById(Integer id) {
        String sql = "SELECT * FROM members WHERE member_id = ?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapRow(rs);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding member", e);
        }
        return null;
    }

    // ── READ (all) ──────────────────────────────────────────────────────────────
    @Override
    public List<Member> findAll() {
        return findAll("member_id", true);
    }

    public List<Member> findAll(String sortColumn, boolean ascending) {
        List<Member> members = new ArrayList<>();
        String dir = ascending ? "ASC" : "DESC";
        String safeCol = switch (sortColumn) {
            case "member_name", "email", "membership_type" -> sortColumn;
            default -> "member_id";
        };
        String sql = "SELECT * FROM members ORDER BY " + safeCol + " " + dir;
        try (Statement st = getConn().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) members.add(mapRow(rs));
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error fetching members", e);
        }
        return members;
    }

    // ── UPDATE ─────────────────────────────────────────────────────────────────
    @Override
    public boolean update(Member member) {
        String sql = "UPDATE members SET member_name=?, email=?, membership_type=? WHERE member_id=?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, member.getMemberName());
            ps.setString(2, member.getEmail());
            ps.setString(3, member.getMembershipType());
            ps.setInt(4, member.getMemberId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error updating member", e);
            return false;
        }
    }

    // ── DELETE ─────────────────────────────────────────────────────────────────
    @Override
    public boolean delete(Integer id) {
        String sql = "DELETE FROM members WHERE member_id = ?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error deleting member", e);
            return false;
        }
    }

    // ── SEARCH ─────────────────────────────────────────────────────────────────

    /** Searches by name or email. */
    public List<Member> search(String keyword) {
        List<Member> members = new ArrayList<>();
        String sql = "SELECT * FROM members WHERE LOWER(member_name) LIKE ? OR LOWER(email) LIKE ?";
        String pattern = "%" + keyword.toLowerCase() + "%";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, pattern);
            ps.setString(2, pattern);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) members.add(mapRow(rs));
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error searching members", e);
        }
        return members;
    }

    /** Filters by membership type (Student / Staff). */
    public List<Member> filterByType(String type) {
        List<Member> members = new ArrayList<>();
        String sql = "SELECT * FROM members WHERE LOWER(membership_type) = LOWER(?)";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, type);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) members.add(mapRow(rs));
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error filtering members", e);
        }
        return members;
    }

    // ── Helper ─────────────────────────────────────────────────────────────────
    private Member mapRow(ResultSet rs) throws SQLException {
        return new Member(
            rs.getInt("member_id"),
            rs.getString("member_name"),
            rs.getString("email"),
            rs.getString("membership_type")
        );
    }
}
