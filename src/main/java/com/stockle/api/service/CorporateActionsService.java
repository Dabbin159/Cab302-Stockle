package com.stockle.api.service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.stockle.api.client.ApiClient;
import com.stockle.api.data.CorporateAction;

/**
 * Service for Alpaca Corporate Actions API.
 */
public class CorporateActionsService {
    private final ApiClient apiClient;
    private final ObjectMapper objectMapper;

    public CorporateActionsService(ApiClient apiClient, ObjectMapper objectMapper) {
        this.apiClient = apiClient;
        this.objectMapper = objectMapper;
    }

    public List<CorporateAction> getCorporateActions(int limit, String sort) {
        return getCorporateActions(null, null, null, null, limit, sort, null);
    }

    public List<CorporateAction> getCorporateActionsBySymbol(String symbol, int limit, String sort) {
        return getCorporateActions(symbol, null, null, null, limit, sort, null);
    }

    public List<CorporateAction> getCorporateActions(
            String symbol,
            String typesCsv,
            LocalDate start,
            LocalDate end,
            int limit,
            String sort,
            String pageToken
    ) {
        List<CorporateAction> result = new ArrayList<>();
        try {
            if (limit <= 0) limit = 100;
            if (limit > 1000) limit = 1000;
            if (sort == null || sort.isBlank()) sort = "asc";

            StringBuilder url = new StringBuilder(ApiClient.DATA_URL)
                    .append("/v1/corporate-actions?limit=")
                    .append(limit)
                    .append("&sort=")
                    .append(URLEncoder.encode(sort, StandardCharsets.UTF_8));

            if (symbol != null && !symbol.isBlank()) {
                url.append("&symbols=").append(URLEncoder.encode(symbol, StandardCharsets.UTF_8));
            }
            if (typesCsv != null && !typesCsv.isBlank()) {
                url.append("&types=").append(URLEncoder.encode(typesCsv, StandardCharsets.UTF_8));
            }
            if (start != null) {
                url.append("&start=").append(start);
            }
            if (end != null) {
                url.append("&end=").append(end);
            }
            if (pageToken != null && !pageToken.isBlank()) {
                url.append("&page_token=").append(URLEncoder.encode(pageToken, StandardCharsets.UTF_8));
            }

            String response = apiClient.makeRequest(url.toString());
            JsonNode root = objectMapper.readTree(response);

            JsonNode itemsNode = null;
            if (root.isArray()) {
                itemsNode = root;
            } else if (root.has("corporate_actions")) {
                itemsNode = root.get("corporate_actions");
            } else if (root.has("announcements")) {
                itemsNode = root.get("announcements");
            }

            if (itemsNode != null && itemsNode.isArray()) {
                for (JsonNode item : itemsNode) {
                    try {
                        CorporateAction action = objectMapper.convertValue(item, CorporateAction.class);
                        action.raw = objectMapper.convertValue(item, new com.fasterxml.jackson.core.type.TypeReference<>() {
                        });
                        result.add(action);
                    } catch (Exception ex) {
                        // Skip malformed entries and continue parsing.
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error fetching corporate actions: " + e.getMessage());
        }

        return result;
    }
}
