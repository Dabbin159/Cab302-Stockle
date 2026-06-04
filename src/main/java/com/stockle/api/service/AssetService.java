package com.stockle.api.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.stockle.api.client.ApiClient;
import com.stockle.api.data.Asset;
import com.stockle.api.data.BarData;

/**
 * Service for retrieving and managing stock asset data from the Alpaca API.
 * 
 * gets asset info, list of assets
 * gets stocks by volume, price, open price
 * 
 */
public class AssetService {
    private List<Asset> cachedAssets;
    private boolean isCaching = false;
    private final ApiClient apiClient;
    private final ObjectMapper objectMapper;
    private final MarketDataService marketDataService;

    /**
     * Constructs an AssetService with required dependencies.
     * 
     * @param apiClient the API client for making HTTP requests to Alpaca
     * @param objectMapper the JSON object mapper for parsing API responses
     * @param marketDataService the service for retrieving market bar data
     */
    public AssetService(ApiClient apiClient, ObjectMapper objectMapper, MarketDataService marketDataService) {
        this.apiClient = apiClient;
        this.objectMapper = objectMapper;
        this.marketDataService = marketDataService;
    }

    /**
     * Retrieves all active tradable assets from the Alpaca API.
     * 
     * Gets a list of all active assets including details such as
     * symbol, name, asset class, exchange, trading, shortable, marginable status.
     * Parses the JSON response into Asset objects. 
     * Returns an empty list if an error occurs during the request.
     * 
     * @return a list of all active assets available for trading
     */
    public List<Asset> getAllAssets() {
        if (cachedAssets != null) {
            return cachedAssets;
        }
        List<Asset> assets = new ArrayList<>();

        try {
            String url = ApiClient.BASE_URL + "/v2/assets?status=active";
            String response = apiClient.makeRequest(url);

            JsonNode root = objectMapper.readTree(response);

            if (root.isArray()) {
                for (JsonNode assetNode : root) {
                    Asset asset = new Asset();
                    asset.id = assetNode.get("id").asText();
                    asset.symbol = assetNode.get("symbol").asText();
                    asset.name = assetNode.get("name").asText();
                    asset.assetClass = assetNode.get("class").asText();
                    asset.exchange = assetNode.get("exchange").asText();
                    asset.tradable = assetNode.get("tradable").asBoolean();
                    asset.shortable = assetNode.get("shortable").asBoolean();
                    asset.marginable = assetNode.get("marginable").asBoolean();

                    assets.add(asset);
                }
            }
            cachedAssets = assets;
        } catch (Exception e) {
            System.err.println("Error fetching all assets: " + e.getMessage());
        }

        return assets;
    }

    /**
     * Retrieves details for a specific asset by symbol.
     * 
     * gets asset information from the Alpaca API for the given symbol.
     * 
     * @param symbol the stock symbol (e.g., "AAPL")
     * @return the Asset object if found, or null if an error occurs
     */
    public Asset getAsset(String symbol) {
        try {
            String url = ApiClient.BASE_URL + "/v2/assets/" + symbol;
            String response = apiClient.makeRequest(url);

            JsonNode root = objectMapper.readTree(response);

            Asset asset = new Asset();
            asset.id = root.get("id").asText();
            asset.symbol = root.get("symbol").asText();
            asset.name = root.get("name").asText();
            asset.assetClass = root.get("class").asText();
            asset.exchange = root.get("exchange").asText();
            asset.tradable = root.get("tradable").asBoolean();
            asset.shortable = root.get("shortable").asBoolean();
            asset.marginable = root.get("marginable").asBoolean();

            return asset;
        } catch (Exception e) {
            System.err.println("Error fetching asset " + symbol + ": " + e.getMessage());
            return null;
        }
    }

    /**
     * Retrieves the stock symbols with the highest trading volume.
     * 
     * gets all tradable US equities and ranks them by trading volume
     * 
     * @param limit the maximum number of stock symbols to return
     * @param feed the market data feed to use (e.g., "sip" or "iex")
     * @return a list of stock symbols sorted by trading volume in descending order
     */
    public List<String> getHighestVolumeStocks(int limit, String feed) {
        // Compute using latest bars for tradable US equities (client-side ranking)
        List<Asset> assets = getAllAssets();
        List<String> symbols = new ArrayList<>();
        for (Asset asset : assets) {
            if (asset.tradable && "us_equity".equals(asset.assetClass)) {
                symbols.add(asset.symbol);
            }
        }

        Map<String, BarData> bars = marketDataService.getLatestBars(symbols, feed);
        return bars.entrySet().stream()
                .sorted((a, b) -> Long.compare(b.getValue().volume, a.getValue().volume))
                .limit(limit)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves the stock symbols with the highest closing prices.
     * 
     * gets all tradable US equities and ranks them by closing price
     * 
     * @param limit the maximum number of stock symbols to return
     * @param feed the market data feed to use (e.g., "sip" or "iex")
     * @return a list of stock symbols sorted by closing price in descending order
     */
    public List<String> getHighestPriceStocks(int limit, String feed) {
        List<Asset> assets = getAllAssets();
        List<String> symbols = new ArrayList<>();
        for (Asset asset : assets) {
            if (asset.tradable && "us_equity".equals(asset.assetClass)) {
                symbols.add(asset.symbol);
            }
        }

        Map<String, BarData> bars = marketDataService.getLatestBars(symbols, feed);
        return bars.entrySet().stream()
                .sorted((a, b) -> Double.compare(b.getValue().close, a.getValue().close))
                .limit(limit)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves the stock symbols with the highest opening prices.
     * 
     * gets all tradable US equities and ranks them by opening price
     * 
     * @param limit the maximum number of stock symbols to return
     * @param feed the market data feed to use (e.g., "sip" or "iex")
     * @return a list of stock symbols sorted by opening price in descending order
     */
    public List<String> getHighestOpenStocks(int limit, String feed) {
        List<Asset> assets = getAllAssets();
        List<String> symbols = new ArrayList<>();
        for (Asset asset : assets) {
            if (asset.tradable && "us_equity".equals(asset.assetClass)) {
                symbols.add(asset.symbol);
            }
        }

        Map<String, BarData> bars = marketDataService.getLatestBars(symbols, feed);
        return bars.entrySet().stream()
                .sorted((a, b) -> Double.compare(b.getValue().open, a.getValue().open))
                .limit(limit)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }
}
