package com.stockle.model;

public class Holding {

    private Long userNumber;
    private String companyID;
    private int quantity;
    private Long averagePrice;

    public Holding(Long userNumber, String companyID, int quantity, Long averagePrice) {
        this.userNumber = userNumber;
        this.companyID = companyID;
        this.quantity = quantity;
        this.averagePrice = averagePrice;
    }

    public Long getUserNumber() {
        return userNumber;
    }

    public String getCompanyID() {
        return companyID;
    }

    public int getQuantity() {
        return quantity;
    }

    public Long getAveragePrice() {
        return averagePrice;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public void setAveragePrice(Long averagePrice) {
        this.averagePrice = averagePrice;
    }
}
