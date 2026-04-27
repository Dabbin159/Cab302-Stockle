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
        SQLTradeDAO.getInstance().addTrade(trade);
        return true;
    }
    // Sell
    public void executeSell(User user, Trade trade, int quantity) {
        // Implement sell logic here
    }
    
    // Trade History
    public void getTradeHistory(User user) {
        // Implement trade history retrieval logic here
    }
    
}
