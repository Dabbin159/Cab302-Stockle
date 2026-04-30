package com.stockle.model;

import org.json.JSONObject;

/**
 * Class representing a trade. Contains information for trade information such as user id, trade id, stock, type, quantity, total value and timestamp. Provides constructors for creating a trade and getters and setters for each field. 
 */
public class Trade {

    private int id;
    private int userNumber;
    private Stock stock;
    private boolean type; // true = Sell, false = Buy
    private long quantity;
    private long totalValue;
    private String timeStamp;

    /**
     * Constructor for creating a new trade
     * @param id The ID of the trade
     * @param userNumber The User ID of the trade
     * @param stock The stock being bought
     * @param type The type (Buy/Sell)
     * @param quantity The quantity of the trade
     * @param totalValue The totale value of the trade
     * @param timeStamp The timestamp of the trade
     */
    public Trade(int id, int userNumber, Stock stock, boolean type, long quantity, long totalValue, String timeStamp) {
        this.id = id;
        this.userNumber = userNumber;
        this.stock = stock;
        this.type = type;
        this.quantity = quantity;
        this.totalValue = totalValue;
        this.timeStamp = timeStamp;
    }

    // Convenience constructor for creating new trades (id assigned by DB after creation)
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

    /**
     * Converts the trade class to a json object for the database
     * @return A JSON object representing the trade
     */

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
    /**
     * Creates a trade object from a JSON object retrieved from the database
     * @param tradeId the ID of the trade
     * @param json the JSON object containing the trade data
     * @return A Trade Object created from the JSON object
     */

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
