package com.kafepos;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/** Pengaturan per kafe (tabel pengaturan: kafe_id, kunci, nilai). */
public class SettingsDAO {

    public static final String CAFE_NAME = "nama_kafe";
    public static final String CAFE_PHONE = "telepon";
    public static final String CAFE_ADDRESS = "alamat";
    public static final String TAX_RATE = "pajak_persen";
    public static final String SERVICE_RATE = "service_persen";

    /** Tarif pajak dan service charge dalam persen. */
    public record Rates(BigDecimal tax, BigDecimal service) {}

    public Map<String, String> loadAll(int kafeId) throws SQLException {
        Map<String, String> map = new HashMap<>();
        String sql = "SELECT kunci, nilai FROM pengaturan WHERE kafe_id = ?";
        try (Connection conn = Db.open();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, kafeId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    map.put(rs.getString(1), rs.getString(2));
                }
            }
        }
        return map;
    }

    public void saveAll(int kafeId, Map<String, String> values) throws SQLException {
        String sql = "INSERT INTO pengaturan (kafe_id, kunci, nilai) VALUES (?, ?, ?) "
                + "ON DUPLICATE KEY UPDATE nilai = VALUES(nilai)";
        try (Connection conn = Db.open()) {
            conn.setAutoCommit(false);
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                for (Map.Entry<String, String> e : values.entrySet()) {
                    stmt.setInt(1, kafeId);
                    stmt.setString(2, e.getKey());
                    stmt.setString(3, e.getValue());
                    stmt.addBatch();
                }
                stmt.executeBatch();
                conn.commit();
            } catch (SQLException ex) {
                conn.rollback();
                throw ex;
            }
        }
    }

    /** Pajak dan service charge; 0 bila belum diatur. */
    public Rates rates(int kafeId) throws SQLException {
        Map<String, String> m = loadAll(kafeId);
        return new Rates(parse(m.get(TAX_RATE)), parse(m.get(SERVICE_RATE)));
    }

    private static BigDecimal parse(String s) {
        try {
            return s == null || s.isBlank() ? BigDecimal.ZERO : new BigDecimal(s.trim());
        } catch (NumberFormatException e) {
            return BigDecimal.ZERO;
        }
    }
}
