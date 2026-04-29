package com.stockle.model;

public class CandleData {
    public final String time;
    public final double open;
    public final double high;
    public final double low;
    public final double close;

    public CandleData(String time, double open, double high, double low, double close) {
        this.time  = time;
        this.open  = open;
        this.high  = high;
        this.low   = low;
        this.close = close;
    }
}
