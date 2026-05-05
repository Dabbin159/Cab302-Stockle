package com.stockle.api.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.stockle.api.client.ApiClient;
import com.stockle.api.data.BarData;
import com.stockle.api.data.QuoteData;
import com.stockle.api.data.TradeData;

/**
 * Service for retrieving historical market data from the Alpaca API.
 * 
 * Provides methods to get historical bar data (ohlcv), quote data (bid/ask),
 * and trade data for stocks within specified date ranges and timeframes.
 * Supports pagination for large datasets.
 * 
 */
public class HistoricalDataService {
    private final ApiClient apiClient;
    private final ObjectMapper objectMapper;

    /**
     * Constructs a HistoricalDataService with required dependencies.
     * 
     * @param apiClient the API client for making HTTP requests to Alpaca
     * @param objectMapper the JSON object mapper for parsing API responses
     */
    public HistoricalDataService(ApiClient apiClient, ObjectMapper objectMapper) {
        this.apiClient = apiClient;
        this.objectMapper = objectMapper;
    }

    /**
     * Retrieves historical bar data (ohlcv) for a stock within a date range.
     * 
     * gets historical bars (open, high, low, close, volume) for the specified symbol
     * and timeframe from the Alpaca API. Automatically handles pagination to retrieve
     * all available data up to a maximum of 100 pages.
     * 
     * @param symbol the stock symbol (e.g., "AAPL")
     * @param startDate the start date for the historical data (inclusive)
     * @param endDate the end date for the historical data (inclusive)
     * @param timeframe the bar timeframe (e.g., "1Min", "5Min", "1Hour", "1Day")
     * @param feed the market data feed to use (e.g., "sip" or "iex")
     * @return a list of BarData objects representing historical bars
     */
    public List<BarData> getHistoricalBars(String symbol, String startDate, String endDate, String timeframe, String feed) {
        List<BarData> bars = new ArrayList<>();
        String nextPageToken = null;
        int maxIterations = 100;
        int iterations = 0;

        try {
            boolean hasMore = true;

            while (hasMore && iterations < maxIterations) {
                iterations++;

                StringBuilder urlBuilder = new StringBuilder(ApiClient.DATA_URL + "/v2/stocks/bars");
                urlBuilder.append("?symbols=").append(symbol);
                urlBuilder.append("&timeframe=").append(timeframe);
                urlBuilder.append("&start=").append(startDate);
                urlBuilder.append("&end=").append(endDate);
                urlBuilder.append("&limit=10000");
                urlBuilder.append("&sort=asc");
                urlBuilder.append("&feed=").append(feed);

                if (nextPageToken != null) {
                    urlBuilder.append("&page_token=").append(nextPageToken);
                }

                String url = urlBuilder.toString();
                String response = apiClient.makeRequest(url);

                JsonNode root = objectMapper.readTree(response);
                JsonNode barsObject = root.get("bars");
                if (barsObject != null && barsObject.isObject()) {
                    JsonNode barsArray = barsObject.get(symbol);
                    if (barsArray != null && barsArray.isArray()) {
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
                    }
                }

                JsonNode nextTokenNode = root.get("next_page_token");
                if (nextTokenNode != null && !nextTokenNode.isNull()) {
                    nextPageToken = nextTokenNode.asText();
                } else {
                    hasMore = false;
                }
            }

            if (iterations >= maxIterations) {
                System.err.println("Warning: Reached maximum iterations (" + maxIterations + ") for historical bars");
            }
        } catch (Exception e) {
            System.err.println("Error fetching historical bars for " + symbol + ": " + e.getMessage());
        }

        return bars;
    }

