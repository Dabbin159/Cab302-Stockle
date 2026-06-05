package com.stockle.api.service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.stockle.api.client.ApiClient;
import com.stockle.api.data.BarData;
import com.stockle.api.data.QuoteData;

/**
 * Service for retrieving latest market data (bars and quotes) from the Alpaca API.
 *
 * Provides convenience methods to fetch the most recent bar (ohlcv) and quote
 * (bid/ask) information for a list of symbols using the configured data feed.
 * Responses are parsed into `BarData` and `QuoteData` objects keyed by symbol.
 *
 */
public class MarketDataService {
    private final ApiClient apiClient;
    private final ObjectMapper objectMapper;

    /**
     * Constructs a MarketDataService with required dependencies.
     *
     * @param apiClient the API client used to make HTTP requests to the data API
     * @param objectMapper the JSON object mapper used to parse API responses
     */
    public MarketDataService(ApiClient apiClient, ObjectMapper objectMapper) {
        this.apiClient = apiClient;
        this.objectMapper = objectMapper;
    }

    /**
     * gets the latest bar (ohlcv) for each symbol in the provided list.
     *
     * Sends a request to the Alpaca latest bars endpoint and parses the
     * per-symbol bar data into `BarData` instances.
     *
     * @param symbols the list of stock symbols to fetch (e.g., ["AAPL","MSFT"])
     * @param feed the market data feed to use (e.g., "sip" or "iex")
     * @return a map from symbol to the latest `BarData` for that symbol; symbols
     *         without data will be absent from the map
     */
    public Map<String, BarData> getLatestBars(List<String> symbols, String feed) {

        Map<String, BarData> result = new HashMap<>();
        try {
            String symbolList = String.join(",", symbols);
            String url = ApiClient.DATA_URL + "/v2/stocks/bars/latest?symbols=" + symbolList + "&feed=" + feed;
            String response = apiClient.makeRequest(url);

            JsonNode root = objectMapper.readTree(response);
            JsonNode barsData = root.get("bars");

            for (String symbol : symbols) {
                JsonNode barNode = barsData.get(symbol);
                if (barNode != null) {
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
        }
        return result;
    }

    /**
     * gets the latest quote (bid/ask) for each symbol in the provided list.
     *
     * Sends a request to the Alpaca latest quotes endpoint and parses the
     * per-symbol quote data into `QuoteData` instances.
     *
     * @param symbols the list of stock symbols to fetch (e.g., ["AAPL","MSFT"])
     * @param feed the market data feed to use (e.g., "sip" or "iex")
     * @return a map from symbol to the latest `QuoteData` for that symbol
     * symbols without data will be absent from the map
     */
    public Map<String, QuoteData> getLatestQuotes(List<String> symbols, String feed) {

        Map<String, QuoteData> result = new HashMap<>();
        try {
            String symbolList = String.join(",", symbols);
            String url = ApiClient.DATA_URL + "/v2/stocks/quotes/latest?symbols=" + symbolList + "&feed=" + feed;
            String response = apiClient.makeRequest(url);

            JsonNode root = objectMapper.readTree(response);
            JsonNode quotesData = root.get("quotes");

            for (String symbol : symbols) {
                JsonNode quoteNode = quotesData.get(symbol);
                if (quoteNode != null) {
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
        }
        return result;
    }
}
