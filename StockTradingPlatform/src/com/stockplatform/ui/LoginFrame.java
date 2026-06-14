package com.stockplatform.ui;

import com.stockplatform.model.User;
import com.stockplatform.service.FileManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Map;

/**
 * Modern Swing login and registration window.
 */
public class LoginFrame extends JFrame implements ThemeManager.ThemeChangeListener {

    public interface LoginCallback {
        void onLoginSuccess(User user);
    }

    private final Map<String, User> userDatabase;
    private final LoginCallback callback;

    private JPanel mainContainer;
    private JPanel cardPanel;
    private CardLayout cardLayout;

    // Login Form Elements
    private JTextField loginUserField;
    private JPasswordField loginPassField;
    private JLabel loginErrorLabel;
    private JButton loginBtn;

    // Register Form Elements
    private JTextField regUserField;
    private JPasswordField regPassField;
    private JPasswordField regPassConfirmField;
    private JLabel regErrorLabel;
    private JButton regBtn;

    // Common Buttons
    private JButton themeToggleBtn;

    /**
     * Constructs the LoginFrame.
     *
     * @param userDatabase Reference to the loaded users.
     * @param callback Callback triggered on successful login.
     */
    public LoginFrame(Map<String, User> userDatabase, LoginCallback callback) {
        this.userDatabase = userDatabase;
        this.callback = callback;

        setTitle("Stock Trading Platform - Auth");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(480, 580);
        setLocationRelativeTo(null);
        setResizable(false);

        // Initialize Theme Manager Observer
        ThemeManager.registerListener(this);

        initUI();
        applyCurrentTheme();
    }

