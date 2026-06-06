package com.stockle.ui;
import java.util.List;

/**
 * MockData to use as sample data throughout development.
 */
public final class MockData {

    /**
     * Data for the dashboard summary section, showing total portfolio value, gain/loss, and buying power. Used in DashboardController.
     */
    public record DashboardSummary(
        String totalValue,
        String totalGain,
        String buyingPower
    ) {}

    /**
     * Data points for the dashboard performance chart. Each point has a label and a value. Used in DashboardController.
     */
    public record DashboardChartPoint(
        String label,
        double value
    ) {}

    /**
     * Data for a holding in the dashboard. Used in DashboardController.
     */
    public record DashboardHolding(
        String symbol,
        String name,
        String shares,
        String price,
        String change,
        boolean positive
    ) {}

    /**
     * Data for a recent trade in the dashboard. Used in DashboardController.
     */
    public record DashboardTrade(
        String type,
        String symbol,
        String detail,
        String time
    ) {}

    /**
     * Data for a stock in the trading view. Used in TradingController.
     */
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

    // Each row: { open, high, low, close } — 20 candles per stock (same order as CHART_DATA)
    public static final double[][][] CANDLE_DATA = {
        // AAPL
        {{180.00,182.50,179.20,181.80},{181.80,183.40,181.00,182.60},{182.60,184.10,181.50,183.00},
         {183.00,183.80,180.90,181.20},{181.20,182.50,180.50,182.10},{182.10,184.00,181.80,183.70},
         {183.70,185.20,183.40,184.90},{184.90,186.00,184.20,185.50},{185.50,185.80,183.60,184.00},
         {184.00,184.50,182.10,182.80},{182.80,183.60,181.00,183.20},{183.20,185.10,182.90,184.80},
         {184.80,186.30,184.50,185.90},{185.90,187.00,185.20,186.50},{186.50,186.80,184.30,184.80},
         {184.80,185.50,183.00,183.50},{183.50,184.20,182.40,184.00},{184.00,185.80,183.80,185.25},
         {185.25,186.10,184.70,185.80},{185.80,187.20,185.50,186.90}},
        // MSFT
        {{420.00,423.50,419.00,422.10},{422.10,424.00,421.00,421.50},{421.50,422.80,419.50,420.30},
         {420.30,421.50,418.00,419.00},{419.00,420.50,417.50,420.00},{420.00,422.00,419.50,421.80},
         {421.80,423.50,421.00,422.90},{422.90,424.50,422.00,423.80},{423.80,424.20,421.50,422.00},
         {422.00,423.00,420.00,420.80},{420.80,422.50,420.00,421.90},{421.90,423.80,421.50,423.20},
         {423.20,425.00,422.80,424.50},{424.50,426.00,424.00,425.20},{425.20,425.80,423.00,423.60},
         {423.60,424.50,422.00,422.80},{422.80,423.50,421.50,423.00},{423.00,424.80,422.80,424.20},
         {424.20,425.50,423.90,425.00},{425.00,426.50,424.80,426.00}},
        // GOOGL
        {{140.00,142.00,139.50,141.50},{141.50,142.50,140.80,141.00},{141.00,141.80,139.90,140.50},
         {140.50,141.20,139.50,140.00},{140.00,141.00,139.00,140.80},{140.80,142.20,140.50,141.90},
         {141.90,143.00,141.50,142.50},{142.50,143.50,142.00,143.00},{143.00,143.40,141.80,142.10},
         {142.10,142.80,140.80,141.50},{141.50,142.50,141.00,142.20},{142.20,143.80,142.00,143.50},
         {143.50,144.50,143.20,144.00},{144.00,145.00,143.80,144.50},{144.50,144.80,143.00,143.40},
         {143.40,144.20,142.50,142.90},{142.90,143.60,142.00,143.40},{143.40,144.50,143.20,144.00},
         {144.00,144.80,143.60,144.30},{144.30,145.20,144.00,144.80}},
        // NVDA — higher volatility
        {{860.00,875.00,855.00,870.00},{870.00,880.00,865.00,865.00},{865.00,868.00,850.00,855.00},
         {855.00,862.00,845.00,858.00},{858.00,870.00,855.00,867.00},{867.00,878.00,864.00,875.00},
         {875.00,888.00,872.00,882.00},{882.00,890.00,878.00,885.00},{885.00,887.00,870.00,872.00},
         {872.00,878.00,860.00,864.00},{864.00,872.00,858.00,870.00},{870.00,882.00,868.00,879.00},
         {879.00,892.00,876.00,888.00},{888.00,895.00,884.00,891.00},{891.00,893.00,876.00,879.00},
         {879.00,886.00,872.00,874.00},{874.00,880.00,869.00,877.00},{877.00,888.00,875.00,885.00},
         {885.00,893.00,882.00,890.00},{890.00,900.00,887.00,897.00}},
        // TSLA
        {{242.00,247.00,240.50,245.50},{245.50,248.00,244.00,244.80},{244.80,246.00,242.00,243.20},
         {243.20,244.50,240.80,241.50},{241.50,243.00,239.00,242.50},{242.50,245.50,242.00,244.80},
         {244.80,247.00,244.00,246.20},{246.20,249.00,245.50,248.00},{248.00,249.50,246.00,246.80},
         {246.80,247.50,244.50,245.20},{245.20,246.80,243.50,246.00},{246.00,248.50,245.80,247.90},
         {247.90,250.00,247.50,249.20},{249.20,251.00,248.80,250.00},{250.00,250.50,247.50,248.00},
         {248.00,249.00,246.50,247.50},{247.50,248.50,246.80,248.00},{248.00,249.80,247.80,249.20},
         {249.20,250.50,248.80,250.00},{250.00,251.50,249.50,251.00}},
        // AMZN — downtrend
        {{180.00,181.50,178.50,179.20},{179.20,180.00,177.50,178.00},{178.00,179.00,176.50,177.20},
         {177.20,178.00,175.80,176.50},{176.50,177.50,175.00,175.80},{175.80,177.00,175.20,176.50},
         {176.50,178.00,175.80,177.20},{177.20,178.50,176.80,177.80},{177.80,178.20,176.00,176.50},
         {176.50,177.20,175.00,175.80},{175.80,176.50,174.50,175.20},{175.20,176.80,175.00,176.20},
         {176.20,177.50,175.80,177.00},{177.00,178.00,176.50,177.50},{177.50,177.80,175.50,176.00},
         {176.00,176.80,174.50,175.00},{175.00,175.80,173.80,174.50},{174.50,176.00,174.20,175.50},
         {175.50,176.50,175.00,175.80},{175.80,177.00,175.50,176.50}},
        // META
        {{502.00,508.00,500.50,506.00},{506.00,509.00,504.50,505.50},{505.50,507.00,503.00,504.20},
         {504.20,505.50,502.00,503.00},{503.00,505.00,501.00,504.50},{504.50,507.50,504.00,506.80},
         {506.80,509.50,506.00,508.50},{508.50,511.00,508.00,510.20},{510.20,511.50,508.50,509.00},
         {509.00,510.20,507.50,508.20},{508.20,509.50,507.00,509.00},{509.00,511.20,508.80,510.50},
         {510.50,513.00,510.00,512.20},{512.20,514.00,512.00,513.50},{513.50,513.80,511.50,512.00},
         {512.00,512.80,510.50,511.50},{511.50,512.50,510.80,512.00},{512.00,513.80,511.80,513.20},
         {513.20,514.50,512.80,514.00},{514.00,515.50,513.50,515.00}},
        // JPM
        {{196.50,198.50,196.00,197.80},{197.80,198.80,197.00,197.50},{197.50,198.20,196.50,197.00},
         {197.00,197.80,196.00,196.50},{196.50,197.50,195.80,197.20},{197.20,198.50,197.00,198.00},
         {198.00,199.20,197.80,198.80},{198.80,199.80,198.50,199.20},{199.20,199.50,198.00,198.50},
         {198.50,199.00,197.50,198.00},{198.00,198.80,197.20,198.50},{198.50,199.50,198.20,199.20},
         {199.20,200.20,199.00,199.80},{199.80,200.80,199.50,200.20},{200.20,200.50,198.80,199.20},
         {199.20,199.80,198.20,198.80},{198.80,199.50,198.00,199.20},{199.20,200.00,199.00,199.80},
         {199.80,200.50,199.50,200.20},{200.20,201.00,200.00,200.80}},
    };

