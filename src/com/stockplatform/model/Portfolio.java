package com.stockplatform.model;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Manages the user's cash balance, stock holdings, and trade transactions.
 */
public class Portfolio {
    private double balance;
    private final Map<String, Holding> holdings;
    private final List<Transaction> transactions;

    /**
     * Constructs a new Portfolio with an initial default cash balance of $100,000.
     */
    public Portfolio() {
        this.balance = 100000.00; // Starting virtual cash
        this.holdings = new LinkedHashMap<>();
        this.transactions = new ArrayList<>();
    }

    public synchronized double getBalance() {
        return balance;
    }

    public synchronized void setBalance(double balance) {
        this.balance = balance;
    }

    public synchronized Map<String, Holding> getHoldings() {
        return new LinkedHashMap<>(holdings);
    }

    public synchronized List<Transaction> getTransactions() {
        return new ArrayList<>(transactions);
    }

    /**
     * Adds a raw holding to the portfolio (useful during file loading).
     */
    public synchronized void addHolding(Holding holding) {
        holdings.put(holding.getSymbol(), holding);
    }

    /**
     * Adds a transaction to the history (useful during file loading).
     */
    public synchronized void addTransaction(Transaction tx) {
        transactions.add(tx);
    }

    /**
     * Executes a buy order.
     *
     * @param symbol The stock ticker symbol.
     * @param quantity The number of shares to buy.
     * @param price The purchase price per share.
     * @return The created Transaction object.
     */
    public synchronized Transaction buyStock(String symbol, int quantity, double price) {
        double cost = quantity * price;
        if (cost > balance) {
            throw new IllegalArgumentException("Insufficient funds to complete buy order.");
        }

        balance -= cost;
        Holding holding = holdings.get(symbol);
        if (holding == null) {
            holding = new Holding(symbol, quantity, price);
            holdings.put(symbol, holding);
        } else {
            holding.buyShares(quantity, price);
        }

        Transaction tx = new Transaction(Transaction.Type.BUY, symbol, quantity, price);
        transactions.add(tx);
        return tx;
    }

    /**
     * Executes a sell order.
     *
     * @param symbol The stock ticker symbol.
     * @param quantity The number of shares to sell.
     * @param price The selling price per share.
     * @return The created Transaction object.
     */
    public synchronized Transaction sellStock(String symbol, int quantity, double price) {
        Holding holding = holdings.get(symbol);
        if (holding == null || holding.getQuantity() < quantity) {
            throw new IllegalArgumentException("Cannot sell more shares than currently owned.");
        }

        double revenue = quantity * price;
        balance += revenue;
        holding.sellShares(quantity);

        if (holding.getQuantity() == 0) {
            holdings.remove(symbol);
        }

        Transaction tx = new Transaction(Transaction.Type.SELL, symbol, quantity, price);
        transactions.add(tx);
        return tx;
    }

    /**
     * Dynamically calculates the total portfolio value (cash balance + market value of holdings).
     *
     * @param stockPrices A map of current stock prices from the market.
     * @return The total value of the portfolio.
     */
    public synchronized double calculateTotalValue(Map<String, Double> stockPrices) {
        double holdingsValue = 0.0;
        for (Holding h : holdings.values()) {
            Double price = stockPrices.get(h.getSymbol());
            if (price != null) {
                holdingsValue += h.getQuantity() * price;
            } else {
                holdingsValue += h.getQuantity() * h.getAveragePurchasePrice();
            }
        }
        return balance + holdingsValue;
    }

    /**
     * Dynamically calculates total profit or loss across all holdings.
     *
     * @param stockPrices A map of current stock prices from the market.
     * @return The net gain or loss value.
     */
    public synchronized double calculateTotalProfitLoss(Map<String, Double> stockPrices) {
        double profitLoss = 0.0;
        for (Holding h : holdings.values()) {
            Double currentPrice = stockPrices.get(h.getSymbol());
            if (currentPrice != null) {
                profitLoss += (currentPrice - h.getAveragePurchasePrice()) * h.getQuantity();
            }
        }
        return profitLoss;
    }
}
