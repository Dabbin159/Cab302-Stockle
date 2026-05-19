package com.stockle.api.apiTests;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stockle.api.client.ApiClient;
import com.stockle.api.data.CompanyLogo;
import com.stockle.api.service.LogoService;

public class LogoService_Test {

    private ApiClient apiClient;
    private ObjectMapper objectMapper;
    private LogoService logoService;

    public void setUp() {
        apiClient = new ApiClient();
        objectMapper = new ObjectMapper();
        logoService = new LogoService(apiClient, objectMapper);
    }

    public void testGetCompanyLogoUrl() {
        System.out.println("--- LogoService Test: getCompanyLogo(\"AAPL\") ---");
        try {
            CompanyLogo logo = logoService.getCompanyLogo("AAPL");
            if (logo == null) {
                System.out.println("Failed to get logo");
            } else {
                System.out.println("Logo retrieved:");
                System.out.println("  Symbol: " + logo.symbol);
                System.out.println("  Image URL: " + logo.imageUrl);
            }
        } catch (Exception e) {
            System.out.println("Exception fetching logo: " + e.getMessage());
            e.printStackTrace();
        }
        System.out.println();
    }

    public void testGetCompanyLogoWithoutPlaceholder() {
        System.out.println("--- LogoService Test: getCompanyLogo(\"TSLA\", false) ---");
        try {
            CompanyLogo logo = logoService.getCompanyLogo("TSLA", false);
            if (logo == null) {
                System.out.println("Failed to get logo");
            } else {
                System.out.println("Logo retrieved:");
                System.out.println("  Symbol: " + logo.symbol);
                System.out.println("  Image URL: " + logo.imageUrl);
            }
        } catch (Exception e) {
            System.out.println("Exception fetching logo: " + e.getMessage());
            e.printStackTrace();
        }
        System.out.println();
    }
}
