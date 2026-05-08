package com.stockle.test;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stockle.api.client.ApiClient;
import com.stockle.api.data.NewsArticle;
import com.stockle.api.service.NewsService;

public class Dashboard_Test {
    private ApiClient apiClient;

    @BeforeEach
    public void setUp() {
        apiClient = new ApiClient();
    }

    @Test
    public void TestLoadHoldings() {
        
    }

}
