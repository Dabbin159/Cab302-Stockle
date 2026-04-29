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

    /** Parse a list of CandleData from an Alpaca bars JSON string.
     *  Expects: { "bars": { "<symbol>": [ { "t", "o", "h", "l", "c", ... } ] } }
     */
    public static java.util.List<CandleData> fromAlpacaJson(String json, String symbol) {
        java.util.List<CandleData> result = new java.util.ArrayList<>();
        try {
            org.json.JSONObject root   = new org.json.JSONObject(json);
            org.json.JSONArray  bars   = root.getJSONObject("bars").getJSONArray(symbol);
            for (int i = 0; i < bars.length(); i++) {
                org.json.JSONObject b = bars.getJSONObject(i);
                // "t" is ISO-8601; keep only date+hour portion for the axis label
                String label = b.getString("t").substring(0, 16).replace("T", " ");
                result.add(new CandleData(
                    label,
                    b.getDouble("o"),
                    b.getDouble("h"),
                    b.getDouble("l"),
                    b.getDouble("c")
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }
}
