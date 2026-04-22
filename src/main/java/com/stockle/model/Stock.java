package com.stockle.model;

import org.json.JSONObject;

public class Stock {

    // Fields
    // Logo
    private Long stockNumber;
    private String companyName;
    private String sector;
    private Long currentPrice;
    private Float dailyChange; // Percentage
    private Long volume;

    // Constructor
    public Stock(Long stockNumber, String companyName, String sector, Long currentPrice, Float dailyChange, Long volume) {
        this.stockNumber = stockNumber;
        this.companyName = companyName;
        this.sector = sector;
        this.currentPrice = currentPrice;
        this.dailyChange = dailyChange;
        this.volume = volume;
    }

    public JSONObject toJSON() {
        JSONObject json = new JSONObject();
        json.put("stockNumber", stockNumber);
        json.put("companyName", companyName);
        json.put("sector", sector);
        json.put("currentPrice", currentPrice);
        json.put("dailyChange", dailyChange);
        json.put("volume", volume);
        return json;
    }

    public static Stock fromJSON(JSONObject json) {
        Long stockNumber = json.getLong("stockNumber");
        String companyName = json.getString("companyName");
        String sector = json.getString("sector");
        Long currentPrice = json.getLong("currentPrice");
        Float dailyChange = json.getFloat("dailyChange");
        Long volume = json.getLong("volume");
        return new Stock(stockNumber, companyName, sector, currentPrice, dailyChange, volume);
    }
}
