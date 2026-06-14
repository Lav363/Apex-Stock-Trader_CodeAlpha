package com.stockplatform.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Represents a historical stock transaction (BUY or SELL).
 */
public class Transaction {
    public enum Type {
        BUY, SELL
    }

    private final Type type;
    private final String symbol;
    private final int quantity;
    private final double price;
    private final String dateTime;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * Constructs a new Transaction with the current local date and time.
     *
     * @param type The type of transaction (BUY/SELL).
     * @param symbol The stock ticker symbol.
     * @param quantity The quantity traded.
     * @param price The transaction price per share.
     */
    public Transaction(Type type, String symbol, int quantity, double price) {
        this.type = type;
        this.symbol = symbol;
        this.quantity = quantity;
        this.price = price;
        this.dateTime = LocalDateTime.now().format(FORMATTER);
    }

    /**
     * Constructs a Transaction with a pre-existing timestamp (useful for loading from files).
     *
     * @param type The type of transaction (BUY/SELL).
     * @param symbol The stock ticker symbol.
     * @param quantity The quantity traded.
     * @param price The transaction price per share.
     * @param dateTime The formatted date and time.
     */
    public Transaction(Type type, String symbol, int quantity, double price, String dateTime) {
        this.type = type;
        this.symbol = symbol;
        this.quantity = quantity;
        this.price = price;
        this.dateTime = dateTime;
    }

    public Type getType() {
        return type;
    }

    public String getSymbol() {
        return symbol;
    }

    public int getQuantity() {
        return quantity;
    }

    public double getPrice() {
        return price;
    }

    public String getDateTime() {
        return dateTime;
    }
}
