
package com.kafepos;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {

    private static final String URL =
            "jdbc:mysql://127.0.0.1:3306/kasir_cafe";

    private static final String USER = "root";
    private static final String PASSWORD = "";

    public static Connection getConnection() {

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");

            Connection conn = DriverManager.getConnection(
                URL, USER, PASSWORD
            );

            System.out.println("Koneksi MySQL berhasil!");

            return conn;

        } catch (SQLException | ClassNotFoundException e) {

            System.out.println("Koneksi gagal: " + e.getMessage());

            return null;
        }
    }
}