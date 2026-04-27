package com.stockle.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.stockle.model.Holding;



public class SQLHoldingDAO implements holdingDAO {

    private static SQLHoldingDAO instance;
    private Connection connection;

    private SQLHoldingDAO() {
        connection = SqliteConnection.getInstance();
    }

    public static SQLHoldingDAO getInstance() {
        if (instance == null) {
            instance = new SQLHoldingDAO();
        }
        return instance;
    }    

    private static final String ADD_HOLDING = "INSERT INTO holdings (userID, companyID, quantity, averagePrice) VALUES (?, ?, ?, ?)";

    private static final String DELETE_HOLDING = "DELETE FROM holdings WHERE id = ?";

    private static final String UPDATE_HOLDING = "UPDATE holdings SET quantity = ?, averagePrice = ? WHERE id = ?";

    private static final String GET_HOLDING_BY_ID = "SELECT * FROM holdings WHERE id = ?";

    private static final String GET_HOLDING_BY_USER_AND_COMPANY = "SELECT * FROM holdings WHERE userID = ? AND companyID = ?";
    
    private static final String GET_USER_HOLDINGS = "SELECT * FROM holdings WHERE userID = ?";

    @Override
    public void addHolding(Holding holding) {
        try {
            PreparedStatement statement = connection.prepareStatement(ADD_HOLDING);
            statement.setInt(1, holding.getUserNumber().intValue());
            statement.setString(2, holding.getCompanyID());
            statement.setInt(3, holding.getQuantity());
            statement.setLong(4, holding.getAveragePrice());
            statement.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void deleteHolding(int holdingId) {
        try {
            PreparedStatement statement = connection.prepareStatement(DELETE_HOLDING);
            statement.setInt(1, holdingId);
            statement.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void updateHolding(Holding holding) {
        try {
            PreparedStatement statement = connection.prepareStatement(UPDATE_HOLDING);
            statement.setInt(1, holding.getQuantity());
            statement.setLong(2, holding.getAveragePrice());
            statement.setInt(3, holding.getId());
            statement.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public Holding getHoldingById(int holdingId) {
        try {
            PreparedStatement statement = connection.prepareStatement(GET_HOLDING_BY_ID);
            statement.setInt(1, holdingId);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                int id = resultSet.getInt("id");
                int userID = resultSet.getInt("userID");
                String companyID = resultSet.getString("companyID");
                int quantity = resultSet.getInt("quantity");
                Long averagePrice = resultSet.getLong("averagePrice");
                
                Holding holding = new Holding((long)userID, companyID, quantity, averagePrice);
                holding.setId(id);
                return holding;
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    @Override
    public Holding getHolding(int userID, String companyID) {
        try {
            PreparedStatement statement = connection.prepareStatement(GET_HOLDING_BY_USER_AND_COMPANY);
            statement.setInt(1, userID);
            statement.setString(2, companyID);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                int id = resultSet.getInt("id");
                int user = resultSet.getInt("userID");
                String company = resultSet.getString("companyID");
                int quantity = resultSet.getInt("quantity");
                Long averagePrice = resultSet.getLong("averagePrice");
                
                Holding holding = new Holding((long)user, company, quantity, averagePrice);
                holding.setId(id);
                return holding;
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    @Override
    public List<Holding> getUserHoldings(int userID) {
        try {
            PreparedStatement statement = connection.prepareStatement(GET_USER_HOLDINGS);
            statement.setInt(1, userID);
            ResultSet resultSet = statement.executeQuery();
            List<Holding> holdings = new ArrayList<>();
            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                int user = resultSet.getInt("userID");
                String companyID = resultSet.getString("companyID");
                int quantity = resultSet.getInt("quantity");
                Long averagePrice = resultSet.getLong("averagePrice");
                
                Holding holding = new Holding((long)user, companyID, quantity, averagePrice);
                holding.setId(id);
                holdings.add(holding);
            }
            return holdings;
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return null;
    }
}
