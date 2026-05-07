package com.stockle.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;

import com.stockle.model.Trade;

/**
 * Class to manage trade-related database operations, such as adding, deleting, updating, and retrieving trades. Implements the TradeDAO interface.
 */
public class SQLTradeDAO implements TradeDAO {

    private static SQLTradeDAO instance;
    private final Connection connection;

    private SQLTradeDAO() {
        connection = SqliteConnection.getInstance(); // Retrive the current database connection
    }

    /**
     * Returns the singleton instance of SQLTradeDAO
     * @return the singleton instance of SQLTradeDAO
     */
    public static SQLTradeDAO getInstance() {
        if (instance == null) {
            instance = new SQLTradeDAO();
        }
        return instance;
    }

    private static final String ADD_TRADE = "INSERT INTO trades (userID, companyID, tradeData, createdAt, quantity, sold, profit) VALUES (?, ?, ?, ?, ?, ?, ?)";

    private static final String DELETE_TRADE = "DELETE FROM trades WHERE id = ?";

    private static final String UPDATE_TRADE = "UPDATE trades SET sold = ?, profit = ? WHERE id = ?";

    private static final String GET_TRADE_BY_ID = "SELECT * FROM trades WHERE id = ?";

    private static final String GET_ALL_TRADES = "SELECT * FROM trades";

    private static final String GET_TRADES_BY_USER_ID = "SELECT * FROM trades WHERE userID = ?";

    @Override
    /**
     * Adds a new trade to the database with the profit set to 0
     * @param trade the trade to be added to the database.
     */
    public void addTrade(Trade trade) {
        addTrade(trade, 0); // Default profit to 0 for new trades
    }

    @Override
    /**
     * Adds a new trade to the database with the specified profit
     * @param trade the trade to be added to the database.
     * @param profit the profit to be added to the trade
     */
    public void addTrade(Trade trade, long profit) {
        try (PreparedStatement statement = connection.prepareStatement(ADD_TRADE)) {
            statement.setInt(1, trade.getUserNumber());
            statement.setString(2, trade.getStock().getCompanyName());
            statement.setString(3, trade.toJSON().toString());
            statement.setString(4, trade.getTimeStamp());
            statement.setLong(5, trade.getQuantity());
            statement.setInt(6, trade.isType() ? 1 : 0);  // sold=1 for SELL, 0 for BUY
            statement.setLong(7, profit);
            statement.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    /**
     * Deletes a trade from the database by its ID
     * @param tradeID the ID of the trade to be deleted
     */
    public void deleteTrade(int tradeId) {
        try (PreparedStatement statement = connection.prepareStatement(DELETE_TRADE)) {
            statement.setInt(1, tradeId);
            statement.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }
    
    @Override
    /**
     * Updates a trade in the database.
     * @param trade The trade containing the information to update
     */
    public void updateTrade(Trade trade) {
        try (PreparedStatement lookup = connection.prepareStatement(GET_TRADE_BY_ID)) {
            lookup.setInt(1, trade.getId());
            long existingProfit = 0L;

            try (ResultSet resultSet = lookup.executeQuery()) {
                if (resultSet.next()) {
                    existingProfit = resultSet.getLong("profit");
                }
            }

            try (PreparedStatement statement = connection.prepareStatement(UPDATE_TRADE)) {
                statement.setInt(1, trade.isType() ? 1 : 0); // sold=1 for SELL, 0 for BUY
                statement.setLong(2, existingProfit); // Keep existing profit
                statement.setInt(3, trade.getId());
                statement.executeUpdate();
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    /**
     * Retrieves a trade from the database by its ID
     * @param tradeID the ID of the trade to be retrieved
     * @return the trade as a trade class object or null if none found
     */
    public Trade getTradeById(int tradeID) {
        try (PreparedStatement statement = connection.prepareStatement(GET_TRADE_BY_ID)) {
            statement.setInt(1, tradeID);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                String tradeData = resultSet.getString("tradeData");
                return Trade.fromJSON(tradeID, new JSONObject(tradeData));
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return null; // Return null if trade not found
    }

    @Override
    /**
     * Retrieves all trades from the database
     * @return An array of all trades in the database
     */
    public Trade[] getAllTrades() {
        try (PreparedStatement statement = connection.prepareStatement(GET_ALL_TRADES)) {
            ResultSet resultSet = statement.executeQuery();
            List<Trade> trades = new ArrayList<>();
            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String tradeData = resultSet.getString("tradeData");
                trades.add(Trade.fromJSON(id, new JSONObject(tradeData)));
            }
            return trades.toArray(Trade[]::new);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return new Trade[0];
    }

    @Override
    /**
     * Retrieves all trades from the database for a specific user
     * @param userID the ID of the user whose trades are retrieved
     * @return An array of trades for the user
     */
    public Trade[] getTradesByUserId(int userID) {
        try (PreparedStatement statement = connection.prepareStatement(GET_TRADES_BY_USER_ID)) {
            statement.setInt(1, userID);
            ResultSet resultSet = statement.executeQuery();
            List<Trade> trades = new ArrayList<>();
            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String tradeData = resultSet.getString("tradeData");
                trades.add(Trade.fromJSON(id, new JSONObject(tradeData)));
            }
            return trades.toArray(Trade[]::new);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return new Trade[0];
    }

}
