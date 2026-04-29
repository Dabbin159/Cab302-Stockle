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
    public void initialize() {
        MockData.DashboardSummary summary = MockData.dashboardSummary();
        totalValueLabel.setText(summary.totalValue());
        totalGainLabel.setText(summary.totalGain());
        buyingPowerLabel.setText(summary.buyingPower());
        loadChart();
        loadHoldings();
        loadTrades();
    }

    private void loadChart() {
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        for (MockData.DashboardChartPoint point : MockData.dashboardChart()) {
            series.getData().add(new XYChart.Data<>(point.label(), point.value()));
        }
        portfolioChart.getData().add(series);
    }

    private void loadHoldings() {
        for (MockData.DashboardHolding holding : MockData.dashboardHoldings()) {
            holdingsContainer.getChildren().add(
                holdingRow(
                    holding.symbol(),
                    holding.name(),
                    holding.shares(),
                    holding.price(),
                    holding.change(),
                    holding.positive()
                )
            );
        }
    }

    private void loadTrades() {
        for (MockData.DashboardTrade trade : MockData.dashboardTrades()) {
            tradesContainer.getChildren().add(
                tradeRow(trade.type(), trade.symbol(), trade.detail(), trade.time())
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

    @FXML private void navAI() throws IOException {
        SceneManager.switchTo("ai/ai-view.fxml");
    }

    @FXML
    private void handleSignOut() throws IOException {
        SceneManager.switchTo("auth/auth-view.fxml");
    }
}
