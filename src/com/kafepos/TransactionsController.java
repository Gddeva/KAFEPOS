
package com.kafepos;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;

public class TransactionsController {

    @FXML
    private TableView<Transaction> transactionTable;

    @FXML
    private TableColumn<Transaction, String> idColumn;

    @FXML
    private TableColumn<Transaction, String> dateColumn;

    @FXML
    private TableColumn<Transaction, String> customerColumn;

    @FXML
    private TableColumn<Transaction, String> paymentColumn;

    @FXML
    private TableColumn<Transaction, String> amountColumn;

    @FXML
    private TableColumn<Transaction, String> statusColumn;

    @FXML
    public void initialize() {

        idColumn.setCellValueFactory(
            new PropertyValueFactory<>("transactionId")
        );

        dateColumn.setCellValueFactory(
            new PropertyValueFactory<>("dateTime")
        );

        customerColumn.setCellValueFactory(
            new PropertyValueFactory<>("customer")
        );

        paymentColumn.setCellValueFactory(
            new PropertyValueFactory<>("paymentMethod")
        );

        amountColumn.setCellValueFactory(
            new PropertyValueFactory<>("amount")
        );

        statusColumn.setCellValueFactory(
            new PropertyValueFactory<>("status")
        );

        // STATUS BADGES
        statusColumn.setCellFactory(column -> new TableCell<Transaction, String>() {

            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);

                if (empty || status == null) {
                    setGraphic(null);
                    setText(null);
                } else {

                    Label badge = new Label(status);
                    badge.setAlignment(Pos.CENTER);

                    badge.setStyle(
                        "-fx-padding: 6 12;" +
                        "-fx-background-radius: 20;" +
                        "-fx-font-size: 11px;" +
                        "-fx-font-weight: bold;"
                    );

                    switch (status) {

                        case "Completed":
                            badge.setStyle(badge.getStyle() +
                                "-fx-background-color: #E4F2E8;" +
                                "-fx-text-fill: #3D8054;");
                            break;

                        case "Pending":
                            badge.setStyle(badge.getStyle() +
                                "-fx-background-color: #FFF2D8;" +
                                "-fx-text-fill: #A87924;");
                            break;

                        case "Cancelled":
                            badge.setStyle(badge.getStyle() +
                                "-fx-background-color: #FBE7E4;" +
                                "-fx-text-fill: #B44F43;");
                            break;
                    }

                    setText(null);
                    setGraphic(badge);
                }
            }
        });

        // SAMPLE TRANSACTION DATA
        ObservableList<Transaction> transactions =
            FXCollections.observableArrayList(

                new Transaction("TRX-001", "19 Nov 2026, 09:15",
                    "Sarah", "QRIS", "Rp 125.000", "Completed"),

                new Transaction("TRX-002", "19 Nov 2026, 09:30",
                    "Michael", "Cash", "Rp 85.000", "Pending"),

                new Transaction("TRX-003", "19 Nov 2026, 10:05",
                    "Jessica", "Debit Card", "Rp 210.000", "Completed"),

                new Transaction("TRX-004", "19 Nov 2026, 10:20",
                    "Daniel", "E-Wallet", "Rp 65.000", "Cancelled"),

                new Transaction("TRX-005", "19 Nov 2026, 11:00",
                    "Amanda", "QRIS", "Rp 150.000", "Completed"),

                new Transaction("TRX-006", "19 Nov 2026, 11:35",
                    "William", "Cash", "Rp 95.000", "Completed")
            );

        transactionTable.setItems(transactions);
    }

    // EXPORT TRANSACTIONS TO CSV
    @FXML
    private void exportTransactions() {

        FileChooser fileChooser = new FileChooser();

        fileChooser.setTitle("Export Transactions");

        fileChooser.setInitialFileName("KAFEPOS_Transactions.csv");

        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("CSV Files", "*.csv")
        );

        File file = fileChooser.showSaveDialog(
            transactionTable.getScene().getWindow()
        );

        if (file == null) {
            return;
        }

        try {

            StringBuilder csv = new StringBuilder();

            // UTF-8 BOM for Excel compatibility
            csv.append('\uFEFF');

            csv.append("Transaction ID,Date & Time,Customer,")
               .append("Payment Method,Amount,Status\n");

            for (Transaction transaction : transactionTable.getItems()) {

                csv.append(escapeCsv(transaction.getTransactionId())).append(",");
                csv.append(escapeCsv(transaction.getDateTime())).append(",");
                csv.append(escapeCsv(transaction.getCustomer())).append(",");
                csv.append(escapeCsv(transaction.getPaymentMethod())).append(",");
                csv.append(escapeCsv(transaction.getAmount())).append(",");
                csv.append(escapeCsv(transaction.getStatus())).append("\n");
            }

            Files.writeString(
                file.toPath(),
                csv.toString(),
                StandardCharsets.UTF_8
            );

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Export Successful");
            alert.setHeaderText("Transactions Exported!");
            alert.setContentText(
                "Your transaction data has been successfully saved."
            );
            alert.showAndWait();

        } catch (IOException e) {

            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Export Failed");
            alert.setHeaderText("Unable to export transactions");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }

    // HANDLE CSV SPECIAL CHARACTERS
    private String escapeCsv(String value) {

        if (value == null) {
            return "";
        }

        return "\"" + value.replace("\"", "\"\"") + "\"";
    }
}