package com.stockplatform.model;

/**
 * Represents a user's holding of a specific stock in their portfolio.
 */
public class Holding {
    private final String symbol;
    private int quantity;
    private double averagePurchasePrice;

    /**
     * Constructs a new Holding.
     *
     * @param symbol The stock ticker symbol.
     * @param quantity The number of shares owned.
     * @param averagePurchasePrice The average price paid per share.
     */
    public Holding(String symbol, int quantity, double averagePurchasePrice) {
        this.symbol = symbol;
        this.quantity = quantity;
        this.averagePurchasePrice = averagePurchasePrice;
    }

    public String getSymbol() {
        return symbol;
    }

    public synchronized int getQuantity() {
        return quantity;
    }

    public synchronized double getAveragePurchasePrice() {
        return averagePurchasePrice;
    }

    /**
     * Adds more shares to this holding, recalculating the average purchase price.
     *
     * @param additionalQty The quantity of new shares bought.
     * @param price The price at which the new shares were bought.
     */
    public synchronized void buyShares(int additionalQty, double price) {
        double totalCost = (this.quantity * this.averagePurchasePrice) + (additionalQty * price);
        this.quantity += additionalQty;
        this.averagePurchasePrice = totalCost / this.quantity;
    }

    /**
     * Deducts shares from this holding.
     *
     * @param qtyToSell The quantity of shares sold.
     */
    public synchronized void sellShares(int qtyToSell) {
        if (qtyToSell > this.quantity) {
            throw new IllegalArgumentException("Cannot sell more shares than currently owned.");
        }
        this.quantity -= qtyToSell;
    }
}
