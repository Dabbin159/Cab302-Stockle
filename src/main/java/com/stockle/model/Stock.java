package com.stockle.model;

import java.util.Collections;
import java.util.Map;

import org.json.JSONObject;

import com.stockle.api.data.Asset;
import com.stockle.api.data.BarData;
import com.stockle.api.data.SnapshotData;
import com.stockle.api.service.AssetService;
import com.stockle.api.service.MarketDataService;
import com.stockle.api.service.SnapshotService;

/**
 *  * Class representing a stock. Contains information for stock such as companmy name, sector, current price, daily change, and volume. Provides constructors for creating a stock and getters and setters for each field
 */
public class Stock {

    private String companyName;
    private String sector;
    private int currentPrice;
    private Float dailyChange; // Percentage
    private Long volume;

    /**
     * Constructor for creating a stock object
     * @param companyName
     * @param sector
     * @param currentPrice
     * @param dailyChange
     * @param volume
     */
    public Stock(String companyName, String sector, int currentPrice, Float dailyChange, Long volume) {
        this.companyName = companyName;
        this.sector = sector;
        this.currentPrice = currentPrice;
        this.dailyChange = dailyChange;
        this.volume = volume;
    }

    /**
     * Build a Stock model from live API objects.
     */
    public static Stock fromApiData(String symbol, Asset asset, SnapshotData snapshot) {
        String stockId = symbol;
        if ((stockId == null || stockId.isBlank()) && asset != null) {
            stockId = asset.symbol;
        }
        if (stockId == null || stockId.isBlank()) {
            throw new IllegalArgumentException("symbol is required to create Stock from API data");
        }

        String resolvedSector = (asset != null && asset.assetClass != null)
                ? asset.assetClass
                : "unknown";

        int resolvedPrice = 0;
        float resolvedDailyChange = 0f;
        long resolvedVolume = 0L;

        if (snapshot != null && snapshot.latestBar != null) {
            double close = snapshot.latestBar.close;
            double open = snapshot.latestBar.open;

            resolvedPrice = (int) Math.round(close);
            resolvedDailyChange = open != 0
                    ? (float) (((close - open) / open) * 100.0)
                    : 0f;
            resolvedVolume = snapshot.latestBar.volume;
        }

        return new Stock(stockId, resolvedSector, resolvedPrice, resolvedDailyChange, resolvedVolume);
    }

    /**
     * Fetch and build a Stock model from Alpaca asset + snapshot endpoints.
     */
    public static Stock fromApi(String symbol, AssetService assetService, SnapshotService snapshotService, String feed) {
        return fromApi(symbol, assetService, snapshotService, null, feed);
    }

    /**
     * Fetch and build a Stock model from Alpaca APIs with bar fallback.
     */
    public static Stock fromApi(
            String symbol,
            AssetService assetService,
            SnapshotService snapshotService,
            MarketDataService marketDataService,
            String feed) {
        if (symbol == null || symbol.isBlank()) {
            throw new IllegalArgumentException("symbol is required");
        }
        if (assetService == null) {
            throw new IllegalArgumentException("assetService is required");
        }
        if (snapshotService == null) {
            throw new IllegalArgumentException("snapshotService is required");
        }

        String normalizedFeed = (feed == null || feed.isBlank()) ? "iex" : feed;

        Asset asset = assetService.getAsset(symbol);
        Map<String, SnapshotData> snapshots = snapshotService.getSnapshots(Collections.singletonList(symbol), normalizedFeed);
        SnapshotData snapshot = snapshots != null ? snapshots.get(symbol) : null;

        if ((snapshot == null || snapshot.latestBar == null) && marketDataService != null) {
            Map<String, BarData> bars = marketDataService.getLatestBars(Collections.singletonList(symbol), normalizedFeed);
            BarData bar = bars != null ? bars.get(symbol) : null;
            if (bar != null) {
                snapshot = new SnapshotData(bar, snapshot != null ? snapshot.latestQuote : null);
            }
        }

        return fromApiData(symbol, asset, snapshot);
    }
    /**
     * Converts the stock object to a JSON object for the database
     * @return
     */
    public JSONObject toJSON() {
        JSONObject json = new JSONObject();
        json.put("companyName", companyName);
        json.put("sector", sector);
        json.put("currentPrice", currentPrice);
        json.put("dailyChange", dailyChange);
        json.put("volume", volume);
        return json;
    }

    /**
     * Creates a stock object from a JSON object retrieved from the database
     * @param json
     * @return
     */
    public static Stock fromJSON(JSONObject json) {
        String companyName = json.getString("companyName");
        String sector = json.getString("sector");
        int currentPrice = json.getInt("currentPrice");
        Float dailyChange = json.getFloat("dailyChange");
        Long volume = json.getLong("volume");
        return new Stock(companyName, sector, currentPrice, dailyChange, volume);
    }
        
    public String getCompanyName() {
        return companyName;
    }

    public String getSector() {
        return sector;
    }

    public int getCurrentPrice() {
        return currentPrice;
    }

    public Float getDailyChange() {
        return dailyChange;
    }

    public Long getVolume() {
        return volume;
    }

    public void setCurrentPrice(int currentPrice) {
        this.currentPrice = currentPrice;
    }

    public void setDailyChange(Float dailyChange) {
        this.dailyChange = dailyChange;
    }

    public void setVolume(Long volume) {
        this.volume = volume;
    }

}
