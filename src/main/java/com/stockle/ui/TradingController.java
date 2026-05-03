package com.stockle.ui;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stockle.SessionManager;
import com.stockle.api.client.ApiClient;
import com.stockle.api.data.Asset;
import com.stockle.api.data.BarData;
import com.stockle.api.service.AssetService;
import com.stockle.api.service.HistoricalDataService;
import com.stockle.api.service.MarketDataService;
import com.stockle.api.service.SnapshotService;
import com.stockle.model.CandleData;
import com.stockle.model.Stock;
import com.stockle.model.TradeController;
import com.stockle.model.User;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
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
    @FXML private Label exchangeLabel;
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
    @FXML private CheckBox favoritesOnly;
    @FXML private Label alertStockLabel;
    @FXML private VBox recentlyViewedContainer;
    @FXML private VBox stockListContainer;
    @FXML private ScrollPane stockListScroll;

    // Current selected stock data (from live API)
    private String selectedSymbol = "";
    private double currentPrice = 0;
    private String currentExchange = "";
    private double currentChange = 0;
    private double currentChangePercent = 0;
    private long currentVolume = 0;
    private final List<String> favorites = new ArrayList<>();

    // Live stock list (Alpaca)
    private List<Asset> liveAssets = new ArrayList<>();
    private int livePageIndex = 0;
    private boolean isLoadingPage = false;
    private int searchGeneration = 0;
    private int chartLoadGeneration = 0;  // Prevents stale chart data
    private int priceLoadGeneration = 0;  // Prevents stale price data
    private static final int PAGE_SIZE = 20;

    // API Services
    private ApiClient apiClient;
    private ObjectMapper objectMapper;
    private HistoricalDataService historicalDataService;
    private AssetService assetService;
    private MarketDataService marketDataService;
    private SnapshotService snapshotService;

    /**
     * Initialises the controller, sets up listeners and API services, then loads
     * all tradable US equity assets in the background. AAPL is auto-selected on load.
     */
    @FXML
    public void initialize() {
        apiClient = new ApiClient();
        objectMapper = new ObjectMapper();
        historicalDataService = new HistoricalDataService(apiClient, objectMapper);
        marketDataService = new MarketDataService(apiClient, objectMapper);
        snapshotService = new SnapshotService(apiClient, objectMapper);
        assetService = new AssetService(apiClient, objectMapper, marketDataService);

        searchField.textProperty().addListener((obs, o, n) -> applyLiveSearch());
        buySharesField.textProperty().addListener((obs, o, n)  -> updateBuyEstimate());
        sellSharesField.textProperty().addListener((obs, o, n) -> updateSellEstimate());

        stockListScroll.vvalueProperty().addListener((obs, o, n) -> {
            if (n.doubleValue() >= 0.9 && !isLoadingPage) loadNextPage();
        });

        // Clear initially since no stock is selected
        recentlyViewedContainer.getChildren().clear();

        new Thread(() -> {
            try {
                List<Asset> assets = assetService.getAllAssets();
                liveAssets = assets.stream()
                    .filter(a -> a.tradable && "us_equity".equals(a.assetClass) && a.symbol != null)
                    .collect(java.util.stream.Collectors.toList());
                Platform.runLater(() -> {
                    loadNextPage();
                    liveAssets.stream()
                        .filter(a -> "AAPL".equals(a.symbol))
                        .findFirst()
                        .ifPresent(this::selectLiveStock);
                });
            } catch (Exception e) {
                System.err.println("Failed to load assets: " + e.getMessage());
            }
        }).start();
    }

    /**
     * Selects a stock and updates the detail panel, chart, and order form.
     * Increments generation counters so stale in-flight responses are discarded.
     *
     * @param asset the asset to display
     */
    private void selectLiveStock(Asset asset) {
        selectedSymbol = asset.symbol;
        currentExchange = asset.exchange != null ? asset.exchange : "—";
        
        stockSymbolLabel.setText(asset.symbol);
        stockNameLabel.setText(asset.name != null ? asset.name : asset.symbol);
        stockPriceLabel.setText("—");
        stockChangeLabel.setText("—");
        volumeLabel.setText("—");
        marketCapLabel.setText("—");
        exchangeLabel.setText(currentExchange);

        boolean isFav = favorites.contains(asset.symbol);
        favoriteBtn.setText(isFav ? "★" : "☆");
        favoriteBtn.getStyleClass().setAll("fav-btn");
        buyButton.setText("Buy " + asset.symbol);
        sellButton.setText("Sell " + asset.symbol);
        alertStockLabel.setText("Alert price for " + asset.symbol);
        
        clearTradeStatus();
        
        // Increment generation counters to cancel stale requests
        chartLoadGeneration++;
        priceLoadGeneration++;

        fetchLivePrice(asset.symbol);
        loadChart(asset.symbol);
        updateBuyEstimate();
        updateSellEstimate();
        updateOwnedSharesLabel();
        refreshStockListSelection();
    }

    /**
     * Fetches the latest bar for the symbol on a background thread and updates the price labels.
     * Discards the result if the user has switched to a different stock before it arrives.
     *
     * @param symbol the stock ticker to fetch
     */
    private void fetchLivePrice(String symbol) {
        final int gen = priceLoadGeneration;  // Capture current generation
        new Thread(() -> {
            try {
                Map<String, BarData> bars = marketDataService.getLatestBars(List.of(symbol), "iex");
                BarData bar = bars.get(symbol);
                if (bar == null) return;

                double changeAmt = bar.close - bar.open;
                double changePct = bar.open != 0 ? (changeAmt / bar.open) * 100 : 0;
                boolean pos = changePct >= 0;
                String sign = pos ? "+" : "";
                final double fClose = bar.close;
                final long fVol = (long) bar.volume;

                Platform.runLater(() -> {
                    // Only update if this is still the current generation
                    if (gen != priceLoadGeneration || !symbol.equals(selectedSymbol)) return;
                    
                    // Store in fields for use in trades and estimates
                    currentPrice = fClose;
                    currentChange = changeAmt;
                    currentChangePercent = changePct;
                    currentVolume = fVol;
                    
                    stockPriceLabel.setText(String.format("$%.2f", fClose));
                    stockChangeLabel.setText(String.format("%s%.2f (%s%.2f%%)", sign, changeAmt, sign, changePct));
                    stockChangeLabel.getStyleClass().setAll(pos ? "stock-change-pos" : "stock-change-neg");
                    volumeLabel.setText(String.format("%,d", fVol));
                    
                    // Update estimates with new price
                    updateBuyEstimate();
                    updateSellEstimate();
                });
            } catch (Exception e) {
                System.err.println("Live price fetch failed for " + symbol + ": " + e.getMessage());
            }
        }).start();
    }

    /**
     * Fetches the last 60 one-minute bars for the symbol and renders them as a candlestick chart.
     * Discards the result if the user switches stocks before the request completes.
     *
     * @param symbol the stock ticker to chart
     */
    private void loadChart(String symbol) {
        final int gen = chartLoadGeneration;  // Capture current generation
        new Thread(() -> {
            try {
                LocalDate today = LocalDate.now();
                List<BarData> bars = historicalDataService.getHistoricalBars(
                    symbol, today.minusDays(90), today, "1Day", "iex");
                List<BarData> last90 = bars.size() > 90 ? bars.subList(bars.size() - 90, bars.size()) : bars;
                DateTimeFormatter timeFmt = DateTimeFormatter.ofPattern("MM/dd/yyyy");
                List<CandleData> candles = new ArrayList<>();
                for (BarData bar : last90) {
                    candles.add(new CandleData(bar.timestamp.format(timeFmt), bar.open, bar.high, bar.low, bar.close));
                }
                Platform.runLater(() -> {
                    // Only update if this is still the current generation
                    if (gen != chartLoadGeneration || !symbol.equals(selectedSymbol)) return;
                    
                    priceChart.setCandles(candles);
                });
            } catch (Exception e) {
                System.err.println("Error loading chart for " + symbol + ": " + e.getMessage());
            }
        }).start();
    }

    // Estimates
    private void updateBuyEstimate() {
        int quantity = parseQuantity(buySharesField);
        double cost = quantity * currentPrice;
        buyEstimateLabel.setText(String.format("$%.2f", cost));
        buyButton.setDisable(cost <= 0);
    }

    private void updateSellEstimate() {
        int quantity = parseQuantity(sellSharesField);
        double proceeds = quantity * currentPrice;
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
        int owned = TradeController.getInstance().getOwnedQuantity(user, selectedSymbol);
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
            selectedSymbol,
            "N/A",  // sector not available from live API
            (int) Math.round(currentPrice),
            (float) currentChangePercent,
            currentVolume
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

        User refreshed = TradeController.getInstance().refreshUserFromDb(user);
        if (refreshed != null) {
            SessionManager.getInstance().setCurrentUser(refreshed);
        }
    }

    /**
     * Loads the next page of stocks into the list, filtered by the current search query and favourites toggle.
     * Rows appear immediately with placeholder prices; a single background API call fills in prices after.
     */
    private void loadNextPage() {
        if (isLoadingPage || livePageIndex >= liveAssets.size()) return;
        isLoadingPage = true;

        String query    = searchField.getText().toLowerCase().trim();
        boolean favOnly = favoritesOnly.isSelected();

        List<Asset> page = new ArrayList<>();
        int idx = livePageIndex;
        while (idx < liveAssets.size() && page.size() < PAGE_SIZE) {
            Asset a = liveAssets.get(idx++);
            if (!query.isEmpty() &&
                !a.symbol.toLowerCase().contains(query) &&
                !(a.name != null && a.name.toLowerCase().contains(query))) continue;
            if (favOnly && !favorites.contains(a.symbol)) continue;
            page.add(a);
        }
        livePageIndex = idx;

        if (page.isEmpty()) { isLoadingPage = false; return; }

        // Add rows immediately with placeholder prices
        Map<String, VBox> priceBoxes = new java.util.HashMap<>();
        for (Asset asset : page) {
            VBox[] holder = new VBox[1];
            stockListContainer.getChildren().add(liveStockRow(asset, holder));
            priceBoxes.put(asset.symbol, holder[0]);
        }
        isLoadingPage = false;

        // Fetch latest bars in background and fill in prices
        List<String> symbols = page.stream().map(a -> a.symbol).collect(java.util.stream.Collectors.toList());
        final int gen = searchGeneration;
        new Thread(() -> {
            try {
                Map<String, BarData> bars = marketDataService.getLatestBars(symbols, "iex");
                Platform.runLater(() -> {
                    if (gen != searchGeneration) return;
                    bars.forEach((sym, bar) -> {
                        VBox right = priceBoxes.get(sym);
                        if (right == null) return;
                        double changePct = bar.open != 0 ? ((bar.close - bar.open) / bar.open) * 100 : 0;
                        boolean pos = changePct >= 0;
                        right.getChildren().setAll(
                            label(String.format("$%.2f", bar.close), "stock-row-price"),
                            label(String.format("%s%.2f%%", pos ? "+" : "", changePct),
                                  pos ? "stock-row-pos" : "stock-row-neg")
                        );
                    });
                });
            } catch (Exception e) {
                System.err.println("Failed to fetch page prices: " + e.getMessage());
            }
        }).start();
    }

    /**
     * Clears and reloads the stock list using the current search text and filters.
     * Increments the search generation to invalidate any in-flight background threads.
     */
    @FXML
    private void applyLiveSearch() {
        searchGeneration++;
        isLoadingPage = false;
        stockListContainer.getChildren().clear();
        livePageIndex = 0;
        loadNextPage();
    }

    /**
     * Builds a stock list row showing the symbol, name, and a price placeholder.
     * priceBoxHolder[0] is set to the right-side VBox so the caller can update prices in-place.
     *
     * @param asset the asset to display
     * @param priceBoxHolder single-element array; [0] receives the price VBox
     * @return the constructed row
     */
    private VBox liveStockRow(Asset asset, VBox[] priceBoxHolder) {
        String displaySymbol = favorites.contains(asset.symbol) ? asset.symbol + " ★" : asset.symbol;
        Label sym  = label(displaySymbol, "stock-row-symbol");
        Label name = label(asset.name != null ? asset.name : asset.symbol, "stock-row-name");
        VBox left  = new VBox(2, sym, name);
        HBox.setHgrow(left, Priority.ALWAYS);

        VBox right = new VBox(2);
        right.setStyle("-fx-alignment: CENTER_RIGHT;");
        right.getChildren().add(label("—", "stock-row-price"));
        if (priceBoxHolder != null) priceBoxHolder[0] = right;

        HBox row  = new HBox(left, right);
        VBox item = new VBox(row);
        item.getStyleClass().add("stock-row");
        item.setUserData(asset.symbol);
        item.setOnMouseClicked(e -> selectLiveStock(asset));
        return item;
    }

    /**
     * Highlights the currently selected stock row and removes highlighting from all others.
     */
    private void refreshStockListSelection() {
        stockListContainer.getChildren().forEach(node -> {
            node.getStyleClass().setAll("stock-row");
            if (selectedSymbol.equals(node.getUserData())) node.getStyleClass().add("stock-row-active");
        });
    }

    private Label label(String text, String... styles) {
        Label l = new Label(text);
        l.getStyleClass().addAll(styles);
        return l;
    }

    /**
     * Toggles the currently selected stock in/out of favourites and refreshes the stock list.
     */
    @FXML
    private void toggleFavorite() {
        if (selectedSymbol.isEmpty()) return;
        if (favorites.contains(selectedSymbol)) favorites.remove(selectedSymbol);
        else favorites.add(selectedSymbol);
        boolean isFav = favorites.contains(selectedSymbol);
        favoriteBtn.setText(isFav ? "★" : "☆");
        favoriteBtn.getStyleClass().setAll("fav-btn");
        applyLiveSearch();
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
            setBuyStatus("Bought " + quantity + " shares of " + selectedSymbol, true);
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
            setSellStatus("Sold " + quantity + " shares of " + selectedSymbol, true);
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

    // Signs User out, Sends to Login page and syncs user data
    @FXML
    private void handleSignOut() throws IOException {
        SessionManager.getInstance().logout();
        SceneManager.switchTo("auth/auth-view.fxml");
    }

    @FXML private void navAI() throws IOException {
        SceneManager.switchTo("ai/ai-view.fxml");
    }
}
