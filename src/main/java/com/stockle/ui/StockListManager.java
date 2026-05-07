package com.stockle.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.stockle.api.data.Asset;
import com.stockle.api.data.BarData;

import javafx.application.Platform;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/** Manages the scrollable stock list panel — paging, search filtering, and row building. */
class StockListManager {

    private final TradingController ctrl;

    StockListManager(TradingController ctrl) {
        this.ctrl = ctrl;
    }

    // Paging

    /** Loads the next page of stock rows into the list. Rows appear instantly with placeholder prices. */
    void loadNextPage() {
        if (ctrl.isLoadingPage || ctrl.livePageIndex >= ctrl.liveAssets.size()) return;
        ctrl.isLoadingPage = true;

        String query = ctrl.searchField.getText().toLowerCase().trim();
        boolean favOnly = ctrl.favoritesOnly.isSelected();

        List<Asset> page = new ArrayList<>();
        int idx = ctrl.livePageIndex;
        while (idx < ctrl.liveAssets.size() && page.size() < TradingController.PAGE_SIZE) {
            Asset a = ctrl.liveAssets.get(idx++);
            if (!query.isEmpty()
                    && !a.symbol.toLowerCase().contains(query)
                    && !(a.name != null && a.name.toLowerCase().contains(query))) continue;
            if (favOnly && !ctrl.favorites.contains(a.symbol)) continue;
            page.add(a);
        }
        ctrl.livePageIndex = idx;

        if (page.isEmpty()) { ctrl.isLoadingPage = false; return; }

        // Add rows immediately with "—" price placeholders
        Map<String, VBox> priceBoxes = new HashMap<>();
        for (Asset asset : page) {
            VBox[] holder = new VBox[1];
            ctrl.stockListContainer.getChildren().add(buildRow(asset, holder));
            priceBoxes.put(asset.symbol, holder[0]);
        }
        ctrl.isLoadingPage = false;

        // Fill prices in background
        List<String> symbols = page.stream().map(a -> a.symbol).collect(Collectors.toList());
        final int gen = ctrl.searchGeneration;
        new Thread(() -> {
            try {
                Map<String, BarData> bars = ctrl.marketDataService.getLatestBars(symbols, "iex");
                Platform.runLater(() -> {
                    if (gen != ctrl.searchGeneration) return;
                    bars.forEach((sym, bar) -> {
                        VBox right = priceBoxes.get(sym);
                        if (right == null) return;
                        double pct = bar.open != 0 ? ((bar.close - bar.open) / bar.open) * 100 : 0;
                        boolean pos = pct >= 0;
                        right.getChildren().setAll(
                            ctrl.label(String.format("$%.2f", bar.close), "stock-row-price"),
                            ctrl.label(String.format("%s%.2f%%", pos ? "+" : "", pct),
                                       pos ? "stock-row-pos" : "stock-row-neg")
                        );
                    });
                });
            } catch (Exception e) {
                System.err.println("Failed to fetch page prices: " + e.getMessage());
            }
        }).start();
    }

    /** Clears the list and reloads from the top using the current search/filter state. */
    void applyLiveSearch() {
        ctrl.searchGeneration++;
        ctrl.isLoadingPage = false;
        ctrl.stockListContainer.getChildren().clear();
        ctrl.livePageIndex = 0;
        loadNextPage();
    }

    /** Highlights the active row and removes highlighting from all others. */
    void refreshStockListSelection() {
        ctrl.stockListContainer.getChildren().forEach(node -> {
            node.getStyleClass().setAll("stock-row");
            if (ctrl.selectedSymbol.equals(node.getUserData()))
                node.getStyleClass().add("stock-row-active");
        });
    }

    // Row builder

    private VBox buildRow(Asset asset, VBox[] priceBoxHolder) {
        String display = ctrl.favorites.contains(asset.symbol) ? asset.symbol + " ★" : asset.symbol;
        javafx.scene.control.Label sym  = ctrl.label(display, "stock-row-symbol");
        javafx.scene.control.Label name = ctrl.label(
            asset.name != null ? asset.name : asset.symbol, "stock-row-name");

        VBox left = new VBox(2, sym, name);
        HBox.setHgrow(left, Priority.ALWAYS);

        VBox right = new VBox(2);
        right.setStyle("-fx-alignment: CENTER_RIGHT;");
        right.getChildren().add(ctrl.label("—", "stock-row-price"));
        if (priceBoxHolder != null) priceBoxHolder[0] = right;

        VBox item = new VBox(new HBox(left, right));
        item.getStyleClass().add("stock-row");
        item.setUserData(asset.symbol);
        item.setOnMouseClicked(e -> ctrl.detailManager.selectStock(asset));
        return item;
    }
}
