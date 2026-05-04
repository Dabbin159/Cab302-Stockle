package com.stockle.ui;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.stockle.api.data.Asset;
import com.stockle.api.data.BarData;
import com.stockle.model.CandleData;

import javafx.application.Platform;

/** Manages the stock detail panel — stock selection, live price fetching, and chart loading. */
class StockDetailManager {

    private final TradingController ctrl;

    StockDetailManager(TradingController ctrl) {
        this.ctrl = ctrl;
    }

    // Stock selection

    /** Selects a stock, updates the detail panel, and kicks off a live price + chart fetch. */
    void selectStock(Asset asset) {
        ctrl.selectedSymbol  = asset.symbol;
        ctrl.currentExchange = asset.exchange != null ? asset.exchange : "—";

        ctrl.stockSymbolLabel.setText(asset.symbol);
        ctrl.stockNameLabel.setText(asset.name != null ? asset.name : asset.symbol);
        ctrl.stockPriceLabel.setText("—");
        ctrl.stockChangeLabel.setText("—");
        ctrl.volumeLabel.setText("—");
        ctrl.marketCapLabel.setText("—");
        ctrl.exchangeLabel.setText(ctrl.currentExchange);

        boolean isFav = ctrl.favorites.contains(asset.symbol);
        ctrl.favoriteBtn.setText(isFav ? "★" : "☆");
        ctrl.favoriteBtn.getStyleClass().setAll("fav-btn");
        ctrl.buyButton.setText("Buy "  + asset.symbol);
        ctrl.sellButton.setText("Sell " + asset.symbol);
        ctrl.alertStockLabel.setText("Alert price for " + asset.symbol);

        ctrl.orderManager.clearTradeStatus();
        ctrl.chartLoadGeneration++;
        ctrl.priceLoadGeneration++;

        fetchLivePrice(asset.symbol);
        loadChart(asset.symbol);
        ctrl.orderManager.updateBuyEstimate();
        ctrl.orderManager.updateSellEstimate();
        ctrl.orderManager.updateOwnedSharesLabel();
        ctrl.listManager.refreshStockListSelection();
    }

    // Live price

    /** Fetches the latest bar for a symbol and updates the price labels. Discards stale responses. */
    void fetchLivePrice(String symbol) {
        final int gen = ctrl.priceLoadGeneration;
        new Thread(() -> {
            try {
                Map<String, BarData> bars = ctrl.marketDataService.getLatestBars(List.of(symbol), "iex");
                BarData bar = bars.get(symbol);
                if (bar == null) return;

                double changeAmt = bar.close - bar.open;
                double changePct = bar.open != 0 ? (changeAmt / bar.open) * 100 : 0;
                boolean pos  = changePct >= 0;
                String  sign = pos ? "+" : "";

                Platform.runLater(() -> {
                    if (gen != ctrl.priceLoadGeneration || !symbol.equals(ctrl.selectedSymbol)) return;

                    ctrl.currentPrice         = bar.close;
                    ctrl.currentChange        = changeAmt;
                    ctrl.currentChangePercent = changePct;
                    ctrl.currentVolume        = (long) bar.volume;

                    ctrl.stockPriceLabel.setText(String.format("$%.2f", bar.close));
                    ctrl.stockChangeLabel.setText(
                        String.format("%s%.2f (%s%.2f%%)", sign, changeAmt, sign, changePct));
                    ctrl.stockChangeLabel.getStyleClass().setAll(
                        pos ? "stock-change-pos" : "stock-change-neg");
                    ctrl.volumeLabel.setText(String.format("%,d", (long) bar.volume));

                    ctrl.orderManager.updateBuyEstimate();
                    ctrl.orderManager.updateSellEstimate();
                });
            } catch (Exception e) {
                System.err.println("Live price fetch failed for " + symbol + ": " + e.getMessage());
            }
        }).start();
    }

    // Chart

    /** Fetches the last 60 one-minute bars and renders them as a candlestick chart. */
    void loadChart(String symbol) {
        final int gen = ctrl.chartLoadGeneration;
        new Thread(() -> {
            try {
                LocalDate today = LocalDate.now();
                List<BarData> bars = ctrl.historicalDataService.getHistoricalBars(
                    symbol, today.minusDays(1), today, "1Min", "iex");
                List<BarData> last60 = bars.size() > 60
                    ? bars.subList(bars.size() - 60, bars.size()) : bars;
                DateTimeFormatter fmt = DateTimeFormatter.ofPattern("HH:mm");
                List<CandleData> candles = new ArrayList<>();
                for (BarData bar : last60) {
                    candles.add(new CandleData(
                        bar.timestamp.format(fmt), bar.open, bar.high, bar.low, bar.close));
                }
                Platform.runLater(() -> {
                    if (gen != ctrl.chartLoadGeneration || !symbol.equals(ctrl.selectedSymbol)) return;
                    ctrl.priceChart.setCandles(candles);
                });
            } catch (Exception e) {
                System.err.println("Error loading chart for " + symbol + ": " + e.getMessage());
            }
        }).start();
    }
}
