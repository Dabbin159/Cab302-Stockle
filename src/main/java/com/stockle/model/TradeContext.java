package com.stockle.model;

public class TradeContext {
    public String action;
    public double priceChangePercent;
    public double profitLoss;
    public int holdingDays;

    public TradeContext(String action, double priceChangePercent, double profitLoss, int holdingDays) {
        this.action = action;
        this.priceChangePercent = priceChangePercent;
        this.profitLoss = profitLoss;
        this.holdingDays = holdingDays;
    }
}
