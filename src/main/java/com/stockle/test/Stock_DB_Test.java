package com.stockle.test;

import java.time.LocalDate;
import java.util.Random;

import com.stockle.model.Stock;
import com.stockle.model.TradeController;
import com.stockle.model.User;

public class Stock_DB_Test {

    public static void main(String[] args) {
        TradeController controller = TradeController.getInstance();
        User user = new User("TestUsername" + new Random().nextInt(100000), "TestPassword", "test@example.com", "Test User",LocalDate.now());
        Stock stock = new Stock("AAPL", "Technology", 150, (float)2.5, (long)1000000);        
        System.out.println("Buying 10 shares...");
        boolean buy = controller.executeBuy(user, stock, 10);
        System.out.println("Buy success: " + buy);
        
        System.out.println("Selling 5 shares at $160...");
        stock.setCurrentPrice(160);
        boolean sell = controller.executeSell(user, stock, 5);
        System.out.println("Sell success: " + sell);
    }

}
