package com.kafepos;

import java.io.IOException;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;

public class MainController {

    @FXML
    private BorderPane rootLayout;

    @FXML
    private Node dashboardPage;

    // =========================
    // HEADER
    // =========================

    @FXML
    private Label roleLabel;

    // =========================
    // SIDEBAR BUTTONS
    // =========================

    @FXML
    private Button productsButton;

    @FXML
    private Button categoriesButton;

    @FXML
    private Button transactionsButton;

    @FXML
    private Button ordersButton;

    @FXML
    private Button reportsButton;

    @FXML
    private Button usersButton;

    @FXML
    private Button settingsButton;

    // =========================
    // USER SESSION
    // =========================

    private int userId;
    private int kafeId;
    private String nama;
    private String role;

    // =========================
    // INITIALIZE
    // =========================

    @FXML
    public void initialize() {
        dashboardPage = rootLayout.getCenter();
    }

    // =========================
    // SET USER SESSION
    // =========================

    public void setUserSession(
            int userId,
            int kafeId,
            String nama,
            String role
    ) {
        this.userId = userId;
        this.kafeId = kafeId;
        this.nama = nama;
        this.role = role;

        applyRolePermissions();
    }

    // =========================
    // GETTERS
    // =========================

    public int getUserId() {
        return userId;
    }

    public int getKafeId() {
        return kafeId;
    }

    public String getNama() {
        return nama;
    }

    public String getRole() {
        return role;
    }

    // =========================
    // ROLE PERMISSIONS
    // =========================

    private void applyRolePermissions() {

        if (roleLabel != null) {
            if (role == null || role.isBlank()) {
                roleLabel.setText("User");
            } else {
                roleLabel.setText(role);
            }
        }

        boolean isCashier = role != null
                && role.trim().equalsIgnoreCase("Cashier");

        // Owner dapat mengakses semua menu.
        // Cashier hanya dapat mengakses menu operasional.

        setMenuAccess(productsButton, !isCashier);
        setMenuAccess(categoriesButton, !isCashier);

        setMenuAccess(transactionsButton, true);
        setMenuAccess(ordersButton, true);

        setMenuAccess(reportsButton, !isCashier);
        setMenuAccess(usersButton, !isCashier);
        setMenuAccess(settingsButton, !isCashier);
    }

    private void setMenuAccess(
            Button button,
            boolean allowed
    ) {
        if (button != null) {
            button.setVisible(allowed);
            button.setManaged(allowed);
        }
    }

    // =========================
    // DASHBOARD
    // =========================

    @FXML
    public void showDashboard() {
        rootLayout.setCenter(dashboardPage);
    }

    // =========================
    // PRODUCTS
    // =========================

    @FXML
    public void showProducts() throws IOException {

        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/fxml/ProductsView.fxml")
        );

        Node page = loader.load();

        ProductsController controller =
                loader.getController();

        controller.setKafeId(this.kafeId);

        rootLayout.setCenter(page);
    }

    // =========================
    // CATEGORIES
    // =========================

    @FXML
    public void showCategories() throws IOException {

        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/fxml/CategoriesView.fxml")
        );

        Node page = loader.load();

        CategoriesController controller =
                loader.getController();

        controller.setKafeId(this.kafeId);

        rootLayout.setCenter(page);
    }

    // =========================
    // TRANSACTIONS
    // =========================

    @FXML
    public void showTransactions() throws IOException {

        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/fxml/TransactionsView.fxml")
        );

        Node page = loader.load();

        TransactionsController controller =
                loader.getController();

        controller.setKafeId(this.kafeId);

        rootLayout.setCenter(page);
    }

    // =========================
    // ORDERS / CASHIER
    // =========================

    @FXML
    public void showOrders() throws IOException {

        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/fxml/OrdersView.fxml")
        );

        Node page = loader.load();

        OrdersController controller =
                loader.getController();

        controller.setKafeId(this.kafeId);
        controller.setUserId(this.userId);

        rootLayout.setCenter(page);
    }

    // =========================
    // REPORTS
    // =========================

    @FXML
    public void showReports() throws IOException {

        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/fxml/ReportsView.fxml")
        );

        Node page = loader.load();

        ReportsController controller =
                loader.getController();

        controller.setKafeId(this.kafeId);

        rootLayout.setCenter(page);
    }
}