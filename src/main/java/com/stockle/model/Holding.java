package com.stockle.model;

/**
 * The holding class represents a users holding of a particular stock, Including the User ID, Company ID, Quantity, Average price and a unique ID of the holding. Contains a constructor and getters and setters for each field.
 */
public class Holding {

    private int userNumber;
    private String companyID;
    private int quantity;
    private long averagePrice;
    private int id; // Unique identifier for the holding

    /**
     * Constructor for creating a new holding
     * @param userNumber The user ID of the holding
     * @param companyID The company ID of the holding
     * @param quantity The quantity of the holding
     * @param averagePrice The average price of the holding
     */
    public Holding(int userNumber, String companyID, int quantity, long averagePrice) {
        this.userNumber = userNumber;
        this.companyID = companyID;
        this.quantity = quantity;
        this.averagePrice = averagePrice;
    }

    public int getUserNumber() {
        return userNumber;
    }

    public String getCompanyID() {
        return companyID;
    }

    public int getQuantity() {
        return quantity;
    }

    public long getAveragePrice() {
        return averagePrice;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public void setAveragePrice(long averagePrice) {
        this.averagePrice = averagePrice;
    }
}
