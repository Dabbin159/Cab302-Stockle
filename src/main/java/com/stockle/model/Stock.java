package com.stockle.model;

import org.json.JSONObject;

public class Stock {

    // Fields
    // Logo
    private String companyName;
    private String sector;
    private int currentPrice;
    private Float dailyChange; // Percentage
    private Long volume;

    // Constructor
    public Stock(String companyName, String sector, int currentPrice, Float dailyChange, Long volume) {
        this.companyName = companyName;
        this.sector = sector;
        this.currentPrice = currentPrice;
        this.dailyChange = dailyChange;
        this.volume = volume;
    }

    public JSONObject toJSON() {
        JSONObject json = new JSONObject();
        json.put("companyName", companyName);
        json.put("sector", sector);
        json.put("currentPrice", currentPrice);
        json.put("dailyChange", dailyChange);
        json.put("volume", volume);
        return json;
    }

    public static Stock fromJSON(JSONObject json) {
        String companyName = json.getString("companyName");
        String sector = json.getString("sector");
        int currentPrice = json.getInt("currentPrice");
        Float dailyChange = json.getFloat("dailyChange");
        Long volume = json.getLong("volume");
        return new Stock(companyName, sector, currentPrice, dailyChange, volume);
    }
        
    public String getCompanyName() {
        return companyName;
    }

    public String getSector() {
        return sector;
    }

    public int getCurrentPrice() {
        return currentPrice;
    }

    public Float getDailyChange() {
        return dailyChange;
    }

    public Long getVolume() {
        return volume;
    }

    public void setCurrentPrice(int currentPrice) {
        this.currentPrice = currentPrice;
    }

    public void setDailyChange(Float dailyChange) {
        this.dailyChange = dailyChange;
    }

    public void setVolume(Long volume) {
        this.volume = volume;
    }

}
