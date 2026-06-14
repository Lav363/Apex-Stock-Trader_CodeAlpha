package com.stockplatform.model;

/**
 * Represents a stock in the stock market simulation.
 */
public class Stock {
    private final String symbol;
    private final String companyName;
    private double currentPrice;
    private double dailyChangePercent;

    /**
     * Constructs a new Stock instance.
     *
     * @param symbol The stock ticker symbol (e.g., AAPL).
     * @param companyName The name of the company (e.g., Apple Inc.).
     * @param currentPrice The initial current price.
     * @param dailyChangePercent The initial daily change percentage.
     */
    public Stock(String symbol, String companyName, double currentPrice, double dailyChangePercent) {
        this.symbol = symbol;
        this.companyName = companyName;
        this.currentPrice = currentPrice;
        this.dailyChangePercent = dailyChangePercent;
    }

    public String getSymbol() {
        return symbol;
    }

    public String getCompanyName() {
        return companyName;
    }

    public synchronized double getCurrentPrice() {
        return currentPrice;
    }

    public synchronized void setCurrentPrice(double currentPrice) {
        this.currentPrice = currentPrice;
    }

    public synchronized double getDailyChangePercent() {
        return dailyChangePercent;
    }

    public synchronized void setDailyChangePercent(double dailyChangePercent) {
        this.dailyChangePercent = dailyChangePercent;
    }

    /**
     * Updates the price and daily change percentage of the stock.
     *
     * @param newPrice The updated price.
     * @param originalPrice The baseline starting price of the stock.
     */
    public synchronized void updatePrice(double newPrice, double originalPrice) {
        this.currentPrice = newPrice;
        this.dailyChangePercent = ((newPrice - originalPrice) / originalPrice) * 100.0;
    }
}
