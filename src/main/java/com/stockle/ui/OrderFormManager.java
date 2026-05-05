package com.stockle.ui;

import com.stockle.model.Stock;
import com.stockle.model.TradeController;
import com.stockle.model.User;

/** Manages the buy/sell order form — estimates, execution, status messages, and owned shares. */
class OrderFormManager {

    private final TradingController ctrl;

    OrderFormManager(TradingController ctrl) {
        this.ctrl = ctrl;
    }

    // Estimates

    void updateBuyEstimate() {
        int qty = ctrl.parseQuantity(ctrl.buySharesField);
        double cost = qty * ctrl.currentPrice;
        ctrl.buyEstimateLabel.setText(String.format("$%.2f", cost));
        ctrl.buyButton.setDisable(cost <= 0);
    }

    void updateSellEstimate() {
        int qty = ctrl.parseQuantity(ctrl.sellSharesField);
        double proceeds = qty * ctrl.currentPrice;
        ctrl.sellEstimateLabel.setText(String.format("$%.2f", proceeds));
        ctrl.sellButton.setDisable(proceeds <= 0);
    }

    void updateOwnedSharesLabel() {
        if (ctrl.ownedSharesLabel == null) return;
        User user = ctrl.currentUser();
        if (user == null) {
            ctrl.ownedSharesLabel.setText("Sign in to see owned shares");
            return;
        }
        int owned = TradeController.getInstance().getOwnedQuantity(user, ctrl.selectedSymbol);
        ctrl.ownedSharesLabel.setText("You own " + owned + " shares");
    }

    // Buy / Sell 
    void handleBuy() {
        User user = ctrl.currentUser();
        if (user == null) { setBuyStatus("Please sign in to trade.", false); return; }

        int qty = ctrl.parseQuantity(ctrl.buySharesField);
        if (qty <= 0) { setBuyStatus("Please enter a valid quantity.", false); return; }

        boolean ok = TradeController.getInstance().executeBuy(user, toTradeStock(), qty);
        if (ok) {
            ctrl.refreshCurrentUser();
            ctrl.buySharesField.clear();
            setBuyStatus("Bought " + qty + " shares of " + ctrl.selectedSymbol, true);
            updateBuyEstimate();
            updateSellEstimate();
            updateOwnedSharesLabel();
        } else {
            setBuyStatus("Insufficient balance to buy " + qty + " shares.", false);
        }
    }

    void handleSell() {
        User user = ctrl.currentUser();
        if (user == null) { setSellStatus("Please sign in to trade.", false); return; }

        int qty = ctrl.parseQuantity(ctrl.sellSharesField);
        if (qty <= 0) { setSellStatus("Please enter a valid quantity.", false); return; }

        boolean ok = TradeController.getInstance().executeSell(user, toTradeStock(), qty);
        if (ok) {
            ctrl.refreshCurrentUser();
            ctrl.sellSharesField.clear();
            setSellStatus("Sold " + qty + " shares of " + ctrl.selectedSymbol, true);
            updateBuyEstimate();
            updateSellEstimate();
            updateOwnedSharesLabel();
        } else {
            setSellStatus("You don't have " + qty + " shares to sell.", false);
        }
    }

    // Status labels
    void clearTradeStatus() {
        if (ctrl.buyStatusLabel  != null) ctrl.buyStatusLabel.setText("");
        if (ctrl.sellStatusLabel != null) ctrl.sellStatusLabel.setText("");
    }

    private void setBuyStatus(String msg, boolean success) {
        if (ctrl.buyStatusLabel == null) return;
        ctrl.buyStatusLabel.setVisible(true);
        ctrl.buyStatusLabel.setManaged(true);
        ctrl.buyStatusLabel.setText(msg);
        ctrl.buyStatusLabel.getStyleClass().setAll(
            "trade-status", success ? "trade-status-success" : "trade-status-error");
    }

    private void setSellStatus(String msg, boolean success) {
        if (ctrl.sellStatusLabel == null) return;
        ctrl.sellStatusLabel.setText(msg);
        ctrl.sellStatusLabel.getStyleClass().setAll(
            "trade-status", success ? "trade-status-success" : "trade-status-error");
    }

    // Helpers
    private Stock toTradeStock() {
        return new Stock(
            ctrl.selectedSymbol,
            "N/A",
            (int) Math.round(ctrl.currentPrice),
            (float) ctrl.currentChangePercent,
            ctrl.currentVolume
        );
    }
}
