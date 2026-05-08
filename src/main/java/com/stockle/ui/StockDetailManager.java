package com.stockle.ui;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
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
    private final List<CandleData> currentCandles = new ArrayList<>();
    String selectedTimeframe = "1Min";

    StockDetailManager(TradingController ctrl) {
        this.ctrl = ctrl;
    }

    void handleTimeframe(javafx.event.ActionEvent event) {
        javafx.scene.control.Button clicked = (javafx.scene.control.Button) event.getSource();
        selectedTimeframe = (String) clicked.getUserData();

        javafx.scene.layout.HBox bar = (javafx.scene.layout.HBox) clicked.getParent();
        for (javafx.scene.Node node : bar.getChildren()) {
            if (node instanceof javafx.scene.control.Button btn) {
                btn.getStyleClass().removeAll("timeframe-btn-active");
                btn.getStyleClass().add("timeframe-btn");
            }
        }
        clicked.getStyleClass().add("timeframe-btn-active");

        System.out.println(selectedTimeframe);
        loadChart(ctrl.selectedSymbol);
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
        currentCandles.clear();

        fetchLivePrice(asset.symbol);
        loadChart(asset.symbol);
        loadPreviousDayVolume(asset.symbol);
        ctrl.orderManager.updateBuyEstimate();
        ctrl.orderManager.updateSellEstimate();
        ctrl.orderManager.updateOwnedSharesLabel();
        ctrl.listManager.refreshStockListSelection();
    }

    // Live price

    /** Fetches the latest bar for a symbol and updates the price labels. Discards stale responses. */
    void fetchLivePrice(String symbol) {
        ctrl.tradingUpdater.refreshNow();
    }

    /** Applies the latest live bar to the currently selected stock detail panel. */
    void applyLivePriceUpdate(String symbol, BarData bar) {
        if (!symbol.equals(ctrl.selectedSymbol)) {
            return;
        }

        ctrl.currentPrice = bar.close;
        double changeAmt = bar.close - bar.open;
        double changePct = bar.open != 0 ? (changeAmt / bar.open) * 100 : 0;
        boolean pos = changePct >= 0;
        String sign = pos ? "+" : "";

        ctrl.currentChange = changeAmt;
        ctrl.currentChangePercent = changePct;

        ctrl.stockPriceLabel.setText(String.format("$%.2f", bar.close));
        ctrl.stockChangeLabel.setText(
            String.format("%s%.2f (%s%.2f%%)", sign, changeAmt, sign, changePct));
        ctrl.stockChangeLabel.getStyleClass().setAll(
            pos ? "stock-change-pos" : "stock-change-neg");

        ctrl.orderManager.updateBuyEstimate();
        ctrl.orderManager.updateSellEstimate();
    }

    /** Fetches previous day's daily bar data and updates volume display. */
    void loadPreviousDayVolume(String symbol) {
        new Thread(() -> {
            try {
                List<String> symbols = java.util.List.of(symbol);
                Map<String, com.stockle.api.data.SnapshotData> snapshots = 
                    ctrl.snapshotService.getSnapshots(symbols, "iex");

                if (snapshots.containsKey(symbol)) {
                    com.stockle.api.data.SnapshotData snapshot = snapshots.get(symbol);
                    if (snapshot != null && snapshot.latestBar != null) {
                        BarData prevDayBar = snapshot.latestBar;
                        Platform.runLater(() -> {
                            if (symbol.equals(ctrl.selectedSymbol)) {
                                ctrl.currentVolume = (long) prevDayBar.volume;
                                ctrl.volumeLabel.setText(String.format("%,d", (long) prevDayBar.volume));
                            }
                        });
                    }
                }
            } catch (Exception e) {
                System.err.println("Error loading snapshot volume for " + symbol + ": " + e.getMessage());
            }
        }).start();
    }

    // Chart
    /** Fetches the last 60 one-minute bars and renders them as a candlestick chart. */
    void loadChart(String symbol) {
        final int gen = ctrl.chartLoadGeneration;
        new Thread(() -> {
            try {
                ZoneId nyse = ZoneId.of("America/New_York");
                ZoneId brisbane = ZoneId.of("Australia/Brisbane");
                
                ZonedDateTime nowNYSE = ZonedDateTime.now(nyse);
                ZonedDateTime nowBrisbane = ZonedDateTime.now(brisbane);

                java.time.LocalTime brisbaneTimeOfDay = nowBrisbane.toLocalTime();

                // If before market open, use previous day's session
                LocalDate sessionDate = nowNYSE.toLocalDate().minusDays(1);
                while (sessionDate.getDayOfWeek().getValue() > 5) {  // Skip weekends
                    sessionDate = sessionDate.minusDays(1);
                }

                ZonedDateTime marketOpen = sessionDate.atTime(9, 30).atZone(nyse);
                ZonedDateTime marketClose = sessionDate.atTime(16, 0).atZone(nyse);

                // Use the lesser of nowNYSE and marketClose as end
                ZonedDateTime endTime = sessionDate.atTime(brisbaneTimeOfDay).atZone(nyse);
                if (endTime.isAfter(marketClose)) {
                    endTime = marketClose;
                }

                DateTimeFormatter formatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME;
                String start = marketOpen.format(formatter);
                String end = endTime.format(formatter);

                String timeframe = selectedTimeframe;

                List<BarData> bars = ctrl.historicalDataService.getHistoricalBars(
                    symbol, timeframe, "iex");
                //Collections.reverse(bars);

                List<BarData> last60 = bars.size() > 60
                    ? bars.subList(bars.size() - 60, bars.size()) : bars;

                DateTimeFormatter fmt = DateTimeFormatter.ofPattern("HH:mm");
                List<CandleData> candles = new ArrayList<>();
                for (BarData bar : bars) {
                    // Convert bar timestamp to NYSE time for display
                    ZonedDateTime nyseTime = bar.timestamp.atZone(ZoneOffset.UTC)
                        .withZoneSameInstant(nyse);
                    candles.add(new CandleData(
                        nyseTime.format(fmt), bar.open, bar.high, bar.low, bar.close));
                }
                Platform.runLater(() -> {
                    if (gen != ctrl.chartLoadGeneration || !symbol.equals(ctrl.selectedSymbol)) return;
                    currentCandles.clear();
                    currentCandles.addAll(candles);
                    ctrl.priceChart.setCandles(candles);
                });
            } catch (Exception e) {
                System.err.println("Error loading chart for " + symbol + ": " + e.getMessage());
            }
        }).start();
    }

    /** Updates the chart with a new price bar (adds it to the end, removes old one). */
    void updateChartWithNewBar(String symbol, BarData newBar) {
        if (!symbol.equals(ctrl.selectedSymbol)) {
            return;
        }
        loadChart(symbol);
    }
}
