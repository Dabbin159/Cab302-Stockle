package com.stockle.model;

import com.stockle.database.SQLHoldingDAO;
import com.stockle.database.SQLTradeDAO;
import com.stockle.database.SQLUserDAO;
/**
 * Trade Controller is a class that handles trading logic within the application it allows for the handling of buy and sell operations.
*/
public class TradeController {

    private TradeController() {
    }

    private static TradeController instance;

    public static TradeController getInstance() {
        if (instance == null) {
            instance = new TradeController();
        }
        return instance;
    }    

    /**
     * Executes a buy operation for a given user, stock and quantity. Checks fort sufficient balance, and updates releveant databases.
     * @param user The user executing the buy
     * @param stock The stock being bought
     * @param quantity The quantity being bought
     * @return
     */
    public boolean executeBuy(User user, Stock stock, int quantity) {
        if (user == null || stock == null || quantity <= 0) {
            return false; // Invalid input
        }
        long totalCost = (long) stock.getCurrentPrice() * quantity;
        SQLHoldingDAO holdingDAO = SQLHoldingDAO.getInstance();
        SQLUserDAO userDAO = SQLUserDAO.getInstance();
        long currentBalance = userDAO.getUserBalance(user.getId());
        
        if (currentBalance < totalCost) {
            return false;
        }
        // Check if holding exists
        Holding existing = holdingDAO.getHolding(user.getId(), stock.getCompanyName());
        
        if (existing != null) {
            // Update: recalculate average price
            long oldCost = existing.getAveragePrice() * existing.getQuantity();
            long newTotalCost = oldCost + totalCost;
            int newQuantity = existing.getQuantity() + quantity;
            long newAvgPrice = (newTotalCost / newQuantity);
            
            existing.setQuantity(newQuantity);
            existing.setAveragePrice(newAvgPrice);
            holdingDAO.updateHolding(existing);
        } else {
            // Create new holding
            Holding holding = new Holding(user.getId(), stock.getCompanyName(), quantity, stock.getCurrentPrice());
            holdingDAO.addHolding(holding);
        }
        // Update balance and record trade
        user.setBalance(currentBalance - totalCost);
        userDAO.updateUserBalance(user.getId(), user.getBalance());
        Trade trade = new Trade(user.getId(), stock, (long)quantity, totalCost, String.valueOf(System.currentTimeMillis()));
        trade.setType(false); // false = BUY
        SQLTradeDAO.getInstance().addTrade(trade);
        return true;
    }

    /**
     * Executes a sell operation for a given user, stock and quantity. Checks for sufficient holdings, calculates proceeds and profit, and updates releveant databases.
     * @param user The user executing the sell
     * @param stock The stock being sold
     * @param quantity The quantity being sold
     * @return
     */

    public boolean executeSell(User user, Stock stock, int quantity) {
        if (user == null || stock == null || quantity <= 0) {
            return false; // Invalid input
        }

        SQLHoldingDAO holdingDAO = SQLHoldingDAO.getInstance();
        SQLTradeDAO tradeDAO = SQLTradeDAO.getInstance();
        SQLUserDAO userDAO = SQLUserDAO.getInstance();

        // Check if user owns the stock
        Holding holding = holdingDAO.getHolding(user.getId(), stock.getCompanyName());
        
        if (holding == null || holding.getQuantity() < quantity) {
            return false; // Don't own enough shares or no holding at all
        }
        
        // Calculate proceeds and profit
        long proceeds = (long) stock.getCurrentPrice() * quantity;
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
        // user.setBalance(user.getBalance() + proceeds);
        // SQLUserDAO.getInstance().updateUserBalance(user.getId(), user.getBalance());
        long currentBalance = userDAO.getUserBalance(user.getId());
        user.setBalance(currentBalance + proceeds);
        userDAO.updateUserBalance(user.getId(), user.getBalance());

        long currentProfit = userDAO.getUserTotalProfit(user.getId());
        user.setTotalProfit(currentProfit + profit);
        userDAO.updateUserTotalProfit(user.getId(), user.getTotalProfit());
        
        Trade trade = new Trade(user.getId(), stock, (long)quantity, proceeds, String.valueOf(System.currentTimeMillis()));
        trade.setType(true); // true = SELL
        tradeDAO.addTrade(trade, profit);
        return true;
    }

    // Trade History

    // Read-only helpers used by UI controllers to avoid direct DAO usage
    public Holding getHoldingForUser(User user, String companyID) {
        if (user == null || companyID == null) return null;
        return SQLHoldingDAO.getInstance().getHolding(user.getId(), companyID);
    }

    public int getOwnedQuantity(User user, String companyID) {
        Holding h = getHoldingForUser(user, companyID);
        return h != null ? h.getQuantity() : 0;
    }

    public long getUserBalance(User user) {
        if (user == null) return 0L;
        return SQLUserDAO.getInstance().getUserBalance(user.getId());
    }

    public User refreshUserFromDb(User user) {
        if (user == null) return null;
        return SQLUserDAO.getInstance().getUserById(user.getId());
    }
        
 }    