package com.kafepos;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;

public class CategoriesController {

    @FXML
    private TableView<Category> categoryTable;

    @FXML
    private TableColumn<Category, Integer> idColumn;

    @FXML
    private TableColumn<Category, String> nameColumn;

    @FXML
    private TableColumn<Category, Void> actionColumn;

    @FXML
    private TextField searchField;

    private final ObservableList<Category> categoryList =
            FXCollections.observableArrayList();

    private FilteredList<Category> filteredList;

    private int kafeId = 1;

    // MENGATUR SESSION KAFE

    public void setKafeId(int kafeId) {
        this.kafeId = kafeId;
        loadCategories();
    }

    // INITIALIZE

    @FXML
    public void initialize() {

        idColumn.setCellValueFactory(
                new PropertyValueFactory<>("id")
        );

        nameColumn.setCellValueFactory(
                new PropertyValueFactory<>("nama")
        );

        filteredList = new FilteredList<>(
                categoryList,
                category -> true
        );

        categoryTable.setItems(filteredList);

        setupActionColumn();

        loadCategories();
    }

    // LOAD CATEGORY

    private void loadCategories() {

        categoryList.clear();

        String sql =
                "SELECT id, nama FROM kategori "
                + "WHERE kafe_id = ? ORDER BY id ASC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, kafeId);

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {

                categoryList.add(
                        new Category(
                                rs.getInt("id"),
                                rs.getString("nama")
                        )
                );
            }

        } catch (SQLException e) {

            showAlert(
                    Alert.AlertType.ERROR,
                    "Database Error",
                    "Gagal mengambil kategori:\n" + e.getMessage()
            );
        }
    }

    // ADD CATEGORY

    @FXML
    private void handleAddCategory() {

        TextInputDialog dialog = new TextInputDialog();

        dialog.setTitle("Add Category");
        dialog.setHeaderText("Add a new menu category");
        dialog.setContentText("Category name:");

        Optional<String> result = dialog.showAndWait();

        if (result.isPresent()) {

            String nama = result.get().trim();

            if (nama.isEmpty()) {
                showAlert(
                        Alert.AlertType.WARNING,
                        "Invalid Input",
                        "Nama kategori tidak boleh kosong."
                );
                return;
            }

            String sql =
                    "INSERT INTO kategori (kafe_id, nama) VALUES (?, ?)";

            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setInt(1, kafeId);
                stmt.setString(2, nama);

                stmt.executeUpdate();

                loadCategories();

            } catch (SQLException e) {

                showAlert(
                        Alert.AlertType.ERROR,
                        "Database Error",
                        "Gagal menambahkan kategori:\n" + e.getMessage()
                );
            }
        }
    }

    // SEARCH

    @FXML
    private void handleSearch() {

        String keyword = searchField.getText();

        if (keyword == null || keyword.trim().isEmpty()) {

            filteredList.setPredicate(category -> true);

        } else {

            String search = keyword.toLowerCase().trim();

            filteredList.setPredicate(category ->
                    category.getNama().toLowerCase().contains(search)
            );
        }
    }

    // ACTION BUTTONS

    private void setupActionColumn() {

        actionColumn.setCellFactory(column ->
                new TableCell<Category, Void>() {

                    private final Button editButton = new Button("Edit");
                    private final Button deleteButton = new Button("Delete");
                    private final HBox buttons =
                            new HBox(8, editButton, deleteButton);

                    {
                        editButton.setStyle(
                                "-fx-background-color: #8B6B4F;"
                                + "-fx-text-fill: white;"
                                + "-fx-background-radius: 6;"
                        );

                        deleteButton.setStyle(
                                "-fx-background-color: #A94442;"
                                + "-fx-text-fill: white;"
                                + "-fx-background-radius: 6;"
                        );

                        editButton.setOnAction(event -> {
                            Category category =
                                    getTableView().getItems().get(getIndex());

                            editCategory(category);
                        });

                        deleteButton.setOnAction(event -> {
                            Category category =
                                    getTableView().getItems().get(getIndex());

                            deleteCategory(category);
                        });
                    }

                    @Override
                    protected void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);

                        setGraphic(empty ? null : buttons);
                    }
                }
        );
    }

    // EDIT CATEGORY

    private void editCategory(Category category) {

        TextInputDialog dialog =
                new TextInputDialog(category.getNama());

        dialog.setTitle("Edit Category");
        dialog.setHeaderText("Edit category name");
        dialog.setContentText("Category name:");

        Optional<String> result = dialog.showAndWait();

        if (result.isPresent()) {

            String nama = result.get().trim();

            if (nama.isEmpty()) {
                showAlert(
                        Alert.AlertType.WARNING,
                        "Invalid Input",
                        "Nama kategori tidak boleh kosong."
                );
                return;
            }

            String sql =
                    "UPDATE kategori SET nama = ? "
                    + "WHERE id = ? AND kafe_id = ?";

            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setString(1, nama);
                stmt.setInt(2, category.getId());
                stmt.setInt(3, kafeId);

                stmt.executeUpdate();

                loadCategories();

            } catch (SQLException e) {

                showAlert(
                        Alert.AlertType.ERROR,
                        "Database Error",
                        "Gagal mengubah kategori:\n" + e.getMessage()
                );
            }
        }
    }

    // DELETE CATEGORY

    private void deleteCategory(Category category) {

        Alert confirmation =
                new Alert(Alert.AlertType.CONFIRMATION);

        confirmation.setTitle("Delete Category");
        confirmation.setHeaderText(
                "Delete " + category.getNama() + "?"
        );
        confirmation.setContentText(
                "Produk dengan kategori ini dapat menjadi Uncategorized."
        );

        Optional<ButtonType> result = confirmation.showAndWait();

        if (result.isPresent()
                && result.get() == ButtonType.OK) {

            String sql =
                    "DELETE FROM kategori WHERE id = ? AND kafe_id = ?";

            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setInt(1, category.getId());
                stmt.setInt(2, kafeId);

                stmt.executeUpdate();

                loadCategories();

            } catch (SQLException e) {

                showAlert(
                        Alert.AlertType.ERROR,
                        "Database Error",
                        "Gagal menghapus kategori:\n" + e.getMessage()
                );
            }
        }
    }

    // ALERT

    private void showAlert(
            Alert.AlertType type,
            String title,
            String message) {

        Alert alert = new Alert(type);

        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        alert.showAndWait();
    }

    // CATEGORY MODEL

    public static class Category {

        private final int id;
        private final String nama;

        public Category(int id, String nama) {
            this.id = id;
            this.nama = nama;
        }

        public int getId() {
            return id;
        }

        public String getNama() {
            return nama;
        }
    }
}