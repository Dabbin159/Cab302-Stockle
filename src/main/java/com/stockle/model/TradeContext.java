package com.stockle.model;

/**
 * The TradeContext class represents the context of a trade, including the action taken, the change in price, the profit or loss from the trade, and the number of days the position was held.
 */
public class TradeContext {
    public String action;
    public double priceChangePercent;
    public double profitLoss;
    public int holdingDays;

    /**
     * Constructs a new TradeContext object with the specified values.
     * @param action the action taken (buy/sell)
     * @param priceChangePercent the percentage change in price
     * @param profitLoss the profit or loss from the trade
     * @param holdingDays the number of days the position was held
     */
    public TradeContext(String action, double priceChangePercent, double profitLoss, int holdingDays) {
        this.action = action;
        this.priceChangePercent = priceChangePercent;
        this.profitLoss = profitLoss;
        this.holdingDays = holdingDays;
    }
}
