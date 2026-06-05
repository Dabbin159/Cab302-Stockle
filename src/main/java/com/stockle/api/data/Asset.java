package com.stockle.api.data;

/**
 * Represents any single asset collected from the API
 */
public class Asset {
    public String id;
    public String symbol;
    public String name;
    public String assetClass;
    public String exchange;
    public boolean tradable;
    public boolean shortable;
    public boolean marginable;
}
