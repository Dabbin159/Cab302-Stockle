package com.stockle.api.data;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
/**
 * Represents a news article related to a stock, as returned by the API
 */
public class NewsArticle {
    public long id;
    public String headline;
    public String summary;
    public String url;
    public String source;
    public String author;

    @JsonAlias("created_at")
    public String published_at;

    public String updated_at;
    public List<String> symbols;

    /**
     * Constructor for empty news article
     */
    public NewsArticle() {
    }
}
