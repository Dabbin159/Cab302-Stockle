package com.stockle.api.data;

/**
 * Data model for company logo information.
 */
public class CompanyLogo {
    public String symbol;
    public String imageUrl;

    /**
     * Constructor for empty Company logo
     */
    public CompanyLogo() {
    }

    /**
     * Constructor for company logo
     * @param symbol stock symbol
     * @param imageUrl URL of company image
     */
    public CompanyLogo(String symbol, String imageUrl) {
        this.symbol = symbol;
        this.imageUrl = imageUrl;
    }
}
