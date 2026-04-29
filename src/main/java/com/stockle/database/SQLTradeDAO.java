package com.stockle.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;

import com.stockle.model.Trade;

public class SQLTradeDAO implements TradeDAO {

    private static SQLTradeDAO instance;
    private final Connection connection;

    private SQLTradeDAO() {
        connection = SqliteConnection.getInstance(); // Retrive the current database connection
    }

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
    public void addTrade(Trade trade) {
        addTrade(trade, 0); // Default profit to 0 for new trades
    }

    @Override
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
            logSqlException(ex);
        }
    }

    @Override
    public void deleteTrade(int tradeId) {
        try (PreparedStatement statement = connection.prepareStatement(DELETE_TRADE)) {
            statement.setInt(1, tradeId);
            statement.executeUpdate();
        } catch (SQLException ex) {
            logSqlException(ex);
        }
    }
    
    @Override
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
            logSqlException(ex);
        }
    }

    @Override
    public Trade getTradeById(int tradeId) {
        try (PreparedStatement statement = connection.prepareStatement(GET_TRADE_BY_ID)) {
            statement.setInt(1, tradeId);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                String tradeData = resultSet.getString("tradeData");
                return Trade.fromJSON(tradeId, new JSONObject(tradeData));
            }
        } catch (SQLException ex) {
            logSqlException(ex);
        }
        return null; // Return null if trade not found
    }

    @Override
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
            logSqlException(ex);
        }
        return new Trade[0];
    }

    @Override
    public Trade[] getTradesByUserId(int userId) {
        try (PreparedStatement statement = connection.prepareStatement(GET_TRADES_BY_USER_ID)) {
            statement.setInt(1, userId);
            ResultSet resultSet = statement.executeQuery();
            List<Trade> trades = new ArrayList<>();
            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String tradeData = resultSet.getString("tradeData");
                trades.add(Trade.fromJSON(id, new JSONObject(tradeData)));
            }
            return trades.toArray(Trade[]::new);
        } catch (SQLException ex) {
            logSqlException(ex);
        }
        return new Trade[0];
    }

    private void logSqlException(SQLException ex) {
        System.err.println(ex.getMessage());
    }
}
