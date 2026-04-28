package com.stockle.model;

import org.json.JSONObject;

public class Trade {

    // Fields
    private final int id;
    private final Double userNumber;
    private final String symbol;
    private final Boolean type;   // Buy (true) or Sell (false)
    private final Long totalValue;
    private final String timeStamp;

    public Trade(int id, Double userNumber, String symbol, Boolean type, Long totalValue, String timeStamp) {
        this.id = id;
        this.userNumber = userNumber;
        this.symbol = symbol;
        this.type = type;
        this.totalValue = totalValue;
        this.timeStamp = timeStamp;
    }

    // Getters
    public int getId() {
        return id;
    }

    public Double getUserNumber() {
        return userNumber;
    }

    public String getSymbol() {
        return symbol;
    }

    public Boolean getType() {
        return type;
    }

    public Long getTotalValue() {
        return totalValue;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public JSONObject toJSON() {
        JSONObject json = new JSONObject();
        json.put("id", id);
        json.put("userNumber", userNumber);
        json.put("symbol", symbol);
        json.put("type", type);
        json.put("totalValue", totalValue);
        json.put("timeStamp", timeStamp);
        return json;
    }

    public static Trade fromJSON(int tradeid, JSONObject json) {
        int id = tradeid;
        Double userNumber = json.getDouble("userNumber");
        String symbol = json.getString("symbol");
        Boolean type = json.getBoolean("type");
        Long totalValue = json.getLong("totalValue");
        String timeStamp = json.getString("timeStamp");
        return new Trade(id, userNumber, symbol, type, totalValue, timeStamp);
    }

}
