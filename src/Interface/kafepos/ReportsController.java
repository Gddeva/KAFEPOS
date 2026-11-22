
package com.kafepos;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.Locale;

import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class ReportsController {

        @FXML
        private ComboBox<String> periodFilter;

        @FXML
        private Label totalRevenueLabel;

        @FXML
        private Label totalOrdersLabel;

        @FXML
        private Label averageOrderValueLabel;

        @FXML
        private Label revenueSubtitleLabel;

        @FXML
        private BarChart<String, Number> revenueChart;

        @FXML
        private BarChart<String, Number> topProductsChart;

        @FXML
        private Label cashAmountLabel;

        @FXML
        private Label cardAmountLabel;

        @FXML
        private Label qrisAmountLabel;

        @FXML
        private Label cashPercentLabel;

        @FXML
        private Label cardPercentLabel;

        @FXML
        private Label qrisPercentLabel;

        @FXML
        private ProgressBar cashProgress;

        @FXML
        private ProgressBar cardProgress;

        @FXML
        private ProgressBar qrisProgress;

        @FXML
        private Label revenueGrowthLabel;

        @FXML
        private Label orderGrowthLabel;

        private final NumberFormat rupiahFormat = NumberFormat.getNumberInstance(
                        Locale.forLanguageTag("id-ID"));

        private double currentRevenue;
        private int currentOrders;

        private double cashPercentage;
        private double cardPercentage;
        private double qrisPercentage;

        private double revenueGrowth;
        private double orderGrowth;

        @FXML
        public void initialize() {

                periodFilter.setValue("This Month");

                periodFilter.setOnAction(event -> updateReport());

                updateReport();
        }

        private void updateReport() {

                String period = periodFilter.getValue();

                currentRevenue = 0;
                currentOrders = 0;

                revenueChart.getData().clear();
                topProductsChart.getData().clear();

                XYChart.Series<String, Number> revenueSeries = new XYChart.Series<>();

                XYChart.Series<String, Number> productSeries = new XYChart.Series<>();

                revenueSeries.setName("Revenue");
                productSeries.setName("Products Sold");

                // =================================
                // TODAY
                // =================================

                if ("Today".equals(period)) {

                        currentRevenue = 3200000;
                        currentOrders = 32;

                        revenueSubtitleLabel.setText(
                                        "Revenue performance today");

                        revenueSeries.getData().add(
                                        new XYChart.Data<>("09:00", 500000));
                        revenueSeries.getData().add(
                                        new XYChart.Data<>("12:00", 850000));
                        revenueSeries.getData().add(
                                        new XYChart.Data<>("15:00", 650000));
                        revenueSeries.getData().add(
                                        new XYChart.Data<>("18:00", 1200000));

                        productSeries.getData().add(
                                        new XYChart.Data<>("Coffee", 18));
                        productSeries.getData().add(
                                        new XYChart.Data<>("Latte", 14));
                        productSeries.getData().add(
                                        new XYChart.Data<>("Tea", 10));
                        productSeries.getData().add(
                                        new XYChart.Data<>("Cake", 8));

                        cashPercentage = 0.30;
                        cardPercentage = 0.20;
                        qrisPercentage = 0.50;

                        revenueGrowth = 5.2;
                        orderGrowth = 3.5;

                        // =================================
                        // THIS WEEK
                        // =================================

                } else if ("This Week".equals(period)) {

                        currentRevenue = 14500000;
                        currentOrders = 148;

                        revenueSubtitleLabel.setText(
                                        "Revenue performance this week");

                        revenueSeries.getData().add(
                                        new XYChart.Data<>("Mon", 1500000));
                        revenueSeries.getData().add(
                                        new XYChart.Data<>("Tue", 1800000));
                        revenueSeries.getData().add(
                                        new XYChart.Data<>("Wed", 1200000));
                        revenueSeries.getData().add(
                                        new XYChart.Data<>("Thu", 2100000));
                        revenueSeries.getData().add(
                                        new XYChart.Data<>("Fri", 2500000));
                        revenueSeries.getData().add(
                                        new XYChart.Data<>("Sat", 3000000));
                        revenueSeries.getData().add(
                                        new XYChart.Data<>("Sun", 2400000));

                        productSeries.getData().add(
                                        new XYChart.Data<>("Coffee", 85));
                        productSeries.getData().add(
                                        new XYChart.Data<>("Latte", 72));
                        productSeries.getData().add(
                                        new XYChart.Data<>("Tea", 54));
                        productSeries.getData().add(
                                        new XYChart.Data<>("Cake", 43));

                        cashPercentage = 0.35;
                        cardPercentage = 0.25;
                        qrisPercentage = 0.40;

                        revenueGrowth = 9.8;
                        orderGrowth = 7.4;

                        // =================================
                        // THIS MONTH
                        // =================================

                } else if ("This Month".equals(period)) {

                        currentRevenue = 16500000;
                        currentOrders = 168;

                        revenueSubtitleLabel.setText(
                                        "Revenue performance this month");

                        revenueSeries.getData().add(
                                        new XYChart.Data<>("Week 1", 3500000));
                        revenueSeries.getData().add(
                                        new XYChart.Data<>("Week 2", 4200000));
                        revenueSeries.getData().add(
                                        new XYChart.Data<>("Week 3", 3800000));
                        revenueSeries.getData().add(
                                        new XYChart.Data<>("Week 4", 5000000));

                        productSeries.getData().add(
                                        new XYChart.Data<>("Coffee", 120));
                        productSeries.getData().add(
                                        new XYChart.Data<>("Latte", 105));
                        productSeries.getData().add(
                                        new XYChart.Data<>("Tea", 82));
                        productSeries.getData().add(
                                        new XYChart.Data<>("Cake", 65));

                        cashPercentage = 0.30;
                        cardPercentage = 0.20;
                        qrisPercentage = 0.50;

                        revenueGrowth = 12.5;
                        orderGrowth = 8.2;

                        // =================================
                        // THIS YEAR
                        // =================================

                } else if ("This Year".equals(period)) {

                        currentRevenue = 86000000;
                        currentOrders = 890;

                        revenueSubtitleLabel.setText(
                                        "Revenue performance this year");

                        revenueSeries.getData().add(
                                        new XYChart.Data<>("Jan", 12000000));
                        revenueSeries.getData().add(
                                        new XYChart.Data<>("Feb", 14000000));
                        revenueSeries.getData().add(
                                        new XYChart.Data<>("Mar", 11000000));
                        revenueSeries.getData().add(
                                        new XYChart.Data<>("Apr", 16000000));
                        revenueSeries.getData().add(
                                        new XYChart.Data<>("May", 18000000));
                        revenueSeries.getData().add(
                                        new XYChart.Data<>("Jun", 15000000));

                        productSeries.getData().add(
                                        new XYChart.Data<>("Coffee", 650));
                        productSeries.getData().add(
                                        new XYChart.Data<>("Latte", 580));
                        productSeries.getData().add(
                                        new XYChart.Data<>("Tea", 420));
                        productSeries.getData().add(
                                        new XYChart.Data<>("Cake", 350));

                        cashPercentage = 0.32;
                        cardPercentage = 0.23;
                        qrisPercentage = 0.45;

                        revenueGrowth = 18.6;
                        orderGrowth = 14.3;
                }

                // =================================
                // SUMMARY CARDS
                // =================================

                totalRevenueLabel.setText(
                                "Rp" + rupiahFormat.format(currentRevenue));

                totalOrdersLabel.setText(
                                rupiahFormat.format(currentOrders));

                double averageOrderValue = currentOrders > 0
                                ? currentRevenue / currentOrders
                                : 0;

                averageOrderValueLabel.setText(
                                "Rp" + rupiahFormat.format(averageOrderValue));

                // =================================
                // PAYMENT SUMMARY
                // =================================

                double cashAmount = currentRevenue * cashPercentage;

                double cardAmount = currentRevenue * cardPercentage;

                double qrisAmount = currentRevenue * qrisPercentage;

                cashAmountLabel.setText(
                                "Rp" + rupiahFormat.format(cashAmount));

                cardAmountLabel.setText(
                                "Rp" + rupiahFormat.format(cardAmount));

                qrisAmountLabel.setText(
                                "Rp" + rupiahFormat.format(qrisAmount));

                cashPercentLabel.setText(
                                String.format("%.0f%%", cashPercentage * 100));

                cardPercentLabel.setText(
                                String.format("%.0f%%", cardPercentage * 100));

                qrisPercentLabel.setText(
                                String.format("%.0f%%", qrisPercentage * 100));

                cashProgress.setProgress(cashPercentage);
                cardProgress.setProgress(cardPercentage);
                qrisProgress.setProgress(qrisPercentage);

                // =================================
                // SALES PERFORMANCE
                // =================================

                revenueGrowthLabel.setText(
                                String.format("%+.1f%%", revenueGrowth));

                orderGrowthLabel.setText(
                                String.format("%+.1f%%", orderGrowth));

                // =================================
                // UPDATE CHARTS
                // =================================

                revenueChart.getData().add(revenueSeries);
                topProductsChart.getData().add(productSeries);
        }

        // =================================
        // EXPORT REPORT
        // =================================

        @FXML
        private void exportReport() {

                FileChooser fileChooser = new FileChooser();

                fileChooser.setTitle("Export KAFEPOS Report");

                fileChooser.setInitialFileName(
                                "KAFEPOS_Report_" +
                                                periodFilter.getValue().replace(" ", "_") +
                                                ".csv");

                fileChooser.getExtensionFilters().add(
                                new FileChooser.ExtensionFilter(
                                                "CSV Files", "*.csv"));

                Stage stage = (Stage) revenueChart.getScene().getWindow();

                File file = fileChooser.showSaveDialog(stage);

                if (file == null) {
                        return;
                }

                try (FileWriter writer = new FileWriter(file)) {

                        writer.write("KAFEPOS - BUSINESS REPORT\n");
                        writer.write("Period," + periodFilter.getValue() + "\n\n");

                        writer.write("SUMMARY\n");
                        writer.write("Total Revenue," + currentRevenue + "\n");
                        writer.write("Total Orders," + currentOrders + "\n");
                        writer.write("Average Order Value," +
                                        (currentOrders > 0
                                                        ? currentRevenue / currentOrders
                                                        : 0)
                                        + "\n\n");

                        writer.write("PAYMENT SUMMARY\n");
                        writer.write("Payment Method,Percentage,Revenue\n");
                        writer.write("Cash," + (cashPercentage * 100) + "%," +
                                        (currentRevenue * cashPercentage) + "\n");
                        writer.write("Card," + (cardPercentage * 100) + "%," +
                                        (currentRevenue * cardPercentage) + "\n");
                        writer.write("QRIS," + (qrisPercentage * 100) + "%," +
                                        (currentRevenue * qrisPercentage) + "\n\n");

                        writer.write("SALES PERFORMANCE\n");
                        writer.write("Revenue Growth," + revenueGrowth + "%\n");
                        writer.write("Order Growth," + orderGrowth + "%\n\n");

                        writer.write("REVENUE BREAKDOWN\n");
                        writer.write("Period,Revenue\n");

                        for (XYChart.Data<String, Number> data : revenueChart.getData().get(0).getData()) {

                                writer.write(
                                                data.getXValue() + "," +
                                                                data.getYValue() + "\n");
                        }

                        writer.write("\nTOP SELLING PRODUCTS\n");
                        writer.write("Product,Units Sold\n");

                        for (XYChart.Data<String, Number> data : topProductsChart.getData().get(0).getData()) {

                                writer.write(
                                                data.getXValue() + "," +
                                                                data.getYValue() + "\n");
                        }

                        System.out.println(
                                        "Report exported successfully!");

                } catch (IOException e) {

                        e.printStackTrace();
                }
        }
}