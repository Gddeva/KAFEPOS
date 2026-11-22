package com.kafepos;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;

/** Halaman Users: tambah, edit, hapus, reset password, aktif/nonaktif. */
public class UsersController {

    private static final String ROLE_OWNER = "owner";
    private static final String ROLE_ADMIN = "admin";
    private static final String ROLE_CASHIER = "kasir";

    @FXML
    private TableView<User> userTable;
    @FXML
    private TableColumn<User, String> idColumn;
    @FXML
    private TableColumn<User, String> nameColumn;
    @FXML
    private TableColumn<User, String> usernameColumn;
    @FXML
    private TableColumn<User, String> roleColumn;
    @FXML
    private TableColumn<User, String> statusColumn;
    @FXML
    private TextField searchField;
    @FXML
    private ComboBox<String> roleFilter;
    @FXML
    private Label totalLabel;
    @FXML
    private Label activeLabel;
    @FXML
    private Label cashierLabel;
    @FXML
    private Button editButton;
    @FXML
    private Button resetButton;
    @FXML
    private Button deleteButton;

    private final UserDAO dao = new UserDAO();
    private final ObservableList<User> users = FXCollections.observableArrayList();
    private FilteredList<User> filtered;

    private int currentUserId;
    private int kafeId;
    private String currentRole = "";

