package com.stockle.api.data;

import java.time.LocalDateTime;

public class TradeData {
    public LocalDateTime timestamp;
    public double price;
    public long size;
    public String exchange;

    public TradeData(LocalDateTime timestamp, double price, long size, String exchange) {
        this.timestamp = timestamp;
        this.price = price;
        this.size = size;
        this.exchange = exchange;
    }
}
