package com.stockle.ui;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stockle.SessionManager;
import com.stockle.api.client.ApiClient;
import com.stockle.api.data.BarData;
import com.stockle.api.service.HistoricalDataService;
import com.stockle.database.SQLHoldingDAO;
import com.stockle.database.SQLUserDAO;
import com.stockle.model.CandleData;
import com.stockle.model.Holding;
import com.stockle.model.Stock;
import com.stockle.model.TradeController;
import com.stockle.model.User;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

@SuppressWarnings("unused")
public class TradingController {
    // Stock detail
    @FXML private Label stockSymbolLabel;
    @FXML private Label stockNameLabel;
    @FXML private Label stockPriceLabel;
    @FXML private Label stockChangeLabel;
    @FXML private Label volumeLabel;
    @FXML private Label marketCapLabel;
    @FXML private Label sectorLabel;
    @FXML private Button favoriteBtn;
    @FXML private CandleStickChart priceChart;

    // Order form
    @FXML private TextField buySharesField;
    @FXML private Label buyEstimateLabel;
    @FXML private Label buyStatusLabel;
    @FXML private Button buyButton;
    @FXML private TextField sellSharesField;
    @FXML private Label sellEstimateLabel;
    @FXML private Label sellStatusLabel;
    @FXML private Button sellButton;
    @FXML private Label ownedSharesLabel;

    // Right panel
    @FXML private TextField searchField;
    @FXML private ComboBox<String> sectorFilter;
    @FXML private CheckBox favoritesOnly;
    @FXML private Label alertStockLabel;
    @FXML private VBox recentlyViewedContainer;
    @FXML private VBox stockListContainer;

    // Data
    private List<MockData.Stock> allStocks;
    private List<MockData.Stock> recentlyViewed;
    private final List<String> favorites = new ArrayList<>();
    private MockData.Stock selectedStock;
    private List<MockData.Stock> filteredStocks;

    // API Services
    private ApiClient apiClient;
    private ObjectMapper objectMapper;
    private HistoricalDataService historicalDataService;

    @FXML
    public void initialize() {
        // Initialize API services
        apiClient = new ApiClient();
        objectMapper = new ObjectMapper();
        historicalDataService = new HistoricalDataService(apiClient, objectMapper);

        allStocks = MockData.allStocks();
        recentlyViewed = MockData.recentlyViewed(allStocks);
        favorites.addAll(MockData.defaultFavorites());
        selectedStock = allStocks.get(0);
        filteredStocks = new ArrayList<>(allStocks);
        sectorFilter.getItems().addAll("All Sectors","Technology","Financial","Healthcare","Consumer","Automotive");
        sectorFilter.getSelectionModel().selectFirst();
        searchField.textProperty().addListener((obs, o, n) -> applyFilters());
        buySharesField.textProperty().addListener((obs, o, n)  -> updateBuyEstimate());
        sellSharesField.textProperty().addListener((obs, o, n) -> updateSellEstimate());
        buildRecentlyViewed();
        buildStockList();
        selectStock(selectedStock);
    }

    // Stock selection
    private void selectStock(MockData.Stock s) {
        selectedStock = s;
        stockSymbolLabel.setText(s.symbol());
        stockNameLabel.setText(s.name());
        stockPriceLabel.setText(String.format("$%.2f", s.price()));

        boolean pos = s.change() >= 0;
        String sign  = pos ? "+" : "";
        stockChangeLabel.setText(String.format("%s%.2f (%s%.2f%%)", sign, s.change(), sign, s.changePct()));
        stockChangeLabel.getStyleClass().setAll(pos ? "stock-change-pos" : "stock-change-neg");

        volumeLabel.setText(s.volume());
        marketCapLabel.setText(s.marketCap());
        sectorLabel.setText(s.sector());

        boolean isFav = favorites.contains(s.symbol());
        favoriteBtn.setText(isFav ? "★" : "☆");
        favoriteBtn.getStyleClass().setAll(isFav ? "fav-btn fav-btn-active" : "fav-btn");

        buyButton.setText("Buy " + s.symbol());
        sellButton.setText("Sell " + s.symbol());
        alertStockLabel.setText("Alert price for " + s.symbol());
        clearTradeStatus();

        loadChart(s);
        updateBuyEstimate();
        updateSellEstimate();
        updateOwnedSharesLabel();
        refreshStockListSelection();
    }

    private void loadChart(MockData.Stock s) {
        new Thread(() -> {
            try {
                // Fetch the latest 60 one-minute bars
                LocalDate today = LocalDate.now();
                List<BarData> bars = historicalDataService.getHistoricalBars(
                    s.symbol(),
                    today.minusDays(1),
                    today,
                    "1Min",
                    "iex"
                );

                // Keep only the last 60 bars
                List<BarData> last60 = bars.size() > 60
                    ? bars.subList(bars.size() - 60, bars.size())
                    : bars;

                // Convert BarData to CandleData
                List<CandleData> candles = new ArrayList<>();
                for (BarData bar : last60) {
                    candles.add(new CandleData(
                        bar.timestamp.toString(),
                        bar.open,
                        bar.high,
                        bar.low,
                        bar.close
                    ));
                }

                // Update UI on JavaFX thread
                javafx.application.Platform.runLater(() -> priceChart.setCandles(candles));
            } catch (Exception e) {
                System.err.println("Error loading chart for " + s.symbol() + ": " + e.getMessage());
            }
        }).start();
    }

