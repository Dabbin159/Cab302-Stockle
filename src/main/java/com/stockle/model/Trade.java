package com.stockle.model;

import org.json.JSONObject;

public class Trade {

    // Fields
<<<<<<< HEAD
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
=======
    private int id;
    private int userNumber;
    private Stock stock;
    private Boolean type; // Buy or Sell
    private Long totalValue;
    private long quantity;
    private String timeStamp; // Time of the trade

    public Trade(int userNumber, Stock stock,Long quantity, Long totalValue, String timeStamp) {
        this.userNumber = userNumber;
        this.stock = stock;
        this.quantity = quantity;
>>>>>>> origin/main
        this.totalValue = totalValue;
        this.timeStamp = timeStamp;
    }

<<<<<<< HEAD
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
=======
    public void setID(int id) {
        this.id = id;
>>>>>>> origin/main
    }

    public JSONObject toJSON() {
        JSONObject json = new JSONObject();
        json.put("id", id);
        json.put("userNumber", userNumber);
<<<<<<< HEAD
        json.put("symbol", symbol);
        json.put("type", type);
=======
        json.put("stock", stock.toJSON());
        json.put("quantity", quantity);
>>>>>>> origin/main
        json.put("totalValue", totalValue);
        json.put("timeStamp", timeStamp);
        return json;
    }

    public static Trade fromJSON(int tradeid, JSONObject json) {
<<<<<<< HEAD
        int id = tradeid;
        Double userNumber = json.getDouble("userNumber");
        String symbol = json.getString("symbol");
        Boolean type = json.getBoolean("type");
        Long totalValue = json.getLong("totalValue");
        String timeStamp = json.getString("timeStamp");
        return new Trade(id, userNumber, symbol, type, totalValue, timeStamp);
=======
        int id = tradeid; // ID is passed separately as it's not part of the JSON object
        int userNumber = json.getInt("userNumber");
        Stock stock = Stock.fromJSON(json.getJSONObject("stock"));
        long quantity = json.getLong("quantity");
        Long totalValue = json.getLong("totalValue");
        String timeStamp = json.getString("timeStamp");
        Trade trade = new Trade(userNumber, stock, quantity, totalValue, timeStamp);
        trade.setID(id);
        return trade;
>>>>>>> origin/main
    }

    public int getId() {
        return id;
    }

    public int getUserNumber() {
        return userNumber;
    }

    public Stock getStock() {
        return stock;
    }

    public Boolean isType() {
        return type;
    }

    public long getQuantity() {
        return quantity;
    }

    public Long getTotalValue() {
        return totalValue;
    }

    public String getTimeStamp() {
        return timeStamp;
    }
    public void setType(Boolean type) {
        this.type = type;
    }
}
