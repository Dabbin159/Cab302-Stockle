package com.stockle.api.data;

/**
 * Data for snapshot of stock data
 */
public class SnapshotData {
    public BarData latestBar;
    public QuoteData latestQuote;

    /**
     * Constructor for SnapshotData
     * 
     * @param latestBar
     * @param latestQuote
     */
    public SnapshotData(BarData latestBar, QuoteData latestQuote) {
        this.latestBar = latestBar;
        this.latestQuote = latestQuote;
    }
}