    // Estimates
    private void updateBuyEstimate() {
        int quantity = parseQuantity(buySharesField);
        double cost = quantity * selectedStock.price();
        buyEstimateLabel.setText(String.format("$%.2f", cost));
        buyButton.setDisable(cost <= 0);
    }

    private void updateSellEstimate() {
        int quantity = parseQuantity(sellSharesField);
        double proceeds = quantity * selectedStock.price();
        sellEstimateLabel.setText(String.format("$%.2f", proceeds));
        sellButton.setDisable(proceeds <= 0);
    }

    private void updateOwnedSharesLabel() {
        if (ownedSharesLabel == null) {
            return;
        }
        User user = currentUser();
        if (user == null) {
            ownedSharesLabel.setText("Sign in to see owned shares");
            return;
        }
        Holding holding = SQLHoldingDAO.getInstance().getHolding(user.getId(), selectedStock.symbol());
        int owned = holding != null ? holding.getQuantity() : 0;
        ownedSharesLabel.setText("You own " + owned + " shares");
    }

    private void clearTradeStatus() {
        if (buyStatusLabel != null) {
            buyStatusLabel.setText("");
        }
        if (sellStatusLabel != null) {
            sellStatusLabel.setText("");
        }
    }

    private void setBuyStatus(String message, boolean success) {
        if (buyStatusLabel == null) {
            return;
        }
        buyStatusLabel.setVisible(true);
        buyStatusLabel.setManaged(true);
        buyStatusLabel.setText(message);
        if (success) {
            buyStatusLabel.getStyleClass().setAll("trade-status", "trade-status-success");
        } else {
            buyStatusLabel.getStyleClass().setAll("trade-status", "trade-status-error");
        }
    }

    private void setSellStatus(String message, boolean success) {
        if (sellStatusLabel == null) {
            return;
        }
        sellStatusLabel.setText(message);
        if (success) {
            sellStatusLabel.getStyleClass().setAll("trade-status", "trade-status-success");
        } else {
            sellStatusLabel.getStyleClass().setAll("trade-status", "trade-status-error");
        }
    }

