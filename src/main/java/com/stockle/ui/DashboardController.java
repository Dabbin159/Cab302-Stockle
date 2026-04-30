package com.stockle.ui;
import java.io.IOException;
import java.text.NumberFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

import com.stockle.SessionManager;
import com.stockle.database.SQLHoldingDAO;
import com.stockle.database.SQLTradeDAO;
import com.stockle.database.SQLUserDAO;
import com.stockle.model.Holding;
import com.stockle.model.Trade;
import com.stockle.model.User;

import com.stockle.SessionManager;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

@SuppressWarnings("unused")
public class DashboardController {
    private static final NumberFormat CURRENCY = NumberFormat.getCurrencyInstance();
    private static final DateTimeFormatter TRADE_TIME_FMT =
        DateTimeFormatter.ofPattern("MMM d, HH:mm").withZone(ZoneId.systemDefault());

    @FXML private Label totalValueLabel;
    @FXML private Label totalGainLabel;
    @FXML private Label buyingPowerLabel;
    @FXML private AreaChart<String, Number> portfolioChart;
    @FXML private VBox holdingsContainer;
    @FXML private VBox tradesContainer;

    @FXML
    public void initialize() {
        loadChart();

        User currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser == null) {
            loadMockSummaryAndLists();
            return;
        }

        User freshUser = SQLUserDAO.getInstance().getUserById(currentUser.getId());
        if (freshUser == null) {
            loadMockSummaryAndLists();
            return;
        }

        SessionManager.getInstance().setCurrentUser(freshUser);
        long holdingsValue = loadHoldings(freshUser.getId());
        loadTrades(freshUser.getId());
        loadSummary(freshUser, holdingsValue);
    }

    private void loadMockSummaryAndLists() {
        MockData.DashboardSummary summary = MockData.dashboardSummary();
        totalValueLabel.setText(summary.totalValue());
        totalGainLabel.setText(summary.totalGain());
        buyingPowerLabel.setText(summary.buyingPower());
        loadHoldingsMock();
        loadTradesMock();
    }

    private void loadChart() {
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        for (MockData.DashboardChartPoint point : MockData.dashboardChart()) {
            series.getData().add(new XYChart.Data<>(point.label(), point.value()));
        }
        portfolioChart.getData().add(series);
    }

    private void loadHoldingsMock() {
        holdingsContainer.getChildren().clear();
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

    private void loadTradesMock() {
        tradesContainer.getChildren().clear();
        for (MockData.DashboardTrade trade : MockData.dashboardTrades()) {
            tradesContainer.getChildren().add(
                tradeRow(trade.type(), trade.symbol(), trade.detail(), trade.time())
            );
        }
    }

    private long loadHoldings(int userId) {
        holdingsContainer.getChildren().clear();
        List<Holding> holdings = SQLHoldingDAO.getInstance().getUserHoldings(userId);
        if (holdings == null || holdings.isEmpty()) {
            holdingsContainer.getChildren().add(styledLabel("No holdings yet", "row-sub"));
            return 0L;
        }

        long totalHoldingsValue = 0L;
        for (Holding holding : holdings) {
            long costBasis = (long) holding.getAveragePrice() * holding.getQuantity();
            totalHoldingsValue += costBasis;
            holdingsContainer.getChildren().add(
                holdingRow(
                    holding.getCompanyID(),
                    holding.getCompanyID(),
                    holding.getQuantity() + " shares",
                    CURRENCY.format(holding.getAveragePrice()),
                    CURRENCY.format(costBasis),
                    true
                )
            );
        }
        return totalHoldingsValue;
    }

    private void loadTrades(int userId) {
        tradesContainer.getChildren().clear();
        Trade[] trades = SQLTradeDAO.getInstance().getTradesByUserId(userId);
        if (trades == null || trades.length == 0) {
            tradesContainer.getChildren().add(styledLabel("No trades yet", "row-sub"));
            return;
        }

        int shown = 0;
        for (int i = trades.length - 1; i >= 0 && shown < 8; i--) {
            Trade trade = trades[i];
            String type = trade.isType() ? "SELL" : "BUY";
            String symbol = trade.getStock() != null ? trade.getStock().getCompanyName() : "-";
            long pricePerShare = trade.getQuantity() > 0 ? trade.getTotalValue() / trade.getQuantity() : 0L;
            String detail = trade.getQuantity() + " shares @ " + CURRENCY.format(pricePerShare);
            tradesContainer.getChildren().add(tradeRow(type, symbol, detail, formatTradeTime(trade.getTimeStamp())));
            shown++;
        }
    }

    private void loadSummary(User user, long holdingsValue) {
        long portfolioValue = user.getBalance() + holdingsValue;
        totalValueLabel.setText(CURRENCY.format(portfolioValue));
        buyingPowerLabel.setText(CURRENCY.format(user.getBalance()));

        long totalProfit = user.getTotalProfit();
        String sign = totalProfit >= 0 ? "+" : "-";
        totalGainLabel.setText(sign + CURRENCY.format(Math.abs(totalProfit)));
        totalGainLabel.getStyleClass().setAll(totalProfit >= 0 ? "stat-positive" : "stat-negative");
    }

    private String formatTradeTime(String timestamp) {
        try {
            long epochMs = Long.parseLong(timestamp);
            return TRADE_TIME_FMT.format(Instant.ofEpochMilli(epochMs));
        } catch (NumberFormatException | DateTimeParseException ex) {
            return timestamp;
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

    // Signs User out, Sends to Login page and syncs user data
    @FXML
    private void handleSignOut() throws IOException {
        SessionManager.getInstance().logout();
        SceneManager.switchTo("auth/auth-view.fxml");
    }
}
