package com.stockle.api;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
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
    
    // Feed options: sip, iex, delayed_sip, boats, overnight, otc
    public enum DataFeed {
        SIP("sip"),                          // All US exchanges
        IEX("iex"),                          // Investors Exchange
        DELAYED_SIP("delayed_sip"),          // SIP with 15-minute delay
        BOATS("boats"),                      // Blue Ocean, overnight US trading data
        OVERNIGHT("overnight"),              // Derived overnight US trading data
        OTC("otc");                          // Over-the-counter exchanges
        
        public final String value;
        
        DataFeed(String value) {
            this.value = value;
        }
    }
    
    // Reusable HTTP client and JSON parser
    private final OkHttpClient httpClient;
    private final ObjectMapper objectMapper;
    private DataFeed dataFeed; // Configurable data feed preference
    
    /**
     * Constructor - initializes API client with credentials from environment variables.
     * Set environment variables:
     * - APCA_API_KEY_ID: Your Alpaca API key
     * - APCA_API_SECRET_KEY: Your Alpaca secret key
     */
    public StockAPI() {
        this.httpClient = new OkHttpClient();
        this.objectMapper = new ObjectMapper();
        this.dataFeed = DataFeed.IEX; // Default to SIP feed (or IEX if unlimited subscription not available)
    }
    
    /**
     * Set the data feed to use for API queries.
     * @param feed The data feed to use (SIP, IEX, DELAYED_SIP, BOATS, OVERNIGHT, OTC)
     */
    public void setDataFeed(DataFeed feed) {
        this.dataFeed = feed;
    }
    
    /**
     * Get the current data feed setting.
     * @return Current data feed
     */
    public DataFeed getDataFeed() {
        return this.dataFeed;
    }
    
    /**
     * Helper method to make GET requests to Alpaca API
     */
    private String makeRequest(String url) throws Exception {
        Request request = new Request.Builder()
                .url(url)
                .get()
                .addHeader("APCA-API-KEY-ID", API_KEY)
                .addHeader("APCA-API-SECRET-KEY", SECRET_KEY)
                .addHeader("accept", "application/json")
                .build();
        
        String responseBody;
        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                String errorBody = response.body() != null ? response.body().string() : "No error details";
                throw new RuntimeException("API Error: " + response.code() + " - " + errorBody);
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
            String url = DATA_URL + "/v2/stocks/bars/latest?symbols=" + symbolList + "&feed=" + dataFeed.value;
            String response = makeRequest(url);
            
            JsonNode root = objectMapper.readTree(response);
            JsonNode barsData = root.get("bars");
            
            for (String symbol : symbols) {
                JsonNode barNode = barsData.get(symbol);
                if (barNode != null) {
                    // Parse timestamp with timezone offset and convert to LocalDateTime
                    String timestamp = barNode.get("t").asText();
                    LocalDateTime dateTime = java.time.OffsetDateTime.parse(timestamp).toLocalDateTime();
                    
                    BarData bar = new BarData(
                            dateTime,
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
        // GET /v2/stocks/quotes/latest?symbols=AAPL,GOOGL,MSFT
        Map<String, QuoteData> result = new HashMap<>();
        
        try {
            String symbolList = String.join(",", symbols);
            String url = DATA_URL + "/v2/stocks/quotes/latest?symbols=" + symbolList + "&feed=" + dataFeed.value;
            String response = makeRequest(url);
            
            JsonNode root = objectMapper.readTree(response);
            JsonNode quotesData = root.get("quotes");
            
            for (String symbol : symbols) {
                JsonNode quoteNode = quotesData.get(symbol);
                if (quoteNode != null) {
                    // Parse timestamp with timezone offset and convert to LocalDateTime
                    String timestamp = quoteNode.get("t").asText();
                    LocalDateTime dateTime = java.time.OffsetDateTime.parse(timestamp).toLocalDateTime();
                    
                    QuoteData quote = new QuoteData(
                            dateTime,
                            quoteNode.get("bp").asDouble(),
                            quoteNode.get("bs").asLong(),
                            quoteNode.get("ap").asDouble(),
                            quoteNode.get("as").asLong(),
                            quoteNode.get("x").asText()
                    );
                    result.put(symbol, quote);
                }
            }
        } catch (Exception e) {
            System.err.println("Error fetching latest quotes: " + e.getMessage());
            e.printStackTrace();
        }
        
        return result;
    }
    
    /**
     * Get snapshot data (bars + quotes) for multiple symbols.
     * @param symbols List of stock symbols
     * @return Map of symbol to snapshot data
     */
    public Map<String, SnapshotData> getSnapshots(List<String> symbols) {
        // GET /v2/stocks/snapshots?symbols=AAPL,GOOGL,MSFT
        Map<String, SnapshotData> result = new HashMap<>();
        
        try {
            String symbolList = String.join(",", symbols);
            String url = DATA_URL + "/v2/stocks/snapshots?symbols=" + symbolList + "&feed=" + dataFeed.value;
            String response = makeRequest(url);
            
            JsonNode root = objectMapper.readTree(response);
            JsonNode snapshotsData = root.get("snapshots");
            
            for (String symbol : symbols) {
                JsonNode snapshotNode = snapshotsData.get(symbol);
                if (snapshotNode != null) {
                    // Extract bar data
                    JsonNode barNode = snapshotNode.get("bar");
                    BarData bar = null;
                    if (barNode != null) {
                        String barTimestamp = barNode.get("t").asText();
                        LocalDateTime barDateTime = java.time.OffsetDateTime.parse(barTimestamp).toLocalDateTime();
                        
                        bar = new BarData(
                                barDateTime,
                                barNode.get("o").asDouble(),
                                barNode.get("h").asDouble(),
                                barNode.get("l").asDouble(),
                                barNode.get("c").asDouble(),
                                barNode.get("v").asLong()
                        );
                    }
                    
                    // Extract quote data
                    JsonNode quoteNode = snapshotNode.get("quote");
                    QuoteData quote = null;
                    if (quoteNode != null) {
                        String quoteTimestamp = quoteNode.get("t").asText();
                        LocalDateTime quoteDateTime = java.time.OffsetDateTime.parse(quoteTimestamp).toLocalDateTime();
                        
                        quote = new QuoteData(
                                quoteDateTime,
                                quoteNode.get("bp").asDouble(),
                                quoteNode.get("bs").asLong(),
                                quoteNode.get("ap").asDouble(),
                                quoteNode.get("as").asLong(),
                                quoteNode.get("x").asText()
                        );
                    }
                    
                    // Create snapshot with both bar and quote data
                    if (bar != null && quote != null) {
                        result.put(symbol, new SnapshotData(bar, quote));
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error fetching snapshots: " + e.getMessage());
            e.printStackTrace();
        }
        
        return result;
    }
    
    // ==================== SINGLE STOCK DETAILED DATA ====================
    
    /**
     * Get historical bar data for a single stock.
     * @param symbol Stock symbol (e.g., "AAPL")
     * @param startDate Start date for historical data (YYYY-MM-DD format)
     * @param endDate End date for historical data (YYYY-MM-DD format)
     * @param timeframe Bar timeframe (e.g., "1Min", "5Min", "1Hour", "1Day", "1Week", "1Month")
     * @return List of bar data points sorted by timestamp
     */
    public List<BarData> getHistoricalBars(String symbol, LocalDate startDate, LocalDate endDate, String timeframe) {
        // GET /v2/stocks/bars?symbols={symbol}&start={startDate}&end={endDate}&timeframe={timeframe}
        List<BarData> bars = new ArrayList<>();
        String nextPageToken = null;
        int maxIterations = 100; // Prevent infinite loops
        int iterations = 0;
        
        try {
            boolean hasMore = true;
            
            while (hasMore && iterations < maxIterations) {
                iterations++;
                
                StringBuilder urlBuilder = new StringBuilder(DATA_URL + "/v2/stocks/bars");
                urlBuilder.append("?symbols=").append(symbol);
                urlBuilder.append("&timeframe=").append(timeframe);
                urlBuilder.append("&start=").append(startDate);
                urlBuilder.append("&end=").append(endDate);
                urlBuilder.append("&limit=10000"); // Max limit per request
                urlBuilder.append("&sort=asc"); // Sort by timestamp ascending
                urlBuilder.append("&feed=").append(dataFeed.value); // Use configured data feed
                
                // Add pagination token if present
                if (nextPageToken != null) {
                    urlBuilder.append("&page_token=").append(nextPageToken);
                }
                
                String url = urlBuilder.toString();
                System.out.println("DEBUG: Fetching bars from: " + url);
                String response = makeRequest(url);
                System.out.println("DEBUG: Response received, length: " + response.length());
                
                JsonNode root = objectMapper.readTree(response);
                
                // Extract bars for the requested symbol
                // The API returns bars as an object: { "bars": { "AAPL": [...], ... } }
                JsonNode barsObject = root.get("bars");
                if (barsObject != null && barsObject.isObject()) {
                    JsonNode barsArray = barsObject.get(symbol);
                    if (barsArray != null && barsArray.isArray()) {
                        System.out.println("DEBUG: Found " + barsArray.size() + " bars in response");
                        for (JsonNode barNode : barsArray) {
                            String timestamp = barNode.get("t").asText();
                            LocalDateTime dateTime = java.time.OffsetDateTime.parse(timestamp).toLocalDateTime();
                            
                            BarData bar = new BarData(
                                    dateTime,
                                    barNode.get("o").asDouble(),
                                    barNode.get("h").asDouble(),
                                    barNode.get("l").asDouble(),
                                    barNode.get("c").asDouble(),
                                    barNode.get("v").asLong()
                            );
                            bars.add(bar);
                        }
                    } else {
                        System.out.println("DEBUG: No bars array found for symbol " + symbol);
                    }
                } else {
                    System.out.println("DEBUG: No bars object found in response");
                }
                
                // Check for pagination token
                JsonNode nextTokenNode = root.get("next_page_token");
                if (nextTokenNode != null && !nextTokenNode.isNull()) {
                    nextPageToken = nextTokenNode.asText();
                    System.out.println("DEBUG: Found next_page_token, continuing...");
                } else {
                    hasMore = false;
                    System.out.println("DEBUG: No next_page_token found, stopping pagination");
                }
            }
            
            if (iterations >= maxIterations) {
                System.err.println("Warning: Reached maximum iterations (" + maxIterations + ") for historical bars");
            }
        } catch (Exception e) {
            System.err.println("Error fetching historical bars for " + symbol + ": " + e.getMessage());
            e.printStackTrace();
        }
        
        return bars;
    }
    
    /**
     * Get historical quote data for a single stock.
     * @param symbol Stock symbol (e.g., "AAPL")
     * @param startDate Start date for historical data (YYYY-MM-DD format)
     * @param endDate End date for historical data (YYYY-MM-DD format)
     * @return List of quote data points sorted by timestamp
     */
    public List<QuoteData> getHistoricalQuotes(String symbol, LocalDate startDate, LocalDate endDate) {
        // GET /v2/stocks/quotes?symbols={symbol}&start={startDate}&end={endDate}
        List<QuoteData> quotes = new ArrayList<>();
        String nextPageToken = null;
        int maxIterations = 100; // Prevent infinite loops
        int iterations = 0;
        
        try {
            boolean hasMore = true;
            
            while (hasMore && iterations < maxIterations) {
                iterations++;
                
                StringBuilder urlBuilder = new StringBuilder(DATA_URL + "/v2/stocks/quotes");
                urlBuilder.append("?symbols=").append(symbol);
                urlBuilder.append("&start=").append(startDate);
                urlBuilder.append("&end=").append(endDate);
                urlBuilder.append("&limit=10000"); // Max limit per request
                urlBuilder.append("&sort=asc"); // Sort by timestamp ascending
                urlBuilder.append("&feed=").append(dataFeed.value); // Use configured data feed
                
                // Add pagination token if present
                if (nextPageToken != null) {
                    urlBuilder.append("&page_token=").append(nextPageToken);
                }
                
                String url = urlBuilder.toString();
                System.out.println("DEBUG: Fetching quotes from: " + url);
                String response = makeRequest(url);
                System.out.println("DEBUG: Response received, length: " + response.length());
                
                JsonNode root = objectMapper.readTree(response);
                
                // Extract quotes for the requested symbol
                // The API returns quotes as an object: { "quotes": { "AAPL": [...], ... } }
                JsonNode quotesObject = root.get("quotes");
                if (quotesObject != null && quotesObject.isObject()) {
                    JsonNode quotesArray = quotesObject.get(symbol);
                    if (quotesArray != null && quotesArray.isArray()) {
                        System.out.println("DEBUG: Found " + quotesArray.size() + " quotes in response");
                        for (JsonNode quoteNode : quotesArray) {
                            String timestamp = quoteNode.get("t").asText();
                            LocalDateTime dateTime = java.time.OffsetDateTime.parse(timestamp).toLocalDateTime();
                            
                            QuoteData quote = new QuoteData(
                                    dateTime,
                                    quoteNode.get("bp").asDouble(),
                                    quoteNode.get("bs").asLong(),
                                    quoteNode.get("ap").asDouble(),
                                    quoteNode.get("as").asLong(),
                                    quoteNode.get("bx").asText()
                            );
                            quotes.add(quote);
                        }
                    } else {
                        System.out.println("DEBUG: No quotes array found for symbol " + symbol);
                    }
                } else {
                    System.out.println("DEBUG: No quotes object found in response");
                }
                
                // Check for pagination token
                JsonNode nextTokenNode = root.get("next_page_token");
                if (nextTokenNode != null && !nextTokenNode.isNull()) {
                    nextPageToken = nextTokenNode.asText();
                    System.out.println("DEBUG: Found next_page_token, continuing...");
                } else {
                    hasMore = false;
                    System.out.println("DEBUG: No next_page_token found, stopping pagination");
                }
            }
            
            if (iterations >= maxIterations) {
                System.err.println("Warning: Reached maximum iterations (" + maxIterations + ") for historical quotes");
            }
        } catch (Exception e) {
            System.err.println("Error fetching historical quotes for " + symbol + ": " + e.getMessage());
            e.printStackTrace();
        }
        
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
        List<Asset> assets = new ArrayList<>();
        
        try {
            String url = BASE_URL + "/v2/assets?status=active";
            String response = makeRequest(url);
            
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
            e.printStackTrace();
        }
        
        return assets;
    }
    
    /**
     * Get details for a specific asset.
     * @param symbolOrId Stock symbol or asset ID
     * @return Asset details
     */
    public Asset getAsset(String symbolOrId) {
        try {
            String url = BASE_URL + "/v2/assets/" + symbolOrId;
            String response = makeRequest(url);
            
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
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Get top stocks by trading volume.
     * @param limit Number of stocks to return
     * @return List of symbols sorted by highest volume
     */
    public List<String> getHighestVolumeStocks(int limit) {
        // Get all tradeable stocks
        List<Asset> assets = getAllAssets();
        List<String> symbols = new ArrayList<>();
        for (Asset asset : assets) {
            // Only include US equity stocks, not crypto or other assets
            if (asset.tradable && "us_equity".equals(asset.assetClass)) {
                symbols.add(asset.symbol);
            }
        }
        
        // Get latest bars to access volume data
        Map<String, BarData> bars = getLatestBars(symbols);
        
        // Sort by volume (highest first)
        return bars.entrySet().stream()
            .sorted((a, b) -> Long.compare(b.getValue().volume, a.getValue().volume))
            .limit(limit)
            .map(Map.Entry::getKey)
            .collect(Collectors.toList());
    }
    
    /**
     * Get top stocks by price.
     * @param limit Number of stocks to return
     * @return List of symbols sorted by highest close price
     */
    public List<String> getHighestPriceStocks(int limit) {
        // Get all tradeable stocks
        List<Asset> assets = getAllAssets();
        List<String> symbols = new ArrayList<>();
        for (Asset asset : assets) {
            // Only include US equity stocks, not crypto or other assets
            if (asset.tradable && "us_equity".equals(asset.assetClass)) {
                symbols.add(asset.symbol);
            }
        }
        
        // Get latest bars to access price data
        Map<String, BarData> bars = getLatestBars(symbols);
        
        // Sort by close price (highest first)
        return bars.entrySet().stream()
            .sorted((a, b) -> Double.compare(b.getValue().close, a.getValue().close))
            .limit(limit)
            .map(Map.Entry::getKey)
            .collect(Collectors.toList());
    }
    
    /**
     * Get stocks with highest open price.
     * @param limit Number of stocks to return
     * @return List of symbols sorted by highest open price
     */
    public List<String> getHighestOpenStocks(int limit) {
        // Get all tradeable stocks
        List<Asset> assets = getAllAssets();
        List<String> symbols = new ArrayList<>();
        for (Asset asset : assets) {
            // Only include US equity stocks, not crypto or other assets
            if (asset.tradable && "us_equity".equals(asset.assetClass)) {
                symbols.add(asset.symbol);
            }
        }
        
        // Get latest bars
        Map<String, BarData> bars = getLatestBars(symbols);
        
        // Sort by open price (highest first)
        return bars.entrySet().stream()
            .sorted((a, b) -> Double.compare(b.getValue().open, a.getValue().open))
            .limit(limit)
            .map(Map.Entry::getKey)
            .collect(Collectors.toList());
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
    
    // maybe remove ------------------------------------
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

}
