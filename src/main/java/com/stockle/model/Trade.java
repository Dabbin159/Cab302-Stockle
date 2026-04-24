package com.stockle.model;

import org.json.JSONObject;

public class Trade {

    // Fields
    private int id;
    private int userNumber;
    private Stock stock;
    private Boolean type; // Buy or Sell
    private Long totalValue;
    private long quantity;
    private String timeStamp; // Time of the trade

    public Trade(int userNumber, Stock stock, Boolean type, Long totalValue, String timeStamp) {
        this.userNumber = userNumber;
        this.stock = stock;
        this.type = type;
        this.quantity = quantity;
        this.totalValue = totalValue;
        this.timeStamp = timeStamp;
    }

    public void setID(int id) {
        this.id = id;
    }

    public JSONObject toJSON() {
        JSONObject json = new JSONObject();
        json.put("id", id);
        json.put("userNumber", userNumber);
        json.put("stock", stock.toJSON());
        json.put("type", type);
        json.put("quantity", quantity);
        json.put("totalValue", totalValue);
        json.put("timeStamp", timeStamp);
        return json;
    }

    public static Trade fromJSON(int tradeid, JSONObject json) {
        int id = tradeid; // ID is passed separately as it's not part of the JSON object
        int userNumber = json.getInt("userNumber");
        Stock stock = Stock.fromJSON(json.getJSONObject("stock"));
        Boolean type = json.getBoolean("type");
        long quantity = json.getLong("quantity");
        Long totalValue = json.getLong("totalValue");
        String timeStamp = json.getString("timeStamp");
        Trade trade = new Trade(userNumber, stock, type, totalValue, timeStamp);
        trade.setID(id);
        return trade;
    }

}
