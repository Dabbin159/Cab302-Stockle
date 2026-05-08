package com.stockle.ui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stockle.SessionManager;
import com.stockle.api.client.ApiClient;
import com.stockle.api.data.Asset;
import com.stockle.api.service.AssetService;
import com.stockle.api.service.HistoricalDataService;
import com.stockle.api.service.MarketDataService;
import com.stockle.api.service.SnapshotService;
import com.stockle.model.TradeController;
import com.stockle.model.User;
import com.stockle.updater.TradingUpdater;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

@SuppressWarnings("unused")
public class TradingController {

    // FXML fields (package-private so managers can access them)
    @FXML Label stockSymbolLabel;
    @FXML Label stockNameLabel;
    @FXML Label stockPriceLabel;
    @FXML Label stockChangeLabel;
    @FXML Label volumeLabel;
    @FXML Label marketCapLabel;
    @FXML Label exchangeLabel;
    @FXML Button favoriteBtn;
    @FXML CandleStickChart priceChart;

    @FXML TextField buySharesField;
    @FXML Label buyEstimateLabel;
    @FXML Label buyStatusLabel;
    @FXML Button buyButton;
    @FXML TextField sellSharesField;
    @FXML Label sellEstimateLabel;
    @FXML Label sellStatusLabel;
    @FXML Button sellButton;
    @FXML Label ownedSharesLabel;

    @FXML TextField searchField;
    @FXML CheckBox favoritesOnly;
    @FXML Label alertStockLabel;
    @FXML VBox recentlyViewedContainer;
    @FXML VBox stockListContainer;
    @FXML ScrollPane stockListScroll;
    @FXML private javafx.scene.image.ImageView darkModeIcon;

    // Shared state
    String selectedSymbol = "";
    double currentPrice = 0;
    String currentExchange = "";
    double currentChange = 0;
    double currentChangePercent = 0;
    long currentVolume = 0;
    final List<String> favorites = new ArrayList<>();

    List<Asset> liveAssets = new ArrayList<>();
    int livePageIndex = 0;
    boolean isLoadingPage = false;
    int searchGeneration = 0;
    int chartLoadGeneration = 0;
    int priceLoadGeneration = 0;
    static final int PAGE_SIZE = 20;

    // API services
    ApiClient apiClient;
    ObjectMapper objectMapper;
    HistoricalDataService historicalDataService;
    AssetService assetService;
    MarketDataService marketDataService;
    SnapshotService snapshotService;
    TradingUpdater tradingUpdater;

    // Managers
    StockListManager listManager;
    StockDetailManager detailManager;
    OrderFormManager orderManager;

    // Init

    @FXML
    public void initialize() {
        SceneManager.applyTheme(stockSymbolLabel);
        syncThemeButton();
        
        apiClient = new ApiClient();
        objectMapper = new ObjectMapper();
        historicalDataService = new HistoricalDataService(apiClient, objectMapper);
        marketDataService = new MarketDataService(apiClient, objectMapper);
        snapshotService = new SnapshotService(apiClient, objectMapper);
        assetService = new AssetService(apiClient, objectMapper, marketDataService);

        listManager = new StockListManager(this);
        detailManager = new StockDetailManager(this);
        orderManager = new OrderFormManager(this);

        tradingUpdater = new TradingUpdater(
            marketDataService,
            () -> selectedSymbol,
            new TradingUpdater.Listener() {
                @Override
                public void onPriceUpdate(String symbol, com.stockle.api.data.BarData bar) {
                    Platform.runLater(() -> {
                        detailManager.applyLivePriceUpdate(symbol, bar);
                        detailManager.updateChartWithNewBar(symbol, bar);  // ← Add this
                    });
                }

                @Override
                public void onError(String symbol, Exception exception) {
                    System.err.println("Live price update failed for " + symbol + ": " + exception.getMessage());
                }
            },
            "iex",
            30L
        );
        tradingUpdater.start();

        searchField.textProperty().addListener((obs, o, n) -> listManager.applyLiveSearch());
        buySharesField.textProperty().addListener((obs, o, n) -> orderManager.updateBuyEstimate());
        sellSharesField.textProperty().addListener((obs, o, n) -> orderManager.updateSellEstimate());

        stockListScroll.vvalueProperty().addListener((obs, o, n) -> {
            if (n.doubleValue() >= 0.9 && !isLoadingPage) listManager.loadNextPage();
        });

        recentlyViewedContainer.getChildren().clear();

        new Thread(() -> {
            try {
                List<Asset> assets = assetService.getAllAssets();
                liveAssets = assets.stream()
                    .filter(a -> a.tradable && "us_equity".equals(a.assetClass) && a.symbol != null)
                    .collect(java.util.stream.Collectors.toList());
                Platform.runLater(() -> {
                    listManager.loadNextPage();
                    liveAssets.stream()
                        .filter(a -> "AAPL".equals(a.symbol))
                        .findFirst()
                        .ifPresent(detailManager::selectStock);
                });
            } catch (Exception e) {
                System.err.println("Failed to load assets: " + e.getMessage());
            }
        }).start();
    }

