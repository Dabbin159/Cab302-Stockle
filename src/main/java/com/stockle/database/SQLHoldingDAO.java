package com.stockle.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.stockle.model.Holding;


/**
 * Class to manage holding-related database operations, such as adding, deleting, updating, and retrieving holdings. Implements the TradeDAO interface.
 */
public class SQLHoldingDAO implements HoldingDAO {

    private static SQLHoldingDAO instance;
    private Connection connection;

    private SQLHoldingDAO() {
        connection = SqliteConnection.getInstance();
    }
    
    /**
     * Returns the singleton instance of SQLHoldingDAO
     * @return the singleton instance of SQLHoldingDAO
     */
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
    /**
     * Adds a new holding to the database
     * @param holding the holding to be added to the database
     */
    public void addHolding(Holding holding) {
        try {
            PreparedStatement statement = connection.prepareStatement(ADD_HOLDING);
            statement.setInt(1, holding.getUserNumber());
            statement.setString(2, holding.getCompanyID());
            statement.setInt(3, holding.getQuantity());
            statement.setLong(4, holding.getAveragePrice());
            statement.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    /**
     * Deletes a holding from the database by its ID
     * @param holdingId the ID of the holding to be deleted
     */
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
    /**
     * Updates a holding in the database
     * @param holding the holding containing the information to update
     */
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
    /**
     * Retrieves a holding from the database by ID
     * @param holdingId the ID of the holding to be retrieved
     * @return the holding as a holding class object or null if none found
     */
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
                int averagePrice = resultSet.getInt("averagePrice");
                
                Holding holding = new Holding(userID, companyID, quantity, averagePrice);
                holding.setId(id);
                return holding;
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    @Override
    /**
     * Retrieves a holding from the database by user ID and company ID
     * @param userID the ID of the user whose holding is to be retrieved
     * @param companyID the ID of the company whose holding is to be retrieved
     * @return the holding as a holding class object or null if none found 
     */
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
                int averagePrice = resultSet.getInt("averagePrice");
                
                Holding holding = new Holding(user, company, quantity, averagePrice);
                holding.setId(id);
                return holding;
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    @Override
    /**
     * Retrieves all holdings for a specific user
     * @param userID the ID of the user whose holding is to be retrieved 
     * @return a list of holdings for the user
     */
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
                int averagePrice = resultSet.getInt("averagePrice");
                
                Holding holding = new Holding(user, companyID, quantity, averagePrice);
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
