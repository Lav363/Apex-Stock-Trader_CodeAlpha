package com.stockplatform.service;

import com.stockplatform.model.User;
import com.stockplatform.model.Holding;
import com.stockplatform.model.Portfolio;
import com.stockplatform.model.Transaction;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;

/**
 * Handles all file I/O operations for saving and loading user profiles,
 * cash balances, portfolio holdings, and transaction history.
 */
public class FileManager {
    private static final String DATA_DIR = "data";
    private static final String USERS_FILE = DATA_DIR + "/users.csv";
    private static final String PORTFOLIOS_FILE = DATA_DIR + "/portfolios.csv";
    private static final String TRANSACTIONS_FILE = DATA_DIR + "/transactions.csv";

    static {
        createDataDirectory();
    }

    /**
     * Ensures that the data directory exists.
     */
    private static void createDataDirectory() {
        File dir = new File(DATA_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    /**
     * Hashes a password using SHA-256.
     *
     * @param password Raw text password.
     * @return Hashed hexadecimal string.
     */
    public static String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception ex) {
            return String.valueOf(password.hashCode()); // Fallback
        }
    }

    /**
     * Saves user accounts, balances, portfolios, and transaction records to CSV files.
     *
     * @param users Map of usernames to User models.
     */
    public static synchronized void saveData(Map<String, User> users) {
        try {
            createDataDirectory();
            saveUsers(users.values());
            savePortfolios(users.values());
            saveTransactions(users.values());
        } catch (IOException e) {
            System.err.println("Error saving system data: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Loads user profiles, portfolios, and transactions from CSV files.
     *
     * @return Map of usernames to User objects.
     */
    public static synchronized Map<String, User> loadData() {
        Map<String, User> users = new HashMap<>();
        try {
            createDataDirectory();
            // Load base user info
            loadUsers(users);
            // Load portfolios (holdings)
            loadPortfolios(users);
            // Load transaction histories
            loadTransactions(users);
        } catch (IOException e) {
            System.err.println("Error loading system data (might be first run): " + e.getMessage());
        }
        return users;
    }

    private static void saveUsers(Collection<User> users) throws IOException {
        try (PrintWriter writer = new PrintWriter(new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(USERS_FILE), StandardCharsets.UTF_8)))) {
            writer.println("username,passwordHash,balance"); // Header
            for (User user : users) {
                writer.printf("%s,%s,%.2f%n",
                        escapeCsv(user.getUsername()),
                        user.getPasswordHash(),
                        user.getPortfolio().getBalance());
            }
        }
    }

    private static void loadUsers(Map<String, User> users) throws IOException {
        File file = new File(USERS_FILE);
        if (!file.exists()) return;

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                new FileInputStream(file), StandardCharsets.UTF_8))) {
            String line = reader.readLine(); // Skip header
            while ((line = reader.readLine()) != null) {
                String[] parts = parseCsvLine(line);
                if (parts.length >= 3) {
                    String username = unescapeCsv(parts[0]);
                    String passwordHash = parts[1];
                    double balance = Double.parseDouble(parts[2]);

                    User user = new User(username, passwordHash);
                    user.getPortfolio().setBalance(balance);
                    users.put(username, user);
                }
            }
        }
    }

    private static void savePortfolios(Collection<User> users) throws IOException {
        try (PrintWriter writer = new PrintWriter(new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(PORTFOLIOS_FILE), StandardCharsets.UTF_8)))) {
            writer.println("username,symbol,quantity,averagePurchasePrice"); // Header
            for (User user : users) {
                Map<String, Holding> holdings = user.getPortfolio().getHoldings();
                for (Holding holding : holdings.values()) {
                    writer.printf("%s,%s,%d,%.4f%n",
                            escapeCsv(user.getUsername()),
                            escapeCsv(holding.getSymbol()),
                            holding.getQuantity(),
                            holding.getAveragePurchasePrice());
                }
            }
        }
    }

    private static void loadPortfolios(Map<String, User> users) throws IOException {
        File file = new File(PORTFOLIOS_FILE);
        if (!file.exists()) return;

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                new FileInputStream(file), StandardCharsets.UTF_8))) {
            String line = reader.readLine(); // Skip header
            while ((line = reader.readLine()) != null) {
                String[] parts = parseCsvLine(line);
                if (parts.length >= 4) {
                    String username = unescapeCsv(parts[0]);
                    String symbol = unescapeCsv(parts[1]);
                    int quantity = Integer.parseInt(parts[2]);
                    double averagePurchasePrice = Double.parseDouble(parts[3]);

                    User user = users.get(username);
                    if (user != null) {
                        Holding holding = new Holding(symbol, quantity, averagePurchasePrice);
                        user.getPortfolio().addHolding(holding);
                    }
                }
            }
        }
    }

    private static void saveTransactions(Collection<User> users) throws IOException {
        try (PrintWriter writer = new PrintWriter(new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(TRANSACTIONS_FILE), StandardCharsets.UTF_8)))) {
            writer.println("username,type,symbol,quantity,price,dateTime"); // Header
            for (User user : users) {
                List<Transaction> transactions = user.getPortfolio().getTransactions();
                for (Transaction tx : transactions) {
                    writer.printf("%s,%s,%s,%d,%.2f,%s%n",
                            escapeCsv(user.getUsername()),
                            tx.getType().name(),
                            escapeCsv(tx.getSymbol()),
                            tx.getQuantity(),
                            tx.getPrice(),
                            escapeCsv(tx.getDateTime()));
                }
            }
        }
    }

    private static void loadTransactions(Map<String, User> users) throws IOException {
        File file = new File(TRANSACTIONS_FILE);
        if (!file.exists()) return;

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                new FileInputStream(file), StandardCharsets.UTF_8))) {
            String line = reader.readLine(); // Skip header
            while ((line = reader.readLine()) != null) {
                String[] parts = parseCsvLine(line);
                if (parts.length >= 6) {
                    String username = unescapeCsv(parts[0]);
                    Transaction.Type type = Transaction.Type.valueOf(parts[1]);
                    String symbol = unescapeCsv(parts[2]);
                    int quantity = Integer.parseInt(parts[3]);
                    double price = Double.parseDouble(parts[4]);
                    String dateTime = unescapeCsv(parts[5]);

                    User user = users.get(username);
                    if (user != null) {
                        Transaction tx = new Transaction(type, symbol, quantity, price, dateTime);
                        user.getPortfolio().addTransaction(tx);
                    }
                }
            }
        }
    }

    // --- CSV Helper Methods ---

    private static String escapeCsv(String val) {
        if (val == null) return "";
        if (val.contains(",") || val.contains("\"") || val.contains("\n") || val.contains("\r")) {
            return "\"" + val.replace("\"", "\"\"") + "\"";
        }
        return val;
    }

    private static String unescapeCsv(String val) {
        if (val == null) return "";
        if (val.startsWith("\"") && val.endsWith("\"")) {
            val = val.substring(1, val.length() - 1);
            return val.replace("\"\"", "\"");
        }
        return val;
    }

    private static String[] parseCsvLine(String line) {
        List<String> list = new ArrayList<>();
        boolean inQuotes = false;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '\"') {
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '\"') {
                    sb.append('\"');
                    i++; // Skip second quote
                } else {
                    inQuotes = !inQuotes;
                }
            } else if (c == ',') {
                if (inQuotes) {
                    sb.append(c);
                } else {
                    list.add(sb.toString());
                    sb.setLength(0);
                }
            } else {
                sb.append(c);
            }
        }
        list.add(sb.toString());
        return list.toArray(new String[0]);
    }
}
