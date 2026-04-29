package com.stockle.model;

import org.json.JSONObject;

public class Trade {

    private int id;
    private int userNumber;
    private Stock stock;
    private boolean type; // true = SELL, false = BUY
    private long quantity;
    private long totalValue;
    private String timeStamp;

    public Trade(int id, int userNumber, Stock stock, boolean type, long quantity, long totalValue, String timeStamp) {
        this.id = id;
        this.userNumber = userNumber;
        this.stock = stock;
        this.type = type;
        this.quantity = quantity;
        this.totalValue = totalValue;
        this.timeStamp = timeStamp;
    }

    // Convenience constructor for creating new trades (id assigned by DB)
    public Trade(int userNumber, Stock stock, long quantity, long totalValue, String timeStamp) {
        this(0, userNumber, stock, false, quantity, totalValue, timeStamp);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserNumber() {
        return userNumber;
    }

    public Stock getStock() {
        return stock;
    }

    public boolean isType() {
        return type;
    }

    public void setType(boolean type) {
        this.type = type;
    }

    public long getQuantity() {
        return quantity;
    }

    public long getTotalValue() {
        return totalValue;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public JSONObject toJSON() {
        JSONObject json = new JSONObject();
        json.put("userNumber", userNumber);
        json.put("stock", stock != null ? stock.toJSON() : JSONObject.NULL);
        json.put("type", type);
        json.put("quantity", quantity);
        json.put("totalValue", totalValue);
        json.put("timeStamp", timeStamp);
        return json;
    }

    public static Trade fromJSON(int tradeId, JSONObject json) {
        int id = tradeId;
        int userNumber = json.getInt("userNumber");
        JSONObject stockJson = json.getJSONObject("stock");
        Stock stock = Stock.fromJSON(stockJson);
        boolean type = json.getBoolean("type");
        long quantity = json.getLong("quantity");
        long totalValue = json.getLong("totalValue");
        String timeStamp = json.getString("timeStamp");
        return new Trade(id, userNumber, stock, type, quantity, totalValue, timeStamp);
    }
    public void setType(Boolean type) {
        this.type = type;
    }
}
