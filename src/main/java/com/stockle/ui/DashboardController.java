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
        HBox row = new HBox();
        row.getStyleClass().add("row-card");

        VBox left = new VBox(2);
        Label sym = new Label(symbol);
        sym.getStyleClass().add("row-symbol");
        Label sh = new Label(shares);
        sh.getStyleClass().add("row-sub");
        left.getChildren().addAll(sym, sh);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        VBox right = new VBox(2);
        right.setStyle("-fx-alignment: CENTER_RIGHT;");
        Label pr = new Label(price);
        pr.getStyleClass().add("row-price");
        Label ch = new Label(change);
        ch.getStyleClass().add(positive ? "row-positive" : "row-negative");
        right.getChildren().addAll(pr, ch);

        row.getChildren().addAll(left, spacer, right);
        return row;
    }

    // Helper method to create a trading row
    private HBox tradeRow(String type, String symbol, String detail, String time) {
        HBox row = new HBox();
        row.getStyleClass().add("row-card");

        Label badge = new Label(type);
        badge.getStyleClass().add(type.equals("BUY") ? "badge-buy" : "badge-sell");
        badge.setStyle(badge.getStyle() + "; -fx-translate-y: 2;");

        VBox info = new VBox(2);
        info.setStyle("-fx-padding: 0 0 0 10;");
        Label sym = new Label(symbol);
        sym.getStyleClass().add("row-symbol");
        Label det = new Label(detail);
        det.getStyleClass().add("row-sub");
        info.getChildren().addAll(sym, det);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label t = new Label(time);
        t.getStyleClass().add("trade-time");
        t.setStyle("-fx-translate-y: 4;");

        row.getChildren().addAll(badge, info, spacer, t);
        return row;
    }

    // Navigation handlers (placeholders for now)
    @FXML private void navDashboard() {}

    @FXML private void navTrading() {}

    @FXML private void navAI() {}

    @FXML
    private void handleSignOut() throws IOException {
        SceneManager.switchTo("auth/auth-view.fxml");
    }
}
