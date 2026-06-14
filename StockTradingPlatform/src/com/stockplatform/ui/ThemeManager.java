package com.stockplatform.ui;

import java.awt.Color;
import java.awt.Font;
import java.util.ArrayList;
import java.util.List;

/**
 * Manages UI themes (Light and Dark modes) and notifies active listeners when the theme is toggled.
 */
public class ThemeManager {
    private static boolean darkMode = true; // Default to dark mode for a premium feel
    private static final List<ThemeChangeListener> listeners = new ArrayList<>();

    // --- Fonts ---
    public static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 24);
    public static final Font FONT_HEADER = new Font("Segoe UI", Font.BOLD, 16);
    public static final Font FONT_BODY_BOLD = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font FONT_BODY = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_SMALL = new Font("Segoe UI", Font.PLAIN, 11);

    // --- Color Palettes ---

    // Dark Mode Colors
    private static final Color DARK_PRIMARY_BG = new Color(11, 15, 25);      // Deep dark blue-black
    private static final Color DARK_CARD_BG = new Color(20, 29, 47);         // Slate card background
    private static final Color DARK_TEXT = new Color(241, 245, 249);         // Off-white text
    private static final Color DARK_SUBTEXT = new Color(148, 163, 184);      // Muted blue-gray text
    private static final Color DARK_BORDER = new Color(30, 41, 59);          // Border color
    private static final Color DARK_GREEN = new Color(16, 185, 129);         // Vibrant emerald green
    private static final Color DARK_RED = new Color(239, 68, 68);            // Vibrant rose red

    // Light Mode Colors
    private static final Color LIGHT_PRIMARY_BG = new Color(248, 250, 252);  // Clean off-white
    private static final Color LIGHT_CARD_BG = new Color(255, 255, 255);     // Pure white card
    private static final Color LIGHT_TEXT = new Color(15, 23, 42);           // Slate dark text
    private static final Color LIGHT_SUBTEXT = new Color(100, 116, 139);     // Muted slate text
    private static final Color LIGHT_BORDER = new Color(226, 232, 240);      // Light gray border
    private static final Color LIGHT_GREEN = new Color(5, 150, 105);         // Emerald forest green
    private static final Color LIGHT_RED = new Color(220, 38, 38);           // Crimson red

    // Common Colors
    public static final Color ACCENT = new Color(59, 130, 246);              // Premium Royal Blue
    public static final Color ACCENT_HOVER = new Color(37, 99, 235);        // Darker blue for hover
    public static final Color ACCENT_SECONDARY = new Color(99, 102, 241);    // Indigo highlight
    public static final Color TABLE_HEADER_DARK = new Color(15, 23, 42);     // Very dark header
    public static final Color TABLE_HEADER_LIGHT = new Color(241, 245, 249);   // Light header background

    public interface ThemeChangeListener {
        void onThemeChanged();
    }

    public static synchronized boolean isDarkMode() {
        return darkMode;
    }

    public static synchronized void toggleTheme() {
        darkMode = !darkMode;
        notifyListeners();
    }

    public static synchronized void registerListener(ThemeChangeListener listener) {
        listeners.add(listener);
    }

    public static synchronized void unregisterListener(ThemeChangeListener listener) {
        listeners.remove(listener);
    }

    private static synchronized void notifyListeners() {
        for (ThemeChangeListener listener : listeners) {
            listener.onThemeChanged();
        }
    }

    // --- Color Getters based on active theme ---

    public static Color getPrimaryBg() {
        return darkMode ? DARK_PRIMARY_BG : LIGHT_PRIMARY_BG;
    }

    public static Color getCardBg() {
        return darkMode ? DARK_CARD_BG : LIGHT_CARD_BG;
    }

    public static Color getText() {
        return darkMode ? DARK_TEXT : LIGHT_TEXT;
    }

    public static Color getSubtext() {
        return darkMode ? DARK_SUBTEXT : LIGHT_SUBTEXT;
    }

    public static Color getBorder() {
        return darkMode ? DARK_BORDER : LIGHT_BORDER;
    }

    public static Color getGreen() {
        return darkMode ? DARK_GREEN : LIGHT_GREEN;
    }

    public static Color getRed() {
        return darkMode ? DARK_RED : LIGHT_RED;
    }

    public static Color getTableHeaderBg() {
        return darkMode ? TABLE_HEADER_DARK : TABLE_HEADER_LIGHT;
    }
}
