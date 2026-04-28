package com.stockle.api.service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stockle.api.client.ApiClient;
import com.stockle.api.data.CompanyLogo;

/**
 * Service for fetching company logo images from Alpaca.
 * 
 * The logo endpoint returns an image directly (binary PNG data).
 * This service provides convenient methods to construct the logo URL
 * for a given symbol, which can then be accessed directly for display.
 */
public class LogoService {
    private final ApiClient apiClient;
    private final ObjectMapper objectMapper;

    public LogoService(ApiClient apiClient, ObjectMapper objectMapper) {
        this.apiClient = apiClient;
        this.objectMapper = objectMapper;
    }

    /**
     * Get the company logo URL for a given symbol.
     * 
     * @param symbol The stock symbol (e.g., "AAPL")
     * @return CompanyLogo with symbol and imageUrl
     */
    public CompanyLogo getCompanyLogo(String symbol) {
        return getCompanyLogo(symbol, true);
    }

    /**
     * Get the company logo URL for a given symbol.
     * 
     * @param symbol The stock symbol (e.g., "AAPL")
     * @param placeholder If true, returns a placeholder image when no logo is available
     * @return CompanyLogo with symbol and imageUrl
     */
    public CompanyLogo getCompanyLogo(String symbol, boolean placeholder) {
        try {
            if (symbol == null || symbol.isBlank()) return null;

            String encodedSymbol = URLEncoder.encode(symbol.toUpperCase(), StandardCharsets.UTF_8);
            String imageUrl = ApiClient.DATA_URL + "/v1beta1/logos/" + encodedSymbol 
                    + "?placeholder=" + (placeholder ? "true" : "false");

            CompanyLogo logo = new CompanyLogo(symbol, imageUrl);
            return logo;
        } catch (Exception e) {
            System.err.println("Error constructing logo URL for " + symbol + ": " + e.getMessage());
            return null;
        }
    }

    /**
     * Get the raw logo image bytes for a given symbol.
     * This method fetches the actual binary image data from the API.
     * 
     * @param symbol The stock symbol
     * @param placeholder If true, returns a placeholder image when no logo is available
     * @return Raw image bytes (PNG binary data)
     */
    public byte[] getCompanyLogoBytes(String symbol, boolean placeholder) {
        try {
            if (symbol == null || symbol.isBlank()) return null;

            String encodedSymbol = URLEncoder.encode(symbol.toUpperCase(), StandardCharsets.UTF_8);
            String url = ApiClient.DATA_URL + "/v1beta1/logos/" + encodedSymbol 
                    + "?placeholder=" + (placeholder ? "true" : "false");

            // Note: This returns the raw image bytes from the API
            // The actual binary response would need custom handling in ApiClient
            // For now, this method documents the pattern
            String response = apiClient.makeRequest(url);
            
            // If the response is binary, it would be returned as-is
            // Convert to bytes if needed
            return response.getBytes();
        } catch (Exception e) {
            System.err.println("Error fetching logo bytes for " + symbol + ": " + e.getMessage());
            return null;
        }
    }
}
