package com.stockle.database;

import java.util.List;

import com.stockle.model.Holding;

/**
 * HoldingDAO is an interface that defines the methods for interacting with the holding data in the database.
 * It provides methods for adding, deleting, updating, and retrieving holdings, as well as handling user holdings.
 */
public interface HoldingDAO {

    /**
     * Adds a new holding to the database
     * @param holding the holding to be added to the database
     */
    public void addHolding(Holding holding);

    /**
     * Deletes a holding from the database by its ID
     * @param holdingId the ID of the holding to be deleted
     */
    public void deleteHolding(int holdingId);

    /**
     * Updates a holding in the database
     * @param holding the holding containing the information to update
     */
    public void updateHolding(Holding holding);

    /**
     * Retrieves a holding from the database by ID
     * @param holdingId the ID of the holding to be retrieved
     * @return the holding as a holding class object or null if none found
     */
    public Holding getHoldingById(int holdingId);

    /**
     * Retrieves a holding from the database by user ID and company ID
     * @param userID the ID of the user whose holding is to be retrieved
     * @param companyID the ID of the company whose holding is to be retrieved
     * @return the holding as a holding class object or null if none found 
     */
    public Holding getHolding(int userID, String companyID);

    /**
     * Retrieves all holdings for a specific user
     * @param userID the ID of the user whose holding is to be retrieved 
     * @return a list of holdings for the user
     */
    public List<Holding> getUserHoldings(int userID);

}
