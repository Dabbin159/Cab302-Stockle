package com.stockle.database;

import com.stockle.model.Trade;

/**
 * TradeDAO is an interface that defines the methods for interacting with the trade data in the database.
 * It provides methods for adding, deleting, updating, and retrieving trades, as well as handling profit calculations.
 */
public interface TradeDAO {

    public void addTrade(Trade trade);

    public void addTrade(Trade trade, long profit);

    public void deleteTrade(int tradeId);    

    public void updateTrade(Trade trade);

    public Trade getTradeById(int tradeId);
    
    public Trade[] getAllTrades();

    public Trade[] getTradesByUserId(int userId);
    
}