package com.stockle.database;

import java.time.LocalDate;
import java.util.List;

import com.stockle.model.Trade;

public interface TradeDAO {

    public void addTrade(Trade trade);

    public void deleteTrade(int tradeId);

}
