package com.kafepos;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.UUID;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

public class ProductsController {

    @FXML private TableView<Product> productTable;

    @FXML private TableColumn<Product, Integer> idColumn;
    @FXML private TableColumn<Product, String> imageColumn;
    @FXML private TableColumn<Product, String> nameColumn;
    @FXML private TableColumn<Product, String> categoryColumn;
    @FXML private TableColumn<Product, BigDecimal> priceColumn;
    @FXML private TableColumn<Product, Integer> stockColumn;
    @FXML private TableColumn<Product, String> statusColumn;

    @FXML private TextField searchField;

    private final ObservableList<Product> productList =
            FXCollections.observableArrayList();

    private FilteredList<Product> filteredProducts;

    private int kafeId;

    private File selectedImageFile;

    @FXML
    public void initialize() {

        idColumn.setCellValueFactory(
                new PropertyValueFactory<>("id")
        );

        imageColumn.setCellValueFactory(
                new PropertyValueFactory<>("gambar")
        );

        nameColumn.setCellValueFactory(
                new PropertyValueFactory<>("nama")
        );

        categoryColumn.setCellValueFactory(
                new PropertyValueFactory<>("kategori")
        );

        // FORMAT PRICE AS RUPIAH
        priceColumn.setCellValueFactory(
                new PropertyValueFactory<>("harga")
        );

        priceColumn.setCellFactory(column ->
                new TableCell<Product, BigDecimal>() {

                    @Override
                    protected void updateItem(
                            BigDecimal harga,
                            boolean empty) {

                        super.updateItem(harga, empty);

                        if (empty || harga == null) {
                            setText(null);
                        } else {
                            setText(
                                    "Rp" + String.format(
                                            "%,.0f",
                                            harga.doubleValue()
                                    ).replace(',', '.')
                            );
                        }
                    }
                }
        );

        stockColumn.setCellValueFactory(
                new PropertyValueFactory<>("stok")
        );

        statusColumn.setCellValueFactory(
                new PropertyValueFactory<>("status")
        );

        // IMAGE THUMBNAIL
        imageColumn.setCellFactory(column ->
                new TableCell<Product, String>() {

                    private final ImageView imageView =
                            new ImageView();

                    {
                        imageView.setFitWidth(55);
                        imageView.setFitHeight(55);
                        imageView.setPreserveRatio(true);
                    }

                    @Override
                    protected void updateItem(
                            String imagePath,
                            boolean empty) {

                        super.updateItem(imagePath, empty);

                        if (empty || imagePath == null
                                || imagePath.isBlank()) {

                            setGraphic(null);

                        } else {

                            try {

                                File file = new File(imagePath);

                                if (!file.exists()) {
                                    file = new File(
                                            System.getProperty("user.dir"),
                                            imagePath
                                    );
                                }

                                if (file.exists()) {

                                    Image image = new Image(
                                            file.toURI().toString(),
                                            55,
                                            55,
                                            true,
                                            true
                                    );

                                    imageView.setImage(image);
                                    setGraphic(imageView);

                                } else {

                                    setGraphic(null);
                                }

                            } catch (Exception e) {

                                setGraphic(null);
                            }
                        }
                    }
                }
        );

        // SEARCH
        filteredProducts = new FilteredList<>(
                productList,
                product -> true
        );

        productTable.setItems(filteredProducts);

        searchField.textProperty().addListener(
                (observable, oldValue, newValue) -> {

                    filteredProducts.setPredicate(product -> {

                        if (newValue == null
                                || newValue.isBlank()) {
                            return true;
                        }

                        String keyword =
                                newValue.toLowerCase();

                        return product.getNama()
                                .toLowerCase()
                                .contains(keyword)
                                || product.getKategori()
                                .toLowerCase()
                                .contains(keyword);
                    });
                }
        );
    }

    // RECEIVE CAFE ID
    public void setKafeId(int kafeId) {

        this.kafeId = kafeId;

        loadProducts();
    }

    // LOAD PRODUCTS
    private void loadProducts() {

        productList.clear();

        String sql = """
                SELECT
                    m.id,
                    m.nama,
                    m.harga,
                    m.stok,
                    m.aktif,
                    m.gambar,
                    m.kategori_id,
                    COALESCE(k.nama, 'Uncategorized') AS kategori
                FROM menu m
                LEFT JOIN kategori k
                    ON m.kategori_id = k.id
                WHERE m.kafe_id = ?
                ORDER BY m.id ASC
                """;

        try (Connection conn =
                     DatabaseConnection.getConnection();

             PreparedStatement stmt =
                     conn.prepareStatement(sql)) {

            stmt.setInt(1, kafeId);

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {

                Product product = new Product(
                        rs.getInt("id"),
                        rs.getString("nama"),
                        rs.getString("kategori"),
                        rs.getBigDecimal("harga"),
                        rs.getInt("stok"),
                        rs.getBoolean("aktif")
                                ? "Active"
                                : "Inactive",
                        rs.getString("gambar"),
                        rs.getInt("kategori_id")
                );

                productList.add(product);
            }

        } catch (SQLException e) {

            showAlert(
                    Alert.AlertType.ERROR,
                    "Database Error",
                    "Gagal memuat produk:\n" + e.getMessage()
            );
        }
    }

