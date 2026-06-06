package com.stockle.ui;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.stockle.api.data.Asset;
import com.stockle.api.data.BarData;
import com.stockle.model.CandleData;

import javafx.application.Platform;

/** Manages the stock detail panel — stock selection, live price fetching, and chart loading. */
class StockDetailManager {

    private final TradingController ctrl;
    private final List<CandleData> currentCandles = new ArrayList<>();
    private final Map<String, Map<String, CachedChart>> chartCache = new ConcurrentHashMap<>();
    String selectedTimeframe = "1Min";
    private static final ZoneId NYSE_ZONE = ZoneId.of("America/New_York");
    private static final ZoneId BRIS_ZONE = ZoneId.of("Australia/Brisbane");
    private static final DateTimeFormatter API_TIME_FORMAT = DateTimeFormatter.ISO_OFFSET_DATE_TIME;
    private static final long CHART_CACHE_TTL_MS = 60_000L;

    /**
     * Creates a new StockDetailManager
     * @param ctrl main controller to access shared state and services
     */
    StockDetailManager(TradingController ctrl) {
        this.ctrl = ctrl;
    }

    /**
     * Handles timeframe selection events
     * @param event the action event
     */
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

        loadChart(ctrl.selectedSymbol);
    }

    // Stock selection

    /** Selects a stock, updates the detail panel, and kicks off a live price + chart fetch.
     * @param asset the selected stock asset
     */
    void selectStock(Asset asset) {
        ctrl.selectedSymbol  = asset.symbol;
        ctrl.currentExchange = asset.exchange != null ? asset.exchange : "—";

        ctrl.stockSymbolLabel.setText(asset.symbol);
        ctrl.stockNameLabel.setText(asset.name != null ? asset.name : asset.symbol);
        ctrl.stockPriceLabel.setText("—");
        ctrl.stockChangeLabel.setText("—");
        ctrl.volumeLabel.setText("—");
        ctrl.marketCapLabel.setText("2.8T");
        ctrl.exchangeLabel.setText(ctrl.currentExchange);

        boolean isFav = ctrl.favorites.contains(asset.symbol);
        ctrl.favoriteBtn.setText(isFav ? "★" : "☆");
        ctrl.favoriteBtn.getStyleClass().setAll("fav-btn");
        ctrl.buyButton.setText("Buy "  + asset.symbol);
        ctrl.sellButton.setText("Sell " + asset.symbol);

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

    /** Fetches the latest bar for a symbol and updates the price labels. Discards stale responses.
     * @param symbol the stock symbol to fetch price for
     */
    void fetchLivePrice(String symbol) {
        ctrl.tradingUpdater.refreshNow();
    }

    /** Applies the latest live bar to the currently selected stock detail panel.
     * @param symbol the stock symbol of the incoming price update
     * @param bar the latest price bar data
     */
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

    /** Fetches previous day's daily bar data and updates volume display.
     * @param symbol the stock symbol to fetch volume for
     */
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
    /** Fetches the last 60 one-minute bars and renders them as a candlestick chart.
     * @param symbol the stock symbol to load chart for
     */
    void loadChart(String symbol) {
        loadChart(symbol, false);
    }

    /**
     * Fetches historical bars for the given symbol and timeframe, builds candles, and updates the chart. Uses caching to avoid unnecessary API calls. Discards stale responses.
     * @param symbol the stock symbol to load chart for
     * @param forceRefresh whether to force a refresh of the chart data
     */
    void loadChart(String symbol, boolean forceRefresh) {
        final int gen = ctrl.chartLoadGeneration;
        String timeframe = selectedTimeframe;

        ZonedDateTime nowNyse = ZonedDateTime.now(NYSE_ZONE);
        ZonedDateTime nowBris = ZonedDateTime.now(BRIS_ZONE);
        java.time.LocalTime brisTimeOfDay = nowBris.toLocalTime();
        LocalDate sessionDate = nowNyse.toLocalDate().minusDays(1);
        ZonedDateTime marketClose = sessionDate.atTime(16, 0).atZone(NYSE_ZONE);

        ZonedDateTime endTimeCandidate = sessionDate.atTime(brisTimeOfDay).atZone(NYSE_ZONE);
        if (endTimeCandidate.isAfter(marketClose)) {
            endTimeCandidate = marketClose;
        }
        final ZonedDateTime endTime = endTimeCandidate;


        if (!forceRefresh) {
            CachedChart cached = getCachedChart(symbol, timeframe);
            if (cached != null) {
                List<CandleData> candles = buildCandles(cached.bars, timeframe, endTime);
                long volume = buildVolume(cached.bars, timeframe, endTime);
                Platform.runLater(() -> {
                    if (gen != ctrl.chartLoadGeneration || !symbol.equals(ctrl.selectedSymbol)) return;
                    currentCandles.clear();
                    currentCandles.addAll(candles);
                    ctrl.priceChart.setCandles(candles);
                    ctrl.currentVolume = volume;
                    ctrl.volumeLabel.setText(formatVolume(volume));
                });
                return;
            }
        }
        new Thread(() -> {
            try {
                TimeRange range = resolveTimeRange(timeframe, endTime);
                String start = range.start.format(API_TIME_FORMAT);
                String end = range.end.format(API_TIME_FORMAT);
                List<BarData> bars = ctrl.historicalDataService.getHistoricalBars(
                    symbol, start, end, timeframe, "iex");

                putCachedChart(symbol, timeframe, bars);
                List<CandleData> candles = buildCandles(bars, timeframe, endTime);
                long volumeToShow = buildVolume(bars, timeframe, endTime);

                Platform.runLater(() -> {
                    if (gen != ctrl.chartLoadGeneration || !symbol.equals(ctrl.selectedSymbol)) return;
                    currentCandles.clear();
                    currentCandles.addAll(candles);
                    ctrl.priceChart.setCandles(candles);
                    ctrl.currentVolume = volumeToShow;
                    ctrl.volumeLabel.setText(formatVolume(volumeToShow));
                });
            } catch (Exception e) {
                System.err.println("Error loading chart for " + symbol + ": " + e.getMessage());
            }
        }).start();
    }

    /** Updates the chart with a new price bar (adds it to the end, removes old one).
     * @param symbol the stock symbol of the incoming price update
     * @param newBar the latest price bar data to add to the chart
     */
    void updateChartWithNewBar(String symbol, BarData newBar) {
        if (!symbol.equals(ctrl.selectedSymbol)) {
            return;
        }
        loadChart(symbol, true);
    }

    /**
     * Resolves the time range for historical bar fetching based on the selected timeframe and current time. 
     * Adjusts end time to last market close if currently outside market hours. 
     * For shorter timeframes, also adjusts start time to limit the number of bars returned.
     * @param timeframe the selected timeframe (e.g. "1Min", "5Min", "1Hour", "1Day", "1Month")
     * @param end the candidate end time to base calculations on
     * @return the resolved time range
     */
    private TimeRange resolveTimeRange(String timeframe, ZonedDateTime end) {
        ZonedDateTime End = end;
        java.time.LocalTime marketOpen = java.time.LocalTime.of(9, 30);
        if (end.toLocalTime().isBefore(marketOpen)) {
            End = end.minusDays(1).with(java.time.LocalTime.of(16, 0));
        }
        
        ZonedDateTime start;
        switch (timeframe) {
            case "1Min" -> start = End.minusDays(3);
            case "5Min" -> start = End.minusDays(5);
            case "10Min"-> start = End.minusDays(6);
            case "1Hour" -> start = End.minusDays(10);
            case "1Day" -> start = End.minusDays(78);
            case "1Month" -> start = ZonedDateTime.of(LocalDate.of(2000, 1, 1),
                java.time.LocalTime.MIDNIGHT, NYSE_ZONE);
            case "All" -> start = ZonedDateTime.of(LocalDate.of(2000, 1, 1),
                java.time.LocalTime.MIDNIGHT, NYSE_ZONE);
            default -> start = End.minusDays(3);
        }

        return new TimeRange(start, End);
    }

    /**
     * Resolves the label formatter for the x-axis of the chart based on the selected timeframe.
     * @param timeframe the selected timeframe (e.g. "1Min", "5Min", "1Hour", "1Day", "1Month")
     * @return the DateTimeFormatter to use for formatting x-axis labels
     */
    private DateTimeFormatter resolveLabelFormatter(String timeframe) {
        return switch (timeframe) {
            case "5Min", "10Min", "1Hour" -> DateTimeFormatter.ofPattern("d/MM HH:mm");
            case "1Day" -> DateTimeFormatter.ofPattern("d/MM");
            case "1Month" -> DateTimeFormatter.ofPattern("MMM yyyy");
            default -> DateTimeFormatter.ofPattern("HH:mm");
        };
    }

    /**
     * Formats a raw volume number into a human-readable string with appropriate suffixes (K, M, B, T).
     * @param volume the raw volume number to format
     * @return the formatted volume string
     */
    private String formatVolume(long volume) {
        if (volume < 1_000) {
            return String.format("%,d", volume);
        }
        if (volume < 1_000_000) {
            return String.format("%.1fK", volume / 1_000.0);
        }
        if (volume < 1_000_000_000) {
            return String.format("%.1fM", volume / 1_000_000.0);
        }
        if (volume < 1_000_000_000_000L) {
            return String.format("%.1fB", volume / 1_000_000_000.0);
        }
        return String.format("%.1fT", volume / 1_000_000_000_000.0);
    }

    /**
     * Retrieves a cached chart for the given symbol and timeframe if it exists and is not stale.
     * If the cached chart is stale, it is removed from the cache.
     * @param symbol the stock symbol
     * @param timeframe the selected timeframe
     * @return the cached chart or null if not found or stale
     */
    private CachedChart getCachedChart(String symbol, String timeframe) {
        Map<String, CachedChart> byTimeframe = chartCache.get(symbol);
        if (byTimeframe == null) {
            return null;
        }
        CachedChart cached = byTimeframe.get(timeframe);
        if (cached == null) {
            return null;
        }
        if (System.currentTimeMillis() - cached.cachedAtMs > CHART_CACHE_TTL_MS) {
            byTimeframe.remove(timeframe);
            return null;
        }
        return cached;
    }

    /**
     * Caches the given chart data for the specified symbol and timeframe.
     * @param symbol the stock symbol
     * @param timeframe the selected timeframe
     * @param bars the bar data to cache
     */
    private void putCachedChart(String symbol, String timeframe, List<BarData> bars) {
        chartCache
            .computeIfAbsent(symbol, key -> new ConcurrentHashMap<>())
            .put(timeframe, new CachedChart(new ArrayList<>(bars), System.currentTimeMillis()));
    }

    /**
     * Builds a list of CandleData from the given list of BarData, filtered to the specified timeframe and window end.
     * Converts timestamps to NYSE time for display. Discards bars outside the window.
     * @param bars the list of bar data
     * @param timeframe the selected timeframe
     * @param windowEnd the end of the display window
     * @return the list of candle data
     */
    private List<CandleData> buildCandles(List<BarData> bars, String timeframe, ZonedDateTime windowEnd) {
        List<BarData> windowed = getWindowedBars(bars, timeframe, windowEnd);
        

        DateTimeFormatter fmt = resolveLabelFormatter(timeframe);
        List<CandleData> candles = new ArrayList<>();
        for (BarData bar : windowed) {
            // Convert bar timestamp to NYSE time for display
            ZonedDateTime nyseTime = bar.timestamp.atZone(ZoneOffset.UTC)
                .withZoneSameInstant(NYSE_ZONE);
            candles.add(new CandleData(
                nyseTime.format(fmt), bar.open, bar.high, bar.low, bar.close));
        }
        return candles;
    }

    /**
     * Sums the volume from the given list of BarData, filtered to the specified timeframe and window end.
     * Discards bars outside the window.
     * @param bars the list of bar data
     * @param timeframe the selected timeframe
     * @param windowEnd the end of the display window
     * @return the total volume for the specified timeframe and window
     */
    private long buildVolume(List<BarData> bars, String timeframe, ZonedDateTime windowEnd) {
        long timeframeVolume = 0L;
        for (BarData bar : getWindowedBars(bars, timeframe, windowEnd)) {
            timeframeVolume += bar.volume;
        }
        return timeframeVolume;
    }

    /**
     * Filters the given list of BarData to those that fall within the display window defined by the selected timeframe and window end.
     * For shorter timeframes, also limits the number of bars returned to avoid overwhelming the chart.
     * @param bars the list of bar data
     * @param timeframe the selected timeframe
     * @param windowEnd the end of the display window
     * @return the filtered list of bar data
     */
    private List<BarData> getWindowedBars(List<BarData> bars, String timeframe, ZonedDateTime windowEnd) {
        if (timeframe.equals("1Min")) {
            ZonedDateTime windowStart = windowEnd.minusHours(1);
            return bars.stream()
                .filter(b -> {
                    ZonedDateTime t = b.timestamp.atZone(ZoneOffset.UTC).withZoneSameInstant(NYSE_ZONE);
                    return !t.isBefore(windowStart) && !t.isAfter(windowEnd);
                })
                .collect(java.util.stream.Collectors.toList());
        }
        return bars.size() > 60 ? bars.subList(bars.size() - 60, bars.size()) : bars;
    }

    /**
     * cache entry for a chart
     */
    private static class CachedChart {
        private final List<BarData> bars;
        private final long cachedAtMs;

        private CachedChart(List<BarData> bars, long cachedAtMs) {
            this.bars = bars;
            this.cachedAtMs = cachedAtMs;
        }
    }
    /**
     * Represents a time range for filtering bar data.
     */
    private static class TimeRange {
        private final ZonedDateTime start;
        private final ZonedDateTime end;

        private TimeRange(ZonedDateTime start, ZonedDateTime end) {
            this.start = start;
            this.end = end;
        }
    }
}
