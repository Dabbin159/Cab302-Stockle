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
    
    /**
     * Returns the total number of trades for a user.
     * @param userId
     * @return total trades count
     */
    public int getTotalTradesCountByUser(int userId);

    /**
     * Returns the number of trades for a user in a specific year/month.
     * @param userId
     * @param year four-digit year (e.g., 2026)
     * @param month 1-12
     * @return trades count for the month
     */
    public int getTradesCountByUserInMonth(int userId, int year, int month);

    /**
     * Returns the number of trades for a user in the previous calendar month.
     * @param userId
     * @return trades count for last month
     */
    public int getTradesCountByUserLastMonth(int userId);
    
}