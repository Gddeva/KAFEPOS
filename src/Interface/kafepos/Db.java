package com.kafepos;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Pembungkus DatabaseConnection. DatabaseConnection.getConnection() mengembalikan null
 * kalau MySQL mati, sehingga pemanggil langsung kena NullPointerException.
 * Db.open() melempar SQLException dengan pesan yang jelas, sehingga bisa
 * ditangkap oleh blok catch (SQLException) yang sudah ada.
 */
public final class Db {

    private Db() {}

    public static Connection open() throws SQLException {
        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) {
            throw new SQLException(
                    "Tidak dapat terhubung ke database. Pastikan MySQL berjalan.");
        }
        return conn;
    }
}