    /**
     * Retrieves historical quote data (bid/ask) for a stock within a date range.
     * 
     * gets historical quotes including bid/ask prices and sizes for the specified
     * symbol from the Alpaca API. Automatically handles pagination to retrieve all
     * available data up to a maximum of 10 pages.
     * 
     * @param symbol the stock symbol (e.g., "AAPL")
     * @param startDate the start date for the historical data (inclusive)
     * @param endDate the end date for the historical data (inclusive)
     * @param feed the market data feed to use (e.g., "sip" or "iex")
     * @return a list of QuoteData objects representing historical quotes
     */
    public List<QuoteData> getHistoricalQuotes(String symbol, String startDate, String endDate, String feed) {
        List<QuoteData> quotes = new ArrayList<>();
        String nextPageToken = null;
        int maxIterations = 10;
        int iterations = 0;

        try {
            boolean hasMore = true;

            while (hasMore && iterations < maxIterations) {
                iterations++;

                StringBuilder urlBuilder = new StringBuilder(ApiClient.DATA_URL + "/v2/stocks/quotes");
                urlBuilder.append("?symbols=").append(symbol);
                urlBuilder.append("&start=").append(startDate);
                urlBuilder.append("&end=").append(endDate);
                urlBuilder.append("&limit=10");
                urlBuilder.append("&sort=asc");
                urlBuilder.append("&feed=").append(feed);

                if (nextPageToken != null) {
                    urlBuilder.append("&page_token=").append(nextPageToken);
                }

                String url = urlBuilder.toString();
                String response = apiClient.makeRequest(url);

                JsonNode root = objectMapper.readTree(response);
                JsonNode quotesObject = root.get("quotes");
                if (quotesObject != null && quotesObject.isObject()) {
                    JsonNode quotesArray = quotesObject.get(symbol);
                    if (quotesArray != null && quotesArray.isArray()) {
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
                    }
                }

                JsonNode nextTokenNode = root.get("next_page_token");
                if (nextTokenNode != null && !nextTokenNode.isNull()) {
                    nextPageToken = nextTokenNode.asText();
                } else {
                    hasMore = false;
                }
            }

            if (iterations >= maxIterations) {
                System.err.println("Warning: Reached maximum iterations (" + maxIterations + ") for historical quotes");
            }
        } catch (Exception e) {
            System.err.println("Error fetching historical quotes for " + symbol + ": " + e.getMessage());
        }

        return quotes;
    }

    /**
     * Retrieves historical trade data for a stock within a date range.
     * 
     * gets historical trade records including price, size, and exchange for the
     * specified symbol from the Alpaca API. Automatically handles pagination to retrieve
     * all available data up to a maximum of 100 pages.
     * 
     * @param symbol the stock symbol (e.g., "AAPL")
     * @param startDate the start date for the historical data (inclusive)
     * @param endDate the end date for the historical data (inclusive)
     * @param feed the market data feed to use (e.g., "sip" or "iex")
     * @return a list of TradeData objects representing historical trades
     */
    public List<TradeData> getHistoricalTrades(String symbol, String startDate, String endDate, String feed) {
        List<TradeData> trades = new ArrayList<>();
        String nextPageToken = null;
        int maxIterations = 100;
        int iterations = 0;

        try {
            boolean hasMore = true;

            while (hasMore && iterations < maxIterations) {
                iterations++;

                StringBuilder urlBuilder = new StringBuilder(ApiClient.DATA_URL + "/v2/stocks/trades");
                urlBuilder.append("?symbols=").append(symbol);
                urlBuilder.append("&start=").append(startDate);
                urlBuilder.append("&end=").append(endDate);
                urlBuilder.append("&limit=10000");
                urlBuilder.append("&sort=asc");
                urlBuilder.append("&feed=").append(feed);

                if (nextPageToken != null) {
                    urlBuilder.append("&page_token=").append(nextPageToken);
                }

                String url = urlBuilder.toString();
                String response = apiClient.makeRequest(url);

                JsonNode root = objectMapper.readTree(response);
                JsonNode tradesObject = root.get("trades");
                if (tradesObject != null && tradesObject.isObject()) {
                    JsonNode tradesArray = tradesObject.get(symbol);
                    if (tradesArray != null && tradesArray.isArray()) {
                        for (JsonNode tradeNode : tradesArray) {
                            String timestamp = tradeNode.get("t").asText();
                            LocalDateTime dateTime = java.time.OffsetDateTime.parse(timestamp).toLocalDateTime();

                            TradeData trade = new TradeData(
                                    dateTime,
                                    tradeNode.get("p").asDouble(),
                                    tradeNode.get("s").asLong(),
                                    tradeNode.get("x").asText()
                            );
                            trades.add(trade);
                        }
                    }
                }

                JsonNode nextTokenNode = root.get("next_page_token");
                if (nextTokenNode != null && !nextTokenNode.isNull()) {
                    nextPageToken = nextTokenNode.asText();
                } else {
                    hasMore = false;
                }
            }

            if (iterations >= maxIterations) {
                System.err.println("Warning: Reached maximum iterations (" + maxIterations + ") for historical trades");
            }
        } catch (Exception e) {
            System.err.println("Error fetching historical trades for " + symbol + ": " + e.getMessage());
        }

        return trades;
    }
}
