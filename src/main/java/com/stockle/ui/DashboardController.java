package com.stockle.ui;

import java.io.IOException;

import javafx.fxml.FXML;
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

public class DashboardController {
    @FXML private Label totalValueLabel;
    @FXML private Label totalGainLabel;
    @FXML private Label buyingPowerLabel;
    @FXML private AreaChart<String, Number> portfolioChart;
    @FXML private VBox holdingsContainer;
    @FXML private VBox tradesContainer;
    @FXML
    // Initialize with placeholder data
    public void initialize() {
        totalValueLabel.setText("$115,000");
        totalGainLabel.setText("+$15,000 (15.0%)");
        buyingPowerLabel.setText("$35,000");

        loadChart();
        loadHoldings();
        loadTrades();
    }

    // Loading placeholder chart data, might be replaced with API graph
    private void loadChart() {
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.getData().add(new XYChart.Data<>("Jan", 100000));
        series.getData().add(new XYChart.Data<>("Feb", 105000));
        series.getData().add(new XYChart.Data<>("Mar", 103000));
        series.getData().add(new XYChart.Data<>("Apr", 112000));
        series.getData().add(new XYChart.Data<>("May", 118000));
        series.getData().add(new XYChart.Data<>("Jun", 115000));
        portfolioChart.getData().add(series);
    }

    
}
