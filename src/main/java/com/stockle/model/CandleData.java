package com.stockle.model;

/**
 * Represents the candlestick data for a specific time period.
 */
public class CandleData {
    public final String time;
    public final double open;
    public final double high;
    public final double low;
    public final double close;

    /**
     * Constructs a new CandleData object with the specified time, open, high, low, and close values.
     * @param time the time of the candlestick data
     * @param open the opening price
     * @param high the highest price
     * @param low the lowest price
     * @param close the closing price
     */
    public CandleData(String time, double open, double high, double low, double close) {
        this.time = time;
        this.open = open;
        this.high = high;
        this.low = low;
        this.close = close;
    }
}
