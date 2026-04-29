package com.stockle.test;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stockle.api.client.ApiClient;
import com.stockle.api.data.NewsArticle;
import com.stockle.api.service.NewsService;

public class NewsService_Test {

    private ApiClient apiClient;
    private ObjectMapper objectMapper;
    private NewsService newsService;

    @BeforeEach
    public void setUp() {
        apiClient = new ApiClient();
        objectMapper = new ObjectMapper();
        newsService = new NewsService(apiClient, objectMapper);
    }

    @Test
    public void testLatestNews() {
        System.out.println("--- NewsService Test: getLatestNews(10) ---");
        try {
            List<NewsArticle> articles = newsService.getLatestNews(10);
            if (articles == null || articles.isEmpty()) {
                System.out.println("No news articles returned");
            } else {
                System.out.println("Fetched " + articles.size() + " articles (showing up to 5):");
                for (int i = 0; i < Math.min(5, articles.size()); i++) {
                    NewsArticle a = articles.get(i);
                    System.out.println("  " + (i + 1) + ". " + a.headline + " (" + a.published_at + ")");
                }
            }
        } catch (Exception e) {
            System.out.println("Exception fetching latest news: " + e.getMessage());
            e.printStackTrace();
        }
        System.out.println();
    }

    @Test
    public void testLatestNewsForSymbol() {
        System.out.println("--- NewsService Test: getLatestNewsForSymbol(\"AAPL\", 10) ---");
        try {
            List<NewsArticle> articles = newsService.getLatestNewsForSymbol("AAPL", 10);
            if (articles == null || articles.isEmpty()) {
                System.out.println("No news articles returned for AAPL");
            } else {
                System.out.println("Fetched " + articles.size() + " articles for AAPL (showing up to 5):");
                for (int i = 0; i < Math.min(5, articles.size()); i++) {
                    NewsArticle a = articles.get(i);
                    System.out.println("  " + (i + 1) + ". " + a.headline + " (" + a.published_at + ")");
                }
            }
        } catch (Exception e) {
            System.out.println("Exception fetching news for AAPL: " + e.getMessage());
            e.printStackTrace();
        }
        System.out.println();
    }
}
