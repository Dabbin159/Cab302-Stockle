package com.stockle.api.data;

import java.time.LocalDateTime;

/**
 *  Data representing single bar of stock data
 */
public class BarData {
    public LocalDateTime timestamp;
    public double open;
    public double high;
    public double low;
    public double close;
    public long volume;

    /**
     * Constructor for Bardata
     * 
     * @param timestamp timestamp of bar
     * @param open open price of bar
     * @param high high price of bar
     * @param low low price of bar
     * @param close close price of bar
     * @param volume trading volume of bar
     */
    public BarData(LocalDateTime timestamp, double open, double high, double low, double close, long volume) {
        this.timestamp = timestamp;
        this.open = open;
        this.high = high;
        this.low = low;
        this.close = close;
        this.volume = volume;
    }
}
