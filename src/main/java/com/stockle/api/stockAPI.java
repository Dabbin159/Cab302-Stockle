package com.stockle.api;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import okhttp3.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * StockAPI handles all communication with the Alpaca Markets API.
 * Provides methods to fetch stock data, quotes, historical data, and manage paper trading.
 */
// API_KEY
// PK4A3ZKIHWXHTZT54S7O7UZNRF
// SECRET_KEY
// 6x4GW4U5ozqS46zvyk1pvay1h4dXswEPakEvZEtBW7Rh
public class StockAPI {
    
    private static final String API_KEY = "PK4A3ZKIHWXHTZT54S7O7UZNRF";
    private static final String SECRET_KEY = "6x4GW4U5ozqS46zvyk1pvay1h4dXswEPakEvZEtBW7Rh";
    private static final String BASE_URL = "https://paper-api.alpaca.markets"; // Paper trading endpoint
    private static final String DATA_URL = "https://data.alpaca.markets";
    
    // Reusable HTTP client and JSON parser
    private final OkHttpClient httpClient;
    private final ObjectMapper objectMapper;
    
    /**
     * Constructor - initializes API client with credentials from environment variables.
     * Set environment variables:
     * - APCA_API_KEY_ID: Your Alpaca API key
     * - APCA_API_SECRET_KEY: Your Alpaca secret key
     */
    public StockAPI() {
        {
            throw new RuntimeException("API credentials not found. Set APCA_API_KEY_ID and APCA_API_SECRET_KEY environment variables.");
        }
    }
    
