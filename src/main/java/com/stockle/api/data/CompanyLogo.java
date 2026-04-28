package com.stockle.api.data;

/**
 * Data model for company logo information.
 */
public class CompanyLogo {
    public String symbol;
    public String imageUrl;

    public CompanyLogo() {
    }

    public CompanyLogo(String symbol, String imageUrl) {
        this.symbol = symbol;
        this.imageUrl = imageUrl;
    }
}
