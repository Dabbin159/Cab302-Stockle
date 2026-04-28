package com.stockle.api.data;

import java.time.LocalDateTime;

public class BarData {
    public LocalDateTime timestamp;
    public double open;
    public double high;
    public double low;
    public double close;
    public long volume;

    public BarData(LocalDateTime timestamp, double open, double high, double low, double close, long volume) {
        this.timestamp = timestamp;
        this.open = open;
        this.high = high;
        this.low = low;
        this.close = close;
        this.volume = volume;
    }
}
