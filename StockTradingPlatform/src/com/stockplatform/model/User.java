package com.stockplatform.model;

/**
 * Represents a registered user on the platform.
 */
public class User {
    private final String username;
    private final String passwordHash;
    private final Portfolio portfolio;

    /**
     * Constructs a new User.
     *
     * @param username The unique username.
     * @param passwordHash The hashed password.
     */
    public User(String username, String passwordHash) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.portfolio = new Portfolio();
    }

    /**
     * Constructs a User with a pre-existing portfolio (useful during file loading).
     *
     * @param username The unique username.
     * @param passwordHash The hashed password.
     * @param portfolio The loaded portfolio.
     */
    public User(String username, String passwordHash, Portfolio portfolio) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.portfolio = portfolio;
    }

    public String getUsername() {
        return username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public Portfolio getPortfolio() {
        return portfolio;
    }
}
