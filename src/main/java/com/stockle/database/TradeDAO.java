package com.stockle.database;

import com.stockle.model.Trade;

/**
 * TradeDAO is an interface that defines the methods for interacting with the trade data in the database.
 * It provides methods for adding, deleting, updating, and retrieving trades, as well as handling profit calculations.
 */
public interface TradeDAO {

    /**
     * Adds a new trade to the database.
     * @param trade
     */
    public void addTrade(Trade trade);

    /**
     * Adds a new trade to the database 
     * @param trade
     * @param profit
     */
    public void addTrade(Trade trade, long profit);

    /**
     * Deletes a trade from the database
     * @param tradeId
     */
    public void deleteTrade(int tradeId);    

    /**
     * Updates a trade in the database
     * @param trade
     */
    public void updateTrade(Trade trade);

    /**
     * Retrieves a trade from the database by its ID
     * @param tradeId
     * @return The trade as a trade class object
     */
    public Trade getTradeById(int tradeId);

    /**
     * Retrieves all trades within the database
     * @return An array of all trades in the database
     */
    public Trade[] getAllTrades();

    /**
     * Retrieves all trades for a specific user from the database
     * @param userId
     * @return An array of trades for the specified user
     */
    public Trade[] getTradesByUserId(int userId);
    
}