package com.stockle.ui;
import java.util.List;

public final class MockData {

    public record DashboardSummary(
        String totalValue,
        String totalGain,
        String buyingPower
    ) {}

    public record DashboardChartPoint(
        String label,
        double value
    ) {}

    public record DashboardHolding(
        String symbol,
        String name,
        String shares,
        String price,
        String change,
        boolean positive
    ) {}

    public record DashboardTrade(
        String type,
        String symbol,
        String detail,
        String time
    ) {}

    public record Stock(
        String symbol,
        String name,
        double price,
        double change,
        double changePct,
        String volume,
        String marketCap,
        String sector,
        int chartIndex
    ) {}

    public static final double[][] CHART_DATA = {
        // AAPL, MSFT, GOOGL, NVDA, TSLA, AMZN, META, JPM
        {180.25, 181.50, 180.75, 182.00, 183.25, 182.50, 184.00, 185.25},
        {421.00, 419.50, 420.80, 421.25, 418.90, 420.15, 421.00, 420.50},
        {140.80, 141.20, 141.90, 142.10, 141.75, 142.00, 142.50, 142.38},
        {862.00, 865.50, 870.20, 872.10, 869.80, 873.00, 874.50, 875.30},
        {243.00, 244.50, 246.00, 247.25, 246.80, 247.90, 248.00, 248.50},
        {179.50, 178.80, 179.10, 178.50, 178.00, 177.90, 178.30, 178.25},
        {503.00, 505.50, 507.80, 509.20, 511.00, 510.50, 512.00, 512.80},
        {197.00, 197.50, 198.00, 198.20, 198.00, 198.30, 198.40, 198.45},
    };

    public static final String[] TIMES = {"9:30", "10:00", "10:30", "11:00", "11:30", "12:00", "12:30", "1:00"};

    private MockData() {
    }

    public static List<Stock> allStocks() {
        return List.of(
            new Stock("AAPL", "Apple Inc.", 182.52, 2.34, 1.30, "52.3M", "3.9T", "Technology", 0),
            new Stock("MSFT", "Microsoft Corp.", 420.15, -3.21, -0.76, "28.1M", "3.1T", "Technology", 1),
            new Stock("GOOGL", "Alphabet Inc.", 142.38, 1.88, 1.34, "31.5M", "4.0T", "Technology", 2),
            new Stock("NVDA", "NVIDIA Corp.", 875.30, 12.45, 1.44, "45.2M", "4.9T", "Technology", 3),
            new Stock("TSLA", "Tesla Inc.", 248.50, 5.22, 2.15, "98.7M", "1.2T", "Automotive", 4),
            new Stock("AMZN", "Amazon.com Inc.", 178.25, -2.15, -1.19, "42.8M", "2.7T", "Consumer", 5),
            new Stock("META", "Meta Platforms", 512.80, 8.90, 1.77, "18.3M", "1.7T", "Technology", 6),
            new Stock("JPM", "JPMorgan Chase", 198.45, 1.23, 0.62, "12.5M", "839B", "Financial", 7)
        );
    }

    public static List<Stock> recentlyViewed(List<Stock> allStocks) {
        return List.of(allStocks.get(3), allStocks.get(4), allStocks.get(6));
    }

    public static List<String> defaultFavorites() {
        return List.of("AAPL", "GOOGL", "TSLA");
    }

    public static DashboardSummary dashboardSummary() {
        return new DashboardSummary("$115,000", "+$15,000 (15.0%)", "$35,000");
    }

    public static List<DashboardChartPoint> dashboardChart() {
        return List.of(
            new DashboardChartPoint("Jan", 100000),
            new DashboardChartPoint("Feb", 105000),
            new DashboardChartPoint("Mar", 103000),
            new DashboardChartPoint("Apr", 112000),
            new DashboardChartPoint("May", 118000),
            new DashboardChartPoint("Jun", 115000)
        );
    }

    public static List<DashboardHolding> dashboardHoldings() {
        return List.of(
            new DashboardHolding("AAPL", "Apple Inc.", "50 shares", "$182.52", "+1.3%", true),
            new DashboardHolding("MSFT", "Microsoft Corp.", "30 shares", "$420.15", "-0.76%", false),
            new DashboardHolding("GOOGL", "Alphabet Inc.", "25 shares", "$142.38", "+1.34%", true),
            new DashboardHolding("TSLA", "Tesla Inc.", "15 shares", "$248.50", "+2.15%", true)
        );
    }

    public static List<DashboardTrade> dashboardTrades() {
        return List.of(
            new DashboardTrade("BUY", "AAPL", "10 shares @ $180.25", "2h ago"),
            new DashboardTrade("SELL", "NVDA", "5 shares @ $875.30", "5h ago"),
            new DashboardTrade("BUY", "TSLA", "8 shares @ $245.00", "1d ago")
        );
    }
}
