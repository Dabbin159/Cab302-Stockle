package com.stockle.ui;
import java.io.IOException;
import java.text.NumberFormat;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
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

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

@SuppressWarnings("unused")
public class DashboardController {
    private static final ZoneId MARKET_ZONE = ZoneId.of("America/New_York");
    private static final LocalTime MARKET_OPEN = LocalTime.of(9, 30);
    private static final LocalTime MARKET_CLOSE = LocalTime.of(16, 0);

    private static final NumberFormat CURRENCY = NumberFormat.getCurrencyInstance();
    private static final DateTimeFormatter TRADE_TIME_FMT =
        DateTimeFormatter.ofPattern("MMM d, HH:mm").withZone(ZoneId.systemDefault());

    @FXML private Label marketStatusLabel;
    @FXML private Label totalValueLabel;
    @FXML private Label totalGainLabel;
    @FXML private Label buyingPowerLabel;
    @FXML private Label totalTradesLabel;
    @FXML private Label totalTradesSubLabel;
    @FXML private AreaChart<String, Number> portfolioChart;
    @FXML private VBox holdingsContainer;
    @FXML private VBox tradesContainer;
    @FXML private javafx.scene.control.Button darkModeBtn;
    @FXML private javafx.scene.image.ImageView darkModeIcon;

    @FXML
    public void initialize() {
        updateMarketStatus();
        syncThemeButton();
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

    @FXML
    private void toggleDarkMode() {
        if (marketStatusLabel == null) return;
        Scene scene = marketStatusLabel.getScene();
        if (scene == null) return;
        Parent root = scene.getRoot();

        SessionManager sessionManager = SessionManager.getInstance();
        sessionManager.setDarkModeEnabled(!sessionManager.isDarkModeEnabled());
        SceneManager.applyTheme(root);
        syncThemeButton();
    }

    private void syncThemeButton() {
        if (darkModeIcon == null) {
            return;
        }

        boolean darkModeEnabled = SessionManager.getInstance().isDarkModeEnabled();
        String iconResource = darkModeEnabled
            ? "/com/stockle/ui/images/light-mode-button.png"
            : "/com/stockle/ui/images/dark-mode-button.png";

        java.net.URL iconUrl = getClass().getResource(iconResource);
        if (iconUrl != null) {
            darkModeIcon.setImage(new javafx.scene.image.Image(iconUrl.toExternalForm()));
        }
    }

    public static boolean isMarketOpenAtEt(ZonedDateTime etNow) {
        DayOfWeek d = etNow.getDayOfWeek();
        LocalTime t = etNow.toLocalTime();
        boolean weekday = d != DayOfWeek.SATURDAY && d != DayOfWeek.SUNDAY;
        return weekday && !t.isBefore(LocalTime.of(9, 30)) && t.isBefore(LocalTime.of(16, 0));
    }

    private void updateMarketStatus() {
        ZonedDateTime nowEt = ZonedDateTime.now(MARKET_ZONE);
        DayOfWeek day = nowEt.getDayOfWeek();
        LocalTime time = nowEt.toLocalTime();

        boolean weekday = day != DayOfWeek.SATURDAY && day != DayOfWeek.SUNDAY;
        boolean inSession = !time.isBefore(MARKET_OPEN) && !time.isAfter(MARKET_CLOSE);
        boolean open = weekday && inSession;

        marketStatusLabel.setText(open ? "OPEN" : "CLOSED");
        marketStatusLabel.getStyleClass().removeAll("market-open", "market-closed");
        marketStatusLabel.getStyleClass().add(open ? "market-open" : "market-closed");
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
        portfolioChart.getData().clear();
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

        // Load trade statistics (total trades and this month's trades)
        loadTradeStats(user.getId());
    }

    private void loadTradeStats(int userId) {
        if (totalTradesLabel == null || totalTradesSubLabel == null) return;
        int total = SQLTradeDAO.getInstance().getTotalTradesCountByUser(userId);
        int monthCount = SQLTradeDAO.getInstance().getTradesCountByUserLastMonth(userId);
        totalTradesLabel.setText(String.valueOf(total));
        totalTradesSubLabel.setText(monthCount + " this month");
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

    @FXML private void navNews() throws IOException {
        SceneManager.switchTo("news/news-view.fxml");
    }

    @FXML private void navLeaderboard() throws IOException {
        SceneManager.switchTo("leaderboard/leaderboard-view.fxml");
    }

    // Signs User out, Sends to Login page and syncs user data
    @FXML
    private void handleSignOut() throws IOException {
        SessionManager.getInstance().logout();
        SceneManager.switchTo("auth/auth-view.fxml");
    }
}
