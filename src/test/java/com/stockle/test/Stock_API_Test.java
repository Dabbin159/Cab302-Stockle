package com.stockle.test;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.stockle.api.StockAPI;
import com.stockle.api.StockAPI.Asset;
import com.stockle.api.StockAPI.BarData;

public class Stock_API_Test {
    
    private StockAPI api;
    
    @BeforeEach
    public void setUp() {
        api = new StockAPI();
    }
    
    @Test
    public void testAll() {
        System.out.println("=== StockAPI Test Suite ===\n");
        
        // Test 1: Get asset by symbol
        testGetAsset();
        
        // Test 2: Get all assets
        testGetAllAssets();
        
        // Test 3: Get latest bars for multiple stocks
        testGetLatestBars();
        
        // Test 4: Get historical bars
        testGetHistoricalBars();
        
        // Test 5: Get historical quotes
        testGetHistoricalQuotes();
        
        // Test 6: Get highest volume stocks
        testGetHighestVolumeStocks();
        
        // Test 7: Get highest price stocks
        testGetHighestPriceStocks();
        
        // Test 8: Get highest open stocks
        testGetHighestOpenStocks();
        
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
     * Test getHistoricalBars() - Fetch historical bar data for a stock
     */
    private void testGetHistoricalBars() {
        System.out.println("--- Test 4: getHistoricalBars(\"AAPL\", \"1Day\") ---");
        try {
            // Get last 30 days of daily bars for AAPL
            LocalDate endDate = LocalDate.now();
            LocalDate startDate = endDate.minusDays(30);
            
            List<BarData> bars = api.getHistoricalBars("AAPL", startDate, endDate, "1Day");
            
            if (bars != null && !bars.isEmpty()) {
                System.out.println("Fetched " + bars.size() + " historical bars for AAPL:");
                System.out.println("  Date Range: " + startDate + " to " + endDate);
                System.out.println("  Timeframe: 1 Day");
                System.out.println("  First 3 bars:");
                for (int i = 0; i < Math.min(3, bars.size()); i++) {
                    BarData bar = bars.get(i);
                    System.out.println("    Bar " + (i + 1) + " (" + bar.timestamp + "):");
                    System.out.println("      Open: $" + String.format("%.2f", bar.open));
                    System.out.println("      High: $" + String.format("%.2f", bar.high));
                    System.out.println("      Low: $" + String.format("%.2f", bar.low));
                    System.out.println("      Close: $" + String.format("%.2f", bar.close));
                    System.out.println("      Volume: " + bar.volume);
                }
            } else {
                System.out.println("No historical bar data found or null response");
            }
        } catch (Exception e) {
            System.out.println("Exception: " + e.getMessage());
            e.printStackTrace();
        }
        System.out.println();
    }
    
    /**
     * Test getHistoricalQuotes() - Fetch historical quote data for a stock
     */
    private void testGetHistoricalQuotes() {
        System.out.println("--- Test 5: getHistoricalQuotes(\"AAPL\") ---");
        try {
            // Get last 30 days of quotes for AAPL
            LocalDate endDate = LocalDate.now();
            LocalDate startDate = endDate.minusDays(30);
            
            List<StockAPI.QuoteData> quotes = api.getHistoricalQuotes("AAPL", startDate, endDate);
            
            if (quotes != null && !quotes.isEmpty()) {
                System.out.println("Fetched " + quotes.size() + " historical quotes for AAPL:");
                System.out.println("  Date Range: " + startDate + " to " + endDate);
                System.out.println("  First 3 quotes:");
                for (int i = 0; i < Math.min(3, quotes.size()); i++) {
                    StockAPI.QuoteData quote = quotes.get(i);
                    System.out.println("    Quote " + (i + 1) + " (" + quote.timestamp + "):");
                    System.out.println("      Bid Price: $" + String.format("%.2f", quote.bidPrice));
                    System.out.println("      Bid Size: " + quote.bidSize);
                    System.out.println("      Ask Price: $" + String.format("%.2f", quote.askPrice));
                    System.out.println("      Ask Size: " + quote.askSize);
                    System.out.println("      Exchange: " + quote.exchange);
                }
            } else {
                System.out.println("No historical quote data found or null response");
            }
        } catch (Exception e) {
            System.out.println("Exception: " + e.getMessage());
            e.printStackTrace();
        }
        System.out.println();
    }
    
    /**
     * Test getHighestVolumeStocks() - Get top stocks by trading volume
     */
    private void testGetHighestVolumeStocks() {
        System.out.println("--- Test 6: getHighestVolumeStocks(10) ---");
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
        System.out.println("--- Test 7: getHighestPriceStocks(10) ---");
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
        System.out.println("--- Test 8: getHighestOpenStocks(10) ---");
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
