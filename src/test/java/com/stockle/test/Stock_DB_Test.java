package com.stockle.test;

import java.time.LocalDate;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.stockle.database.SQLHoldingDAO;
import com.stockle.database.SQLUserDAO;
import com.stockle.model.Stock;
import com.stockle.model.TradeController;
import com.stockle.model.User;

public class Stock_DB_Test {

    private TradeController controller;
    private User user;
    private SQLUserDAO userDAO;
    private SQLHoldingDAO holdingDAO;

    @BeforeEach
    public void setUp() {
        controller =  TradeController.getInstance();
        userDAO = SQLUserDAO.getInstance();
        holdingDAO = SQLHoldingDAO.getInstance();
        user = new User("Test Username" + new Random().nextInt(100000), "Test1234", "test"+ new Random().nextInt(100000)+"@test.test", "Test User", LocalDate.now(), 100000, 0);
        userDAO.addUser(user);
    }

    @Test
    public void testBuy() {
        Stock stock = new Stock("AAPL", "Technology", 150, (float)2.5, (long)1000000);
        long initalBalance = user.getBalance();
        boolean buy = controller.executeBuy(user, stock, 10);
        assertEquals(true, buy);
        User updatedUser = userDAO.getUserById(user.getId());
        assertEquals( initalBalance - (150 * 10), updatedUser.getBalance());
    }
    
    @Test
    public void testSell() {
        Stock stockBuy = new Stock("AAPL", "Technology", 150, (float)2.5, (long)1000000);
        long initalBalance = user.getBalance();
        boolean buy = controller.executeBuy(user, stockBuy, 10);
        Stock stockSell = new Stock("AAPL", "Technology", 160, (float)2.5, (long)1000000);
        boolean sell = controller.executeSell(user, stockSell, 10);
        assertEquals(true, sell);
        User updatedUser = userDAO.getUserById(user.getId());
        assertEquals(initalBalance + (10 * 10), updatedUser.getBalance());
    }

    @Test
    public void testCompanyName() {
        Stock stock = new Stock("AAPL", "Technology", 150, (float)2.5, (long)1000000);
        assertEquals(150, stock.getCurrentPrice());
    }

    @Test
    void testSector() {
        Stock stock = new Stock("AAPL", "Technology", 150, (float)2.5, (long)1000000);
        assertEquals("Technology", stock.getSector());
    }

    @Test
    void testCurrentPrice() {
        Stock stock = new Stock("AAPL", "Technology", 150, (float)2.5, (long)1000000);
        assertEquals(150, stock.getCurrentPrice());
    }

    @Test
    void testDailyChange() {
        Stock stock = new Stock("AAPL", "Technology", 150, (float)2.5, (long)1000000);
        assertEquals((float)2.5, stock.getDailyChange());
    }

    @Test
    void testVolume() {
        Stock stock = new Stock("AAPL", "Technology", 150, (float)2.5, (long)1000000);
        assertEquals((long)1000000, stock.getVolume());
    }

}
