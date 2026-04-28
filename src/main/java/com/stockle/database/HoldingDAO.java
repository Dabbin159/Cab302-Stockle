package com.stockle.database;

import java.util.List;

import com.stockle.model.Holding;

/**
 * HoldingDAO is an interface that defines the methods for interacting with the holding data in the database.
 * It provides methods for adding, deleting, updating, and retrieving holdings, as well as handling user holdings.
 */
public interface HoldingDAO {

    public void addHolding(Holding holding);

    public void deleteHolding(int holdingId);

    public void updateHolding(Holding holding);

    public Holding getHoldingById(int holdingId);

    public Holding getHolding(int userID, String companyID);

    public List<Holding> getUserHoldings(int userID);

}