    private void initUI() {
        mainContainer = new JPanel(new GridBagLayout());
        mainContainer.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Card Panel to hold Login and Register forms
        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout) {
            @Override
            protected void paintComponent(Graphics g) {
                // Paint smooth card background
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(ThemeManager.getCardBg());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                g2.setColor(ThemeManager.getBorder());
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 16, 16);
                g2.dispose();
            }
        };
        cardPanel.setOpaque(false);
        cardPanel.setPreferredSize(new Dimension(380, 460));

        // Create the Login and Register cards
        JPanel loginCard = createLoginCard();
        JPanel regCard = createRegisterCard();

        cardPanel.add(loginCard, "LOGIN");
        cardPanel.add(regCard, "REGISTER");

        // Layout constraints to center the card panel
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.NONE;
        mainContainer.add(cardPanel, gbc);

        // Theme Toggle Row at the bottom
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomPanel.setOpaque(false);
        themeToggleBtn = createIconButton("Toggle Theme");
        themeToggleBtn.addActionListener(e -> ThemeManager.toggleTheme());
        bottomPanel.add(themeToggleBtn);

        gbc.gridy = 1;
        gbc.weighty = 0.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        mainContainer.add(bottomPanel, gbc);

        setContentPane(mainContainer);
    }

    private JPanel createLoginCard() {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(new EmptyBorder(30, 30, 30, 30));

        // Header Title
        JLabel titleLabel = new JLabel("Welcome Back");
        titleLabel.setFont(ThemeManager.FONT_TITLE);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(titleLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 5)));

        JLabel subLabel = new JLabel("Login to manage your portfolio");
        subLabel.setFont(ThemeManager.FONT_SMALL);
        subLabel.setForeground(ThemeManager.getSubtext());
        subLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(subLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 30)));

        // Username Input
        JLabel userLabel = new JLabel("Username");
        userLabel.setFont(ThemeManager.FONT_BODY_BOLD);
        userLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(userLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 5)));

        loginUserField = createStyledTextField();
        panel.add(loginUserField);
        panel.add(Box.createRigidArea(new Dimension(0, 15)));

        // Password Input
        JLabel passLabel = new JLabel("Password");
        passLabel.setFont(ThemeManager.FONT_BODY_BOLD);
        passLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(passLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 5)));

        loginPassField = createStyledPasswordField();
        panel.add(loginPassField);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));

        // Error message label
        loginErrorLabel = new JLabel(" ");
        loginErrorLabel.setFont(ThemeManager.FONT_SMALL);
        loginErrorLabel.setForeground(ThemeManager.getRed());
        loginErrorLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(loginErrorLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 15)));

        // Login Button
        loginBtn = createStyledButton("Sign In");
        loginBtn.addActionListener(e -> handleLogin());
        panel.add(loginBtn);
        panel.add(Box.createRigidArea(new Dimension(0, 15)));

        // Switch to register link
        JPanel linkPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        linkPanel.setOpaque(false);
        JLabel noAccountLabel = new JLabel("New to the platform? ");
        noAccountLabel.setFont(ThemeManager.FONT_SMALL);
        JButton switchRegBtn = createLinkButton("Register here");
        switchRegBtn.addActionListener(e -> {
            loginErrorLabel.setText(" ");
            cardLayout.show(cardPanel, "REGISTER");
        });
        linkPanel.add(noAccountLabel);
        linkPanel.add(switchRegBtn);
        linkPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(linkPanel);

        return panel;
    }

    private JPanel createRegisterCard() {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(new EmptyBorder(25, 30, 25, 30));

        // Header Title
        JLabel titleLabel = new JLabel("Create Account");
        titleLabel.setFont(ThemeManager.FONT_TITLE);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(titleLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 5)));

        JLabel subLabel = new JLabel("Join and start trading stocks risk-free");
        subLabel.setFont(ThemeManager.FONT_SMALL);
        subLabel.setForeground(ThemeManager.getSubtext());
        subLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(subLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 25)));

        // Username Input
        JLabel userLabel = new JLabel("Username");
        userLabel.setFont(ThemeManager.FONT_BODY_BOLD);
        userLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(userLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 5)));

        regUserField = createStyledTextField();
        panel.add(regUserField);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));

        // Password Input
        JLabel passLabel = new JLabel("Password");
        passLabel.setFont(ThemeManager.FONT_BODY_BOLD);
        passLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(passLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 5)));

        regPassField = createStyledPasswordField();
        panel.add(regPassField);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));

        // Confirm Password Input
        JLabel passConfirmLabel = new JLabel("Confirm Password");
        passConfirmLabel.setFont(ThemeManager.FONT_BODY_BOLD);
        passConfirmLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(passConfirmLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 5)));

        regPassConfirmField = createStyledPasswordField();
        panel.add(regPassConfirmField);
        panel.add(Box.createRigidArea(new Dimension(0, 5)));

        // Error message label
        regErrorLabel = new JLabel(" ");
        regErrorLabel.setFont(ThemeManager.FONT_SMALL);
        regErrorLabel.setForeground(ThemeManager.getRed());
        regErrorLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(regErrorLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));

        // Register Button
        regBtn = createStyledButton("Sign Up");
        regBtn.addActionListener(e -> handleRegistration());
        panel.add(regBtn);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));

        // Switch to login link
        JPanel linkPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        linkPanel.setOpaque(false);
        JLabel hasAccountLabel = new JLabel("Already have an account? ");
        hasAccountLabel.setFont(ThemeManager.FONT_SMALL);
        JButton switchLoginBtn = createLinkButton("Sign In");
        switchLoginBtn.addActionListener(e -> {
            regErrorLabel.setText(" ");
            cardLayout.show(cardPanel, "LOGIN");
        });
        linkPanel.add(hasAccountLabel);
        linkPanel.add(switchLoginBtn);
        linkPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(linkPanel);

        return panel;
    }

    // --- Validation and Event Handlers ---

    private void handleLogin() {
        String username = loginUserField.getText().trim();
        String password = new String(loginPassField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            loginErrorLabel.setText("Please enter username and password.");
            return;
        }

        User user = userDatabase.get(username);
        if (user == null) {
            loginErrorLabel.setText("Username not found.");
            return;
        }

        String hashedPassword = FileManager.hashPassword(password);
        if (!user.getPasswordHash().equals(hashedPassword)) {
            loginErrorLabel.setText("Incorrect password.");
            return;
        }

        loginErrorLabel.setText(" ");
        // Trigger callback on success
        this.dispose();
        ThemeManager.unregisterListener(this);
        callback.onLoginSuccess(user);
    }

    private void handleRegistration() {
        String username = regUserField.getText().trim();
        String password = new String(regPassField.getPassword());
        String confirm = new String(regPassConfirmField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            regErrorLabel.setText("All fields are required.");
            return;
        }

        if (username.length() < 3) {
            regErrorLabel.setText("Username must be at least 3 characters.");
            return;
        }

        if (password.length() < 4) {
            regErrorLabel.setText("Password must be at least 4 characters.");
            return;
        }

        if (!password.equals(confirm)) {
            regErrorLabel.setText("Passwords do not match.");
            return;
        }

        if (userDatabase.containsKey(username)) {
            regErrorLabel.setText("Username already exists.");
            return;
        }

        // Create new user
        String passHash = FileManager.hashPassword(password);
        User newUser = new User(username, passHash);
        userDatabase.put(username, newUser);

        // Save immediately
        FileManager.saveData(userDatabase);

        regErrorLabel.setText(" ");
        JOptionPane.showMessageDialog(this,
                "Registration successful! Please login.",
                "Success", JOptionPane.INFORMATION_MESSAGE);

        // Clear and switch
        regUserField.setText("");
        regPassField.setText("");
        regPassConfirmField.setText("");
        cardLayout.show(cardPanel, "LOGIN");
    }

    // --- Component Styling Helpers ---

    private JTextField createStyledTextField() {
        JTextField field = new JTextField();
        field.setFont(ThemeManager.FONT_BODY);
        field.setPreferredSize(new Dimension(320, 36));
        field.setMaximumSize(new Dimension(320, 36));
        field.setMargin(new Insets(5, 10, 5, 10));
        return field;
    }

    private JPasswordField createStyledPasswordField() {
        JPasswordField field = new JPasswordField();
        field.setFont(ThemeManager.FONT_BODY);
        field.setPreferredSize(new Dimension(320, 36));
        field.setMaximumSize(new Dimension(320, 36));
        field.setMargin(new Insets(5, 10, 5, 10));
        return field;
    }

    private JButton createStyledButton(String text) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isPressed()) {
                    g2.setColor(ThemeManager.ACCENT_HOVER.darker());
                } else if (getModel().isRollover()) {
                    g2.setColor(ThemeManager.ACCENT_HOVER);
                } else {
                    g2.setColor(ThemeManager.ACCENT);
                }
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        button.setFont(ThemeManager.FONT_BODY_BOLD);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(320, 38));
        button.setMaximumSize(new Dimension(320, 38));
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        return button;
    }

    private JButton createLinkButton(String text) {
        JButton button = new JButton(text);
        button.setFont(ThemeManager.FONT_SMALL);
        button.setForeground(ThemeManager.ACCENT);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setMargin(new Insets(0, 0, 0, 0));

        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setText("<html><u>" + text + "</u></html>");
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setText(text);
            }
        });
        return button;
    }

    private JButton createIconButton(String tooltip) {
        JButton button = new JButton() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(ThemeManager.isDarkMode() ? new Color(255, 255, 255, 20) : new Color(0, 0, 0, 20));
                g2.fillOval(0, 0, getWidth(), getHeight());

                // Paint a sun/moon icon using simple shapes
                g2.setColor(ThemeManager.getText());
                int w = getWidth();
                int h = getHeight();
                if (ThemeManager.isDarkMode()) {
                    // Moon representation
                    g2.fillArc(w/4, h/4, w/2, h/2, -90, 180);
                    g2.setColor(ThemeManager.getCardBg());
                    g2.fillOval(w/3 + 1, h/4 + 1, w/2 - 2, h/2 - 2);
                } else {
                    // Sun representation
                    g2.fillOval(w/3, h/3, w/3, h/3);
                    g2.setStroke(new BasicStroke(1.5f));
                    for (int i = 0; i < 8; i++) {
                        double angle = i * Math.PI / 4.0;
                        int x1 = (int) (w/2 + (w/6) * Math.cos(angle));
                        int y1 = (int) (h/2 + (h/6) * Math.sin(angle));
                        int x2 = (int) (w/2 + (w/3) * Math.cos(angle));
                        int y2 = (int) (h/2 + (h/3) * Math.sin(angle));
                        g2.drawLine(x1, y1, x2, y2);
                    }
                }
                g2.dispose();
            }
        };
        button.setPreferredSize(new Dimension(32, 32));
        button.setFocusPainted(false);
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setToolTipText(tooltip);
        return button;
    }

    private void applyCurrentTheme() {
        Color primaryBg = ThemeManager.getPrimaryBg();
        Color cardBg = ThemeManager.getCardBg();
        Color text = ThemeManager.getText();
        Color border = ThemeManager.getBorder();

        mainContainer.setBackground(primaryBg);
        loginErrorLabel.setForeground(ThemeManager.getRed());
        regErrorLabel.setForeground(ThemeManager.getRed());

        // Update fields border & colors
        updateFieldTheme(loginUserField, cardBg, text, border);
        updateFieldTheme(loginPassField, cardBg, text, border);
        updateFieldTheme(regUserField, cardBg, text, border);
        updateFieldTheme(regPassField, cardBg, text, border);
        updateFieldTheme(regPassConfirmField, cardBg, text, border);

        // Recurse children text colors
        updatePanelLabels(cardPanel, text);

        repaint();
    }

    private void updateFieldTheme(JTextField field, Color bg, Color fg, Color border) {
        if (field == null) return;
        field.setBackground(bg);
        field.setForeground(fg);
        field.setCaretColor(fg);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(border, 1, true),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
    }

    private void updatePanelLabels(Container parent, Color text) {
        for (Component child : parent.getComponents()) {
            if (child instanceof JLabel) {
                JLabel lbl = (JLabel) child;
                // Exclude status/error labels which have their own colors
                if (lbl != loginErrorLabel && lbl != regErrorLabel) {
                    if (lbl.getFont().equals(ThemeManager.FONT_SMALL)) {
                        lbl.setForeground(ThemeManager.getSubtext());
                    } else {
                        lbl.setForeground(text);
                    }
                }
            } else if (child instanceof Container) {
                updatePanelLabels((Container) child, text);
            }
        }
    }

    @Override
    public void onThemeChanged() {
        SwingUtilities.invokeLater(this::applyCurrentTheme);
    }
}