    public static final String[] CANDLE_TIMES = {
        "Jan 2","Jan 3","Jan 6","Jan 7","Jan 8","Jan 9","Jan 10",
        "Jan 13","Jan 14","Jan 15","Jan 16","Jan 17","Jan 20","Jan 21",
        "Jan 22","Jan 23","Jan 24","Jan 27","Jan 28","Jan 29"
    };

    /**
     * Data for a leaderboard entry. Used in LeaderboardController. (INDEV)
     */
    public record LeaderboardEntry(
        int rank,
        String displayName,
        String username,
        double portfolioValue,
        double changePct
    ) {}

    public static List<LeaderboardEntry> leaderboard() {
        return List.of(
            new LeaderboardEntry(1,  "Sarah Chen",      "@sarahc",    287450.00,  12.4),
            new LeaderboardEntry(2,  "Marcus Johnson",  "@marcusj",   251200.00,   8.7),
            new LeaderboardEntry(3,  "Alex Rivera",     "@arivera",   198750.00,   5.2),
            new LeaderboardEntry(4,  "Jordan Park",     "@jordanp",   175300.00,   3.9),
            new LeaderboardEntry(5,  "Emily Walsh",     "@ewalsh",    162800.00,   2.1),
            new LeaderboardEntry(6,  "Tyler Brooks",    "@tbrooks",   145600.00,  -1.3),
            new LeaderboardEntry(7,  "Priya Sharma",    "@priyas",    134200.00,  -2.8),
            new LeaderboardEntry(8,  "Chris Morgan",    "@cmorgan",   121500.00,   0.5),
            new LeaderboardEntry(9,  "Kai Nakamura",    "@kain",      108900.00,  -4.1),
            new LeaderboardEntry(10, "Sam Taylor",      "@samtaylor",  95400.00,   1.8)
        );
    }

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