    /**
     * Helper method to make GET requests to Alpaca API
     */
    private String makeRequest(String url) throws Exception {
        Request request = new Request.Builder()
                .url(url)
                .get()
                .addHeader("APCA-API-KEY-ID", API_KEY)
                .addHeader("accept", "application/json")
                .build();
        
        String responseBody;
        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new RuntimeException("API Error: " + response.code() + " - " + response.body().string());
            }   responseBody = response.body().string();
        }
        return responseBody;
    }
    
    // ==================== MULTIPLE STOCKS DATA ====================
    
    /**
     * Get latest bar data for multiple symbols.
     * @param symbols List of stock symbols (e.g., ["AAPL", "GOOGL", "MSFT"])
     * @return Map of symbol to latest bar data
     */
    public Map<String, BarData> getLatestBars(List<String> symbols) {
        Map<String, BarData> result = new HashMap<>();
        
        try {
            String symbolList = String.join(",", symbols);
            String url = DATA_URL + "/v1/bars/latest?symbols=" + symbolList + "&timeframe=1Min";
            String response = makeRequest(url);
            
            JsonNode root = objectMapper.readTree(response);
            JsonNode barsData = root.get("bars");
            
            for (String symbol : symbols) {
                JsonNode barNode = barsData.get(symbol);
                if (barNode != null) {
                    BarData bar = new BarData(
                            LocalDateTime.parse(barNode.get("t").asText().replace("Z", "+00:00")),
                            barNode.get("o").asDouble(),
                            barNode.get("h").asDouble(),
                            barNode.get("l").asDouble(),
                            barNode.get("c").asDouble(),
                            barNode.get("v").asLong()
                    );
                    result.put(symbol, bar);
                }
            }
        } catch (Exception e) {
            System.err.println("Error fetching latest bars: " + e.getMessage());
            e.printStackTrace();
        }
        
        return result;
    }
    
    /**
     * Get latest quotes (bid/ask) for multiple symbols.
     * @param symbols List of stock symbols
     * @return Map of symbol to quote data
     */
    public Map<String, QuoteData> getLatestQuotes(List<String> symbols) {
        // GET /v1/quotes/latest?symbols=AAPL,GOOGL,MSFT
        Map<String, QuoteData> result = new HashMap<>();
        // TODO: Implement HTTP request
        // Example response:
        // {
        //   "ap": 151.25,  // ask price
        //   "as": 1000,    // ask size
        //   "bp": 151.00,  // bid price
        //   "bs": 500,     // bid size
        //   "t": "2024-04-21T14:30:00Z"
        // }
        return result;
    }
    
    /**
     * Get snapshot data (bars + quotes) for multiple symbols.
     * @param symbols List of stock symbols
     * @return Map of symbol to snapshot data
     */
    public Map<String, SnapshotData> getSnapshots(List<String> symbols) {
        // GET /v1/snapshots?symbols=AAPL,GOOGL,MSFT
        Map<String, SnapshotData> result = new HashMap<>();
        // TODO: Implement HTTP request
        return result;
    }
    
    // ==================== SINGLE STOCK DETAILED DATA ====================
    
    /**
     * Get historical bar data for a single stock.
     * @param symbol Stock symbol (e.g., "AAPL")
     * @param startDate Start date for historical data
     * @param endDate End date for historical data
     * @param timeframe Bar timeframe (e.g., "1Min", "1Hour", "1Day")
     * @return List of bar data points
     */
    public List<BarData> getHistoricalBars(String symbol, LocalDate startDate, LocalDate endDate, String timeframe) {
        // GET /v1/bars/{symbol}?start={startDate}&end={endDate}&timeframe={timeframe}
        List<BarData> bars = new ArrayList<>();
        // TODO: Implement HTTP request with pagination if needed
        // Alpaca returns bars in chronological order
        return bars;
    }
    
    /**
     * Get historical quote data for a single stock.
     * @param symbol Stock symbol
     * @param startDate Start date
     * @param endDate End date
     * @return List of quote data points
     */
    public List<QuoteData> getHistoricalQuotes(String symbol, LocalDate startDate, LocalDate endDate) {
        // GET /v1/quotes/{symbol}?start={startDate}&end={endDate}
        List<QuoteData> quotes = new ArrayList<>();
        // TODO: Implement HTTP request with pagination
        return quotes;
    }
    
    /**
     * Get historical trade data for a single stock.
     * @param symbol Stock symbol
     * @param startDate Start date
     * @param endDate End date
     * @return List of trade data points
     */
    public List<TradeData> getHistoricalTrades(String symbol, LocalDate startDate, LocalDate endDate) {
        // GET /v1/trades/{symbol}?start={startDate}&end={endDate}
        List<TradeData> trades = new ArrayList<>();
        // TODO: Implement HTTP request with pagination
        // Each trade includes: price, size, timestamp, exchange code
        return trades;
    }
    
    // ==================== ASSETS & SYMBOLS ====================
    
    /**
     * Get list of all available trading assets.
     * @return List of available assets with details
     */
    public List<Asset> getAllAssets() {
        // GET /v2/assets
        List<Asset> assets = new ArrayList<>();
        // TODO: Implement HTTP request
        // Returns all tradeable assets with properties like:
        // id, class, exchange, symbol, name, status, tradable, shortable, marginable, etc.
        return assets;
    }
    
    /**
     * Get details for a specific asset.
     * @param symbolOrId Stock symbol or asset ID
     * @return Asset details
     */
    public Asset getAsset(String symbolOrId) {
        // GET /v2/assets/{symbol_or_asset_id}
        // TODO: Implement HTTP request
        return null;
    }
    
    // ==================== PAPER TRADING FUNCTIONS ====================
    
    /**
     * Get account information.
     * @return Account details including buying power, cash, portfolio value
     */
    public AccountInfo getAccount() {
        try {
            String url = BASE_URL + "/v2/account";
            String response = makeRequest(url);
            
            JsonNode root = objectMapper.readTree(response);
            
            AccountInfo account = new AccountInfo();
            account.id = root.get("id").asText();
            account.cash = root.get("cash").asDouble();
            account.portfolioValue = root.get("portfolio_value").asDouble();
            account.buyingPower = root.get("buying_power").asDouble();
            account.status = root.get("status").asText();
            
            return account;
        } catch (Exception e) {
            System.err.println("Error fetching account: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Get all open positions.
     * @return List of current holdings
     */
    public List<Position> getOpenPositions() {
        // GET /v2/positions
        // TODO: Implement HTTP request
        return new ArrayList<>();
    }
    
    /**
     * Get portfolio history.
     * @param period Time period (e.g., "1M", "3M", "1A")
     * @return Portfolio performance data
     */
    public PortfolioHistory getPortfolioHistory(String period) {
        // GET /v2/account/portfolio/history?period={period}
        // TODO: Implement HTTP request
        return null;
    }
    
    /**
     * Place a market buy order.
     * @param symbol Stock symbol
     * @param quantity Number of shares
     * @return Order confirmation
     */
    public Order buyMarket(String symbol, int quantity) {
        // POST /v2/orders
        // TODO: Implement HTTP POST request with order details
        return null;
    }
    
    /**
     * Place a market sell order.
     * @param symbol Stock symbol
     * @param quantity Number of shares
     * @return Order confirmation
     */
    public Order sellMarket(String symbol, int quantity) {
        // POST /v2/orders
        // TODO: Implement HTTP POST request
        return null;
    }
    
    /**
     * Get all orders.
     * @return List of all orders (open and closed)
     */
    public List<Order> getAllOrders() {
        // GET /v2/orders
        // TODO: Implement HTTP request
        return new ArrayList<>();
    }
    
    // ==================== DATA CLASSES ====================
    
    public static class BarData {
        public LocalDateTime timestamp;
        public double open;
        public double high;
        public double low;
        public double close;
        public long volume;
        
        public BarData(LocalDateTime timestamp, double open, double high, double low, double close, long volume) {
            this.timestamp = timestamp;
            this.open = open;
            this.high = high;
            this.low = low;
            this.close = close;
            this.volume = volume;
        }
    }
    
    public static class QuoteData {
        public LocalDateTime timestamp;
        public double bidPrice;
        public long bidSize;
        public double askPrice;
        public long askSize;
        public String exchange;
        
        public QuoteData(LocalDateTime timestamp, double bidPrice, long bidSize, double askPrice, long askSize, String exchange) {
            this.timestamp = timestamp;
            this.bidPrice = bidPrice;
            this.bidSize = bidSize;
            this.askPrice = askPrice;
            this.askSize = askSize;
            this.exchange = exchange;
        }
    }
    
    public static class TradeData {
        public LocalDateTime timestamp;
        public double price;
        public long size;
        public String exchange;
        
        public TradeData(LocalDateTime timestamp, double price, long size, String exchange) {
            this.timestamp = timestamp;
            this.price = price;
            this.size = size;
            this.exchange = exchange;
        }
    }
    
    public static class SnapshotData {
        public BarData latestBar;
        public QuoteData latestQuote;
        
        public SnapshotData(BarData latestBar, QuoteData latestQuote) {
            this.latestBar = latestBar;
            this.latestQuote = latestQuote;
        }
    }
    
    public static class Asset {
        public String id;
        public String symbol;
        public String name;
        public String assetClass;
        public String exchange;
        public boolean tradable;
        public boolean shortable;
        public boolean marginable;
    }
    
    public static class AccountInfo {
        public String id;
        public double cash;
        public double portfolioValue;
        public double buyingPower;
        public String status;
    }
    
    public static class Position {
        public String symbol;
        public int quantity;
        public double currentPrice;
        public double lastPrice;
        public double costBasis;
    }
    
    public static class PortfolioHistory {
        public List<Double> equity;
        public List<Long> timestamps;
        public double totalReturn;
    }
    
    public static class Order {
        public String id;
        public String symbol;
        public int quantity;
        public String side; // "buy" or "sell"
        public String type; // "market", "limit", etc.
        public String status;
        public double filledQty;
        public LocalDateTime createdAt;
    }
}
