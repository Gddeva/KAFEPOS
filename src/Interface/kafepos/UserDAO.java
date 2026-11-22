package com.kafepos;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/** CRUD tabel users. Password disimpan dengan format PasswordUtil (pbkdf2$...). */
public class UserDAO {

    public List<User> findAll(int kafeId) throws SQLException {
        String sql = "SELECT id, kafe_id, username, nama, role, aktif "
                + "FROM users WHERE kafe_id = ? ORDER BY nama ASC";
        List<User> list = new ArrayList<>();
        try (Connection conn = Db.open();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, kafeId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    User u = new User();
                    u.setId(rs.getInt("id"));
                    u.setKafeId(rs.getInt("kafe_id"));
                    u.setUsername(rs.getString("username"));
                    u.setNama(rs.getString("nama"));
                    u.setRole(rs.getString("role"));
                    u.setAktif(rs.getBoolean("aktif"));
                    list.add(u);
                }
            }
        }
        return list;
    }

    /**
     * Username dicek di SEMUA kafe, karena LoginController mencari user
     * hanya dari username (tanpa kafe_id).
     */
    public boolean usernameExists(String username, int exceptId) throws SQLException {
        String sql = "SELECT 1 FROM users WHERE username = ? AND id <> ?";
        try (Connection conn = Db.open();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            stmt.setInt(2, exceptId);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    public void insert(User u, String plainPassword) throws SQLException {
        String sql = "INSERT INTO users (kafe_id, username, password, nama, role, aktif) "
                + "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = Db.open();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, u.getKafeId());
            stmt.setString(2, u.getUsername());
            stmt.setString(3, PasswordUtil.hash(plainPassword));
            stmt.setString(4, u.getNama());
            stmt.setString(5, u.getRole());
            stmt.setBoolean(6, u.isAktif());
            stmt.executeUpdate();
        }
    }

    public void update(User u) throws SQLException {
        String sql = "UPDATE users SET nama = ?, username = ?, role = ?, aktif = ? "
                + "WHERE id = ? AND kafe_id = ?";
        try (Connection conn = Db.open();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, u.getNama());
            stmt.setString(2, u.getUsername());
            stmt.setString(3, u.getRole());
            stmt.setBoolean(4, u.isAktif());
            stmt.setInt(5, u.getId());
            stmt.setInt(6, u.getKafeId());
            stmt.executeUpdate();
        }
    }

    public void delete(int id, int kafeId) throws SQLException {
        String sql = "DELETE FROM users WHERE id = ? AND kafe_id = ?";
        try (Connection conn = Db.open();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.setInt(2, kafeId);
            stmt.executeUpdate();
        }
    }

    public void setPassword(int id, int kafeId, String plainPassword) throws SQLException {
        String sql = "UPDATE users SET password = ? WHERE id = ? AND kafe_id = ?";
        try (Connection conn = Db.open();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, PasswordUtil.hash(plainPassword));
            stmt.setInt(2, id);
            stmt.setInt(3, kafeId);
            stmt.executeUpdate();
        }
    }

    public String getPasswordHash(int id) throws SQLException {
        String sql = "SELECT password FROM users WHERE id = ?";
        try (Connection conn = Db.open();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? rs.getString(1) : null;
            }
        }
    }

    /** Jumlah owner/admin yang masih aktif (agar tidak ada kafe tanpa pengelola). */
    public int countActiveManagers(int kafeId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM users "
                + "WHERE kafe_id = ? AND role IN ('owner','admin') AND aktif = 1";
        try (Connection conn = Db.open();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, kafeId);
            try (ResultSet rs = stmt.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }
}
