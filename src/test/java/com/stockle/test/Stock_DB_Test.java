package com.stockle.test;

import java.time.LocalDate;
import java.util.Random;

import com.stockle.database.SQLHoldingDAO;
import com.stockle.database.SQLUserDAO;
import com.stockle.model.Holding;
import com.stockle.model.Stock;
import com.stockle.model.TradeController;
import com.stockle.model.User;

public class Stock_DB_Test {

    public static void main(String[] args) {
        TradeController controller =  TradeController.getInstance();
        SQLUserDAO userDAO = SQLUserDAO.getInstance();
        User user = new User("TestUsername" + new Random().nextInt(100000), "TestPassword", "test@example.com", "Test User", LocalDate.now());
        userDAO.addUser(user);  // Save to database first
        SQLHoldingDAO holdingDAO = SQLHoldingDAO.getInstance();
        
        Stock stock = new Stock("AAPL", "Technology", 150, (float)2.5, (long)1000000);
        
        long initialBalance = user.getBalance();
        System.out.println("=== INITIAL STATE ===");
        System.out.println("Starting balance: $" + initialBalance);
        assert initialBalance > 0 : "Initial balance should be positive";
        System.out.println("Initial balance check passed");
        System.out.println();
        
        System.out.println("=== BUYING 10 SHARES AT $150 ===");
        boolean buy = controller.executeBuy(user, stock, 10);
        assert buy : "Buy should succeed";
        System.out.println("Buy executed successfully");
        
        User updatedUser = userDAO.getUserById(user.getId());
        Holding holding = holdingDAO.getHolding(user.getId(), "AAPL");
        
        long expectedBalanceAfterBuy = initialBalance - (150 * 10);
        assert updatedUser.getBalance() == expectedBalanceAfterBuy : "Balance should be reduced by $1500";
        System.out.println("Balance after buy: $" + updatedUser.getBalance() + " (expected: $" + expectedBalanceAfterBuy + ")");
        
        assert holding != null : "Holding should be created";
        assert holding.getQuantity() == 10 : "Holding quantity should be 10";
        System.out.println("Holding quantity: " + holding.getQuantity() + " shares");
        
        assert holding.getAveragePrice() == 150 : "Average price should be $150";
        System.out.println("Average price: $" + holding.getAveragePrice());
        System.out.println();
        
        System.out.println("=== SELLING 5 SHARES AT $160 ===");
        stock.setCurrentPrice(160);
        boolean sell = controller.executeSell(user, stock, 5);
        assert sell : "Sell should succeed";
        System.out.println("Sell executed successfully");
        
        updatedUser = userDAO.getUserById(user.getId());
        holding = holdingDAO.getHolding(user.getId(), "AAPL");
        
        long proceeds = 160 * 5;
        long profit = (160 - 150) * 5;
        long expectedBalanceAfterSell = expectedBalanceAfterBuy + proceeds;
        
        assert updatedUser.getBalance() == expectedBalanceAfterSell : "Balance should be increased by proceeds";
        System.out.println("Balance after sell: $" + updatedUser.getBalance() + " (expected: $" + expectedBalanceAfterSell + ")");
        
        System.out.println("Proceeds from sale: $" + proceeds);
        System.out.println("Profit on sale: $" + profit);
        
        assert holding.getQuantity() == 5 : "Remaining quantity should be 5";
        System.out.println("Remaining quantity: " + holding.getQuantity() + " shares");
        
        assert holding.getAveragePrice() == 150 : "Average price should still be $150";
        System.out.println("Average price: $" + holding.getAveragePrice());
        
        System.out.println();
        System.out.println("=== ALL TESTS PASSED ===");
    }

}