    // ADD PRODUCT
    @FXML
    private void handleAddProduct() {

        showProductDialog(null);
    }

    // EDIT PRODUCT
    @FXML
    private void handleEditProduct() {

        Product selected =
                productTable.getSelectionModel()
                        .getSelectedItem();

        if (selected == null) {

            showAlert(
                    Alert.AlertType.WARNING,
                    "No Product Selected",
                    "Pilih produk terlebih dahulu."
            );

            return;
        }

        showProductDialog(selected);
    }

    // DELETE PRODUCT
    @FXML
    private void handleDeleteProduct() {

        Product selected =
                productTable.getSelectionModel()
                        .getSelectedItem();

        if (selected == null) {

            showAlert(
                    Alert.AlertType.WARNING,
                    "No Product Selected",
                    "Pilih produk terlebih dahulu."
            );

            return;
        }

        Alert confirmation = new Alert(
                Alert.AlertType.CONFIRMATION
        );

        confirmation.setTitle("Delete Product");
        confirmation.setHeaderText(
                "Hapus " + selected.getNama() + "?"
        );

        confirmation.setContentText(
                "Produk akan dihapus dari database."
        );

        confirmation.showAndWait().ifPresent(response -> {

            if (response == ButtonType.OK) {

                String sql = """
                        DELETE FROM menu
                        WHERE id = ?
                        AND kafe_id = ?
                        """;

                try (Connection conn =
                             DatabaseConnection.getConnection();

                     PreparedStatement stmt =
                             conn.prepareStatement(sql)) {

                    stmt.setInt(1, selected.getId());
                    stmt.setInt(2, kafeId);

                    stmt.executeUpdate();

                    loadProducts();

                    showAlert(
                            Alert.AlertType.INFORMATION,
                            "Success",
                            "Produk berhasil dihapus."
                    );

                } catch (SQLException e) {

                    showAlert(
                            Alert.AlertType.ERROR,
                            "Delete Failed",
                            "Produk gagal dihapus.\n"
                                    + e.getMessage()
                    );
                }
            }
        });
    }

