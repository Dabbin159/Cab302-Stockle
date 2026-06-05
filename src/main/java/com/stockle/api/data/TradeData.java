package com.stockle.api.data;

import java.time.LocalDateTime;

/**
 * Data for a single trade from Alpaca trade V2 API
 */
public class TradeData {
    public LocalDateTime timestamp;
    public double price;
    public long size;
    public String exchange;

    /**
     * Constructor for TradeData
     * 
     * @param timestamp
     * @param price
     * @param size
     * @param exchange
     */
    public TradeData(LocalDateTime timestamp, double price, long size, String exchange) {
        this.timestamp = timestamp;
        this.price = price;
        this.size = size;
        this.exchange = exchange;
    }
}
