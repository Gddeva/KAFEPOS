
package com.kafepos;

import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Base64;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class LoginController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label errorLabel;

    @FXML
    private void handleLogin() {

        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            errorLabel.setText(
                "Please enter your username and password."
            );
            return;
        }

        String sql = """
            SELECT id, kafe_id, nama, role, password
            FROM users
            WHERE username = ? AND aktif = TRUE
            """;

        try (Connection conn = DatabaseConnection.getConnection()) {

            if (conn == null) {
                errorLabel.setText("Database connection failed!");
                return;
            }

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setString(1, username);

                try (ResultSet rs = stmt.executeQuery()) {

                    if (!rs.next()) {
                        errorLabel.setText(
                            "Invalid username or password."
                        );
                        return;
                    }

                    String storedPassword = rs.getString("password");

                    if (!verifyPassword(password, storedPassword)) {
                        errorLabel.setText(
                            "Invalid username or password."
                        );
                        return;
                    }

                    int userId = rs.getInt("id");
                    int kafeId = rs.getInt("kafe_id");
                    String nama = rs.getString("nama");
                    String role = rs.getString("role");

                    // Load halaman utama
                    FXMLLoader loader = new FXMLLoader(
                        getClass().getResource("/fxml/MainView.fxml")
                    );

                    Scene scene = new Scene(loader.load());

                    // Kirim informasi user ke MainController
                    MainController mainController = loader.getController();

                    mainController.setUserSession(
                        userId,
                        kafeId,
                        nama,
                        role
                    );

                    Stage stage = (Stage) usernameField
                        .getScene().getWindow();

                    stage.setScene(scene);
                    stage.setTitle("KAFEPOS - Maison Café");
                    stage.centerOnScreen();

                    System.out.println("LOGIN BERHASIL!");
                    System.out.println("User: " + nama);
                    System.out.println("Role: " + role);
                    System.out.println("Kafe ID: " + kafeId);
                }
            }

        } catch (Exception e) {
            errorLabel.setText("Login error. Please try again.");
            e.printStackTrace();
        }
    }

    private boolean verifyPassword(
        String password,
        String storedPassword
    ) {

        try {
            String[] parts = storedPassword.split("\\$");

            if (parts.length != 4 ||
                !parts[0].equals("pbkdf2")) {
                return false;
            }

            int iterations = Integer.parseInt(parts[1]);

            byte[] salt = Base64.getDecoder().decode(parts[2]);
            byte[] expectedHash = Base64.getDecoder().decode(parts[3]);

            PBEKeySpec spec = new PBEKeySpec(
                password.toCharArray(),
                salt,
                iterations,
                expectedHash.length * 8
            );

            byte[] actualHash = SecretKeyFactory
                .getInstance("PBKDF2WithHmacSHA256")
                .generateSecret(spec)
                .getEncoded();

            spec.clearPassword();

            return MessageDigest.isEqual(
                expectedHash,
                actualHash
            );

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}