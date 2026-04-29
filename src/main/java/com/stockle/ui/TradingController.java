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
import com.stockle.model.CandleData;
import com.stockle.model.Stock;
import com.stockle.model.TradeController;
import com.stockle.model.User;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

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
    @FXML private Button buyButton;
    @FXML private TextField sellSharesField;
    @FXML private Label sellEstimateLabel;
    @FXML private Button sellButton;

    // Right panel
    @FXML private TextField searchField;
    @FXML private ComboBox<String> sectorFilter;
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

    @FXML
    public void initialize() {
        apiClient = new ApiClient();
        objectMapper = new ObjectMapper();
        historicalDataService = new HistoricalDataService(apiClient, objectMapper);
        marketDataService = new MarketDataService(apiClient, objectMapper);
        assetService = new AssetService(apiClient, objectMapper, marketDataService);

        allStocks = MockData.allStocks();
        recentlyViewed = MockData.recentlyViewed(allStocks);
        favorites.addAll(MockData.defaultFavorites());
        selectedStock = allStocks.get(0);

        sectorFilter.getItems().addAll("All Sectors","Technology","Financial","Healthcare","Consumer","Automotive");
        sectorFilter.getSelectionModel().selectFirst();
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
        sectorLabel.setText(s.sector());

        boolean isFav = favorites.contains(s.symbol());
        favoriteBtn.setText(isFav ? "★" : "☆");
        favoriteBtn.getStyleClass().setAll("fav-btn");

        buyButton.setText("Buy " + s.symbol());
        sellButton.setText("Sell " + s.symbol());
        alertStockLabel.setText("Alert price for " + s.symbol());

        loadChart(s.symbol());
        fetchLivePrice(s.symbol());
        updateBuyEstimate();
        updateSellEstimate();
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
        sectorLabel.setText(asset.exchange != null ? asset.exchange : "—");

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
                System.err.println("Chart error for " + symbol + ": " + e.getMessage());
            }
        }).start();
    }

    // Estimates
    private void updateBuyEstimate() {
        double cost = parseShares(buySharesField) * getDisplayedPrice();
        buyEstimateLabel.setText(String.format("$%.2f", cost));
        buyButton.setDisable(cost <= 0);
    }

    private void updateSellEstimate() {
        double proceeds = parseShares(sellSharesField) * getDisplayedPrice();
        sellEstimateLabel.setText(String.format("$%.2f", proceeds));
        sellButton.setDisable(proceeds <= 0);
    }

    private double getDisplayedPrice() {
        if (stockPriceLabel == null || stockPriceLabel.getText() == null) {
            return 0;
        }
        String value = stockPriceLabel.getText().replace("$", "").replace(",", "").trim();
        if (value.isEmpty() || "—".equals(value)) {
            return 0;
        }
        try {
            return Math.max(0, Double.parseDouble(value));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private double parseShares(TextField field) {
        try { return Math.max(0, Double.parseDouble(field.getText().trim())); }
        catch (NumberFormatException e) { return 0; }
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

    private void applyLiveSearch() {
        searchGeneration++;
        isLoadingPage = false;
        stockListContainer.getChildren().clear();
        livePageIndex = 0;
        loadNextPage();
    }

    @FXML
    private void applyFilters() { applyLiveSearch(); }

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
        User user = SessionManager.getInstance().getCurrentUser();
        int quantity = (int) parseShares(buySharesField);

        if (user == null || selectedSymbol == null || selectedSymbol.isBlank() || quantity <= 0) {
            return;
        }

        Stock liveStock = Stock.fromApi(selectedSymbol, assetService, snapshotService, marketDataService, "iex");
        boolean success = TradeController.getInstance().executeBuy(user, liveStock, quantity);

        if (success) {
            buySharesField.clear();
            updateBuyEstimate();
        }
    }

    @FXML
    private void handleSell() {
        User user = SessionManager.getInstance().getCurrentUser();
        int quantity = (int) parseShares(sellSharesField);

        if (user == null || selectedSymbol == null || selectedSymbol.isBlank() || quantity <= 0) {
            return;
        }

        Stock liveStock = Stock.fromApi(selectedSymbol, assetService, snapshotService, marketDataService, "iex");
        boolean success = TradeController.getInstance().executeSell(user, liveStock, quantity);

        if (success) {
            sellSharesField.clear();
            updateSellEstimate();
        }
    }

    // Navigation
    @FXML
    private void navDashboard() throws IOException {
        SceneManager.switchTo("dashboard/dashboard-view.fxml");
    }

    @FXML
    private void handleSignOut() throws IOException {
        SceneManager.switchTo("auth/auth-view.fxml");
    }

    @FXML private void navAI() throws IOException {
        SceneManager.switchTo("ai/ai-view.fxml");
    }
}
