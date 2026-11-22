package com.kafepos;

import java.io.IOException;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;

public class MainController {

    @FXML
    private BorderPane rootLayout;

    @FXML
    private Node dashboardPage;

    @FXML
    private Button usersButton;

    private int userId;
    private int kafeId;
    private String nama;
    private String role;

    @FXML
    public void initialize() {
        dashboardPage = rootLayout.getCenter();
    }

    // USER SESSION

    public void setUserSession(
            int userId,
            int kafeId,
            String nama,
            String role) {

        this.userId = userId;
        this.kafeId = kafeId;
        this.nama = nama;
        this.role = role;

        System.out.println("USER SESSION BERHASIL DIBUAT");
        System.out.println("Nama: " + nama);
        System.out.println("Role: " + role);
        System.out.println("Kafe ID: " + kafeId);

        // Menu Users hanya untuk owner/admin
        boolean manager = isManager();
        usersButton.setVisible(manager);
        usersButton.setManaged(manager);
    }

    private boolean isManager() {
        return "owner".equals(role) || "admin".equals(role);
    }

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

    // DASHBOARD

    @FXML
    public void showDashboard() {
        rootLayout.setCenter(dashboardPage);
    }

    // PRODUCTS

    @FXML
    public void showProducts() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource(
                            "/fxml/ProductsView.fxml"));

            Node page = loader.load();

            ProductsController controller = loader.getController();

            controller.setKafeId(this.kafeId);

            rootLayout.setCenter(page);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // CATEGORIES

    @FXML
    public void showCategories() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource(
                            "/fxml/CategoriesView.fxml"));

            Node page = loader.load();

            CategoriesController controller = loader.getController();

            controller.setKafeId(this.kafeId);

            rootLayout.setCenter(page);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // TRANSACTIONS

    @FXML
    public void showTransactions() {
        try {
            Node page = FXMLLoader.load(
                    getClass().getResource(
                            "/fxml/TransactionsView.fxml"));

            rootLayout.setCenter(page);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // ORDERS

    @FXML
    public void showOrders() {
        try {
            Node page = FXMLLoader.load(
                    getClass().getResource(
                            "/fxml/OrdersView.fxml"));

            rootLayout.setCenter(page);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // REPORTS

    @FXML
    public void showReports() {
        try {
            Node page = FXMLLoader.load(
                    getClass().getResource(
                            "/fxml/ReportsView.fxml"));

            rootLayout.setCenter(page);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // USERS

    @FXML
    public void showUsers() {
        if (!isManager()) {
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource(
                            "/fxml/UsersView.fxml"));

            Node page = loader.load();

            UsersController controller = loader.getController();
            controller.setSession(this.userId, this.kafeId, this.role);

            rootLayout.setCenter(page);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // SETTINGS

    @FXML
    public void showSettings() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource(
                            "/fxml/SettingsView.fxml"));

            Node page = loader.load();

            SettingsController controller = loader.getController();
            controller.setSession(this.userId, this.kafeId, this.role);

            rootLayout.setCenter(page);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
