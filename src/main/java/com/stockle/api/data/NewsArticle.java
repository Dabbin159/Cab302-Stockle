package com.stockle.api.data;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
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

<<<<<<< HEAD
    /**
     * Constructor for empty news article
     */
    public NewsArticle() {
    }
=======
    public NewsArticle() {}
>>>>>>> e27b0e69b8ee6121a6382dd9085b60b2d0e3a1c9
}
