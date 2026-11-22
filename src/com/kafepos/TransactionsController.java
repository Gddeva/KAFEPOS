
package com.kafepos;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

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

    private int kafeId;

    private final DateTimeFormatter dateFormatter =
            DateTimeFormatter.ofPattern(
                    "dd MMM yyyy, HH:mm",
                    Locale.ENGLISH
            );

    private final NumberFormat rupiahFormatter =
            NumberFormat.getIntegerInstance(
                    new Locale("id", "ID")
            );

    // INITIALIZE

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

        setupStatusBadge();
    }

    // SESSION

    public void setKafeId(int kafeId) {

        this.kafeId = kafeId;

        loadTransactions();
    }

    // LOAD TRANSACTIONS FROM MYSQL

    private void loadTransactions() {

        ObservableList<Transaction> transactions =
                FXCollections.observableArrayList();

        String sql = """
                SELECT
                    t.id,
                    t.tanggal,
                    t.nama_customer,
                    t.metode_pembayaran,
                    t.total
                FROM transaksi t
                WHERE t.kafe_id = ?
                ORDER BY t.tanggal DESC
                """;

        try (Connection connection =
                     DatabaseConnection.getConnection()) {

            if (connection == null) {

                showAlert(
                        Alert.AlertType.ERROR,
                        "Database Connection Failed",
                        "Tidak dapat terhubung "
                                + "ke database MySQL."
                );

                return;
            }

            try (PreparedStatement statement =
                         connection.prepareStatement(sql)) {

                statement.setInt(1, kafeId);

                try (ResultSet resultSet =
                             statement.executeQuery()) {

                    while (resultSet.next()) {

                        int id =
                                resultSet.getInt("id");

                        String transactionId =
                                String.format(
                                        "TRX-%03d",
                                        id
                                );

                        LocalDateTime dateTime =
                                resultSet.getTimestamp(
                                        "tanggal"
                                ).toLocalDateTime();

                        String formattedDate =
                                dateTime.format(
                                        dateFormatter
                                );

                        // CUSTOMER NAME

                        String customerName =
                                resultSet.getString(
                                        "nama_customer"
                                );

                        if (customerName == null
                                || customerName.isBlank()) {

                            customerName =
                                    "Walk-in Customer";
                        }

                        // PAYMENT METHOD

                        String paymentMethod =
                                resultSet.getString(
                                        "metode_pembayaran"
                                );

                        if (paymentMethod == null
                                || paymentMethod.isBlank()) {

                            paymentMethod = "Cash";
                        }

                        // TOTAL AMOUNT

                        double total =
                                resultSet.getDouble(
                                        "total"
                                );

                        String formattedAmount =
                                "Rp "
                                        + rupiahFormatter.format(
                                                total
                                        );

                        // TRANSACTION STATUS

                        String status =
                                "Completed";

                        Transaction transaction =
                                new Transaction(
                                        transactionId,
                                        formattedDate,
                                        customerName,
                                        paymentMethod,
                                        formattedAmount,
                                        status
                                );

                        transactions.add(
                                transaction
                        );
                    }
                }
            }

            transactionTable.setItems(
                    transactions
            );

        } catch (SQLException e) {

            e.printStackTrace();

            showAlert(
                    Alert.AlertType.ERROR,
                    "Failed to Load Transactions",
                    "Gagal mengambil data transaksi "
                            + "dari database.\n"
                            + e.getMessage()
            );
        }
    }

    // STATUS BADGES

    private void setupStatusBadge() {

        statusColumn.setCellFactory(
                column ->
                        new TableCell<Transaction, String>() {

            @Override
            protected void updateItem(
                    String status,
                    boolean empty) {

                super.updateItem(
                        status,
                        empty
                );

                if (empty || status == null) {

                    setGraphic(null);
                    setText(null);

                    return;
                }

                Label badge =
                        new Label(status);

                badge.setAlignment(
                        Pos.CENTER
                );

                badge.setStyle(
                        "-fx-padding: 6 12;"
                                + "-fx-background-radius: 20;"
                                + "-fx-font-size: 11px;"
                                + "-fx-font-weight: bold;"
                );

                switch (status) {

                    case "Completed":

                        badge.setStyle(
                                badge.getStyle()
                                        + "-fx-background-color: #E4F2E8;"
                                        + "-fx-text-fill: #3D8054;"
                        );

                        break;

                    case "Pending":

                        badge.setStyle(
                                badge.getStyle()
                                        + "-fx-background-color: #FFF2D8;"
                                        + "-fx-text-fill: #A87924;"
                        );

                        break;

                    case "Cancelled":

                        badge.setStyle(
                                badge.getStyle()
                                        + "-fx-background-color: #FBE7E4;"
                                        + "-fx-text-fill: #B44F43;"
                        );

                        break;
                }

                setText(null);
                setGraphic(badge);
            }
        });
    }

    // EXPORT TRANSACTIONS TO CSV

    @FXML
    private void exportTransactions() {

        FileChooser fileChooser =
                new FileChooser();

        fileChooser.setTitle(
                "Export Transactions"
        );

        fileChooser.setInitialFileName(
                "KAFEPOS_Transactions.csv"
        );

        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter(
                        "CSV Files",
                        "*.csv"
                )
        );

        File file =
                fileChooser.showSaveDialog(
                        transactionTable.getScene()
                                .getWindow()
                );

        if (file == null) {
            return;
        }

        try {

            StringBuilder csv =
                    new StringBuilder();

            // UTF-8 BOM FOR EXCEL

            csv.append('\uFEFF');

            csv.append(
                    "Transaction ID,"
                            + "Date & Time,"
                            + "Customer,"
                            + "Payment Method,"
                            + "Amount,"
                            + "Status\n"
            );

            for (Transaction transaction :
                    transactionTable.getItems()) {

                csv.append(
                        escapeCsv(
                                transaction.getTransactionId()
                        )
                ).append(",");

                csv.append(
                        escapeCsv(
                                transaction.getDateTime()
                        )
                ).append(",");

                csv.append(
                        escapeCsv(
                                transaction.getCustomer()
                        )
                ).append(",");

                csv.append(
                        escapeCsv(
                                transaction.getPaymentMethod()
                        )
                ).append(",");

                csv.append(
                        escapeCsv(
                                transaction.getAmount()
                        )
                ).append(",");

                csv.append(
                        escapeCsv(
                                transaction.getStatus()
                        )
                ).append("\n");
            }

            java.nio.file.Files.writeString(
                    file.toPath(),
                    csv.toString(),
                    StandardCharsets.UTF_8
            );

            showAlert(
                    Alert.AlertType.INFORMATION,
                    "Export Successful",
                    "Data transaksi berhasil disimpan."
            );

        } catch (IOException e) {

            showAlert(
                    Alert.AlertType.ERROR,
                    "Export Failed",
                    e.getMessage()
            );
        }
    }

    // CSV ESCAPE

    private String escapeCsv(String value) {

        if (value == null) {
            return "";
        }

        return "\""
                + value.replace("\"", "\"\"")
                + "\"";
    }

    // ALERT

    private void showAlert(
            Alert.AlertType type,
            String title,
            String message) {

        Alert alert =
                new Alert(type);

        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        alert.showAndWait();
    }
}