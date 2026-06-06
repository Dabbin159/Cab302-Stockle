package com.stockle.model;
/**
 * context of a trade, used for training the AI model
 */
public class TradeContext {
    public String action;
    public double priceChangePercent;
    public double profitLoss;
    public int holdingDays;

    /**
     * Constructs a TradeContext with the given parameters.
     * @param action trade action taken
     * @param priceChangePercent percentage change in price during the trade
     * @param profitLoss profit or loss from trade
     * @param holdingDays number of days the stock was held
     */
    public TradeContext(String action, double priceChangePercent, double profitLoss, int holdingDays) {
        this.action = action;
        this.priceChangePercent = priceChangePercent;
        this.profitLoss = profitLoss;
        this.holdingDays = holdingDays;
    }
}
