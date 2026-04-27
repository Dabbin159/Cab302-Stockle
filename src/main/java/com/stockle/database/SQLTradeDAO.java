package com.stockle.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

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
        } catch (SQLException e) {
            System.err.println(e.getMessage() + " - " + e.toString());
        }
    }

    public void deleteTrade(int tradeId) {
        try (PreparedStatement statement = connection.prepareStatement(DELETE_TRADE)) {
            statement.setInt(1, tradeId);
            statement.executeUpdate();
        } catch (SQLException e) {
            System.err.println(e.getMessage() + " - " + e.toString());
        }
    }

}
