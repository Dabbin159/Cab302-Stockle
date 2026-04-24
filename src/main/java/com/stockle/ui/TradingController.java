package com.stockle.ui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.stockle.api.StockAPI;
import com.stockle.api.StockAPI.Asset;
import com.stockle.api.StockAPI.BarData;
import com.stockle.api.StockAPI.SnapshotData;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

public class TradingController {

    private final StockAPI stockAPI = new StockAPI();

    // =========================
    // FXML STOCK DETAIL FIELDS
    // =========================
    @FXML private Label stockSymbolLabel;
    @FXML private Label stockNameLabel;
    @FXML private Label stockPriceLabel;
    @FXML private Label stockChangeLabel;
    @FXML private Label volumeLabel;
    @FXML private Label marketCapLabel;
    @FXML private Label sectorLabel;
    @FXML private Button favoriteBtn;

    @FXML private LineChart<String, Number> priceChart;

    // =========================
    // ORDER FORM FIELDS
    // =========================
    @FXML private TextField buySharesField;
    @FXML private Label buyEstimateLabel;
    @FXML private Button buyButton;

    @FXML private TextField sellSharesField;
    @FXML private Label sellEstimateLabel;
    @FXML private Button sellButton;

    @FXML private Label ownedSharesLabel;

    @FXML private TextField buyStopLossField;
    @FXML private TextField buyTakeProfitField;
    @FXML private TextField sellStopLossField;
    @FXML private TextField sellTakeProfitField;

    @FXML private TextField alertPriceField;

    // =========================
    // RIGHT PANEL
    // =========================
    @FXML private TextField searchField;
    @FXML private ComboBox<String> sectorFilter;
    @FXML private CheckBox favoritesOnly;

    @FXML private Label alertStockLabel;

    @FXML private VBox recentlyViewedContainer;
    @FXML private VBox stockListContainer;

    // IMPORTANT: MUST exist in FXML if you want scroll detection
    @FXML private ScrollPane stockScrollPane;

    // =========================
    // DATA
    // =========================
    private List<Asset> allAssets = new ArrayList<>();
    private List<Asset> filteredAssets = new ArrayList<>();
    private final Set<String> favorites = new HashSet<>();
    private final List<Asset> recentlyViewed = new ArrayList<>();

    private Asset selectedAsset;

    // =========================
    // INFINITE SCROLL STATE
    // =========================
    private static final int CHUNK_SIZE = 30;
    private int renderIndex = 0;
    private boolean isLoadingChunk = false;

