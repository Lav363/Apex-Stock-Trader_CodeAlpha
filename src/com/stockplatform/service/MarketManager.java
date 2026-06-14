package com.stockplatform.service;

import com.stockplatform.model.Stock;

import java.util.*;

/**
 * Manages the stock market simulation, including initializing stocks,
 * simulating price fluctuations, and resolving stocks by symbol.
 */
public class MarketManager {
    private final List<Stock> stocks;
    private final Map<String, Double> originalPrices;
    private final Random random;

    /**
     * Initializes the MarketManager with a list of predefined stocks.
     */
    public MarketManager() {
        this.stocks = new ArrayList<>();
        this.originalPrices = new HashMap<>();
        this.random = new Random();

        // Add pre-defined blue-chip stocks
        addStock("AAPL", "Apple Inc.", 175.50);
        addStock("MSFT", "Microsoft Corp.", 420.20);
        addStock("GOOGL", "Alphabet Inc.", 172.50);
        addStock("AMZN", "Amazon.com Inc.", 185.10);
        addStock("TSLA", "Tesla Inc.", 170.80);
        addStock("NVDA", "NVIDIA Corp.", 925.00);
        addStock("META", "Meta Platforms Inc.", 475.60);
        addStock("NFLX", "Netflix Inc.", 610.30);
        addStock("AMD", "Advanced Micro Devices", 160.40);
        addStock("BABA", "Alibaba Group", 75.20);
    }

    private void addStock(String symbol, String name, double initialPrice) {
        Stock stock = new Stock(symbol, name, initialPrice, 0.0);
        stocks.add(stock);
        originalPrices.put(symbol, initialPrice);
    }

    /**
     * Retrieves all stocks currently in the market.
     *
     * @return List of stocks.
     */
    public synchronized List<Stock> getStocks() {
        return new ArrayList<>(stocks);
    }

    /**
     * Finds a stock by its ticker symbol.
     *
     * @param symbol The stock symbol.
     * @return The Stock, or null if not found.
     */
    public synchronized Stock getStock(String symbol) {
        for (Stock stock : stocks) {
            if (stock.getSymbol().equalsIgnoreCase(symbol)) {
                return stock;
            }
        }
        return null;
    }

    /**
     * Returns a map of stock symbols to their current prices.
     *
     * @return Map of symbol -> price.
     */
    public synchronized Map<String, Double> getPriceMap() {
        Map<String, Double> prices = new HashMap<>();
        for (Stock stock : stocks) {
            prices.put(stock.getSymbol(), stock.getCurrentPrice());
        }
        return prices;
    }

    /**
     * Simulates market price ticks.
     * Fluctuate each stock's price slightly (between -2% and +2%).
     */
    public synchronized void simulateFluctuations() {
        for (Stock stock : stocks) {
            double current = stock.getCurrentPrice();
            double original = originalPrices.get(stock.getSymbol());

            // Fluctuate price between -2.0% and +2.0%
            double changePercent = (random.nextDouble() * 4.0) - 2.0; 
            double priceDelta = current * (changePercent / 100.0);
            double newPrice = Math.max(0.50, current + priceDelta); // Floor at $0.50

            stock.updatePrice(newPrice, original);
        }
    }
}
