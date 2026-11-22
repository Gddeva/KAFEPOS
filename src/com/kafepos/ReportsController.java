
package com.kafepos;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.NumberFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class ReportsController {

    @FXML private ComboBox<String> periodFilter;

    @FXML private Label totalRevenueLabel;
    @FXML private Label totalOrdersLabel;
    @FXML private Label averageOrderValueLabel;
    @FXML private Label revenueSubtitleLabel;

    @FXML private BarChart<String, Number> revenueChart;
    @FXML private BarChart<String, Number> topProductsChart;

    @FXML private Label cashAmountLabel;
    @FXML private Label cardAmountLabel;
    @FXML private Label qrisAmountLabel;

    @FXML private Label cashPercentLabel;
    @FXML private Label cardPercentLabel;
    @FXML private Label qrisPercentLabel;

    @FXML private ProgressBar cashProgress;
    @FXML private ProgressBar cardProgress;
    @FXML private ProgressBar qrisProgress;

    @FXML private Label revenueGrowthLabel;
    @FXML private Label orderGrowthLabel;

    private int kafeId;

    private double currentRevenue;
    private int currentOrders;

    private double cashRevenue;
    private double cardRevenue;
    private double qrisRevenue;

    private double revenueGrowth;
    private double orderGrowth;

    private final NumberFormat rupiahFormat =
            NumberFormat.getNumberInstance(
                    Locale.forLanguageTag("id-ID")
            );

    private final Map<String, Double> revenueData =
            new LinkedHashMap<>();

    private final Map<String, Integer> productData =
            new LinkedHashMap<>();

    public void setKafeId(int kafeId) {
        this.kafeId = kafeId;
        updateReport();
    }

    @FXML
    public void initialize() {

        periodFilter.setValue("This Month");

        periodFilter.setOnAction(event -> updateReport());
    }

    // =========================================
    // DATE RANGE
    // =========================================

    private LocalDate getStartDate(String period) {

        LocalDate today = LocalDate.now();

        switch (period) {

            case "Today":
                return today;

            case "This Week":
                return today.with(
                        DayOfWeek.MONDAY
                );

            case "This Month":
                return today.withDayOfMonth(1);

            case "This Year":
                return today.withDayOfYear(1);

            default:
                return today.withDayOfMonth(1);
        }
    }

    private LocalDate getEndDate() {
        return LocalDate.now().plusDays(1);
    }

    private LocalDate getPreviousStartDate(String period) {

        LocalDate start = getStartDate(period);

        switch (period) {

            case "Today":
                return start.minusDays(1);

            case "This Week":
                return start.minusWeeks(1);

            case "This Month":
                return start.minusMonths(1);

            case "This Year":
                return start.minusYears(1);

            default:
                return start.minusMonths(1);
        }
    }

    private LocalDate getPreviousEndDate(String period) {

        LocalDate start = getStartDate(period);

        switch (period) {

            case "Today":
                return start;

            case "This Week":
                return start;

            case "This Month":
                return start;

            case "This Year":
                return start;

            default:
                return start;
        }
    }

    // =========================================
    // UPDATE REPORT
    // =========================================

    private void updateReport() {

        if (kafeId <= 0) {
            return;
        }

        String period = periodFilter.getValue();

        LocalDate startDate =
                getStartDate(period);

        LocalDate endDate =
                getEndDate();

        LocalDate previousStart =
                getPreviousStartDate(period);

        LocalDate previousEnd =
                getPreviousEndDate(period);

        currentRevenue = 0;
        currentOrders = 0;

        cashRevenue = 0;
        cardRevenue = 0;
        qrisRevenue = 0;

        revenueGrowth = 0;
        orderGrowth = 0;

        revenueData.clear();
        productData.clear();

        revenueChart.getData().clear();
        topProductsChart.getData().clear();

        try {

            loadSummary(startDate, endDate);

            loadRevenueChart(
                    startDate,
                    endDate,
                    period
            );

            loadTopProducts(
                    startDate,
                    endDate
            );

            loadGrowth(
                    previousStart,
                    previousEnd
            );

            updateSummaryCards();
            updatePaymentSummary();
            updateSalesPerformance();
            updateCharts();

            revenueSubtitleLabel.setText(
                    "Revenue performance " +
                    period.toLowerCase()
            );

        } catch (SQLException e) {

            e.printStackTrace();
        }
    }

    // =========================================
    // SUMMARY
    // =========================================

    private void loadSummary(
            LocalDate startDate,
            LocalDate endDate
    ) throws SQLException {

        String sql = """
                SELECT
                    COUNT(id) AS total_orders,
                    COALESCE(SUM(total), 0) AS total_revenue
                FROM transaksi
                WHERE kafe_id = ?
                AND tanggal >= ?
                AND tanggal < ?
                """;

        try (
                Connection connection =
                        DatabaseConnection.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setInt(1, kafeId);

            statement.setTimestamp(
                    2,
                    Timestamp.valueOf(
                            startDate.atStartOfDay()
                    )
            );

            statement.setTimestamp(
                    3,
                    Timestamp.valueOf(
                            endDate.atStartOfDay()
                    )
            );

            try (ResultSet rs = statement.executeQuery()) {

                if (rs.next()) {

                    currentOrders =
                            rs.getInt("total_orders");

                    currentRevenue =
                            rs.getDouble("total_revenue");
                }
            }
        }

        loadPaymentRevenue(
                startDate,
                endDate
        );
    }

    // =========================================
    // PAYMENT REVENUE
    // =========================================

    private void loadPaymentRevenue(
            LocalDate startDate,
            LocalDate endDate
    ) throws SQLException {

        String sql = """
                SELECT
                    metode_pembayaran,
                    COALESCE(SUM(total), 0) AS revenue
                FROM transaksi
                WHERE kafe_id = ?
                AND tanggal >= ?
                AND tanggal < ?
                GROUP BY metode_pembayaran
                """;

        try (
                Connection connection =
                        DatabaseConnection.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setInt(1, kafeId);

            statement.setTimestamp(
                    2,
                    Timestamp.valueOf(
                            startDate.atStartOfDay()
                    )
            );

            statement.setTimestamp(
                    3,
                    Timestamp.valueOf(
                            endDate.atStartOfDay()
                    )
            );

            try (ResultSet rs = statement.executeQuery()) {

                while (rs.next()) {

                    String method =
                            rs.getString(
                                    "metode_pembayaran"
                            );

                    double amount =
                            rs.getDouble("revenue");

                    if (method == null) {
                        continue;
                    }

                    switch (
                            method.trim().toLowerCase()
                    ) {

                        case "cash":
                            cashRevenue += amount;
                            break;

                        case "qris":
                            qrisRevenue += amount;
                            break;

                        case "debit":
                        case "credit":
                        case "card":
                            cardRevenue += amount;
                            break;
                    }
                }
            }
        }
    }

    // =========================================
    // REVENUE CHART
    // =========================================

    private void loadRevenueChart(
            LocalDate startDate,
            LocalDate endDate,
            String period
    ) throws SQLException {

        String sql = """
                SELECT tanggal, total
                FROM transaksi
                WHERE kafe_id = ?
                AND tanggal >= ?
                AND tanggal < ?
                ORDER BY tanggal
                """;

        try (
                Connection connection =
                        DatabaseConnection.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setInt(1, kafeId);

            statement.setTimestamp(
                    2,
                    Timestamp.valueOf(
                            startDate.atStartOfDay()
                    )
            );

            statement.setTimestamp(
                    3,
                    Timestamp.valueOf(
                            endDate.atStartOfDay()
                    )
            );

            try (ResultSet rs = statement.executeQuery()) {

                while (rs.next()) {

                    Timestamp timestamp =
                            rs.getTimestamp("tanggal");

                    if (timestamp == null) {
                        continue;
                    }

                    LocalDateTime dateTime =
                            timestamp.toLocalDateTime();

                    String label;

                    switch (period) {

                        case "Today":
                            label = String.format(
                                    "%02d:00",
                                    dateTime.getHour()
                            );
                            break;

                        case "This Year":
                            label = dateTime.getMonth()
                                    .getDisplayName(
                                            java.time.format.TextStyle.SHORT,
                                            Locale.ENGLISH
                                    );
                            break;

                        default:
                            label = dateTime.toLocalDate()
                                    .toString();
                            break;
                    }

                    double amount =
                            rs.getDouble("total");

                    revenueData.put(
                            label,
                            revenueData.getOrDefault(
                                    label,
                                    0.0
                            ) + amount
                    );
                }
            }
        }
    }

    // =========================================
    // TOP PRODUCTS
    // =========================================

    private void loadTopProducts(
            LocalDate startDate,
            LocalDate endDate
    ) throws SQLException {

        String sql = """
                SELECT
                    d.nama_menu,
                    SUM(d.jumlah) AS total_sold
                FROM detail_transaksi d
                JOIN transaksi t
                    ON d.transaksi_id = t.id
                WHERE t.kafe_id = ?
                AND t.tanggal >= ?
                AND t.tanggal < ?
                GROUP BY d.nama_menu
                ORDER BY total_sold DESC
                LIMIT 5
                """;

        try (
                Connection connection =
                        DatabaseConnection.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setInt(1, kafeId);

            statement.setTimestamp(
                    2,
                    Timestamp.valueOf(
                            startDate.atStartOfDay()
                    )
            );

            statement.setTimestamp(
                    3,
                    Timestamp.valueOf(
                            endDate.atStartOfDay()
                    )
            );

            try (ResultSet rs = statement.executeQuery()) {

                while (rs.next()) {

                    productData.put(
                            rs.getString("nama_menu"),
                            rs.getInt("total_sold")
                    );
                }
            }
        }
    }

    // =========================================
    // GROWTH
    // =========================================

    private void loadGrowth(
            LocalDate previousStart,
            LocalDate previousEnd
    ) throws SQLException {

        String sql = """
                SELECT
                    COUNT(id) AS total_orders,
                    COALESCE(SUM(total), 0) AS total_revenue
                FROM transaksi
                WHERE kafe_id = ?
                AND tanggal >= ?
                AND tanggal < ?
                """;

        double previousRevenue = 0;
        int previousOrders = 0;

        try (
                Connection connection =
                        DatabaseConnection.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setInt(1, kafeId);

            statement.setTimestamp(
                    2,
                    Timestamp.valueOf(
                            previousStart.atStartOfDay()
                    )
            );

            statement.setTimestamp(
                    3,
                    Timestamp.valueOf(
                            previousEnd.atStartOfDay()
                    )
            );

            try (ResultSet rs = statement.executeQuery()) {

                if (rs.next()) {

                    previousOrders =
                            rs.getInt("total_orders");

                    previousRevenue =
                            rs.getDouble("total_revenue");
                }
            }
        }

        if (previousRevenue > 0) {

            revenueGrowth =
                    ((currentRevenue - previousRevenue)
                    / previousRevenue) * 100;
        }

        if (previousOrders > 0) {

            orderGrowth =
                    ((currentOrders - previousOrders)
                    / (double) previousOrders) * 100;
        }
    }

    // =========================================
    // SUMMARY CARDS
    // =========================================

    private void updateSummaryCards() {

        totalRevenueLabel.setText(
                "Rp" + rupiahFormat.format(
                        currentRevenue
                )
        );

        totalOrdersLabel.setText(
                rupiahFormat.format(
                        currentOrders
                )
        );

        double average =
                currentOrders > 0
                        ? currentRevenue / currentOrders
                        : 0;

        averageOrderValueLabel.setText(
                "Rp" + rupiahFormat.format(
                        average
                )
        );
    }

    // =========================================
    // PAYMENT SUMMARY
    // =========================================

    private void updatePaymentSummary() {

        double total = currentRevenue;

        double cashPercentage =
                total > 0 ? cashRevenue / total : 0;

        double cardPercentage =
                total > 0 ? cardRevenue / total : 0;

        double qrisPercentage =
                total > 0 ? qrisRevenue / total : 0;

        cashAmountLabel.setText(
                "Rp" + rupiahFormat.format(
                        cashRevenue
                )
        );

        cardAmountLabel.setText(
                "Rp" + rupiahFormat.format(
                        cardRevenue
                )
        );

        qrisAmountLabel.setText(
                "Rp" + rupiahFormat.format(
                        qrisRevenue
                )
        );

        cashPercentLabel.setText(
                String.format(
                        "%.1f%%",
                        cashPercentage * 100
                )
        );

        cardPercentLabel.setText(
                String.format(
                        "%.1f%%",
                        cardPercentage * 100
                )
        );

        qrisPercentLabel.setText(
                String.format(
                        "%.1f%%",
                        qrisPercentage * 100
                )
        );

        cashProgress.setProgress(cashPercentage);
        cardProgress.setProgress(cardPercentage);
        qrisProgress.setProgress(qrisPercentage);
    }

    // =========================================
    // SALES PERFORMANCE
    // =========================================

    private void updateSalesPerformance() {

        revenueGrowthLabel.setText(
                String.format(
                        "%+.1f%%",
                        revenueGrowth
                )
        );

        orderGrowthLabel.setText(
                String.format(
                        "%+.1f%%",
                        orderGrowth
                )
        );
    }

    // =========================================
    // CHARTS
    // =========================================

    private void updateCharts() {

        XYChart.Series<String, Number> revenueSeries =
                new XYChart.Series<>();

        revenueSeries.setName("Revenue");

        for (
                Map.Entry<String, Double> entry :
                revenueData.entrySet()
        ) {

            revenueSeries.getData().add(
                    new XYChart.Data<>(
                            entry.getKey(),
                            entry.getValue()
                    )
            );
        }

        revenueChart.getData().add(
                revenueSeries
        );

        XYChart.Series<String, Number> productSeries =
                new XYChart.Series<>();

        productSeries.setName("Products Sold");

        for (
                Map.Entry<String, Integer> entry :
                productData.entrySet()
        ) {

            productSeries.getData().add(
                    new XYChart.Data<>(
                            entry.getKey(),
                            entry.getValue()
                    )
            );
        }

        topProductsChart.getData().add(
                productSeries
        );
    }

    // =========================================
    // EXPORT REPORT
    // =========================================

    @FXML
    private void exportReport() {

        FileChooser fileChooser =
                new FileChooser();

        fileChooser.setTitle(
                "Export KAFEPOS Report"
        );

        fileChooser.setInitialFileName(
                "KAFEPOS_Report_" +
                periodFilter.getValue()
                        .replace(" ", "_") +
                ".csv"
        );

        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter(
                        "CSV Files",
                        "*.csv"
                )
        );

        Stage stage =
                (Stage) revenueChart
                        .getScene()
                        .getWindow();

        File file =
                fileChooser.showSaveDialog(stage);

        if (file == null) {
            return;
        }

        try (
                FileWriter writer =
                        new FileWriter(file)
        ) {

            writer.write(
                    "KAFEPOS BUSINESS REPORT\n"
            );

            writer.write(
                    "Period," +
                    periodFilter.getValue() +
                    "\n\n"
            );

            writer.write("SUMMARY\n");

            writer.write(
                    "Total Revenue," +
                    currentRevenue +
                    "\n"
            );

            writer.write(
                    "Total Orders," +
                    currentOrders +
                    "\n"
            );

            writer.write(
                    "Average Order Value," +
                    (currentOrders > 0
                            ? currentRevenue /
                              currentOrders
                            : 0) +
                    "\n\n"
            );

            writer.write("PAYMENT SUMMARY\n");

            writer.write(
                    "Payment Method,Revenue\n"
            );

            writer.write(
                    "Cash," +
                    cashRevenue +
                    "\n"
            );

            writer.write(
                    "Card," +
                    cardRevenue +
                    "\n"
            );

            writer.write(
                    "QRIS," +
                    qrisRevenue +
                    "\n\n"
            );

            writer.write("REVENUE BREAKDOWN\n");

            writer.write(
                    "Period,Revenue\n"
            );

            for (
                    Map.Entry<String, Double> entry :
                    revenueData.entrySet()
            ) {

                writer.write(
                        entry.getKey() +
                        "," +
                        entry.getValue() +
                        "\n"
                );
            }

            writer.write(
                    "\nTOP SELLING PRODUCTS\n"
            );

            writer.write(
                    "Product,Units Sold\n"
            );

            for (
                    Map.Entry<String, Integer> entry :
                    productData.entrySet()
            ) {

                writer.write(
                        entry.getKey() +
                        "," +
                        entry.getValue() +
                        "\n"
                );
            }

            writer.write(
                    "\nSALES PERFORMANCE\n"
            );

            writer.write(
                    "Revenue Growth," +
                    revenueGrowth +
                    "%\n"
            );

            writer.write(
                    "Order Growth," +
                    orderGrowth +
                    "%\n"
            );

            System.out.println(
                    "Report exported successfully!"
            );

        } catch (IOException e) {

            e.printStackTrace();
        }
    }
}