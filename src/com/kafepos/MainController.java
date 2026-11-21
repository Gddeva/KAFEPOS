package com.kafepos;

import java.io.IOException;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.layout.BorderPane;

public class MainController {

    @FXML
    private BorderPane rootLayout;

    @FXML
    private Node dashboardPage;

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
                            "/fxml/ProductsView.fxml"
                    )
            );

            Node page = loader.load();

            ProductsController controller =
                    loader.getController();

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
                            "/fxml/CategoriesView.fxml"
                    )
            );

            Node page = loader.load();

            CategoriesController controller =
                    loader.getController();

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
                            "/fxml/TransactionsView.fxml"
                    )
            );

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
                            "/fxml/OrdersView.fxml"
                    )
            );

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
                            "/fxml/ReportsView.fxml"
                    )
            );

            rootLayout.setCenter(page);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}