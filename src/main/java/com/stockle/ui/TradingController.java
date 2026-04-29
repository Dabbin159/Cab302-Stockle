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

    // Mock data (used for selected stock detail / recently viewed)
    private List<MockData.Stock> allStocks;
    private List<MockData.Stock> recentlyViewed;
    private final List<String> favorites = new ArrayList<>();
    private MockData.Stock selectedStock;
    private String selectedSymbol = "";

    // Live stock list (Alpaca)
    private List<Asset> liveAssets = new ArrayList<>();
    private int livePageIndex = 0;
    private boolean isLoadingPage = false;
    private int searchGeneration = 0;
    private static final int PAGE_SIZE = 20;

    // API Services
    private ApiClient apiClient;
    private ObjectMapper objectMapper;
    private HistoricalDataService historicalDataService;
    private AssetService assetService;
    private MarketDataService marketDataService;
    private SnapshotService snapshotService;

    @FXML
    public void initialize() {
        apiClient = new ApiClient();
        objectMapper = new ObjectMapper();
        historicalDataService = new HistoricalDataService(apiClient, objectMapper);
        marketDataService = new MarketDataService(apiClient, objectMapper);
        snapshotService = new SnapshotService(apiClient, objectMapper);
        assetService = new AssetService(apiClient, objectMapper, marketDataService);

        allStocks = MockData.allStocks();
        recentlyViewed = MockData.recentlyViewed(allStocks);
        favorites.addAll(MockData.defaultFavorites());
        selectedStock = allStocks.get(0);

        searchField.textProperty().addListener((obs, o, n) -> applyLiveSearch());
        buySharesField.textProperty().addListener((obs, o, n)  -> updateBuyEstimate());
        sellSharesField.textProperty().addListener((obs, o, n) -> updateSellEstimate());

        stockListScroll.vvalueProperty().addListener((obs, o, n) -> {
            if (n.doubleValue() >= 0.9 && !isLoadingPage) loadNextPage();
        });

        buildRecentlyViewed();
        selectStock(selectedStock);

        new Thread(() -> {
            try {
                List<Asset> assets = assetService.getAllAssets();
                liveAssets = assets.stream()
                    .filter(a -> a.tradable && "us_equity".equals(a.assetClass) && a.symbol != null)
                    .collect(java.util.stream.Collectors.toList());
                Platform.runLater(this::loadNextPage);
            } catch (Exception e) {
                System.err.println("Failed to load assets: " + e.getMessage());
            }
        }).start();
    }

    // Stock selection
    private void selectStock(MockData.Stock s) {
        selectedStock = s;
        selectedSymbol = s.symbol();
        stockSymbolLabel.setText(s.symbol());
        stockNameLabel.setText(s.name());
        stockPriceLabel.setText("—");
        stockChangeLabel.setText("—");
        volumeLabel.setText("—");
        marketCapLabel.setText("—");
        exchangeLabel.setText("—");

        boolean isFav = favorites.contains(s.symbol());
        favoriteBtn.setText(isFav ? "★" : "☆");
        favoriteBtn.getStyleClass().setAll("fav-btn");

        buyButton.setText("Buy " + s.symbol());
        sellButton.setText("Sell " + s.symbol());
        alertStockLabel.setText("Alert price for " + s.symbol());
        clearTradeStatus();

        loadChart(s.symbol());
        fetchLivePrice(s.symbol());
        updateBuyEstimate();
        updateSellEstimate();
        updateOwnedSharesLabel();
        refreshStockListSelection();
    }

    private void selectLiveStock(Asset asset) {
        selectedSymbol = asset.symbol;
        stockSymbolLabel.setText(asset.symbol);
        stockNameLabel.setText(asset.name != null ? asset.name : asset.symbol);
        stockPriceLabel.setText("—");
        stockChangeLabel.setText("—");
        volumeLabel.setText("—");
        marketCapLabel.setText("—");
        exchangeLabel.setText(asset.exchange != null ? asset.exchange : "—");

        boolean isFav = favorites.contains(asset.symbol);
        favoriteBtn.setText(isFav ? "★" : "☆");
        favoriteBtn.getStyleClass().setAll("fav-btn");
        buyButton.setText("Buy " + asset.symbol);
        sellButton.setText("Sell " + asset.symbol);
        alertStockLabel.setText("Alert price for " + asset.symbol);

        fetchLivePrice(asset.symbol);
        loadChart(asset.symbol);
    }

    private void fetchLivePrice(String symbol) {
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
                    stockPriceLabel.setText(String.format("$%.2f", fClose));
                    stockChangeLabel.setText(String.format("%s%.2f (%s%.2f%%)", sign, changeAmt, sign, changePct));
                    stockChangeLabel.getStyleClass().setAll(pos ? "stock-change-pos" : "stock-change-neg");
                    volumeLabel.setText(String.format("%,d", fVol));
                });
            } catch (Exception e) {
                System.err.println("Live price fetch failed for " + symbol + ": " + e.getMessage());
            }
        }).start();
    }

    private void loadChart(String symbol) {
        new Thread(() -> {
            try {
                LocalDate today = LocalDate.now();
                List<BarData> bars = historicalDataService.getHistoricalBars(
                    symbol, today.minusDays(1), today, "1Min", "iex");
                List<BarData> last60 = bars.size() > 60 ? bars.subList(bars.size() - 60, bars.size()) : bars;
                DateTimeFormatter timeFmt = DateTimeFormatter.ofPattern("HH:mm");
                List<CandleData> candles = new ArrayList<>();
                for (BarData bar : last60) {
                    candles.add(new CandleData(bar.timestamp.format(timeFmt), bar.open, bar.high, bar.low, bar.close));
                }
                Platform.runLater(() -> priceChart.setCandles(candles));
            } catch (Exception e) {
                System.err.println("Error loading chart for " + symbol + ": " + e.getMessage());
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
        int owned = TradeController.getInstance().getOwnedQuantity(user, selectedStock.symbol());
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

        User refreshed = TradeController.getInstance().refreshUserFromDb(user);
        if (refreshed != null) {
            SessionManager.getInstance().setCurrentUser(refreshed);
        }
    }

    // Live stock list
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

    @FXML
    private void applyLiveSearch() {
        searchGeneration++;
        isLoadingPage = false;
        stockListContainer.getChildren().clear();
        livePageIndex = 0;
        loadNextPage();
    }

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

    // Recently viewed / mock list builders
    private void buildRecentlyViewed() {
        recentlyViewedContainer.getChildren().clear();
        for (MockData.Stock s : recentlyViewed) {
            recentlyViewedContainer.getChildren().add(recentRow(s));
        }
    }

    private void refreshStockListSelection() {
        String sym = selectedStock != null ? selectedStock.symbol() : "";
        stockListContainer.getChildren().forEach(node -> {
            node.getStyleClass().setAll("stock-row");
            if (sym.equals(node.getUserData())) node.getStyleClass().add("stock-row-active");
        });
    }

    private VBox recentRow(MockData.Stock s) {
        Label sym   = label(s.symbol(), "stock-row-symbol");
        Label price = label(String.format("$%.2f", s.price()), "stock-row-name");
        VBox left   = new VBox(2, sym, price);
        HBox.setHgrow(left, Priority.ALWAYS);

        boolean pos = s.changePct() >= 0;
        Label pct   = label((pos ? "+" : "") + s.changePct() + "%", pos ? "stock-row-pos" : "stock-row-neg");

        HBox row  = new HBox(left, pct);
        row.getStyleClass().add("stock-row");
        row.setOnMouseClicked(e -> selectStock(s));
        return new VBox(row);
    }

    private Label label(String text, String... styles) {
        Label l = new Label(text);
        l.getStyleClass().addAll(styles);
        return l;
    }

    // Order actions
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
