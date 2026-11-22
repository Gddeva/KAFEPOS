
package com.kafepos;

import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Base64;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public class CreateOwnerAccount {

    private static final String PASSWORD = "maisonowner123";
    private static final int ITERATIONS = 120000;

    public static void main(String[] args) {

        try (Connection conn = DatabaseConnection.getConnection()) {

            if (conn == null) {
                System.out.println("Database connection failed!");
                return;
            }

            // Cek apakah username sudah ada
            String checkSql = "SELECT id FROM users WHERE kafe_id = 1 AND username = 'owner'";

            try (PreparedStatement check = conn.prepareStatement(checkSql);
                    ResultSet rs = check.executeQuery()) {

                if (rs.next()) {
                    System.out.println("Akun Owner sudah tersedia!");
                    return;
                }
            }

            // Membuat password hash
            byte[] salt = new byte[16];
            new SecureRandom().nextBytes(salt);

            PBEKeySpec spec = new PBEKeySpec(
                    PASSWORD.toCharArray(),
                    salt,
                    ITERATIONS,
                    256);

            byte[] hash = SecretKeyFactory
                    .getInstance("PBKDF2WithHmacSHA256")
                    .generateSecret(spec)
                    .getEncoded();

            spec.clearPassword();

            String hashedPassword = "pbkdf2$" + ITERATIONS + "$"
                    + Base64.getEncoder().encodeToString(salt)
                    + "$"
                    + Base64.getEncoder().encodeToString(hash);

            // Simpan akun Owner
            String sql = """
                    INSERT INTO users
                    (kafe_id, username, password, nama, role, aktif)
                    VALUES (?, ?, ?, ?, ?, ?)
                    """;

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setInt(1, 1);
                stmt.setString(2, "owner");
                stmt.setString(3, hashedPassword);
                stmt.setString(4, "Owner Maison Café");
                stmt.setString(5, "owner");
                stmt.setBoolean(6, true);

                stmt.executeUpdate();
            }

            System.out.println("================================");
            System.out.println("OWNER ACCOUNT BERHASIL DIBUAT!");
            System.out.println("Username: owner");
            System.out.println("Password: " + PASSWORD);
            System.out.println("Role: owner");
            System.out.println("================================");

        } catch (Exception e) {
            System.out.println("Gagal membuat akun Owner!");
            e.printStackTrace();
        }
    }
}