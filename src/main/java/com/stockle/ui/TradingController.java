package com.stockle.ui;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
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
    @FXML private LineChart<String, Number> priceChart;

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

    // Data
    private List<MockData.Stock> allStocks;
    private List<MockData.Stock> recentlyViewed;
    private final List<String> favorites = new ArrayList<>();
    private MockData.Stock selectedStock;
    private List<MockData.Stock> filteredStocks;

    @FXML
    public void initialize() {
        allStocks = MockData.allStocks();
        recentlyViewed = MockData.recentlyViewed(allStocks);
        favorites.addAll(MockData.defaultFavorites());
        selectedStock = allStocks.get(0);
        filteredStocks = new ArrayList<>(allStocks);
        sectorFilter.getItems().addAll(
            "All Sectors","Technology","Financial","Healthcare","Consumer","Automotive"
        );
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
        stockChangeLabel.setText(
            String.format("%s%.2f (%s%.2f%%)", sign, s.change(), sign, s.changePct()));
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

        loadChart(s);
        updateBuyEstimate();
        updateSellEstimate();
        refreshStockListSelection();
    }

    private void loadChart(MockData.Stock s) {
        priceChart.getData().clear();
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        double[] prices = MockData.CHART_DATA[s.chartIndex()];
        for (int i = 0; i < MockData.TIMES.length; i++) {
            series.getData().add(new XYChart.Data<>(MockData.TIMES[i], prices[i]));
        }
        priceChart.getData().add(series);
    }

    // Estimates
    private void updateBuyEstimate() {
        double cost = parseShares(buySharesField) * selectedStock.price();
        buyEstimateLabel.setText(String.format("$%.2f", cost));
        buyButton.setDisable(cost <= 0);
    }

    private void updateSellEstimate() {
        double proceeds = parseShares(sellSharesField) * selectedStock.price();
        sellEstimateLabel.setText(String.format("$%.2f", proceeds));
        sellButton.setDisable(proceeds <= 0);
    }

    private double parseShares(TextField field) {
        try { return Math.max(0, Double.parseDouble(field.getText().trim())); }
        catch (NumberFormatException e) { return 0; }
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
        Label pct = label((pos ? "+" : "") + s.changePct() + "%",
                          pos ? "stock-row-pos" : "stock-row-neg");

        HBox row = new HBox(left, pct);
        row.getStyleClass().add("stock-row");
        row.setOnMouseClicked(e -> selectStock(s));
        return new VBox(row);
    }

    private VBox stockRow(MockData.Stock s) {
        boolean active = s.symbol().equals(selectedStock.symbol());
        boolean pos    = s.change() >= 0;

        String displaySymbol = favorites.contains(s.symbol()) ? s.symbol() + " ★" : s.symbol();
        Label sym   = label(displaySymbol, "stock-row-symbol");
        Label name  = label(s.name(), "stock-row-name");
        VBox left   = new VBox(2, sym, name);
        HBox.setHgrow(left, Priority.ALWAYS);

        Label price = label(String.format("$%.2f", s.price()), "stock-row-price");
        Label pct   = label((pos ? "+" : "") + s.changePct() + "%",
                            pos ? "stock-row-pos" : "stock-row-neg");
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
    private void handleBuy() {}

    @FXML
    private void handleSell() {}

    // Navigation
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
