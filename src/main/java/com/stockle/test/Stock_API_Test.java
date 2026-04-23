package com.stockle.test;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.stockle.api.StockAPI;
import com.stockle.api.StockAPI.Asset;
import com.stockle.api.StockAPI.BarData;

public class Stock_API_Test {
    
    private static StockAPI api;
    
    public Stock_API_Test() {
        api = new StockAPI();
    }
    
    public static void main(String[] args) {
        System.out.println("=== StockAPI Test Suite ===\n");
        
        Stock_API_Test tester = new Stock_API_Test();
        
        // Test 1: Get asset by symbol
        tester.testGetAsset();
        
        // Test 2: Get all assets
        tester.testGetAllAssets();
        
        // Test 3: Get latest bars for multiple stocks
        tester.testGetLatestBars();
        
        // Test 4: Get highest volume stocks
        tester.testGetHighestVolumeStocks();
        
        // Test 5: Get highest price stocks
        tester.testGetHighestPriceStocks();
        
        // Test 6: Get highest open stocks
        tester.testGetHighestOpenStocks();
        
        System.out.println("\n=== All Tests Complete ===");
    }
    
    /**
     * Test getAsset() - Fetch details for a specific stock
     */
    private void testGetAsset() {
        System.out.println("--- Test 1: getAsset(\"AAPL\") ---");
        try {
            Asset asset = api.getAsset("AAPL");
            if (asset != null) {
                System.out.println("Asset Found:");
                System.out.println("  Symbol: " + asset.symbol);
                System.out.println("  Name: " + asset.name);
                System.out.println("  Exchange: " + asset.exchange);
                System.out.println("  Asset Class: " + asset.assetClass);
                System.out.println("  Tradable: " + asset.tradable);
                System.out.println("  Shortable: " + asset.shortable);
                System.out.println("  Marginable: " + asset.marginable);
            } else {
                System.out.println("Failed to fetch asset");
            }
        } catch (Exception e) {
            System.out.println("Exception: " + e.getMessage());
        }
        System.out.println();
    }
    
    /**
     * Test getAllAssets() - Fetch all available trading assets
     */
    private void testGetAllAssets() {
        System.out.println("--- Test 2: getAllAssets() ---");
        try {
            List<Asset> assets = api.getAllAssets();
            if (assets != null && !assets.isEmpty()) {
                System.out.println("Fetched " + assets.size() + " assets");
                System.out.println("  First 5 assets:");
                for (int i = 0; i < Math.min(5, assets.size()); i++) {
                    Asset asset = assets.get(i);
                    System.out.println("    " + (i + 1) + ". " + asset.symbol + " (" + asset.name + ")");
                }
            } else {
                System.out.println("No assets found or null response");
            }
        } catch (Exception e) {
            System.out.println("Exception: " + e.getMessage());
        }
        System.out.println();
    }
    
    /**
     * Test getLatestBars() - Fetch latest bar data for multiple stocks
     */
    private void testGetLatestBars() {
        System.out.println("--- Test 3: getLatestBars([\"AAPL\", \"GOOGL\", \"MSFT\", \"TSLA\"]) ---");
        try {
            List<String> symbols = Arrays.asList("AAPL", "GOOGL", "MSFT", "TSLA");
            Map<String, BarData> bars = api.getLatestBars(symbols);
            
            if (bars != null && !bars.isEmpty()) {
                System.out.println("Fetched bar data for " + bars.size() + " stocks:");
                for (String symbol : symbols) {
                    BarData bar = bars.get(symbol);
                    if (bar != null) {
                        System.out.println("  " + symbol + ":");
                        System.out.println("    Open: $" + String.format("%.2f", bar.open));
                        System.out.println("    High: $" + String.format("%.2f", bar.high));
                        System.out.println("    Low: $" + String.format("%.2f", bar.low));
                        System.out.println("    Close: $" + String.format("%.2f", bar.close));
                        System.out.println("    Volume: " + bar.volume);
                        System.out.println("    Timestamp: " + bar.timestamp);
                    } else {
                        System.out.println("  " + symbol + ": No data available");
                    }
                }
            } else {
                System.out.println("No bar data found or null response");
            }
        } catch (Exception e) {
            System.out.println("Exception: " + e.getMessage());
        }
        System.out.println();
    }
    
    /**
     * Test getHighestVolumeStocks() - Get top stocks by trading volume
     */
    private void testGetHighestVolumeStocks() {
        System.out.println("--- Test 4: getHighestVolumeStocks(10) ---");
        try {
            List<String> topVolume = api.getHighestVolumeStocks(10);
            if (topVolume != null && !topVolume.isEmpty()) {
                System.out.println("Top 10 stocks by volume:");
                for (int i = 0; i < topVolume.size(); i++) {
                    System.out.println("  " + (i + 1) + ". " + topVolume.get(i));
                }
            } else {
                System.out.println("No volume data found or null response");
            }
        } catch (Exception e) {
            System.out.println("Exception: " + e.getMessage());
        }
        System.out.println();
    }
    
    /**
     * Test getHighestPriceStocks() - Get top stocks by price
     */
    private void testGetHighestPriceStocks() {
        System.out.println("--- Test 5: getHighestPriceStocks(10) ---");
        try {
            List<String> topPrice = api.getHighestPriceStocks(10);
            if (topPrice != null && !topPrice.isEmpty()) {
                System.out.println("Top 10 stocks by price:");
                for (int i = 0; i < topPrice.size(); i++) {
                    System.out.println("  " + (i + 1) + ". " + topPrice.get(i));
                }
            } else {
                System.out.println("No price data found or null response");
            }
        } catch (Exception e) {
            System.out.println("Exception: " + e.getMessage());
        }
        System.out.println();
    }
    
    /**
     * Test getHighestOpenStocks() - Get top stocks by opening price
     */
    private void testGetHighestOpenStocks() {
        System.out.println("--- Test 6: getHighestOpenStocks(10) ---");
        try {
            List<String> topOpen = api.getHighestOpenStocks(10);
            if (topOpen != null && !topOpen.isEmpty()) {
                System.out.println("Top 10 stocks by open price:");
                for (int i = 0; i < topOpen.size(); i++) {
                    System.out.println("  " + (i + 1) + ". " + topOpen.get(i));
                }
            } else {
                System.out.println("No open price data found or null response");
            }
        } catch (Exception e) {
            System.out.println("Exception: " + e.getMessage());
        }
        System.out.println();
    }
}