    @FXML
    private void toggleDarkMode() {
        if (stockSymbolLabel == null || stockSymbolLabel.getScene() == null) {
            return;
        }

        SessionManager sessionManager = SessionManager.getInstance();
        sessionManager.setDarkModeEnabled(!sessionManager.isDarkModeEnabled());
        SceneManager.applyTheme(stockSymbolLabel.getScene().getRoot());
        syncThemeButton();
    }

    private void syncThemeButton() {
        if (darkModeIcon == null) {
            return;
        }

        boolean darkModeEnabled = SessionManager.getInstance().isDarkModeEnabled();
        String iconPath = darkModeEnabled
            ? "/com/stockle/ui/images/light-mode-button.png"
            : "/com/stockle/ui/images/dark-mode-button.png";

        java.net.URL iconUrl = getClass().getResource(iconPath);
        if (iconUrl != null) {
            darkModeIcon.setImage(new javafx.scene.image.Image(iconUrl.toExternalForm()));
        }
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
    }

    // FXML handlers

    @FXML void applyLiveSearch() { listManager.applyLiveSearch(); }
    @FXML void handleBuy() { orderManager.handleBuy(); }
    @FXML void handleSell() { orderManager.handleSell(); }
    @FXML void handleSetAlert() {}

    @FXML
    private void handleTimeframe(javafx.event.ActionEvent event) {
        detailManager.handleTimeframe(event);
    }

    @FXML
    private void toggleFavorite() {
        if (selectedSymbol.isEmpty()) return;
        if (favorites.contains(selectedSymbol)) favorites.remove(selectedSymbol);
        else favorites.add(selectedSymbol);
        favoriteBtn.setText(favorites.contains(selectedSymbol) ? "★" : "☆");
        favoriteBtn.getStyleClass().setAll("fav-btn");
        listManager.applyLiveSearch();
    }

    // Shared utilities

    Label label(String text, String... styles) {
        Label l = new Label(text);
        l.getStyleClass().addAll(styles);
        return l;
    }

    int parseQuantity(TextField field) {
        try { return Math.max(0, Integer.parseInt(field.getText().trim())); }
        catch (NumberFormatException e) { return 0; }
    }

    User currentUser() { return SessionManager.getInstance().getCurrentUser(); }

    void refreshCurrentUser() {
        User user = currentUser();
        if (user == null) return;
        User refreshed = TradeController.getInstance().refreshUserFromDb(user);
        if (refreshed != null) SessionManager.getInstance().setCurrentUser(refreshed);
    }

    // Navigation

    @FXML private void navDashboard()   throws IOException { SceneManager.switchTo("dashboard/dashboard-view.fxml"); }
    @FXML private void navAI()          throws IOException { SceneManager.switchTo("ai/ai-view.fxml"); }
    @FXML private void navNews()        throws IOException { SceneManager.switchTo("news/news-view.fxml"); }
    @FXML private void navLeaderboard() throws IOException { SceneManager.switchTo("leaderboard/leaderboard-view.fxml"); }

    @FXML
    private void handleSignOut() throws IOException {
        if (tradingUpdater != null) {
            tradingUpdater.stop();
        }
        SessionManager.getInstance().logout();
        SceneManager.switchTo("auth/auth-view.fxml");
    }
    
}
