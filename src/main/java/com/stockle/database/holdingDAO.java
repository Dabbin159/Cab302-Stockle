package com.stockle.database;

import java.util.List;

import com.stockle.model.Holding;

public interface holdingDAO {

    public void addHolding(Holding holding);

    public void deleteHolding(int holdingId);

    public void updateHolding(Holding holding);

    public Holding getHoldingById(int holdingId);

    public Holding getHolding(int userID, String companyID);

    public List<Holding> getUserHoldings(int userID);

}
