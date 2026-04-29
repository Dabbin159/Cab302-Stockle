package com.stockle.api.data;

import java.time.LocalDateTime;

public class QuoteData {
    public LocalDateTime timestamp;
    public double bidPrice;
    public long bidSize;
    public double askPrice;
    public long askSize;
    public String exchange;

    public QuoteData(LocalDateTime timestamp, double bidPrice, long bidSize, double askPrice, long askSize, String exchange) {
        this.timestamp = timestamp;
        this.bidPrice = bidPrice;
        this.bidSize = bidSize;
        this.askPrice = askPrice;
        this.askSize = askSize;
        this.exchange = exchange;
    }
}