    @FXML
    public void initialize() {
        idColumn.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().getId())));
        nameColumn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getNama()));
        usernameColumn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getUsername()));
        roleColumn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getRole()));
        statusColumn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().isAktif() ? "Active" : "Inactive"));

        roleColumn.setCellFactory(col -> badgeCell(
                "-fx-background-color: #E9DFD5; -fx-text-fill: #5B4030;"));
        statusColumn.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) {
                    setGraphic(null);
                    return;
                }
                boolean active = status.equals("Active");
                setGraphic(badge(status, active
                        ? "-fx-background-color: #E4F2E8; -fx-text-fill: #3D8054;"
                        : "-fx-background-color: #ECE9E6; -fx-text-fill: #7A726B;"));
            }
        });

        roleFilter.setItems(FXCollections.observableArrayList(
                "All Roles", ROLE_OWNER, ROLE_ADMIN, ROLE_CASHIER));
        roleFilter.setValue("All Roles");

        userTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        filtered = new FilteredList<>(users, u -> true);
        userTable.setItems(filtered);

        searchField.textProperty().addListener((o, a, b) -> applyFilter());
        roleFilter.valueProperty().addListener((o, a, b) -> applyFilter());
        userTable.getSelectionModel().selectedItemProperty()
                .addListener((o, a, b) -> updateButtons());
        userTable.setRowFactory(tv -> {
            javafx.scene.control.TableRow<User> row = new javafx.scene.control.TableRow<>();
            row.setOnMouseClicked(e -> {
                if (e.getClickCount() == 2 && !row.isEmpty()) {
                    handleEdit();
                }
            });
            return row;
        });

        updateButtons();
    }

    public void setSession(int userId, int kafeId, String role) {
        this.currentUserId = userId;
        this.kafeId = kafeId;
        this.currentRole = role == null ? "" : role;
        loadUsers();
    }

    private void loadUsers() {
        try {
            List<User> list = dao.findAll(kafeId);
            users.setAll(list);
            totalLabel.setText(String.valueOf(list.size()));
            activeLabel.setText(String.valueOf(list.stream().filter(User::isAktif).count()));
            cashierLabel.setText(String.valueOf(
                    list.stream().filter(u -> ROLE_CASHIER.equals(u.getRole())).count()));
        } catch (SQLException e) {
            error("Gagal memuat user", e);
        }
    }

    private void applyFilter() {
        String q = searchField.getText() == null
                ? ""
                : searchField.getText().trim().toLowerCase();
        String role = roleFilter.getValue();
        filtered.setPredicate(u -> (role == null || role.equals("All Roles") || role.equals(u.getRole()))
                && (q.isEmpty()
                        || u.getNama().toLowerCase().contains(q)
                        || u.getUsername().toLowerCase().contains(q)));
    }

    private void updateButtons() {
        User selected = userTable.getSelectionModel().getSelectedItem();
        boolean disabled = selected == null || !canManage(selected);
        editButton.setDisable(disabled);
        resetButton.setDisable(disabled);
        deleteButton.setDisable(disabled);
    }

    private boolean canManage(User target) {
        return ROLE_OWNER.equals(currentRole) || !ROLE_OWNER.equals(target.getRole());
    }

    @FXML
    private void handleAdd() {
        showUserDialog(null);
    }

    @FXML
    private void handleEdit() {
        User selected = userTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            alert(Alert.AlertType.WARNING, "No User Selected", "Pilih user terlebih dahulu.");
            return;
        }
        if (!canManage(selected)) {
            alert(Alert.AlertType.WARNING, "Not Allowed",
                    "Hanya owner yang dapat mengubah akun owner.");
            return;
        }
        showUserDialog(selected);
    }

    @FXML
    private void handleDelete() {
        User selected = userTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            alert(Alert.AlertType.WARNING, "No User Selected", "Pilih user terlebih dahulu.");
            return;
        }
        if (selected.getId() == currentUserId) {
            alert(Alert.AlertType.WARNING, "Not Allowed",
                    "Anda tidak dapat menghapus akun yang sedang digunakan.");
            return;
        }
        try {
            if (isLastActiveManager(selected)) {
                alert(Alert.AlertType.WARNING, "Not Allowed",
                        "Minimal harus ada satu owner/admin aktif.");
                return;
            }
        } catch (SQLException e) {
            error("Gagal memeriksa user", e);
            return;
        }

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Delete User");
        confirmation.setHeaderText("Hapus " + selected.getNama() + "?");
        confirmation.setContentText(
                "Akun akan dihapus permanen. Untuk menonaktifkan sementara, "
                        + "gunakan Edit User lalu hilangkan centang Active.");

        Optional<ButtonType> result = confirmation.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                dao.delete(selected.getId(), kafeId);
                loadUsers();
            } catch (SQLException e) {
                error("Gagal menghapus user", e);
            }
        }
    }

    @FXML
    private void handleResetPassword() {
        User selected = userTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            alert(Alert.AlertType.WARNING, "No User Selected", "Pilih user terlebih dahulu.");
            return;
        }
        if (!canManage(selected)) {
            alert(Alert.AlertType.WARNING, "Not Allowed",
                    "Hanya owner yang dapat mereset password owner.");
            return;
        }

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Reset Password");
        dialog.setHeaderText("Password baru untuk " + selected.getUsername());

        PasswordField pass = new PasswordField();
        pass.setPromptText("New password (min 6 characters)");
        PasswordField confirm = new PasswordField();
        confirm.setPromptText("Confirm password");

        GridPane grid = grid();
        grid.add(new Label("New Password:"), 0, 0);
        grid.add(pass, 1, 0);
        grid.add(new Label("Confirm:"), 0, 1);
        grid.add(confirm, 1, 1);
        dialog.getDialogPane().setContent(grid);

        ButtonType save = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(save, ButtonType.CANCEL);

        Button saveNode = (Button) dialog.getDialogPane().lookupButton(save);
        saveNode.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            String message = validatePassword(pass.getText(), confirm.getText());
            if (message != null) {
                alert(Alert.AlertType.WARNING, "Validation", message);
                event.consume();
            }
        });

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == save) {
            try {
                dao.setPassword(selected.getId(), kafeId, pass.getText());
                alert(Alert.AlertType.INFORMATION, "Success", "Password berhasil diperbarui.");
            } catch (SQLException e) {
                error("Gagal mereset password", e);
            }
        }
    }

    // ----------------------------------------------------------- DIALOG

    private void showUserDialog(User existing) {
        boolean isEdit = existing != null;

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle(isEdit ? "Edit User" : "Add User");
        dialog.setHeaderText(isEdit ? "Update user information" : "Create a new staff account");

        TextField nameField = new TextField(isEdit ? existing.getNama() : "");
        nameField.setPromptText("Full name");
        TextField usernameField = new TextField(isEdit ? existing.getUsername() : "");
        usernameField.setPromptText("Username");
        PasswordField passField = new PasswordField();
        passField.setPromptText("Password (min 6 characters)");
        PasswordField confirmField = new PasswordField();
        confirmField.setPromptText("Confirm password");

        ComboBox<String> roleCombo = new ComboBox<>();
        if (ROLE_OWNER.equals(currentRole)) {
            roleCombo.getItems().add(ROLE_OWNER);
        }
        roleCombo.getItems().addAll(ROLE_ADMIN, ROLE_CASHIER);
        roleCombo.setValue(isEdit && roleCombo.getItems().contains(existing.getRole())
                ? existing.getRole()
                : ROLE_CASHIER);
        roleCombo.setPrefWidth(220);

        CheckBox activeBox = new CheckBox("Active");
        activeBox.setSelected(!isEdit || existing.isAktif());

        GridPane grid = grid();
        int row = 0;
        grid.add(new Label("Full Name:"), 0, row);
        grid.add(nameField, 1, row++);
        grid.add(new Label("Username:"), 0, row);
        grid.add(usernameField, 1, row++);
        if (!isEdit) {
            grid.add(new Label("Password:"), 0, row);
            grid.add(passField, 1, row++);
            grid.add(new Label("Confirm:"), 0, row);
            grid.add(confirmField, 1, row++);
        }
        grid.add(new Label("Role:"), 0, row);
        grid.add(roleCombo, 1, row++);
        grid.add(new Label("Status:"), 0, row);
        grid.add(activeBox, 1, row);
        dialog.getDialogPane().setContent(grid);

        ButtonType save = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(save, ButtonType.CANCEL);

        Button saveNode = (Button) dialog.getDialogPane().lookupButton(save);
        saveNode.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            String message = validateUser(existing, nameField.getText(), usernameField.getText(),
                    passField.getText(), confirmField.getText(), isEdit,
                    roleCombo.getValue(), activeBox.isSelected());
            if (message != null) {
                alert(Alert.AlertType.WARNING, "Validation", message);
                event.consume();
            }
        });

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isEmpty() || result.get() != save) {
            return;
        }

        try {
            User u = isEdit ? existing : new User();
            u.setKafeId(kafeId);
            u.setNama(nameField.getText().trim());
            u.setUsername(usernameField.getText().trim());
            u.setRole(roleCombo.getValue());
            u.setAktif(activeBox.isSelected());

            if (isEdit) {
                dao.update(u);
            } else {
                dao.insert(u, passField.getText());
            }
            loadUsers();
        } catch (SQLException e) {
            error("Gagal menyimpan user", e);
        }
    }

    private String validateUser(User existing, String nama, String username, String pass,
            String confirm, boolean isEdit, String role, boolean aktif) {
        if (nama.isBlank()) {
            return "Nama lengkap wajib diisi.";
        }
        if (!username.trim().matches("[A-Za-z0-9._-]{3,30}")) {
            return "Username 3-30 karakter: huruf, angka, titik, garis bawah, atau strip.";
        }
        if (!isEdit) {
            String message = validatePassword(pass, confirm);
            if (message != null) {
                return message;
            }
        }
        try {
            if (dao.usernameExists(username.trim(), isEdit ? existing.getId() : 0)) {
                return "Username sudah dipakai.";
            }
            if (isEdit && existing.getId() == currentUserId && !aktif) {
                return "Anda tidak dapat menonaktifkan akun yang sedang digunakan.";
            }
            if (isEdit && existing.getId() == currentUserId && !role.equals(existing.getRole())) {
                return "Anda tidak dapat mengubah role akun Anda sendiri.";
            }
            if (isEdit && isLastActiveManager(existing)
                    && (!aktif || !(ROLE_OWNER.equals(role) || ROLE_ADMIN.equals(role)))) {
                return "Minimal harus ada satu owner/admin aktif.";
            }
        } catch (SQLException e) {
            return "Database error: " + e.getMessage();
        }
        return null;
    }

    private String validatePassword(String pass, String confirm) {
        if (pass.length() < 6) {
            return "Password minimal 6 karakter.";
        }
        if (!pass.equals(confirm)) {
            return "Konfirmasi password tidak sama.";
        }
        return null;
    }

    private boolean isLastActiveManager(User u) throws SQLException {
        return u.isAktif()
                && (ROLE_OWNER.equals(u.getRole()) || ROLE_ADMIN.equals(u.getRole()))
                && dao.countActiveManagers(kafeId) <= 1;
    }

    private GridPane grid() {
        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(12);
        grid.setPadding(new Insets(15));
        return grid;
    }

    private TableCell<User, String> badgeCell(String style) {
        return new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty || item == null ? null : badge(item, style));
            }
        };
    }

    private Label badge(String text, String colors) {
        Label label = new Label(text);
        label.setAlignment(Pos.CENTER);
        label.setStyle("-fx-padding: 5 12; -fx-background-radius: 20; "
                + "-fx-font-size: 11px; -fx-font-weight: bold; " + colors);
        return label;
    }

    private void alert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void error(String title, Exception e) {
        e.printStackTrace();
        alert(Alert.AlertType.ERROR, "Database Error", title + ":\n" + e.getMessage());
    }
}
