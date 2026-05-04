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

    @JsonAlias("created_at")
    public String published_at;

    public String updated_at;
    public List<String> symbols;

    public NewsArticle() {}
}
