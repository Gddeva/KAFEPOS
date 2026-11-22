
package com.kafepos;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;

import java.util.stream.Collectors;
import javafx.geometry.Pos;
import javafx.scene.control.TableCell;
import javafx.scene.control.TextInputDialog;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

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
    private TextField searchField;

    @FXML
    private ComboBox<String> statusFilter;

    private final ObservableList<Order> allOrders =
            FXCollections.observableArrayList();

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

        statusColumn.setCellFactory(column -> new TableCell<Order, String>() {

            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);

                if (empty || status == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(status);
                    setAlignment(Pos.CENTER);

                    if (status.equals("Completed")) {
                        setStyle(
                            "-fx-background-color: #E4F2E8;" +
                            "-fx-text-fill: #3D8054;" +
                            "-fx-font-weight: bold;" +
                            "-fx-background-radius: 8;"
                        );
                    } else if (status.equals("Pending")) {
                        setStyle(
                            "-fx-background-color: #FFF2D8;" +
                            "-fx-text-fill: #A87924;" +
                            "-fx-font-weight: bold;" +
                            "-fx-background-radius: 8;"
                        );
                    } else {
                        setStyle("");
                    }
                }
            }
        });

        statusFilter.setItems(FXCollections.observableArrayList(
                "All Status",
                "Pending",
                "Completed"
        ));

        statusFilter.setValue("All Status");

        loadSampleOrders();

        orderTable.setItems(allOrders);

        searchField.textProperty().addListener(
                (observable, oldValue, newValue) -> filterOrders()
        );

        statusFilter.valueProperty().addListener(
                (observable, oldValue, newValue) -> filterOrders()
        );
    }

    private void loadSampleOrders() {

        allOrders.addAll(
                new Order("ORD-001", "Sarah", "19 Nov 2026, 09:15",
                        "3 Items", "Rp125.000", "Completed"),

                new Order("ORD-002", "Michael", "19 Nov 2026, 09:30",
                        "2 Items", "Rp85.000", "Pending"),

                new Order("ORD-003", "Jessica", "19 Nov 2026, 10:05",
                        "4 Items", "Rp210.000", "Completed"),

                new Order("ORD-004", "Daniel", "19 Nov 2026, 10:20",
                        "1 Item", "Rp65.000", "Pending"),

                new Order("ORD-005", "Amanda", "19 Nov 2026, 11:00",
                        "3 Items", "Rp150.000", "Completed"),

                new Order("ORD-006", "William", "19 Nov 2026, 11:35",
                        "2 Items", "Rp95.000", "Pending")
        );
    }

    private void filterOrders() {

        String keyword = searchField.getText() == null
                ? ""
                : searchField.getText().toLowerCase().trim();

        String selectedStatus = statusFilter.getValue();

        ObservableList<Order> filteredOrders =
                allOrders.stream()
                        .filter(order ->
                                order.getOrderId().toLowerCase().contains(keyword)
                                || order.getCustomer().toLowerCase().contains(keyword))
                        .filter(order ->
                                selectedStatus == null
                                || selectedStatus.equals("All Status")
                                || order.getStatus().equals(selectedStatus))
                        .collect(Collectors.toCollection(
                                FXCollections::observableArrayList));

        orderTable.setItems(filteredOrders);
    }

    @FXML
    private void addOrder() {

        TextInputDialog customerDialog = new TextInputDialog();
        customerDialog.setTitle("Add New Order");
        customerDialog.setHeaderText("New Customer Order");
        customerDialog.setContentText("Customer Name:");

        String customer = customerDialog.showAndWait().orElse(null);

        if (customer == null || customer.trim().isEmpty()) {
            return;
        }

        TextInputDialog itemsDialog = new TextInputDialog("1");
        itemsDialog.setTitle("Add New Order");
        itemsDialog.setHeaderText("Order Details");
        itemsDialog.setContentText("Number of Items:");

        String items = itemsDialog.showAndWait().orElse(null);

        if (items == null || items.trim().isEmpty()) {
            return;
        }

        TextInputDialog totalDialog = new TextInputDialog();
        totalDialog.setTitle("Add New Order");
        totalDialog.setHeaderText("Payment Details");
        totalDialog.setContentText("Total Price (e.g. Rp100.000):");

        String total = totalDialog.showAndWait().orElse(null);

        if (total == null || total.trim().isEmpty()) {
            return;
        }

        String orderId = String.format("ORD-%03d", allOrders.size() + 1);

        String date = LocalDateTime.now().format(
            DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm",
                                        java.util.Locale.ENGLISH)
        );

        Order newOrder = new Order(
            orderId,
            customer.trim(),
            date,
            items.trim() + (items.trim().equals("1") ? " Item" : " Items"),
            total.trim(),
            "Pending"
        );

        allOrders.add(newOrder);

        filterOrders();
    }

    @FXML
    private void editOrder() {

        Order selectedOrder = orderTable.getSelectionModel().getSelectedItem();

        if (selectedOrder == null) {
            javafx.scene.control.Alert alert =
                    new javafx.scene.control.Alert(
                            javafx.scene.control.Alert.AlertType.WARNING);

            alert.setTitle("Edit Order");
            alert.setHeaderText("No Order Selected");
            alert.setContentText("Please select an order from the table first.");
            alert.showAndWait();
            return;
        }

        TextInputDialog customerDialog =
                new TextInputDialog(selectedOrder.getCustomer());

        customerDialog.setTitle("Edit Order");
        customerDialog.setHeaderText("Edit Customer Name");
        customerDialog.setContentText("Customer Name:");

        String customer = customerDialog.showAndWait().orElse(null);

        if (customer == null || customer.trim().isEmpty()) {
            return;
        }

        String currentItems = selectedOrder.getItems().split(" ")[0];

        TextInputDialog itemsDialog = new TextInputDialog(currentItems);
        itemsDialog.setTitle("Edit Order");
        itemsDialog.setHeaderText("Edit Order Details");
        itemsDialog.setContentText("Number of Items:");

        String items = itemsDialog.showAndWait().orElse(null);

        if (items == null || items.trim().isEmpty()) {
            return;
        }

        TextInputDialog totalDialog =
                new TextInputDialog(selectedOrder.getTotal());

        totalDialog.setTitle("Edit Order");
        totalDialog.setHeaderText("Edit Payment Details");
        totalDialog.setContentText("Total Price:");

        String total = totalDialog.showAndWait().orElse(null);

        if (total == null || total.trim().isEmpty()) {
            return;
        }

        Order updatedOrder = new Order(
                selectedOrder.getOrderId(),
                customer.trim(),
                selectedOrder.getDate(),
                items.trim() + (items.trim().equals("1") ? " Item" : " Items"),
                total.trim(),
                selectedOrder.getStatus()
        );

        int index = allOrders.indexOf(selectedOrder);
        allOrders.set(index, updatedOrder);

        filterOrders();
    }

    @FXML
    private void deleteOrder() {

        Order selectedOrder = orderTable.getSelectionModel().getSelectedItem();

        if (selectedOrder == null) {
            javafx.scene.control.Alert alert =
                    new javafx.scene.control.Alert(
                            javafx.scene.control.Alert.AlertType.WARNING);

            alert.setTitle("Delete Order");
            alert.setHeaderText("No Order Selected");
            alert.setContentText("Please select an order from the table first.");
            alert.showAndWait();
            return;
        }

        javafx.scene.control.Alert confirmation =
                new javafx.scene.control.Alert(
                        javafx.scene.control.Alert.AlertType.CONFIRMATION);

        confirmation.setTitle("Delete Order");
        confirmation.setHeaderText("Delete " + selectedOrder.getOrderId() + "?");
        confirmation.setContentText(
                "Are you sure you want to delete this order?");

        java.util.Optional<javafx.scene.control.ButtonType> result =
                confirmation.showAndWait();

        if (result.isPresent()
                && result.get() == javafx.scene.control.ButtonType.OK) {

            allOrders.remove(selectedOrder);
            filterOrders();

            javafx.scene.control.Alert success =
                    new javafx.scene.control.Alert(
                            javafx.scene.control.Alert.AlertType.INFORMATION);

            success.setTitle("Order Deleted");
            success.setHeaderText(null);
            success.setContentText("The order has been deleted successfully.");
            success.showAndWait();
        }
    }

    @FXML
    private void updateOrderStatus() {

        Order selectedOrder = orderTable.getSelectionModel().getSelectedItem();

        if (selectedOrder == null) {
            javafx.scene.control.Alert alert =
                    new javafx.scene.control.Alert(
                            javafx.scene.control.Alert.AlertType.WARNING);

            alert.setTitle("Update Status");
            alert.setHeaderText("No Order Selected");
            alert.setContentText("Please select an order first.");
            alert.showAndWait();
            return;
        }

        String newStatus = selectedOrder.getStatus().equals("Pending")
                ? "Completed"
                : "Pending";

        selectedOrder.setStatus(newStatus);

        filterOrders();
        orderTable.refresh();

        javafx.scene.control.Alert alert =
                new javafx.scene.control.Alert(
                        javafx.scene.control.Alert.AlertType.INFORMATION);

        alert.setTitle("Status Updated");
        alert.setHeaderText(null);
        alert.setContentText(
                selectedOrder.getOrderId() + " is now " + newStatus + ".");
        alert.showAndWait();
    }

    public static class Order {

        private final String orderId;
        private final String customer;
        private final String date;
        private final String items;
        private final String total;
        private String status;

        public Order(String orderId, String customer, String date,
                     String items, String total, String status) {
            this.orderId = orderId;
            this.customer = customer;
            this.date = date;
            this.items = items;
            this.total = total;
            this.status = status;
        }

        public String getOrderId() { return orderId; }
        public String getCustomer() { return customer; }
        public String getDate() { return date; }
        public String getItems() { return items; }
        public String getTotal() { return total; }
        public String getStatus() { return status; }

        public void setStatus(String status) {
            this.status = status;
        }
    }
}