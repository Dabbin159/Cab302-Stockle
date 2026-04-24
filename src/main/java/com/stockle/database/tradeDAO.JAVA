package com.stockle.database;

import java.time.LocalDate;
import java.util.List;

import com.stockle.model.User;

public interface tradeDAO {

    public void addTrade(int userID, String companyID, String tradeData, LocalDate createdAt, boolean sold, long profit);

    public void sellTrade(int tradeID, long profit);

    public List<Trade> getTradesByUserId(int userID);

    public List<Trade> getOpenTradesByUserId(int userID);

    public void deleteTrade(int tradeID);

    public void deleteTradesByUserId(int userID);

    public Trade getTradeByUserandCompany(int userID, String companyID);

    public Trade getTradeByUserId(int userID);

    public Trade getTradeByID(int tradeID);
}