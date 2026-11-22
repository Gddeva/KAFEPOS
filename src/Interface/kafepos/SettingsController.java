package com.kafepos;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

/**
 * Halaman Settings. Profil kafe, pajak, dan service charge hanya bisa diubah
 * owner/admin (kasir hanya melihat). Ganti password tersedia untuk semua role.
 */
public class SettingsController {

    private static final String OK_STYLE = "-fx-font-size: 12px; -fx-text-fill: #3D8054;";
    private static final String ERROR_STYLE = "-fx-font-size: 12px; -fx-text-fill: #B44F43;";

    @FXML private VBox profileCard;
    @FXML private TextField cafeNameField;
    @FXML private TextField phoneField;
    @FXML private TextField addressField;
    @FXML private TextField taxField;
    @FXML private TextField serviceField;
    @FXML private Button saveButton;
    @FXML private Button reloadButton;
    @FXML private Label statusLabel;
    @FXML private PasswordField oldPassField;
    @FXML private PasswordField newPassField;
    @FXML private PasswordField confirmPassField;
    @FXML private Label passwordStatusLabel;

    private final SettingsDAO settingsDao = new SettingsDAO();
    private final UserDAO userDao = new UserDAO();

    private int userId;
    private int kafeId;
    private boolean canEdit;

    /** Dipanggil MainController setelah halaman dimuat. */
    public void setSession(int userId, int kafeId, String role) {
        this.userId = userId;
        this.kafeId = kafeId;
        this.canEdit = "owner".equals(role) || "admin".equals(role);

        profileCard.setDisable(!canEdit);
        taxField.setDisable(!canEdit);
        serviceField.setDisable(!canEdit);
        saveButton.setDisable(!canEdit);
        reloadButton.setDisable(!canEdit);
        if (!canEdit) {
            show(statusLabel, "Hanya owner atau admin yang dapat mengubah pengaturan.", false);
        }

        loadSettings();
    }

    @FXML
    private void handleReload() {
        loadSettings();
        if (canEdit) {
            show(statusLabel, "", true);
        }
    }

    private void loadSettings() {
        try {
            Map<String, String> m = settingsDao.loadAll(kafeId);
            cafeNameField.setText(m.getOrDefault(SettingsDAO.CAFE_NAME, ""));
            phoneField.setText(m.getOrDefault(SettingsDAO.CAFE_PHONE, ""));
            addressField.setText(m.getOrDefault(SettingsDAO.CAFE_ADDRESS, ""));
            taxField.setText(m.getOrDefault(SettingsDAO.TAX_RATE, "0"));
            serviceField.setText(m.getOrDefault(SettingsDAO.SERVICE_RATE, "0"));
        } catch (SQLException e) {
            show(statusLabel, "Gagal memuat pengaturan: " + e.getMessage(), false);
        }
    }

    @FXML
    private void handleSave() {
        if (!canEdit) {
            show(statusLabel, "Hanya owner atau admin yang dapat mengubah pengaturan.", false);
            return;
        }
        if (cafeNameField.getText().isBlank()) {
            show(statusLabel, "Nama kafe wajib diisi.", false);
            return;
        }

        BigDecimal tax;
        BigDecimal service;
        try {
            tax = parsePercent(taxField.getText());
            service = parsePercent(serviceField.getText());
        } catch (NumberFormatException e) {
            show(statusLabel, "Pajak dan service harus berupa angka 0 - 100.", false);
            return;
        }

        Map<String, String> values = new LinkedHashMap<>();
        values.put(SettingsDAO.CAFE_NAME, cafeNameField.getText().trim());
        values.put(SettingsDAO.CAFE_PHONE, phoneField.getText().trim());
        values.put(SettingsDAO.CAFE_ADDRESS, addressField.getText().trim());
        values.put(SettingsDAO.TAX_RATE, tax.stripTrailingZeros().toPlainString());
        values.put(SettingsDAO.SERVICE_RATE, service.stripTrailingZeros().toPlainString());

        try {
            settingsDao.saveAll(kafeId, values);
            taxField.setText(values.get(SettingsDAO.TAX_RATE));
            serviceField.setText(values.get(SettingsDAO.SERVICE_RATE));
            show(statusLabel, "Pengaturan tersimpan. Berlaku untuk pesanan baru.", true);
        } catch (SQLException e) {
            show(statusLabel, "Gagal menyimpan: " + e.getMessage(), false);
        }
    }

    @FXML
    private void handleChangePassword() {
        String oldPass = oldPassField.getText();
        String newPass = newPassField.getText();

        if (oldPass.isEmpty()) {
            show(passwordStatusLabel, "Masukkan password saat ini.", false);
            return;
        }
        if (newPass.length() < 6) {
            show(passwordStatusLabel, "Password baru minimal 6 karakter.", false);
            return;
        }
        if (!newPass.equals(confirmPassField.getText())) {
            show(passwordStatusLabel, "Konfirmasi password tidak sama.", false);
            return;
        }

        try {
            String stored = userDao.getPasswordHash(userId);
            if (stored == null || !PasswordUtil.verify(oldPass, stored)) {
                show(passwordStatusLabel, "Password saat ini salah.", false);
                return;
            }
            userDao.setPassword(userId, kafeId, newPass);
            oldPassField.clear();
            newPassField.clear();
            confirmPassField.clear();
            show(passwordStatusLabel, "Password berhasil diperbarui.", true);
        } catch (SQLException e) {
            show(passwordStatusLabel, "Gagal: " + e.getMessage(), false);
        }
    }

    private BigDecimal parsePercent(String text) {
        BigDecimal value = new BigDecimal(text.trim().replace(',', '.'));
        if (value.signum() < 0 || value.compareTo(BigDecimal.valueOf(100)) > 0) {
            throw new NumberFormatException("out of range");
        }
        return value;
    }

    private void show(Label label, String text, boolean ok) {
        label.setStyle(ok ? OK_STYLE : ERROR_STYLE);
        label.setText(text);
    }
}
