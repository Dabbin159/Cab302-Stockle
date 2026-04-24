package com.stockle.model;

import com.stockle.database.SQLUserDAO;

public class TradeController {

    // Buy
    public void executeBuy(User user, Stock stock, int quantity) {
        SQLUserDAO userDAO = SQLUserDAO.getInstance();
        long balance = user.getBalance();
        long totalCost = stock.getCurrentPrice() * quantity;
        if (balance >= totalCost) {
            long balancechange = balance - totalCost;
            user.setBalance(balancechange);
            userDAO.updateUserBalance(user.getId(), balancechange);
            Trade trade =  new Trade(user.getId(), stock, true, (long) totalCost, String.valueOf(System.currentTimeMillis()));
            // Add Trade to Database
            // Implement buy logic here
        } else {
            // Handle insufficient balance case
        }
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
