package com.stockle.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;

import com.stockle.model.Trade;

public class SQLTradeDAO implements tradeDAO {

    private static SQLTradeDAO instance;
    private Connection connection;

    private SQLTradeDAO() {
        connection = SqliteConnection.getInstance(); // Retrive the current database connection
    }

    public static SQLTradeDAO getInstance() {
        if (instance == null) {
            instance = new SQLTradeDAO();
        }
        return instance;
    }

    private static final String ADD_TRADE = "INSERT INTO trades (userID, companyID, tradeData, createdAt, quantity, sold, profit) VALUES (?, ?, ?, ?, ?, ?)";

    private static final String DELETE_TRADE = "DELETE FROM trades WHERE id = ?";

    private static final String UPDATE_TRADE = "UPDATE trades SET sold = ?, profit = ? WHERE id = ?";

    private static final String GET_TRADE_BY_ID = "SELECT * FROM trades WHERE id = ?";

    private static final String GET_ALL_TRADES = "SELECT * FROM trades";

    private static final String GET_TRADES_BY_USER_ID = "SELECT * FROM trades WHERE userID = ?";

    public void addTrade(Trade trade) {
        try (PreparedStatement statement = connection.prepareStatement(ADD_TRADE)) {
            statement.setInt(1, trade.getUserNumber());
            statement.setString(2, trade.getStock().getCompanyName());
            statement.setString(3, trade.toJSON().toString());
            statement.setString(4, trade.getTimeStamp());
            statement.setLong(5, trade.getQuantity());
            statement.setInt(6, 0); // Initial sold status is set to 0 (not sold)
            statement.setLong(7, 0); // Initial profit is set to 0
            statement.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public void deleteTrade(int tradeId) {
        try (PreparedStatement statement = connection.prepareStatement(DELETE_TRADE)) {
            statement.setInt(1, tradeId);
            statement.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }
    
    public void updateTrade(Trade trade) {
        try (PreparedStatement statement = connection.prepareStatement(UPDATE_TRADE)) {
            statement.setInt(1, trade.getId());
            statement.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public Trade getTradeById(int tradeId) {
        try (PreparedStatement statement = connection.prepareStatement(GET_TRADE_BY_ID)) {
            statement.setInt(1, tradeId);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                String tradeData = resultSet.getString("tradeData");
                return Trade.fromJSON(tradeId, new JSONObject(tradeData));
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return null; // Return null if trade not found
    }

    public Trade[] getAllTrades() {
        try (PreparedStatement statement = connection.prepareStatement(GET_ALL_TRADES)) {
            ResultSet resultSet = statement.executeQuery();
            List<Trade> trades = new ArrayList<>();
            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String tradeData = resultSet.getString("tradeData");
                trades.add(Trade.fromJSON(id, new JSONObject(tradeData)));
            }
            return trades.toArray(new Trade[0]);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return new Trade[0];
    }

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
            return trades.toArray(new Trade[0]);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return new Trade[0];
    }
}
