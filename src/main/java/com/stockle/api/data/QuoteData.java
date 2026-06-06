package com.stockle.api.data;

import java.time.LocalDateTime;

/**
 * Data for a single quote from Alpaca Quote V2 API
 */
public class QuoteData {
    public LocalDateTime timestamp;
    public double bidPrice;
    public long bidSize;
    public double askPrice;
    public long askSize;
    public String exchange;

    /**
     * Constructor for QuoteData
     * 
     * @param timestamp
     * @param bidPrice
     * @param bidSize
     * @param askPrice
     * @param askSize
     * @param exchange
     */
    public QuoteData(LocalDateTime timestamp, double bidPrice, long bidSize, double askPrice, long askSize, String exchange) {
        this.timestamp = timestamp;
        this.bidPrice = bidPrice;
        this.bidSize = bidSize;
        this.askPrice = askPrice;
        this.askSize = askSize;
        this.exchange = exchange;
    }
}