    // PRODUCT FORM
    private void showProductDialog(Product product) {

        boolean isEdit = product != null;

        Dialog<ButtonType> dialog =
                new Dialog<>();

        dialog.setTitle(
                isEdit ? "Edit Product" : "Add Product"
        );

        dialog.setHeaderText(
                isEdit
                        ? "Update product information"
                        : "Enter new product information"
        );

        ButtonType saveButton = new ButtonType(
                "Save",
                ButtonBar.ButtonData.OK_DONE
        );

        dialog.getDialogPane()
                .getButtonTypes()
                .addAll(saveButton, ButtonType.CANCEL);

        TextField nameField = new TextField();
        nameField.setPromptText("Product name");

        ComboBox<CategoryOption> categoryCombo =
                new ComboBox<>();

        categoryCombo.setPromptText("Select category");
        categoryCombo.setPrefWidth(220);

        TextField priceField = new TextField();
        priceField.setPromptText("Price");

        TextField stockField = new TextField();
        stockField.setPromptText("Stock");

        CheckBox activeCheckBox =
                new CheckBox("Active");

        activeCheckBox.setSelected(true);

        Label imageLabel =
                new Label("No image selected");

        Button chooseImageButton =
                new Button("Choose Image");

        ImageView previewImage =
                new ImageView();

        previewImage.setFitWidth(100);
        previewImage.setFitHeight(100);
        previewImage.setPreserveRatio(true);

        selectedImageFile = null;

        loadCategories(categoryCombo);

        if (isEdit) {

            nameField.setText(product.getNama());

            priceField.setText(
                    product.getHarga().toString()
            );

            stockField.setText(
                    String.valueOf(product.getStok())
            );

            activeCheckBox.setSelected(
                    product.getStatus().equals("Active")
            );

            for (CategoryOption category :
                    categoryCombo.getItems()) {

                if (category.getId()
                        == product.getKategoriId()) {

                    categoryCombo.setValue(category);
                    break;
                }
            }

            if (product.getGambar() != null
                    && !product.getGambar().isBlank()) {

                imageLabel.setText(
                        new File(product.getGambar())
                                .getName()
                );

                File imageFile =
                        new File(product.getGambar());

                if (!imageFile.exists()) {

                    imageFile = new File(
                            System.getProperty("user.dir"),
                            product.getGambar()
                    );
                }

                if (imageFile.exists()) {

                    previewImage.setImage(
                            new Image(
                                    imageFile.toURI()
                                            .toString()
                            )
                    );
                }
            }
        }

        chooseImageButton.setOnAction(event -> {

            FileChooser fileChooser =
                    new FileChooser();

            fileChooser.setTitle(
                    "Choose Product Image"
            );

            fileChooser.getExtensionFilters()
                    .addAll(
                            new FileChooser
                                    .ExtensionFilter(
                                            "Image Files",
                                            "*.png",
                                            "*.jpg",
                                            "*.jpeg"
                                    )
                    );

            File file = fileChooser.showOpenDialog(
                    dialog.getDialogPane()
                            .getScene()
                            .getWindow()
            );

            if (file != null) {

                selectedImageFile = file;

                imageLabel.setText(file.getName());

                previewImage.setImage(
                        new Image(
                                file.toURI().toString(),
                                100,
                                100,
                                true,
                                true
                        )
                );
            }
        });

        GridPane grid = new GridPane();

        grid.setHgap(15);
        grid.setVgap(12);
        grid.setPadding(
                new javafx.geometry.Insets(15)
        );

        grid.add(new Label("Product Name:"), 0, 0);
        grid.add(nameField, 1, 0);

        grid.add(new Label("Category:"), 0, 1);
        grid.add(categoryCombo, 1, 1);

        grid.add(new Label("Price:"), 0, 2);
        grid.add(priceField, 1, 2);

        grid.add(new Label("Stock:"), 0, 3);
        grid.add(stockField, 1, 3);

        grid.add(new Label("Status:"), 0, 4);
        grid.add(activeCheckBox, 1, 4);

        grid.add(new Label("Product Image:"), 0, 5);

        VBox imageBox = new VBox(
                8,
                chooseImageButton,
                imageLabel,
                previewImage
        );

        grid.add(imageBox, 1, 5);

        dialog.getDialogPane()
                .setContent(grid);

        // VALIDATION
        Button saveNode = (Button) dialog
                .getDialogPane()
                .lookupButton(saveButton);

        saveNode.addEventFilter(
                javafx.event.ActionEvent.ACTION,
                event -> {

                    if (nameField.getText()
                            .trim().isEmpty()) {

                        showAlert(
                                Alert.AlertType.WARNING,
                                "Validation",
                                "Nama produk wajib diisi."
                        );

                        event.consume();
                        return;
                    }

                    try {

                        BigDecimal price =
                                new BigDecimal(
                                        priceField.getText()
                                                .trim()
                                );

                        int stock = Integer.parseInt(
                                stockField.getText()
                                        .trim()
                        );

                        if (price.compareTo(
                                BigDecimal.ZERO) < 0
                                || stock < 0) {

                            throw new NumberFormatException();
                        }

                    } catch (Exception e) {

                        showAlert(
                                Alert.AlertType.WARNING,
                                "Validation",
                                "Harga dan stok harus berupa angka "
                                        + "yang valid dan tidak negatif."
                        );

                        event.consume();
                    }
                }
        );

        dialog.showAndWait().ifPresent(response -> {

            if (response == saveButton) {

                try {

                    String nama =
                            nameField.getText().trim();

                    BigDecimal harga =
                            new BigDecimal(
                                    priceField.getText()
                                            .trim()
                            );

                    int stok = Integer.parseInt(
                            stockField.getText()
                                    .trim()
                    );

                    CategoryOption category =
                            categoryCombo.getValue();

                    Integer kategoriId =
                            category == null
                                    || category.getId() == 0
                                    ? null
                                    : category.getId();

                    String gambar = isEdit
                            ? product.getGambar()
                            : null;

                    if (selectedImageFile != null) {

                        gambar = copyImage(
                                selectedImageFile
                        );
                    }

                    saveProduct(
                            isEdit
                                    ? product.getId()
                                    : 0,
                            nama,
                            kategoriId,
                            harga,
                            stok,
                            activeCheckBox.isSelected(),
                            gambar,
                            isEdit
                    );

                } catch (Exception e) {

                    showAlert(
                            Alert.AlertType.ERROR,
                            "Save Failed",
                            e.getMessage()
                    );
                }
            }
        });
    }

