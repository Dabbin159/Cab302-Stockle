package com.stockle.database;

import com.stockle.model.Trade;

public interface TradeDAO {

    public void addTrade(Trade trade);

    public void addTrade(Trade trade, long profit);

    public void deleteTrade(int tradeId);    

    public void updateTrade(Trade trade);

    public Trade getTradeById(int tradeId);
    
    public Trade[] getAllTrades();

    public Trade[] getTradesByUserId(int userId);
    
}