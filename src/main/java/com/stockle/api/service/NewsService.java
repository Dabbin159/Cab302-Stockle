package com.stockle.api.service;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.stockle.api.client.ApiClient;
import com.stockle.api.data.NewsArticle;

/**
 * Service for fetching news articles from the Alpaca News API.
 *
 * Provides methods to retrieve the latest global news and symbol-specific news
 * from Alpaca's news endpoints. Responses are parsed into `NewsArticle` objects.
 *
 */
public class NewsService {
    private final ApiClient apiClient;
    private final ObjectMapper objectMapper;

    /**
     * Constructs a NewsService with required dependencies.
     *
     * @param apiClient the API client for making HTTP requests to Alpaca
     * @param objectMapper the JSON object mapper for parsing API responses
     */
    public NewsService(ApiClient apiClient, ObjectMapper objectMapper) {
        this.apiClient = apiClient;
        this.objectMapper = objectMapper;
    }

    /**
     * Retrieves the latest global news articles.
     *
     * gets up to a certain amount of most recent articles from the Alpaca News endpoint.
     * If limit is less than or equal to zero, a default value of 10 is used.
     *
     * @param limit maximum number of articles to return
     * @return a list of newsarticle objects, empty list if an error occurs
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
     * Retrieves the latest news articles for a specific stock symbol.
     *
     * gets up to a certain amount of most recent articles filtered by the provided symbol.
     * If symbol is null or empty, an empty list is returned. If limit
     * is less than or equal to zero, a default value of 10 is used.
     *
     * @param symbol the stock symbol to filter news by (e.g., "AAPL")
     * @param limit maximum number of articles to return
     * @return a list of newsarticle objects, empty list if symbol is invalid or an error occurs
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