    // LOAD CATEGORIES
    private void loadCategories(
            ComboBox<CategoryOption> comboBox) {

        comboBox.getItems().clear();

        comboBox.getItems().add(
                new CategoryOption(
                        0,
                        "Uncategorized"
                )
        );

        String sql = """
                SELECT id, nama
                FROM kategori
                WHERE kafe_id = ?
                ORDER BY nama ASC
                """;

        try (Connection conn =
                     DatabaseConnection.getConnection();

             PreparedStatement stmt =
                     conn.prepareStatement(sql)) {

            stmt.setInt(1, kafeId);

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {

                comboBox.getItems().add(
                        new CategoryOption(
                                rs.getInt("id"),
                                rs.getString("nama")
                        )
                );
            }

        } catch (SQLException e) {

            showAlert(
                    Alert.AlertType.ERROR,
                    "Category Error",
                    e.getMessage()
            );
        }
    }

    // SAVE PRODUCT
    private void saveProduct(
            int id,
            String nama,
            Integer kategoriId,
            BigDecimal harga,
            int stok,
            boolean aktif,
            String gambar,
            boolean isEdit) throws SQLException {

        String sql;

        if (isEdit) {

            sql = """
                    UPDATE menu
                    SET nama = ?,
                        kategori_id = ?,
                        harga = ?,
                        stok = ?,
                        aktif = ?,
                        gambar = ?
                    WHERE id = ?
                    AND kafe_id = ?
                    """;

        } else {

            sql = """
                    INSERT INTO menu
                    (kafe_id, nama, kategori_id,
                     harga, stok, aktif, gambar)
                    VALUES (?, ?, ?, ?, ?, ?, ?)
                    """;
        }

        try (Connection conn =
                     DatabaseConnection.getConnection();

             PreparedStatement stmt =
                     conn.prepareStatement(sql)) {

            if (isEdit) {

                stmt.setString(1, nama);

                if (kategoriId == null) {
                    stmt.setNull(2, Types.INTEGER);
                } else {
                    stmt.setInt(2, kategoriId);
                }

                stmt.setBigDecimal(3, harga);
                stmt.setInt(4, stok);
                stmt.setBoolean(5, aktif);
                stmt.setString(6, gambar);
                stmt.setInt(7, id);
                stmt.setInt(8, kafeId);

            } else {

                stmt.setInt(1, kafeId);
                stmt.setString(2, nama);

                if (kategoriId == null) {
                    stmt.setNull(3, Types.INTEGER);
                } else {
                    stmt.setInt(3, kategoriId);
                }

                stmt.setBigDecimal(4, harga);
                stmt.setInt(5, stok);
                stmt.setBoolean(6, aktif);
                stmt.setString(7, gambar);
            }

            stmt.executeUpdate();
        }

        loadProducts();

        showAlert(
                Alert.AlertType.INFORMATION,
                "Success",
                isEdit
                        ? "Produk berhasil diperbarui."
                        : "Produk berhasil ditambahkan."
        );
    }

    // COPY IMAGE
    private String copyImage(File source)
            throws IOException {

        Path folder = Path.of(
                System.getProperty("user.dir"),
                "images",
                "products"
        );

        Files.createDirectories(folder);

        String extension = "";

        String fileName = source.getName();

        int dotIndex = fileName.lastIndexOf('.');

        if (dotIndex >= 0) {
            extension = fileName.substring(dotIndex);
        }

        String newName =
                UUID.randomUUID() + extension;

        Path destination =
                folder.resolve(newName);

        Files.copy(
                source.toPath(),
                destination,
                StandardCopyOption.REPLACE_EXISTING
        );

        return "images/products/" + newName;
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

    // PRODUCT MODEL
    public static class Product {

        private final int id;
        private final String nama;
        private final String kategori;
        private final BigDecimal harga;
        private final int stok;
        private final String status;
        private final String gambar;
        private final int kategoriId;

        public Product(
                int id,
                String nama,
                String kategori,
                BigDecimal harga,
                int stok,
                String status,
                String gambar,
                int kategoriId) {

            this.id = id;
            this.nama = nama;
            this.kategori = kategori;
            this.harga = harga;
            this.stok = stok;
            this.status = status;
            this.gambar = gambar;
            this.kategoriId = kategoriId;
        }

        public int getId() {
            return id;
        }

        public String getNama() {
            return nama;
        }

        public String getKategori() {
            return kategori;
        }

        public BigDecimal getHarga() {
            return harga;
        }

        public int getStok() {
            return stok;
        }

        public String getStatus() {
            return status;
        }

        public String getGambar() {
            return gambar;
        }

        public int getKategoriId() {
            return kategoriId;
        }
    }

    // CATEGORY MODEL
    public static class CategoryOption {

        private final int id;
        private final String nama;

        public CategoryOption(int id, String nama) {
            this.id = id;
            this.nama = nama;
        }

        public int getId() {
            return id;
        }

        public String getNama() {
            return nama;
        }

        @Override
        public String toString() {
            return nama;
        }
    }
}