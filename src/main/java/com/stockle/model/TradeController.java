package com.stockle.model;

import com.stockle.database.SQLHoldingDAO;
import com.stockle.database.SQLTradeDAO;
import com.stockle.database.SQLUserDAO;

public class TradeController {

    // Buy
    public boolean executeBuy(User user, Stock stock, int quantity) {
        long totalCost = stock.getCurrentPrice() * quantity;
        SQLHoldingDAO holdingDAO = SQLHoldingDAO.getInstance();
        
        if (user.getBalance() < totalCost) {
            return false;
        }
        // Check if holding exists
        Holding existing = holdingDAO.getHolding(user.getId(), stock.getCompanyName());
        
        if (existing != null) {
            // Update: recalculate average price
            long oldCost = existing.getAveragePrice() * existing.getQuantity();
            long newTotalCost = oldCost + totalCost;
            int newQuantity = existing.getQuantity() + quantity;
            long newAvgPrice = newTotalCost / newQuantity;
            
            existing.setQuantity(newQuantity);
            existing.setAveragePrice(newAvgPrice);
            holdingDAO.updateHolding(existing);
        } else {
            // Create new holding
            Holding holding = new Holding(user.getId(), stock.getCompanyName(), quantity, stock.getCurrentPrice());
            holdingDAO.addHolding(holding);
        }
        // Update balance and record trade
        user.setBalance(user.getBalance() - totalCost);
        SQLUserDAO.getInstance().updateUserBalance(user.getId(), user.getBalance());
        Trade trade = new Trade(user.getId(), stock, (long)quantity, totalCost, String.valueOf(System.currentTimeMillis()));
        trade.setType(false); // false = BUY
        SQLTradeDAO.getInstance().addTrade(trade);
        return true;
    }
    // Sell
    public boolean executeSell(User user, Stock stock, int quantity) {
        SQLHoldingDAO holdingDAO = SQLHoldingDAO.getInstance();
        SQLTradeDAO tradeDAO = SQLTradeDAO.getInstance();
        
        // Check if user owns the stock
        Holding holding = holdingDAO.getHolding(user.getId(), stock.getCompanyName());
        
        if (holding == null || holding.getQuantity() < quantity) {
            return false; // Don't own enough shares
        }
        
        // Calculate proceeds and profit
        long proceeds = stock.getCurrentPrice() * quantity;
        long profit = (stock.getCurrentPrice() - holding.getAveragePrice()) * quantity;
        
        // Update holdings
        int newQuantity = holding.getQuantity() - quantity;
        if (newQuantity == 0) {
            holdingDAO.deleteHolding(holding.getId()); // Remove if empty
        } else {
            holding.setQuantity(newQuantity);
            holdingDAO.updateHolding(holding);
        }
        
        // Update balance and record trade
        user.setBalance(user.getBalance() + proceeds);
        SQLUserDAO.getInstance().updateUserBalance(user.getId(), user.getBalance());
        
        Trade trade = new Trade(user.getId(), stock, (long)quantity, proceeds, String.valueOf(System.currentTimeMillis()));
        trade.setType(true); // true = SELL
        tradeDAO.addTrade(trade, profit);
        return true;
    }    
        // Trade History
    public void getTradeHistory(User user) {
        // Implement trade history retrieval logic here
    }
        
 }    