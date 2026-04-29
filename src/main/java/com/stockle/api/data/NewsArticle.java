package com.stockle.api.data;

import java.util.List;

/**
 * Data model for a news article from Alpaca News v3
 */
public class NewsArticle {
    public String id;
    public String headline;
    public String summary;
    public String url;
    public String published_at;
    public String updated_at;
    public String source;
    public String image_url;
    public List<String> symbols;

    public NewsArticle() {
    }
}
