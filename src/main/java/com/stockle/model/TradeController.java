package com.stockle.model;

public class TradeController {

    // Buy
    public void executeBuy(User user, Stock stock, int quantity) {
        long balance = user.getBalance();
        double totalCost = stock.getCurrentPrice() * quantity;
        if (balance >= totalCost) {
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
