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
