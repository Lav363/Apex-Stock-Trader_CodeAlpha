package com.stockplatform;

import com.stockplatform.model.User;
import com.stockplatform.service.FileManager;
import com.stockplatform.service.MarketManager;
import com.stockplatform.ui.LoginFrame;
import com.stockplatform.ui.DashboardFrame;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Map;

/**
 * Main application entry point that manages the state lifecycle,
 * database initialization, shutdown hooks, and frame orchestration.
 */
public class Main {
    private static Map<String, User> userDatabase;
    private static MarketManager marketManager;

    public static void main(String[] args) {
        // Configure standard Swing anti-aliasing system properties
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");

        // Load data from files
        userDatabase = FileManager.loadData();

        // Initialize stock market
        marketManager = new MarketManager();

        // Register a JVM shutdown hook to ensure data is saved on force quits
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("JVM shutting down... saving data.");
            FileManager.saveData(userDatabase);
        }));

        // Launch login interface on the Event Dispatch Thread (EDT)
        SwingUtilities.invokeLater(Main::showLoginScreen);
    }

    /**
     * Initializes and displays the Login screen.
     */
    private static void showLoginScreen() {
        LoginFrame loginFrame = new LoginFrame(userDatabase, Main::showDashboard);
        loginFrame.setVisible(true);
    }

    /**
     * Initializes and displays the main Trading Dashboard for the authenticated user.
     *
     * @param user The logged-in User instance.
     */
    private static void showDashboard(User user) {
        DashboardFrame dashboardFrame = new DashboardFrame(
                user,
                marketManager,
                userDatabase,
                Main::showLoginScreen // Logout handler returns user to Login Screen
        );

        // Save data and exit when the main dashboard is closed
        dashboardFrame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                System.out.println("Closing application. Saving user database...");
                FileManager.saveData(userDatabase);
                System.exit(0);
            }
        });

        dashboardFrame.setVisible(true);
    }
}
