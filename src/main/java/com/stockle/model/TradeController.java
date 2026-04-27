package com.stockle.model;

import com.stockle.database.SQLTradeDAO;
import com.stockle.database.SQLUserDAO;

public class TradeController {

    // Buy
    public boolean executeBuy(User user, Stock stock, int quantity) {
        SQLUserDAO userDAO = SQLUserDAO.getInstance();
        SQLTradeDAO tradeDAO = SQLTradeDAO.getInstance();
        long balance = user.getBalance();
        long totalCost = stock.getCurrentPrice() * quantity;
        if (balance >= totalCost) {
            long balancechange = balance - totalCost;
            user.setBalance(balancechange);
            userDAO.updateUserBalance(user.getId(), balancechange);
            Trade trade =  new Trade(user.getId(), stock, (long) quantity, (long) totalCost, String.valueOf(System.currentTimeMillis()));
            tradeDAO.addTrade(trade);
            return true;
        } else {
            return false;
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
