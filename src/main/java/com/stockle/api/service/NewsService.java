package com.stockle.api.service;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.stockle.api.client.ApiClient;
import com.stockle.api.data.NewsArticle;

/**
 * Service for fetching news articles from Alpaca News v3
 */
public class NewsService {
    private final ApiClient apiClient;
    private final ObjectMapper objectMapper;

    public NewsService(ApiClient apiClient, ObjectMapper objectMapper) {
        this.apiClient = apiClient;
        this.objectMapper = objectMapper;
    }

    /**
     * Get the latest news articles (global). Returns up to `limit` articles.
     */
    public List<NewsArticle> getLatestNews(int limit) {
        List<NewsArticle> result = new ArrayList<>();
        try {
            if (limit <= 0) limit = 10;
            String url = ApiClient.DATA_URL + "/v1beta1/news?limit=" + limit;
            String response = apiClient.makeRequest(url);

            JsonNode root = objectMapper.readTree(response);

            JsonNode articlesNode = null;
            if (root.isArray()) {
                articlesNode = root;
            } else if (root.has("articles")) {
                articlesNode = root.get("articles");
            } else if (root.has("news")) {
                articlesNode = root.get("news");
            } else {
                // fallback: try treating root as single-article array
                articlesNode = root;
            }

            if (articlesNode != null && articlesNode.isArray()) {
                for (JsonNode item : articlesNode) {
                    try {
                        NewsArticle article = objectMapper.convertValue(item, NewsArticle.class);
                        result.add(article);
                    } catch (Exception ex) {
                        // skip malformed article
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error fetching latest news: " + e.getMessage());
        }
        return result;
    }

    /**
     * Get the latest news articles related to a specific symbol. Returns up to `limit` articles.
     */
    public List<NewsArticle> getLatestNewsForSymbol(String symbol, int limit) {
        List<NewsArticle> result = new ArrayList<>();
        try {
            if (symbol == null || symbol.isEmpty()) return result;
            if (limit <= 0) limit = 10;
            String url = ApiClient.DATA_URL + "/v1beta1/news?symbols=" + java.net.URLEncoder.encode(symbol, java.nio.charset.StandardCharsets.UTF_8) + "&limit=" + limit;
            String response = apiClient.makeRequest(url);

            JsonNode root = objectMapper.readTree(response);

            JsonNode articlesNode = null;
            if (root.isArray()) {
                articlesNode = root;
            } else if (root.has("articles")) {
                articlesNode = root.get("articles");
            } else if (root.has("news")) {
                articlesNode = root.get("news");
            } else {
                articlesNode = root;
            }

            if (articlesNode != null && articlesNode.isArray()) {
                for (JsonNode item : articlesNode) {
                    try {
                        NewsArticle article = objectMapper.convertValue(item, NewsArticle.class);
                        result.add(article);
                    } catch (Exception ex) {
                        // skip malformed article
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error fetching news for symbol " + symbol + ": " + e.getMessage());
        }
        return result;
    }
}