    // =========================
    // INIT
    // =========================
    @FXML
    public void initialize() {

        sectorFilter.getItems().addAll(
                "All Sectors",
                "Technology",
                "Financial",
                "Healthcare",
                "Consumer",
                "Automotive"
        );
        sectorFilter.getSelectionModel().selectFirst();

        loadStocks();

        searchField.textProperty().addListener((obs, o, n) -> applyFilters());
        sectorFilter.setOnAction(e -> applyFilters());
        favoritesOnly.setOnAction(e -> applyFilters());

        buySharesField.textProperty().addListener((obs, o, n) -> updateBuyEstimate());
        sellSharesField.textProperty().addListener((obs, o, n) -> updateSellEstimate());

        // INFINITE SCROLL
        stockScrollPane.vvalueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.doubleValue() >= 0.9) {
                renderNextChunk();
            }
        });
    }

    // =========================
    // LOAD ALL STOCKS (CACHED)
    // =========================
    private void loadStocks() {

        new Thread(() -> {
            allAssets = stockAPI.getAllAssets();
            filteredAssets = new ArrayList<>(allAssets);

            Platform.runLater(() -> {
                resetAndRender();

                if (!allAssets.isEmpty()) {
                    selectStock(allAssets.get(0));
                }
            });
        }).start();
    }

    // =========================
    // SELECT STOCK
    // =========================
    private void selectStock(Asset asset) {
        selectedAsset = asset;

        stockSymbolLabel.setText(asset.symbol);
        stockNameLabel.setText(asset.name);

        alertStockLabel.setText("Alert price for " + asset.symbol);
        buyButton.setText("Buy " + asset.symbol);
        sellButton.setText("Sell " + asset.symbol);

        loadSnapshot(asset.symbol);
        loadChart(asset.symbol);

        updateBuyEstimate();
        updateSellEstimate();
    }

    // =========================
    // SNAPSHOT DATA
    // =========================
    private void loadSnapshot(String symbol) {

        new Thread(() -> {

            Map<String, SnapshotData> snapshot =
                    stockAPI.getSnapshots(List.of(symbol));

            SnapshotData data = snapshot.get(symbol);

            if (data == null || data.latestBar == null) return;

            BarData bar = data.latestBar;

            Platform.runLater(() -> {
                stockPriceLabel.setText(String.format("$%.2f", bar.close));
                volumeLabel.setText(String.valueOf(bar.volume));
                sectorLabel.setText("US Equity");

                if (data.latestQuote != null) {
                    stockChangeLabel.setText(
                            String.format("Bid: %.2f | Ask: %.2f",
                                    data.latestQuote.bidPrice,
                                    data.latestQuote.askPrice)
                    );
                }
            });
        }).start();
    }

    // =========================
    // CHART
    // =========================
    private void loadChart(String symbol) {

        new Thread(() -> {

            List<BarData> bars = stockAPI.getHistoricalBars(
                    symbol,
                    java.time.LocalDate.now().minusDays(7),
                    java.time.LocalDate.now(),
                    "1Day"
            );

            Platform.runLater(() -> {

                priceChart.getData().clear();

                XYChart.Series<String, Number> series = new XYChart.Series<>();

                for (BarData bar : bars) {
                    series.getData().add(
                            new XYChart.Data<>(
                                    bar.timestamp.toLocalDate().toString(),
                                    bar.close
                            )
                    );
                }

                priceChart.getData().add(series);
            });
        }).start();
    }

    // =========================
    // FILTERING (RESET CHUNKS)
    // =========================
    @FXML
    private void applyFilters() {

        String query = searchField.getText() == null ? "" : searchField.getText().toLowerCase();
        String sector = sectorFilter.getValue();

        filteredAssets = allAssets.stream()
                .filter(a -> query.isEmpty()
                        || a.symbol.toLowerCase().contains(query)
                        || a.name.toLowerCase().contains(query))
                .filter(a -> sector == null
                        || sector.equals("All Sectors")
                        || true)
                .filter(a -> !favoritesOnly.isSelected()
                        || favorites.contains(a.symbol))
                .toList();

        resetAndRender();
    }

    // =========================
    // RESET + FIRST CHUNK
    // =========================
    private void resetAndRender() {
        stockListContainer.getChildren().clear();
        renderIndex = 0;
        renderNextChunk();
    }

    // =========================
    // CHUNK RENDERING (CORE FIX)
    // =========================
    private void renderNextChunk() {

        if (isLoadingChunk) return;
        if (renderIndex >= filteredAssets.size()) return;

        isLoadingChunk = true;

        int end = Math.min(renderIndex + CHUNK_SIZE, filteredAssets.size());
        List<Asset> chunk = filteredAssets.subList(renderIndex, end);

        Platform.runLater(() -> {

            for (Asset a : chunk) {

                Button row = new Button(a.symbol + " - " + a.name);
                row.setMaxWidth(Double.MAX_VALUE);
                row.getStyleClass().add("stock-row");

                row.setOnAction(e -> selectStock(a));

                stockListContainer.getChildren().add(row);
            }

            renderIndex = end;
            isLoadingChunk = false;
        });
    }

    // =========================
    // ESTIMATES
    // =========================
    private void updateBuyEstimate() {
        double shares = parse(buySharesField);
        double price = getCurrentPrice();
        buyEstimateLabel.setText(String.format("$%.2f", shares * price));
        buyButton.setDisable(shares <= 0);
    }

    private void updateSellEstimate() {
        double shares = parse(sellSharesField);
        double price = getCurrentPrice();
        sellEstimateLabel.setText(String.format("$%.2f", shares * price));
        sellButton.setDisable(shares <= 0);
    }

    private double getCurrentPrice() {
        try {
            return Double.parseDouble(stockPriceLabel.getText().replace("$", ""));
        } catch (Exception e) {
            return 0;
        }
    }

    private double parse(TextField f) {
        try {
            return Math.max(0, Double.parseDouble(f.getText()));
        } catch (Exception e) {
            return 0;
        }
    }

    // =========================
    // ACTIONS
    // =========================
    @FXML private void handleBuy() {
        System.out.println("Buy disabled (UI only mode)");
    }

    @FXML private void handleSell() {
        System.out.println("Sell disabled (UI only mode)");
    }

    @FXML private void handleSetAlert() {
        System.out.println("Alert not implemented yet");
    }

    @FXML private void toggleFavorite() {
        if (selectedAsset == null) return;

        if (favorites.contains(selectedAsset.symbol)) {
            favorites.remove(selectedAsset.symbol);
            favoriteBtn.setText("☆");
        } else {
            favorites.add(selectedAsset.symbol);
            favoriteBtn.setText("★");
        }
    }

    // =========================
    // NAVIGATION
    // =========================
    @FXML
    private void navDashboard() throws IOException {
        SceneManager.switchTo("dashboard/dashboard-view.fxml");
    }

    @FXML
    private void handleSignOut() throws IOException {
        SceneManager.switchTo("auth/auth-view.fxml");
    }

    @FXML private void navAI() {}
}