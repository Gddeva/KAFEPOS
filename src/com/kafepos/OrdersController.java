
package com.kafepos;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.cell.PropertyValueFactory;

public class OrdersController {

    @FXML
    private TableView<Order> orderTable;

    @FXML
    private TableColumn<Order, String> orderIdColumn;

    @FXML
    private TableColumn<Order, String> customerColumn;

    @FXML
    private TableColumn<Order, String> dateColumn;

    @FXML
    private TableColumn<Order, String> itemsColumn;

    @FXML
    private TableColumn<Order, String> totalColumn;

    @FXML
    private TableColumn<Order, String> statusColumn;

    @FXML
    private javafx.scene.control.TextField searchField;

    @FXML
    private ComboBox<String> statusFilter;

    private final ObservableList<Order> allOrders =
            FXCollections.observableArrayList();

    private int kafeId;
    private int userId;

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm");

    // SESSION

    public void setKafeId(int kafeId) {
        this.kafeId = kafeId;
        loadOrders();
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    // INITIALIZE

    @FXML
    public void initialize() {

        orderIdColumn.setCellValueFactory(
                new PropertyValueFactory<>("orderId"));

        customerColumn.setCellValueFactory(
                new PropertyValueFactory<>("customer"));

        dateColumn.setCellValueFactory(
                new PropertyValueFactory<>("date"));

        itemsColumn.setCellValueFactory(
                new PropertyValueFactory<>("items"));

        totalColumn.setCellValueFactory(
                new PropertyValueFactory<>("total"));

        statusColumn.setCellValueFactory(
                new PropertyValueFactory<>("status"));

        statusFilter.setItems(
                FXCollections.observableArrayList(
                        "All Status",
                        "Completed",
                        "Pending"
                )
        );

        statusFilter.setValue("All Status");

        searchField.textProperty().addListener(
                (observable, oldValue, newValue) ->
                        filterOrders()
        );

        statusFilter.valueProperty().addListener(
                (observable, oldValue, newValue) ->
                        filterOrders()
        );
    }

    // LOAD ORDERS

    private void loadOrders() {

        if (kafeId <= 0) {
            return;
        }

        allOrders.clear();

        String sql = """
                SELECT
                    t.id,
                    t.tanggal,
                    t.nama_customer,
                    t.total,
                    COALESCE(SUM(dt.jumlah), 0) AS total_item
                FROM transaksi t
                LEFT JOIN detail_transaksi dt
                    ON t.id = dt.transaksi_id
                WHERE t.kafe_id = ?
                GROUP BY
                    t.id,
                    t.tanggal,
                    t.nama_customer,
                    t.total
                ORDER BY t.tanggal DESC
                """;

        try (Connection conn =
                     DatabaseConnection.getConnection()) {

            if (conn == null) {
                showAlert(
                        Alert.AlertType.ERROR,
                        "Database Error",
                        "Koneksi database gagal."
                );
                return;
            }

            try (PreparedStatement stmt =
                         conn.prepareStatement(sql)) {

                stmt.setInt(1, kafeId);

                try (ResultSet rs = stmt.executeQuery()) {

                    while (rs.next()) {

                        int id = rs.getInt("id");

                        LocalDateTime tanggal =
                                rs.getTimestamp("tanggal")
                                        .toLocalDateTime();

                        String customer =
                                rs.getString("nama_customer");

                        BigDecimal total =
                                rs.getBigDecimal("total");

                        int jumlahItem =
                                rs.getInt("total_item");

                        allOrders.add(
                                new Order(
                                        "ORD-" + String.format("%03d", id),
                                        customer,
                                        tanggal.format(DATE_FORMAT),
                                        String.valueOf(jumlahItem),
                                        formatRupiah(total),
                                        "Completed",
                                        id
                                )
                        );
                    }
                }
            }

            filterOrders();

        } catch (SQLException e) {

            e.printStackTrace();

            showAlert(
                    Alert.AlertType.ERROR,
                    "Gagal Memuat Orders",
                    e.getMessage()
            );
        }
    }

    // FILTER

    private void filterOrders() {

        if (orderTable == null) {
            return;
        }

        String keyword = searchField.getText() == null
                ? ""
                : searchField.getText()
                        .toLowerCase()
                        .trim();

        String selectedStatus =
                statusFilter.getValue() == null
                        ? "All Status"
                        : statusFilter.getValue();

        ObservableList<Order> filtered =
                allOrders.stream()
                        .filter(order ->
                                order.getOrderId()
                                        .toLowerCase()
                                        .contains(keyword)
                                || order.getCustomer()
                                        .toLowerCase()
                                        .contains(keyword)
                        )
                        .filter(order ->
                                selectedStatus.equals("All Status")
                                || order.getStatus()
                                        .equals(selectedStatus)
                        )
                        .collect(Collectors.toCollection(
                                FXCollections::observableArrayList
                        ));

        orderTable.setItems(filtered);
    }

    // ADD ORDER - MULTI ITEM CART

    @FXML
    public void addOrder() {

        if (kafeId <= 0 || userId <= 0) {

            showAlert(
                    Alert.AlertType.WARNING,
                    "Session Belum Siap",
                    "Silakan login kembali."
            );

            return;
        }

        // CUSTOMER

        TextInputDialog customerDialog =
                new TextInputDialog("Walk-in Customer");

        customerDialog.setTitle("Customer");
        customerDialog.setHeaderText(
                "Informasi Customer"
        );
        customerDialog.setContentText(
                "Nama customer:"
        );

        String customerInput =
                customerDialog.showAndWait().orElse(null);

        if (customerInput == null) {
            return;
        }

        String customerName = customerInput.trim();

        if (customerName.isEmpty()) {
            customerName = "Walk-in Customer";
        }

        // PRODUCTS

        List<MenuOption> products =
                getAvailableProducts();

        if (products.isEmpty()) {

            showAlert(
                    Alert.AlertType.INFORMATION,
                    "Produk Kosong",
                    "Belum ada produk aktif "
                            + "atau stok produk habis."
            );

            return;
        }

        // CART

        Map<Integer, CartItem> cart =
                new LinkedHashMap<>();

        boolean finished = false;

        while (!finished) {

            List<MenuOption> choices =
                    new ArrayList<>(products);

            MenuOption finishOption =
                    new MenuOption(
                            -1,
                            "Selesai / Checkout",
                            BigDecimal.ZERO,
                            0
                    );

            choices.add(finishOption);

            ChoiceDialog<MenuOption> productDialog =
                    new ChoiceDialog<>(
                            choices.get(0),
                            choices
                    );

            productDialog.setTitle(
                    "Keranjang Pesanan"
            );

            productDialog.setHeaderText(
                    "Customer: " + customerName
                            + "\nItem dalam keranjang: "
                            + cart.size()
            );

            productDialog.setContentText(
                    "Pilih produk atau checkout:"
            );

            MenuOption selected =
                    productDialog.showAndWait()
                            .orElse(null);

            if (selected == null) {
                return;
            }

            // CHECKOUT

            if (selected.id == -1) {

                if (cart.isEmpty()) {

                    showAlert(
                            Alert.AlertType.WARNING,
                            "Keranjang Kosong",
                            "Tambahkan minimal satu produk."
                    );

                    continue;
                }

                finished = true;
                break;
            }

            // QUANTITY

            TextInputDialog quantityDialog =
                    new TextInputDialog("1");

            quantityDialog.setTitle(
                    "Jumlah Produk"
            );

            quantityDialog.setHeaderText(
                    selected.nama
                            + "\nStok tersedia: "
                            + selected.stok
            );

            quantityDialog.setContentText(
                    "Jumlah:"
            );

            String quantityText =
                    quantityDialog.showAndWait()
                            .orElse(null);

            if (quantityText == null) {
                continue;
            }

            int quantity;

            try {

                quantity = Integer.parseInt(
                        quantityText.trim()
                );

                if (quantity <= 0) {
                    throw new NumberFormatException();
                }

            } catch (NumberFormatException e) {

                showAlert(
                        Alert.AlertType.WARNING,
                        "Jumlah Tidak Valid",
                        "Masukkan jumlah berupa angka positif."
                );

                continue;
            }

            CartItem existing =
                    cart.get(selected.id);

            int alreadyInCart =
                    existing == null
                            ? 0
                            : existing.quantity;

            if (alreadyInCart + quantity
                    > selected.stok) {

                showAlert(
                        Alert.AlertType.WARNING,
                        "Stok Tidak Cukup",
                        "Stok tersedia: "
                                + selected.stok
                                + "\nSudah di keranjang: "
                                + alreadyInCart
                );

                continue;
            }

            if (existing == null) {

                cart.put(
                        selected.id,
                        new CartItem(
                                selected.id,
                                selected.nama,
                                selected.harga,
                                quantity,
                                selected.stok
                        )
                );

            } else {

                existing.quantity += quantity;
            }

            showAlert(
                    Alert.AlertType.INFORMATION,
                    "Produk Ditambahkan",
                    selected.nama
                            + " berhasil ditambahkan ke keranjang."
            );
        }

        if (cart.isEmpty()) {
            return;
        }

        // CALCULATE TOTAL

        BigDecimal total =
                BigDecimal.ZERO;

        StringBuilder cartSummary =
                new StringBuilder();

        for (CartItem item : cart.values()) {

            BigDecimal subtotal =
                    item.harga.multiply(
                            BigDecimal.valueOf(
                                    item.quantity
                            )
                    );

            total = total.add(subtotal);

            cartSummary.append(
                    item.nama
                            + " x "
                            + item.quantity
                            + " = "
                            + formatRupiah(subtotal)
                            + "\n"
            );
        }

        // PAYMENT METHOD

        List<String> paymentMethods = List.of(
                "Cash",
                "QRIS",
                "Debit",
                "Credit"
        );

        ChoiceDialog<String> methodDialog =
                new ChoiceDialog<>(
                        paymentMethods.get(0),
                        paymentMethods
                );

        methodDialog.setTitle(
                "Metode Pembayaran"
        );

        methodDialog.setHeaderText(
                "Customer: " + customerName
                        + "\n\n"
                        + cartSummary
                        + "\nTotal: "
                        + formatRupiah(total)
        );

        methodDialog.setContentText(
                "Pilih metode pembayaran:"
        );

        String metodePembayaran =
                methodDialog.showAndWait()
                        .orElse(null);

        if (metodePembayaran == null) {
            return;
        }

        // PAYMENT AMOUNT

        BigDecimal bayar;

        if (metodePembayaran.equals("Cash")) {

            TextInputDialog paymentDialog =
                    new TextInputDialog(
                            total.toPlainString()
                    );

            paymentDialog.setTitle(
                    "Pembayaran Cash"
            );

            paymentDialog.setHeaderText(
                    "Customer: "
                            + customerName
                            + "\n\n"
                            + cartSummary
                            + "\nTotal: "
                            + formatRupiah(total)
            );

            paymentDialog.setContentText(
                    "Uang dibayar:"
            );

            String paymentText =
                    paymentDialog.showAndWait()
                            .orElse(null);

            if (paymentText == null) {
                return;
            }

            try {

                bayar = new BigDecimal(
                        paymentText.trim()
                );

                if (bayar.compareTo(total) < 0) {

                    showAlert(
                            Alert.AlertType.WARNING,
                            "Pembayaran Kurang",
                            "Uang pembayaran harus "
                                    + "mencukupi total."
                    );

                    return;
                }

            } catch (NumberFormatException e) {

                showAlert(
                        Alert.AlertType.WARNING,
                        "Pembayaran Tidak Valid",
                        "Masukkan nominal pembayaran yang benar."
                );

                return;
            }

        } else {

            // QRIS, Debit, Credit
            // Dibayar sesuai total transaksi

            bayar = total;
        }

        // SAVE CART

        saveCartOrder(
                customerName,
                cart,
                total,
                bayar,
                metodePembayaran
        );
    }

    // GET PRODUCTS

    private List<MenuOption> getAvailableProducts() {

        List<MenuOption> products =
                new ArrayList<>();

        String sql = """
                SELECT id, nama, harga, stok
                FROM menu
                WHERE kafe_id = ?
                  AND aktif = 1
                  AND stok > 0
                ORDER BY nama
                """;

        try (Connection conn =
                     DatabaseConnection.getConnection()) {

            if (conn == null) {
                return products;
            }

            try (PreparedStatement stmt =
                         conn.prepareStatement(sql)) {

                stmt.setInt(1, kafeId);

                try (ResultSet rs =
                             stmt.executeQuery()) {

                    while (rs.next()) {

                        products.add(
                                new MenuOption(
                                        rs.getInt("id"),
                                        rs.getString("nama"),
                                        rs.getBigDecimal("harga"),
                                        rs.getInt("stok")
                                )
                        );
                    }
                }
            }

        } catch (SQLException e) {

            e.printStackTrace();

            showAlert(
                    Alert.AlertType.ERROR,
                    "Gagal Mengambil Produk",
                    e.getMessage()
            );
        }

        return products;
    }

    // SAVE MULTI ITEM TRANSACTION

    private void saveCartOrder(
            String namaCustomer,
            Map<Integer, CartItem> cart,
            BigDecimal total,
            BigDecimal bayar,
            String metodePembayaran) {

        String insertTransaction = """
                INSERT INTO transaksi
                    (kafe_id, user_id, nama_customer,
                     metode_pembayaran, total, bayar, kembalian)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """;

        String insertDetail = """
                INSERT INTO detail_transaksi
                    (transaksi_id, menu_id, nama_menu,
                     harga_satuan, jumlah, subtotal)
                VALUES (?, ?, ?, ?, ?, ?)
                """;

        String updateStock = """
                UPDATE menu
                SET stok = stok - ?
                WHERE id = ?
                  AND kafe_id = ?
                  AND stok >= ?
                  AND aktif = 1
                """;

        try (Connection conn =
                     DatabaseConnection.getConnection()) {

            if (conn == null) {

                showAlert(
                        Alert.AlertType.ERROR,
                        "Database Error",
                        "Koneksi database gagal."
                );

                return;
            }

            conn.setAutoCommit(false);

            try {

                // INSERT TRANSACTION HEADER

                int transactionId;

                try (PreparedStatement transactionStmt =
                             conn.prepareStatement(
                                     insertTransaction,
                                     Statement.RETURN_GENERATED_KEYS
                             )) {

                    transactionStmt.setInt(1, kafeId);
                    transactionStmt.setInt(2, userId);

                    transactionStmt.setString(
                            3,
                            namaCustomer
                    );

                    transactionStmt.setString(
                            4,
                            metodePembayaran
                    );

                    transactionStmt.setBigDecimal(
                            5,
                            total
                    );

                    transactionStmt.setBigDecimal(
                            6,
                            bayar
                    );

                    transactionStmt.setBigDecimal(
                            7,
                            bayar.subtract(total)
                    );

                    transactionStmt.executeUpdate();

                    try (ResultSet keys =
                                 transactionStmt.getGeneratedKeys()) {

                        if (!keys.next()) {

                            throw new SQLException(
                                    "ID transaksi gagal dibuat."
                            );
                        }

                        transactionId =
                                keys.getInt(1);
                    }
                }

                // INSERT EACH CART ITEM

                for (CartItem item : cart.values()) {

                    // UPDATE STOCK

                    try (PreparedStatement stockStmt =
                                 conn.prepareStatement(updateStock)) {

                        stockStmt.setInt(
                                1,
                                item.quantity
                        );

                        stockStmt.setInt(
                                2,
                                item.id
                        );

                        stockStmt.setInt(
                                3,
                                kafeId
                        );

                        stockStmt.setInt(
                                4,
                                item.quantity
                        );

                        int updated =
                                stockStmt.executeUpdate();

                        if (updated == 0) {

                            throw new SQLException(
                                    "Stok produk "
                                            + item.nama
                                            + " tidak mencukupi."
                            );
                        }
                    }

                    // DETAIL SUBTOTAL

                    BigDecimal subtotal =
                            item.harga.multiply(
                                    BigDecimal.valueOf(
                                            item.quantity
                                    )
                            );

                    // INSERT DETAIL

                    try (PreparedStatement detailStmt =
                                 conn.prepareStatement(insertDetail)) {

                        detailStmt.setInt(
                                1,
                                transactionId
                        );

                        detailStmt.setInt(
                                2,
                                item.id
                        );

                        detailStmt.setString(
                                3,
                                item.nama
                        );

                        detailStmt.setBigDecimal(
                                4,
                                item.harga
                        );

                        detailStmt.setInt(
                                5,
                                item.quantity
                        );

                        detailStmt.setBigDecimal(
                                6,
                                subtotal
                        );

                        detailStmt.executeUpdate();
                    }
                }

                conn.commit();

                showAlert(
                        Alert.AlertType.INFORMATION,
                        "Order Berhasil",
                        "Order ORD-"
                                + String.format(
                                        "%03d",
                                        transactionId
                                )
                                + " untuk "
                                + namaCustomer
                                + " berhasil disimpan.\n\n"
                                + "Metode: "
                                + metodePembayaran
                                + "\nTotal: "
                                + formatRupiah(total)
                                + "\nDibayar: "
                                + formatRupiah(bayar)
                                + "\nKembalian: "
                                + formatRupiah(
                                        bayar.subtract(total)
                                )
                );

                loadOrders();

            } catch (SQLException e) {

                conn.rollback();
                throw e;

            } finally {

                conn.setAutoCommit(true);
            }

        } catch (SQLException e) {

            e.printStackTrace();

            showAlert(
                    Alert.AlertType.ERROR,
                    "Gagal Menyimpan Order",
                    e.getMessage()
            );
        }
    }

    // EDIT ORDER ITEM

    @FXML
    public void editOrder() {

        Order selected =
                orderTable.getSelectionModel()
                        .getSelectedItem();

        if (selected == null) {

            showAlert(
                    Alert.AlertType.WARNING,
                    "Pilih Order",
                    "Pilih order yang ingin diedit."
            );

            return;
        }

        List<DetailOption> details =
                getOrderDetails(
                        selected.getDatabaseId()
                );

        if (details.isEmpty()) {

            showAlert(
                    Alert.AlertType.INFORMATION,
                    "Detail Kosong",
                    "Tidak ada detail produk "
                            + "pada transaksi ini."
            );

            return;
        }

        ChoiceDialog<DetailOption> detailDialog =
                new ChoiceDialog<>(
                        details.get(0),
                        details
                );

        detailDialog.setTitle(
                "Edit Order"
        );

        detailDialog.setHeaderText(
                "Pilih item yang ingin diubah"
        );

        detailDialog.setContentText(
                "Item:"
        );

        detailDialog.showAndWait()
                .ifPresent(detail -> {

                    TextInputDialog quantityDialog =
                            new TextInputDialog(
                                    String.valueOf(
                                            detail.quantity
                                    )
                            );

                    quantityDialog.setTitle(
                            "Edit Jumlah"
                    );

                    quantityDialog.setHeaderText(
                            detail.nama
                                    + "\nJumlah saat ini: "
                                    + detail.quantity
                    );

                    quantityDialog.setContentText(
                            "Jumlah baru:"
                    );

                    quantityDialog.showAndWait()
                            .ifPresent(text -> {

                                int newQuantity;

                                try {

                                    newQuantity =
                                            Integer.parseInt(
                                                    text.trim()
                                            );

                                    if (newQuantity <= 0) {
                                        throw new NumberFormatException();
                                    }

                                } catch (
                                        NumberFormatException e
                                ) {

                                    showAlert(
                                            Alert.AlertType.WARNING,
                                            "Jumlah Tidak Valid",
                                            "Masukkan jumlah "
                                                    + "berupa angka positif."
                                    );

                                    return;
                                }

                                updateOrderItem(
                                        selected.getDatabaseId(),
                                        detail,
                                        newQuantity
                                );
                            });
                });
    }

    private List<DetailOption> getOrderDetails(
            int transactionId) {

        List<DetailOption> details =
                new ArrayList<>();

        String sql = """
                SELECT
                    dt.id,
                    dt.menu_id,
                    dt.nama_menu,
                    dt.harga_satuan,
                    dt.jumlah
                FROM detail_transaksi dt
                JOIN transaksi t
                    ON dt.transaksi_id = t.id
                WHERE t.id = ?
                  AND t.kafe_id = ?
                """;

        try (Connection conn =
                     DatabaseConnection.getConnection()) {

            if (conn == null) {
                return details;
            }

            try (PreparedStatement stmt =
                         conn.prepareStatement(sql)) {

                stmt.setInt(1, transactionId);
                stmt.setInt(2, kafeId);

                try (ResultSet rs =
                             stmt.executeQuery()) {

                    while (rs.next()) {

                        details.add(
                                new DetailOption(
                                        rs.getInt("id"),
                                        rs.getInt("menu_id"),
                                        rs.getString("nama_menu"),
                                        rs.getBigDecimal(
                                                "harga_satuan"
                                        ),
                                        rs.getInt("jumlah")
                                )
                        );
                    }
                }
            }

        } catch (SQLException e) {

            e.printStackTrace();

            showAlert(
                    Alert.AlertType.ERROR,
                    "Gagal Mengambil Detail",
                    e.getMessage()
            );
        }

        return details;
    }

    private void updateOrderItem(
            int transactionId,
            DetailOption detail,
            int newQuantity) {

        try (Connection conn =
                     DatabaseConnection.getConnection()) {

            if (conn == null) {
                return;
            }

            conn.setAutoCommit(false);

            try {

                int difference =
                        newQuantity - detail.quantity;

                // ADJUST STOCK

                if (difference != 0) {

                    String stockSql;

                    if (difference > 0) {

                        stockSql = """
                                UPDATE menu
                                SET stok = stok - ?
                                WHERE id = ?
                                  AND kafe_id = ?
                                  AND stok >= ?
                                """;

                    } else {

                        stockSql = """
                                UPDATE menu
                                SET stok = stok + ?
                                WHERE id = ?
                                  AND kafe_id = ?
                                """;
                    }

                    try (PreparedStatement stockStmt =
                                 conn.prepareStatement(stockSql)) {

                        stockStmt.setInt(
                                1,
                                Math.abs(difference)
                        );

                        stockStmt.setInt(
                                2,
                                detail.menuId
                        );

                        stockStmt.setInt(
                                3,
                                kafeId
                        );

                        if (difference > 0) {
                            stockStmt.setInt(
                                    4,
                                    difference
                            );
                        }

                        int updated =
                                stockStmt.executeUpdate();

                        if (updated == 0) {

                            conn.rollback();

                            showAlert(
                                    Alert.AlertType.WARNING,
                                    "Stok Tidak Cukup",
                                    "Stok produk tidak mencukupi."
                            );

                            return;
                        }
                    }
                }

                BigDecimal newSubtotal =
                        detail.harga.multiply(
                                BigDecimal.valueOf(
                                        newQuantity
                                )
                        );

                // UPDATE DETAIL

                String updateDetail = """
                        UPDATE detail_transaksi
                        SET jumlah = ?, subtotal = ?
                        WHERE id = ?
                        """;

                try (PreparedStatement stmt =
                             conn.prepareStatement(updateDetail)) {

                    stmt.setInt(
                            1,
                            newQuantity
                    );

                    stmt.setBigDecimal(
                            2,
                            newSubtotal
                    );

                    stmt.setInt(
                            3,
                            detail.detailId
                    );

                    stmt.executeUpdate();
                }

                // RECALCULATE TRANSACTION TOTAL

                String totalQuery = """
                        SELECT COALESCE(SUM(subtotal), 0)
                        AS total_baru
                        FROM detail_transaksi
                        WHERE transaksi_id = ?
                        """;

                BigDecimal newTotal;

                try (PreparedStatement stmt =
                             conn.prepareStatement(totalQuery)) {

                    stmt.setInt(
                            1,
                            transactionId
                    );

                    try (ResultSet rs =
                                 stmt.executeQuery()) {

                        rs.next();

                        newTotal =
                                rs.getBigDecimal(
                                        "total_baru"
                                );
                    }
                }

                // GET PAYMENT

                BigDecimal bayar;

                try (PreparedStatement stmt =
                             conn.prepareStatement(
                                     "SELECT bayar FROM transaksi "
                                             + "WHERE id = ? "
                                             + "AND kafe_id = ?"
                             )) {

                    stmt.setInt(1, transactionId);
                    stmt.setInt(2, kafeId);

                    try (ResultSet rs =
                                 stmt.executeQuery()) {

                        if (!rs.next()) {
                            throw new SQLException(
                                    "Transaksi tidak ditemukan."
                            );
                        }

                        bayar =
                                rs.getBigDecimal("bayar");
                    }
                }

                if (bayar.compareTo(newTotal) < 0) {

                    conn.rollback();

                    showAlert(
                            Alert.AlertType.WARNING,
                            "Pembayaran Kurang",
                            "Jumlah baru melebihi pembayaran "
                                    + "yang sudah diterima. "
                                    + "Hapus transaksi dan buat "
                                    + "order baru jika perlu."
                    );

                    return;
                }

                // UPDATE TOTAL AND CHANGE

                String updateTransaction = """
                        UPDATE transaksi
                        SET total = ?, kembalian = ?
                        WHERE id = ?
                          AND kafe_id = ?
                        """;

                try (PreparedStatement stmt =
                             conn.prepareStatement(
                                     updateTransaction
                             )) {

                    stmt.setBigDecimal(
                            1,
                            newTotal
                    );

                    stmt.setBigDecimal(
                            2,
                            bayar.subtract(newTotal)
                    );

                    stmt.setInt(
                            3,
                            transactionId
                    );

                    stmt.setInt(
                            4,
                            kafeId
                    );

                    stmt.executeUpdate();
                }

                conn.commit();

                showAlert(
                        Alert.AlertType.INFORMATION,
                        "Order Diperbarui",
                        "Item berhasil diperbarui."
                );

                loadOrders();

            } catch (SQLException e) {

                conn.rollback();
                throw e;

            } finally {

                conn.setAutoCommit(true);
            }

        } catch (SQLException e) {

            e.printStackTrace();

            showAlert(
                    Alert.AlertType.ERROR,
                    "Gagal Mengedit Order",
                    e.getMessage()
            );
        }
    }

    // UPDATE STATUS

    @FXML
    public void updateOrderStatus() {

        Order selected =
                orderTable.getSelectionModel()
                        .getSelectedItem();

        if (selected == null) {

            showAlert(
                    Alert.AlertType.WARNING,
                    "Pilih Order",
                    "Pilih order terlebih dahulu."
            );

            return;
        }

        showAlert(
                Alert.AlertType.INFORMATION,
                "Status Transaksi",
                "Transaksi yang tersimpan "
                        + "saat ini berstatus Completed."
        );
    }

    // DELETE ORDER

    @FXML
    public void deleteOrder() {

        Order selected =
                orderTable.getSelectionModel()
                        .getSelectedItem();

        if (selected == null) {

            showAlert(
                    Alert.AlertType.WARNING,
                    "Pilih Order",
                    "Pilih order yang ingin dihapus."
            );

            return;
        }

        Alert confirmation = new Alert(
                Alert.AlertType.CONFIRMATION
        );

        confirmation.setTitle(
                "Hapus Order"
        );

        confirmation.setHeaderText(
                "Hapus "
                        + selected.getOrderId()
                        + "?"
        );

        confirmation.setContentText(
                "Semua item dalam transaksi akan dihapus "
                        + "dan stok produk dikembalikan."
        );

        confirmation.showAndWait()
                .ifPresent(response -> {

                    if (response == ButtonType.OK) {

                        deleteOrderFromDatabase(
                                selected.getDatabaseId()
                        );
                    }
                });
    }

    private void deleteOrderFromDatabase(
            int transactionId) {

        try (Connection conn =
                     DatabaseConnection.getConnection()) {

            if (conn == null) {
                return;
            }

            conn.setAutoCommit(false);

            try {

                // RESTORE STOCK FOR ALL ITEMS

                String restoreStock = """
                        UPDATE menu m
                        JOIN detail_transaksi dt
                            ON m.id = dt.menu_id
                        SET m.stok = m.stok + dt.jumlah
                        WHERE dt.transaksi_id = ?
                          AND m.kafe_id = ?
                        """;

                try (PreparedStatement stmt =
                             conn.prepareStatement(
                                     restoreStock
                             )) {

                    stmt.setInt(
                            1,
                            transactionId
                    );

                    stmt.setInt(
                            2,
                            kafeId
                    );

                    stmt.executeUpdate();
                }

                // DELETE ALL DETAILS

                try (PreparedStatement stmt =
                             conn.prepareStatement(
                                     "DELETE FROM detail_transaksi "
                                             + "WHERE transaksi_id = ?"
                             )) {

                    stmt.setInt(
                            1,
                            transactionId
                    );

                    stmt.executeUpdate();
                }

                // DELETE TRANSACTION

                try (PreparedStatement stmt =
                             conn.prepareStatement(
                                     "DELETE FROM transaksi "
                                             + "WHERE id = ? "
                                             + "AND kafe_id = ?"
                             )) {

                    stmt.setInt(
                            1,
                            transactionId
                    );

                    stmt.setInt(
                            2,
                            kafeId
                    );

                    stmt.executeUpdate();
                }

                conn.commit();

                showAlert(
                        Alert.AlertType.INFORMATION,
                        "Order Dihapus",
                        "Transaksi berhasil dihapus."
                );

                loadOrders();

            } catch (SQLException e) {

                conn.rollback();
                throw e;

            } finally {

                conn.setAutoCommit(true);
            }

        } catch (SQLException e) {

            e.printStackTrace();

            showAlert(
                    Alert.AlertType.ERROR,
                    "Gagal Menghapus Order",
                    e.getMessage()
            );
        }
    }

    // FORMAT RUPIAH

    private String formatRupiah(
            BigDecimal amount) {

        if (amount == null) {
            amount = BigDecimal.ZERO;
        }

        return "Rp "
                + amount.stripTrailingZeros()
                        .toPlainString();
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

    private static class MenuOption {

        private final int id;
        private final String nama;
        private final BigDecimal harga;
        private final int stok;

        public MenuOption(
                int id,
                String nama,
                BigDecimal harga,
                int stok) {

            this.id = id;
            this.nama = nama;
            this.harga = harga;
            this.stok = stok;
        }

        @Override
        public String toString() {

            return nama
                    + " - Rp "
                    + harga.stripTrailingZeros()
                            .toPlainString()
                    + " (Stok: "
                    + stok
                    + ")";
        }
    }

    // CART ITEM MODEL

    private static class CartItem {

        private final int id;
        private final String nama;
        private final BigDecimal harga;
        private int quantity;
        private final int stok;

        public CartItem(
                int id,
                String nama,
                BigDecimal harga,
                int quantity,
                int stok) {

            this.id = id;
            this.nama = nama;
            this.harga = harga;
            this.quantity = quantity;
            this.stok = stok;
        }
    }

    // ORDER DETAIL MODEL

    private static class DetailOption {

        private final int detailId;
        private final int menuId;
        private final String nama;
        private final BigDecimal harga;
        private final int quantity;

        public DetailOption(
                int detailId,
                int menuId,
                String nama,
                BigDecimal harga,
                int quantity) {

            this.detailId = detailId;
            this.menuId = menuId;
            this.nama = nama;
            this.harga = harga;
            this.quantity = quantity;
        }

        @Override
        public String toString() {

            return nama
                    + " x "
                    + quantity
                    + " ("
                    + formatPrice(harga)
                    + ")";
        }

        private String formatPrice(
                BigDecimal price) {

            return "Rp "
                    + price.stripTrailingZeros()
                            .toPlainString();
        }
    }

    // ORDER MODEL

    public static class Order {

        private final String orderId;
        private final String customer;
        private final String date;
        private final String items;
        private final String total;
        private final String status;
        private final int databaseId;

        public Order(
                String orderId,
                String customer,
                String date,
                String items,
                String total,
                String status,
                int databaseId) {

            this.orderId = orderId;
            this.customer = customer;
            this.date = date;
            this.items = items;
            this.total = total;
            this.status = status;
            this.databaseId = databaseId;
        }

        public String getOrderId() {
            return orderId;
        }

        public String getCustomer() {
            return customer;
        }

        public String getDate() {
            return date;
        }

        public String getItems() {
            return items;
        }

        public String getTotal() {
            return total;
        }

        public String getStatus() {
            return status;
        }

        public int getDatabaseId() {
            return databaseId;
        }
    }
}