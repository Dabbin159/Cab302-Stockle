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

public class SnapshotService {
    private final ApiClient apiClient;
    private final ObjectMapper objectMapper;

    public SnapshotService(ApiClient apiClient, ObjectMapper objectMapper) {
        this.apiClient = apiClient;
        this.objectMapper = objectMapper;
    }

    public Map<String, SnapshotData> getSnapshots(List<String> symbols, String feed) {
        Map<String, SnapshotData> result = new HashMap<>();

        try {
            String symbolList = String.join(",", symbols);
            String url = ApiClient.DATA_URL + "/v2/stocks/snapshots?symbols=" + symbolList + "&feed=" + feed;
            String response = apiClient.makeRequest(url);

            JsonNode root = objectMapper.readTree(response);
            JsonNode snapshotsData = root.get("snapshots");

            for (String symbol : symbols) {
                JsonNode snapshotNode = snapshotsData.get(symbol);
                if (snapshotNode != null) {
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

                    if (bar != null && quote != null) {
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
