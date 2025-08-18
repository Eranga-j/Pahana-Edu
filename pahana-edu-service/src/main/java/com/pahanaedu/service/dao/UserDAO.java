package com.pahanaedu.service.dao;
import com.pahanaedu.service.db.Database; import com.pahanaedu.service.model.User; import java.sql.*;
public class UserDAO {
    public User validateLogin(String username, String passwordHash) throws SQLException {
        String sql = "SELECT id, username, role FROM users WHERE username = ? AND password_hash = ?";
        try (Connection con = Database.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, username); ps.setString(2, passwordHash);
            try (ResultSet rs = ps.executeQuery()) { if (rs.next()) return new User(rs.getInt(1), rs.getString(2), rs.getString(3)); }
            return null;
        }
    }
    public String getSaltForUser(String username) throws SQLException {
        try (Connection con = Database.getConnection(); PreparedStatement ps = con.prepareStatement("SELECT salt FROM users WHERE username=?")) {
            ps.setString(1, username); try (ResultSet rs = ps.executeQuery()) { return rs.next() ? rs.getString(1) : null; }
        }
    }
}
