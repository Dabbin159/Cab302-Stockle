package com.stockle.api.data;

public class SnapshotData {
    public BarData latestBar;
    public QuoteData latestQuote;

    public SnapshotData(BarData latestBar, QuoteData latestQuote) {
        this.latestBar = latestBar;
        this.latestQuote = latestQuote;
    }
}
