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

public class AssetService {
    private final ApiClient apiClient;
    private final ObjectMapper objectMapper;
    private final MarketDataService marketDataService;

    public AssetService(ApiClient apiClient, ObjectMapper objectMapper, MarketDataService marketDataService) {
        this.apiClient = apiClient;
        this.objectMapper = objectMapper;
        this.marketDataService = marketDataService;
    }

    public List<Asset> getAllAssets() {
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
        } catch (Exception e) {
            System.err.println("Error fetching all assets: " + e.getMessage());
        }

        return assets;
    }

    public Asset getAsset(String symbolOrId) {
        try {
            String url = ApiClient.BASE_URL + "/v2/assets/" + symbolOrId;
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
            System.err.println("Error fetching asset " + symbolOrId + ": " + e.getMessage());
            return null;
        }
    }

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
