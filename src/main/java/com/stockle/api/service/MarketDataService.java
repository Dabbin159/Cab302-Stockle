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

public class MarketDataService {
    private final ApiClient apiClient;
    private final ObjectMapper objectMapper;

    public MarketDataService(ApiClient apiClient, ObjectMapper objectMapper) {
        this.apiClient = apiClient;
        this.objectMapper = objectMapper;
    }

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
