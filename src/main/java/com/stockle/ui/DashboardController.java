package com.stockle.ui;
import java.io.IOException;

import javafx.fxml.FXML;
import javafx.scene.Node;
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

    // Loading placeholder holdings
    private void loadHoldings() {
        Object[][] data = {
            {"AAPL", "Apple Inc.", "50 shares", "$182.52", "+1.3%",  true},
            {"MSFT", "Microsoft Corp.", "30 shares", "$420.15", "-0.76%", false},
            {"GOOGL","Alphabet Inc.", "25 shares", "$142.38", "+1.34%", true},
            {"TSLA", "Tesla Inc.", "15 shares", "$248.50", "+2.15%", true},
        };
        for (Object[] row : data) {
            holdingsContainer.getChildren().add(
                holdingRow((String)row[0], (String)row[1],
                           (String)row[2], (String)row[3],
                           (String)row[4], (boolean)row[5])
            );
        }
    }

    // Loading placeholder trades
    private void loadTrades() {
        Object[][] data = {
            {"BUY", "AAPL", "10 shares @ $180.25", "2h ago"},
            {"SELL", "NVDA", "5 shares @ $875.30", "5h ago"},
            {"BUY", "TSLA", "8 shares @ $245.00", "1d ago"},
        };
        for (Object[] row : data) {
            tradesContainer.getChildren().add(
                tradeRow((String)row[0], (String)row[1],
                         (String)row[2], (String)row[3])
            );
        }
    }

    // Helper method to create a holding row
    private HBox holdingRow(String symbol, String name, String shares, String price, String change, boolean positive) {
        VBox left = new VBox(2,
            styledLabel(symbol, "row-symbol"),
            styledLabel(name, "row-sub"),
            styledLabel(shares, "row-sub")
        );

        VBox right = new VBox(2,
            styledLabel(price, "row-price"),
            styledLabel(change, positive ? "row-positive" : "row-negative")
        );
        right.getStyleClass().add("row-right");
        return cardRow(left, spacer(), right);
    }

    // Helper method to create a trading row
    private HBox tradeRow(String type, String symbol, String detail, String time) {
        Label badge = styledLabel(type, type.equals("BUY") ? "badge-buy" : "badge-sell", "trade-badge");
        VBox info = new VBox(2, styledLabel(symbol, "row-symbol"), styledLabel(detail, "row-sub"));
        info.getStyleClass().add("trade-info");
        Label timeLabel = styledLabel(time, "trade-time");
        return cardRow(badge, info, spacer(), timeLabel);
    }

    private HBox cardRow(Node... children) {
        HBox row = new HBox(children);
        row.getStyleClass().add("row-card");
        return row;
    }

    private Label styledLabel(String text, String... styleClasses) {
        Label label = new Label(text);
        label.getStyleClass().addAll(styleClasses);
        return label;
    }

    private Region spacer() {
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        return spacer;
    }

    // Navigation handlers (placeholders for now)
    @FXML private void navDashboard() {}

    @FXML private void navTrading() throws IOException {
        SceneManager.switchTo("trading/trading-view.fxml");
    }

    @FXML private void navAI() {}

    @FXML
    private void handleSignOut() throws IOException {
        SceneManager.switchTo("auth/auth-view.fxml");
    }
}