    private int parseQuantity(TextField field) {
        try {
            return Math.max(0, Integer.parseInt(field.getText().trim()));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private long parseVolume(String volumeText) {
        if (volumeText == null || volumeText.isBlank()) {
            return 0L;
        }
        
        String normalised = volumeText.trim().toUpperCase();
        double multiplier = 1d;

        if (normalised.endsWith("B")) {
            multiplier = 1_000_000_000d;
            normalised = normalised.substring(0, normalised.length() - 1);
        } else if (normalised.endsWith("M")) {
            multiplier = 1_000_000d;
            normalised = normalised.substring(0, normalised.length() - 1);
        } else if (normalised.endsWith("K")) {
            multiplier = 1_000d;
            normalised = normalised.substring(0, normalised.length() - 1);
        }

        try {
            return Math.round(Double.parseDouble(normalised) * multiplier);
        } catch (NumberFormatException e) {
            return 0L;
        }
    }

    private Stock toTradeStock() {
        return new Stock(
            selectedStock.symbol(),
            selectedStock.sector(),
            (int) Math.round(selectedStock.price()),
            (float) selectedStock.changePct(),
            parseVolume(selectedStock.volume())
        );   
    }

    private User currentUser() {
        return SessionManager.getInstance().getCurrentUser();
    }

    private void refreshCurrentUser() {
        User user = currentUser();
        if (user == null) {
            return;
        }

        User refreshed = SQLUserDAO.getInstance().getUserById(user.getId());
        if (refreshed != null) {
            SessionManager.getInstance().setCurrentUser(refreshed);
        }
    }

    // Filters / list
    @FXML
    private void applyFilters() {
        String query  = searchField.getText().toLowerCase().trim();
        String sector = sectorFilter.getValue();
        boolean favOnly = favoritesOnly.isSelected();

        filteredStocks = allStocks.stream()
            .filter(s -> query.isEmpty()
                || s.symbol().toLowerCase().contains(query)
                || s.name().toLowerCase().contains(query))
            .filter(s -> sector == null || sector.equals("All Sectors") || s.sector().equals(sector))
            .filter(s -> !favOnly || favorites.contains(s.symbol()))
            .toList();

        buildStockList();
    }

    // List builders
    private void buildRecentlyViewed() {
        recentlyViewedContainer.getChildren().clear();
        for (MockData.Stock s : recentlyViewed) {
            recentlyViewedContainer.getChildren().add(recentRow(s));
        }
    }

    private void buildStockList() {
        stockListContainer.getChildren().clear();
        for (MockData.Stock s : filteredStocks) {
            stockListContainer.getChildren().add(stockRow(s));
        }
    }

    private void refreshStockListSelection() {
        stockListContainer.getChildren().forEach(node -> {
            Object tag = node.getUserData();
            boolean active = tag instanceof MockData.Stock st && st.symbol().equals(selectedStock.symbol());
            node.getStyleClass().setAll("stock-row");
            if (active) node.getStyleClass().add("stock-row-active");
        });
    }

    private VBox recentRow(MockData.Stock s) {
        Label sym  = label(s.symbol(), "stock-row-symbol");
        Label price = label(String.format("$%.2f", s.price()), "stock-row-name");
        VBox left = new VBox(2, sym, price);
        HBox.setHgrow(left, Priority.ALWAYS);

        boolean pos = s.changePct() >= 0;
        Label pct = label((pos ? "+" : "") + s.changePct() + "%", pos ? "stock-row-pos" : "stock-row-neg");

        HBox row = new HBox(left, pct);
        row.getStyleClass().add("stock-row");
        row.setOnMouseClicked(e -> selectStock(s));
        return new VBox(row);
    }

    private VBox stockRow(MockData.Stock s) {
        boolean active = s.symbol().equals(selectedStock.symbol());
        boolean pos = s.change() >= 0;

        String displaySymbol = favorites.contains(s.symbol()) ? s.symbol() + " ★" : s.symbol();
        Label sym = label(displaySymbol, "stock-row-symbol");
        Label name = label(s.name(), "stock-row-name");
        VBox left = new VBox(2, sym, name);
        HBox.setHgrow(left, Priority.ALWAYS);

        Label price = label(String.format("$%.2f", s.price()), "stock-row-price");
        Label pct = label((pos ? "+" : "") + s.changePct() + "%", pos ? "stock-row-pos" : "stock-row-neg");
        VBox right  = new VBox(2, price, pct);
        right.setStyle("-fx-alignment: CENTER_RIGHT;");

        HBox row = new HBox(left, right);
        VBox item = new VBox(row);
        item.getStyleClass().add("stock-row");
        if (active) item.getStyleClass().add("stock-row-active");
        item.setUserData(s);
        item.setOnMouseClicked(e -> selectStock(s));
        return item;
    }

    private Label label(String text, String... styles) {
        Label l = new Label(text);
        l.getStyleClass().addAll(styles);
        return l;
    }

    // Order actions
    @FXML
    private void toggleFavorite() {
        String sym = selectedStock.symbol();
        if (favorites.contains(sym)) favorites.remove(sym);
        else favorites.add(sym);
        boolean isFav = favorites.contains(sym);
        favoriteBtn.setText(isFav ? "★" : "☆");
        if (favoritesOnly.isSelected()) applyFilters();
        else buildStockList();
    }

    @FXML
    private void handleSetAlert() {}

    @FXML
    private void handleBuy() {
        User user = currentUser();
        if (user == null) {
            setBuyStatus("Please sign in to trade.", false);
            return;
        }

        int quantity = parseQuantity(buySharesField);
        if (quantity <= 0) {
            setBuyStatus("Please enter a valid quantity.", false);
            return;
        }

        boolean success = TradeController.getInstance().executeBuy(user, toTradeStock(), quantity);
        if (success) {
            refreshCurrentUser();
            buySharesField.clear();
            setBuyStatus("Bought " + quantity + " shares of " + selectedStock.symbol(), true);
            updateBuyEstimate();
            updateSellEstimate();
            updateOwnedSharesLabel();
        } else {
            setBuyStatus("Insufficient balance to buy " + quantity + " shares.", false);
        }
    }

    @FXML
    private void handleSell() {
        User user = currentUser();
        if (user == null) {
            setSellStatus("Please sign in to trade.", false);
            return;
        }

        int quantity = parseQuantity(sellSharesField);
        if (quantity <= 0) {
            setSellStatus("Please enter a valid quantity.", false);
            return;
        }

        boolean success = TradeController.getInstance().executeSell(user, toTradeStock(), quantity);
        if (success) {
            refreshCurrentUser();
            sellSharesField.clear();
            setSellStatus("Sold " + quantity + " shares of " + selectedStock.symbol(), true);
            updateBuyEstimate();
            updateSellEstimate();
            updateOwnedSharesLabel();
        } else {
            setSellStatus("You don't have " + quantity + " shares to sell.", false);
        }
    }

    // Navigation
    @FXML
    private void navDashboard() throws IOException {
        SceneManager.switchTo("dashboard/dashboard-view.fxml");
    }

    @FXML
    private void handleSignOut() throws IOException {
        SessionManager.getInstance().logout();
        SceneManager.switchTo("auth/auth-view.fxml");
    }

    @FXML private void navAI() throws IOException {
        SceneManager.switchTo("ai/ai-view.fxml");
    }
}
