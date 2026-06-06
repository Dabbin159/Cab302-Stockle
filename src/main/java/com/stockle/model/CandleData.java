package com.stockle.model;

/**
 * Represents the data for a single candle in the chart
 */
public class CandleData {
    public final String time;
    public final double open;
    public final double high;
    public final double low;
    public final double close;

    /**
     * Constructs a CandleData with the given parameters.
     * @param time the time of the candle
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
