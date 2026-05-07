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
import com.stockle.api.data.SnapshotData;

/**
 * Service for retrieving per-symbol snapshot data from the Alpaca API.
 *
 * snapshot contains the most recent bar and quote information for a symbol.
 * This service gets snapshots for a list of symbols and parses the response
 * into `SnapshotData` objects keyed by symbol.
 */
public class SnapshotService {
    private final ApiClient apiClient;
    private final ObjectMapper objectMapper;

    /**
     * Constructs a SnapshotService with required dependencies.
     *
     * @param apiClient the API client used to make HTTP requests to the data API
     * @param objectMapper the JSON object mapper used to parse API responses
     */
    public SnapshotService(ApiClient apiClient, ObjectMapper objectMapper) {
        this.apiClient = apiClient;
        this.objectMapper = objectMapper;
    }

    /**
     * gets snapshots (latest bar and quote) for the given symbols.
     *
     * Sends a request to the Alpaca snapshots endpoint and extracts per-symbol
     * minute/daily/previous bar and latest quote information. If a bar or quote
     * is not available for a symbol it will be omitted for that symbol.
     *
     * @param symbols the list of stock symbols to fetch snapshots for
     * @param feed the market data feed to use (e.g., "sip" or "iex")
     * @return a map from symbol to `SnapshotData` containing bar and quote information
     */
    public Map<String, SnapshotData> getSnapshots(List<String> symbols, String feed) {
        Map<String, SnapshotData> result = new HashMap<>();

        try {
            String symbolList = String.join(",", symbols);
            String url = ApiClient.DATA_URL + "/v2/stocks/snapshots?symbols=" + symbolList + "&feed=" + feed;
            String response = apiClient.makeRequest(url);

            JsonNode root = objectMapper.readTree(response);
            JsonNode snapshotsData = root.get("snapshots");
            if (snapshotsData == null || snapshotsData.isNull()) {
                return result;
            }

            for (String symbol : symbols) {
                JsonNode snapshotNode = snapshotsData.get(symbol);
                if (snapshotNode != null) {
                    JsonNode barNode = snapshotNode.get("minuteBar");
                    if (barNode == null || barNode.isNull()) {
                        barNode = snapshotNode.get("dailyBar");
                    }
                    if (barNode == null || barNode.isNull()) {
                        barNode = snapshotNode.get("prevDailyBar");
                    }
                    if (barNode == null || barNode.isNull()) {
                        barNode = snapshotNode.get("bar");
                    }

                    BarData bar = null;
                    if (barNode != null && !barNode.isNull() && barNode.get("t") != null) {
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

                    JsonNode quoteNode = snapshotNode.get("latestQuote");
                    if (quoteNode == null || quoteNode.isNull()) {
                        quoteNode = snapshotNode.get("quote");
                    }

                    QuoteData quote = null;
                    if (quoteNode != null && !quoteNode.isNull() && quoteNode.get("t") != null) {
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

                    if (bar != null || quote != null) {
                        result.put(symbol, new SnapshotData(bar, quote));
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error fetching snapshots: " + e.getMessage());
        }

        return result;
    }
}
