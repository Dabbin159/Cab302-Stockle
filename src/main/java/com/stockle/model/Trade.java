package com.stockle.model;

import org.json.JSONObject;

public class Trade {

    // Fields
    private int id;
    private Double userNumber;
    private Stock stock;
    private Boolean type; // Buy or Sell
    private Long totalValue;
    private String timeStamp; // Time of the trade

    public Trade(int id, Double userNumber, Stock stock, Boolean type, Long totalValue, String timeStamp) {
        this.id = id;
        this.userNumber = userNumber;
        this.stock = stock;
        this.type = type;
        this.totalValue = totalValue;
        this.timeStamp = timeStamp;
    }
        public JSONObject toJSON() {
        JSONObject json = new JSONObject();
        json.put("id", id);
        json.put("userNumber", userNumber);
        json.put("stock", stock.toJSON());
        json.put("type", type);
        json.put("totalValue", totalValue);
        json.put("timeStamp", timeStamp);
        return json;
    }

    public static Trade fromJSON(int tradeid, JSONObject json) {
        int id = tradeid; // ID is passed separately as it's not part of the JSON object
        Double userNumber = json.getDouble("userNumber");
        Stock stock = Stock.fromJSON(json.getJSONObject("stock"));
        Boolean type = json.getBoolean("type");
        Long totalValue = json.getLong("totalValue");
        String timeStamp = json.getString("timeStamp");
        return new Trade(id, userNumber, stock, type, totalValue, timeStamp);
    }

}
