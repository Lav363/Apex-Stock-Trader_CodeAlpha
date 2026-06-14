package com.stockplatform.ui;

import com.stockplatform.model.*;
import com.stockplatform.service.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Map;

/**
 * Main application dashboard featuring live stock feeds, portfolio metrics,
 * buy/sell forms, and transaction logs in a premium, responsive layout.
 */
public class DashboardFrame extends JFrame implements ThemeManager.ThemeChangeListener {

    private final User currentUser;
    private final MarketManager marketManager;
    private final Map<String, User> userDatabase;
    private final Runnable logoutHandler;

    // Layout containers
    private JPanel sidebarPanel;
    private JPanel headerPanel;
    private JPanel contentPanel;
    private CardLayout contentCardLayout;

    // Navigation Buttons
    private JButton btnOverview;
    private JButton btnMarket;
    private JButton btnPortfolio;
    private JButton btnHistory;

    // Header Components
    private JLabel lblUserGreeting;
    private JButton btnToggleTheme;
    private JButton btnLogout;

    // STAT CARDS (Overview Panel)
    private JPanel cardBalance;
    private JPanel cardPortfolioValue;
    private JPanel cardGainLoss;
    private JLabel lblBalanceVal;
    private JLabel lblPortfolioVal;
    private JLabel lblGainLossVal;

    // TABLES & MODELS
    private JTable tblMarket;
    private DefaultTableModel modelMarket;
    
    private JTable tblPortfolio;
    private DefaultTableModel modelPortfolio;

    private JTable tblHistory;
    private DefaultTableModel modelHistory;

    // BUY/SELL FORM PANEL (Located in Market watch view)
    private JTextField txtSymbol;
    private JTextField txtQuantity;
    private JLabel lblEstTotal;
    private JButton btnBuy;
    private JButton btnSell;
    private JLabel lblTradeMessage;

    // Live Tick Timer
    private Timer simulationTimer;

    private static final DecimalFormat CURRENCY_FORMAT = new DecimalFormat("$#,##0.00");
    private static final DecimalFormat PERCENT_FORMAT = new DecimalFormat("+#,##0.00%;-#,##0.00%");

    /**
     * Constructs the DashboardFrame.
     */
    public DashboardFrame(User currentUser, MarketManager marketManager, Map<String, User> userDatabase, Runnable logoutHandler) {
        this.currentUser = currentUser;
        this.marketManager = marketManager;
        this.userDatabase = userDatabase;
        this.logoutHandler = logoutHandler;

        setTitle("Apex Stock Trader - Dashboard");
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setSize(1080, 720);
        setMinimumSize(new Dimension(960, 600));
        setLocationRelativeTo(null);

        // Register for Theme Updates
        ThemeManager.registerListener(this);

        initUI();
        applyCurrentTheme();
        startSimulationTimer();
    }

    private void initUI() {
        JPanel rootPanel = new JPanel(new BorderLayout());

        // 1. Sidebar (West)
        sidebarPanel = createSidebar();
        rootPanel.add(sidebarPanel, BorderLayout.WEST);

        // 2. Header (North)
        headerPanel = createHeader();
        rootPanel.add(headerPanel, BorderLayout.NORTH);

        // 3. Central Content Panel (Center)
        contentCardLayout = new CardLayout();
        contentPanel = new JPanel(contentCardLayout);

        // Create the views
        JPanel panelOverview = createOverviewPanel();
        JPanel panelMarket = createMarketPanel();
        JPanel panelPortfolio = createPortfolioPanel();
        JPanel panelHistory = createHistoryPanel();

        contentPanel.add(panelOverview, "OVERVIEW");
        contentPanel.add(panelMarket, "MARKET");
        contentPanel.add(panelPortfolio, "PORTFOLIO");
        contentPanel.add(panelHistory, "HISTORY");

        rootPanel.add(contentPanel, BorderLayout.CENTER);

        setContentPane(rootPanel);

        // Default to Overview
        switchView("OVERVIEW", btnOverview);
    }

    private JPanel createSidebar() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(new EmptyBorder(25, 15, 25, 15));
        panel.setPreferredSize(new Dimension(200, getHeight()));

        // App Logo/Branding
        JLabel lblLogo = new JLabel("APEX TRADER");
        lblLogo.setFont(ThemeManager.FONT_HEADER);
        lblLogo.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(lblLogo);
        panel.add(Box.createRigidArea(new Dimension(0, 40)));

        // Navigation Sidebar Buttons
        btnOverview = createSidebarButton("Dashboard", "OVERVIEW");
        btnMarket = createSidebarButton("Market Watch", "MARKET");
        btnPortfolio = createSidebarButton("My Portfolio", "PORTFOLIO");
        btnHistory = createSidebarButton("Transactions", "HISTORY");

        panel.add(btnOverview);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));
        panel.add(btnMarket);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));
        panel.add(btnPortfolio);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));
        panel.add(btnHistory);

        // Push everything else down
        panel.add(Box.createGlue());

        // Small version details
        JLabel lblVer = new JLabel("v1.0.0 (Swing)");
        lblVer.setFont(ThemeManager.FONT_SMALL);
        lblVer.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(lblVer);

        return panel;
    }

    private JPanel createHeader() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(15, 20, 15, 20));

        // Welcome text
        lblUserGreeting = new JLabel("Welcome back, " + currentUser.getUsername());
        lblUserGreeting.setFont(ThemeManager.FONT_HEADER);
        panel.add(lblUserGreeting, BorderLayout.WEST);

        // Actions panel (Theme toggle + Logout)
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        actions.setOpaque(false);

        btnToggleTheme = createIconButton("Toggle Theme");
        btnToggleTheme.addActionListener(e -> ThemeManager.toggleTheme());
        actions.add(btnToggleTheme);

        btnLogout = createAccentButton("Sign Out", false);
        btnLogout.addActionListener(e -> handleLogout());
        actions.add(btnLogout);

        panel.add(actions, BorderLayout.EAST);
        return panel;
    }

    // --- Overview View ---
    private JPanel createOverviewPanel() {
        JPanel panel = new JPanel(new BorderLayout(20, 20));
        panel.setBorder(new EmptyBorder(25, 25, 25, 25));

        // Top Grid of Stat Cards (Balance, Portfolio Value, Profit/Loss)
        JPanel gridStats = new JPanel(new GridLayout(1, 3, 20, 0));
        gridStats.setOpaque(false);

        cardBalance = createStatCard("Available Cash");
        lblBalanceVal = (JLabel) cardBalance.getClientProperty("valLabel");
        gridStats.add(cardBalance);

        cardPortfolioValue = createStatCard("Net Portfolio Value");
        lblPortfolioVal = (JLabel) cardPortfolioValue.getClientProperty("valLabel");
        gridStats.add(cardPortfolioValue);

        cardGainLoss = createStatCard("Total Unrealized PnL");
        lblGainLossVal = (JLabel) cardGainLoss.getClientProperty("valLabel");
        gridStats.add(cardGainLoss);

        panel.add(gridStats, BorderLayout.NORTH);

        // Center card with simple greeting and instructions
        JPanel welcomeCard = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(ThemeManager.getCardBg());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.setColor(ThemeManager.getBorder());
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
                g2.dispose();
            }
        };
        welcomeCard.setOpaque(false);
        welcomeCard.setLayout(new GridBagLayout());
        welcomeCard.setBorder(new EmptyBorder(40, 40, 40, 40));

        JLabel mainWelcome = new JLabel("Apex Simulation Platform");
        mainWelcome.setFont(ThemeManager.FONT_TITLE);
        
        JLabel hintLabel = new JLabel("<html><center>Simulate real-time stock buying and selling with fake funds.<br>"
                + "Prices update automatically every 4 seconds.<br><br>"
                + "Use the sidebar menu items to watch the market, place orders, or review transactions.</center></html>");
        hintLabel.setFont(ThemeManager.FONT_BODY);
        hintLabel.setHorizontalAlignment(SwingConstants.CENTER);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(0, 0, 15, 0);
        welcomeCard.add(mainWelcome, gbc);
        gbc.gridy = 1;
        welcomeCard.add(hintLabel, gbc);

        panel.add(welcomeCard, BorderLayout.CENTER);

        return panel;
    }

    // --- Market Watch View ---
    private JPanel createMarketPanel() {
        JPanel panel = new JPanel(new BorderLayout(20, 20));
        panel.setBorder(new EmptyBorder(25, 25, 25, 25));

        // Left: Market Table
        JPanel tableContainer = new JPanel(new BorderLayout(10, 10)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(ThemeManager.getCardBg());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.setColor(ThemeManager.getBorder());
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
                g2.dispose();
            }
        };
        tableContainer.setOpaque(false);
        tableContainer.setBorder(new EmptyBorder(15, 15, 15, 15));

        JLabel title = new JLabel("Market Watchlist");
        title.setFont(ThemeManager.FONT_HEADER);
        tableContainer.add(title, BorderLayout.NORTH);

        // Configure Stock Table
        String[] cols = {"Symbol", "Company Name", "Price", "24h Change"};
        modelMarket = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
        tblMarket = new JTable(modelMarket);
        styleJTable(tblMarket);
        tblMarket.getColumnModel().getColumn(3).setCellRenderer(new ChangePercentCellRenderer());

        // Mouse click loads symbol in Buy/Sell form
        tblMarket.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = tblMarket.getSelectedRow();
                if (row >= 0) {
                    String symbol = (String) modelMarket.getValueAt(row, 0);
                    txtSymbol.setText(symbol);
                    updateEstTotal();
                }
            }
        });

        JScrollPane scroll = new JScrollPane(tblMarket);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        tableContainer.add(scroll, BorderLayout.CENTER);

        panel.add(tableContainer, BorderLayout.CENTER);

        // Right: Trade Panel (Buy/Sell order form)
        JPanel tradePanel = createTradeForm();
        panel.add(tradePanel, BorderLayout.EAST);

        return panel;
    }

    private JPanel createTradeForm() {
        JPanel form = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(ThemeManager.getCardBg());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.setColor(ThemeManager.getBorder());
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
                g2.dispose();
            }
        };
        form.setOpaque(false);
        form.setPreferredSize(new Dimension(280, getHeight()));
        form.setBorder(new EmptyBorder(20, 20, 20, 20));
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Place Order");
        title.setFont(ThemeManager.FONT_HEADER);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(title);
        form.add(Box.createRigidArea(new Dimension(0, 20)));

        JLabel lblSym = new JLabel("Ticker Symbol");
        lblSym.setFont(ThemeManager.FONT_BODY_BOLD);
        lblSym.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(lblSym);
        form.add(Box.createRigidArea(new Dimension(0, 5)));

        txtSymbol = new JTextField();
        txtSymbol.setFont(ThemeManager.FONT_BODY);
        txtSymbol.setMaximumSize(new Dimension(240, 36));
        txtSymbol.setAlignmentX(Component.LEFT_ALIGNMENT);
        txtSymbol.addCaretListener(e -> updateEstTotal());
        form.add(txtSymbol);
        form.add(Box.createRigidArea(new Dimension(0, 15)));

        JLabel lblQty = new JLabel("Shares Quantity");
        lblQty.setFont(ThemeManager.FONT_BODY_BOLD);
        lblQty.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(lblQty);
        form.add(Box.createRigidArea(new Dimension(0, 5)));

        txtQuantity = new JTextField();
        txtQuantity.setFont(ThemeManager.FONT_BODY);
        txtQuantity.setMaximumSize(new Dimension(240, 36));
        txtQuantity.setAlignmentX(Component.LEFT_ALIGNMENT);
        txtQuantity.addCaretListener(e -> updateEstTotal());
        form.add(txtQuantity);
        form.add(Box.createRigidArea(new Dimension(0, 20)));

        lblEstTotal = new JLabel("Estimated Total: $0.00");
        lblEstTotal.setFont(ThemeManager.FONT_BODY);
        lblEstTotal.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(lblEstTotal);
        form.add(Box.createRigidArea(new Dimension(0, 10)));

        lblTradeMessage = new JLabel(" ");
        lblTradeMessage.setFont(ThemeManager.FONT_SMALL);
        lblTradeMessage.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(lblTradeMessage);
        form.add(Box.createRigidArea(new Dimension(0, 15)));

        // Buttons
        btnBuy = createAccentButton("Buy Stock", false);
        btnBuy.setMaximumSize(new Dimension(240, 38));
        btnBuy.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnBuy.addActionListener(e -> executeTrade(Transaction.Type.BUY));
        form.add(btnBuy);
        form.add(Box.createRigidArea(new Dimension(0, 10)));

        btnSell = createAccentButton("Sell Stock", true); // Accent secondary button
        btnSell.setMaximumSize(new Dimension(240, 38));
        btnSell.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnSell.addActionListener(e -> executeTrade(Transaction.Type.SELL));
        form.add(btnSell);

        // Force manual refresh button at the bottom of form
        form.add(Box.createGlue());
        JButton btnForceRefresh = createIconButton("Refresh Market Prices Now");
        btnForceRefresh.addActionListener(e -> refreshMarketTick());
        btnForceRefresh.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JPanel refreshWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER));
        refreshWrapper.setOpaque(false);
        refreshWrapper.add(btnForceRefresh);
        refreshWrapper.add(new JLabel("Force Refresh"));
        refreshWrapper.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(refreshWrapper);

        return form;
    }

    // --- Portfolio View ---
    private JPanel createPortfolioPanel() {
        JPanel panel = new JPanel(new BorderLayout(20, 20));
        panel.setBorder(new EmptyBorder(25, 25, 25, 25));

        JPanel container = new JPanel(new BorderLayout(10, 10)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(ThemeManager.getCardBg());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.setColor(ThemeManager.getBorder());
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
                g2.dispose();
            }
        };
        container.setOpaque(false);
        container.setBorder(new EmptyBorder(15, 15, 15, 15));

        JLabel title = new JLabel("My Stock Holdings");
        title.setFont(ThemeManager.FONT_HEADER);
        container.add(title, BorderLayout.NORTH);

        // Configure Portfolio Table
        String[] cols = {"Symbol", "Shares Owned", "Avg Purchase Price", "Market Price", "Market Value", "Total PnL"};
        modelPortfolio = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
        tblPortfolio = new JTable(modelPortfolio);
        styleJTable(tblPortfolio);
        tblPortfolio.getColumnModel().getColumn(5).setCellRenderer(new PnlValueCellRenderer());

        JScrollPane scroll = new JScrollPane(tblPortfolio);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        container.add(scroll, BorderLayout.CENTER);

        panel.add(container, BorderLayout.CENTER);
        return panel;
    }

    // --- History View ---
    private JPanel createHistoryPanel() {
        JPanel panel = new JPanel(new BorderLayout(20, 20));
        panel.setBorder(new EmptyBorder(25, 25, 25, 25));

        JPanel container = new JPanel(new BorderLayout(10, 10)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(ThemeManager.getCardBg());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.setColor(ThemeManager.getBorder());
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
                g2.dispose();
            }
        };
        container.setOpaque(false);
        container.setBorder(new EmptyBorder(15, 15, 15, 15));

        JLabel title = new JLabel("Transaction Log");
        title.setFont(ThemeManager.FONT_HEADER);
        container.add(title, BorderLayout.NORTH);

        // Configure Transaction Table
        String[] cols = {"Date & Time", "Action", "Symbol", "Shares", "Execution Price", "Total Value"};
        modelHistory = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
        tblHistory = new JTable(modelHistory);
        styleJTable(tblHistory);
        tblHistory.getColumnModel().getColumn(1).setCellRenderer(new TransactionTypeCellRenderer());

        JScrollPane scroll = new JScrollPane(tblHistory);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        container.add(scroll, BorderLayout.CENTER);

        panel.add(container, BorderLayout.CENTER);
        return panel;
    }

    // --- Event Logic & Order Processing ---

    private void updateEstTotal() {
        String symbol = txtSymbol.getText().trim().toUpperCase();
        String qtyStr = txtQuantity.getText().trim();

        if (symbol.isEmpty() || qtyStr.isEmpty()) {
            lblEstTotal.setText("Estimated Total: $0.00");
            return;
        }

        try {
            int qty = Integer.parseInt(qtyStr);
            Stock stock = marketManager.getStock(symbol);
            if (stock != null && qty > 0) {
                double total = qty * stock.getCurrentPrice();
                lblEstTotal.setText("Estimated Total: " + CURRENCY_FORMAT.format(total));
            } else {
                lblEstTotal.setText("Estimated Total: --");
            }
        } catch (NumberFormatException e) {
            lblEstTotal.setText("Estimated Total: --");
        }
    }

    private void executeTrade(Transaction.Type type) {
        String symbol = txtSymbol.getText().trim().toUpperCase();
        String qtyStr = txtQuantity.getText().trim();

        if (symbol.isEmpty() || qtyStr.isEmpty()) {
            showTradeMessage("Ticker symbol and quantity are required.", true);
            return;
        }

        Stock stock = marketManager.getStock(symbol);
        if (stock == null) {
            showTradeMessage("Invalid stock symbol.", true);
            return;
        }

        int qty;
        try {
            qty = Integer.parseInt(qtyStr);
            if (qty <= 0) {
                showTradeMessage("Quantity must be greater than 0.", true);
                return;
            }
        } catch (NumberFormatException e) {
            showTradeMessage("Quantity must be a valid integer.", true);
            return;
        }

        Portfolio portfolio = currentUser.getPortfolio();
        double price = stock.getCurrentPrice();

        try {
            if (type == Transaction.Type.BUY) {
                portfolio.buyStock(symbol, qty, price);
                showTradeMessage("Bought " + qty + " shares of " + symbol, false);
            } else {
                portfolio.sellStock(symbol, qty, price);
                showTradeMessage("Sold " + qty + " shares of " + symbol, false);
            }

            // Save automatically on transaction
            FileManager.saveData(userDatabase);

            // Clear input fields and update views
            txtQuantity.setText("");
            lblEstTotal.setText("Estimated Total: $0.00");
            refreshDataViews();

        } catch (IllegalArgumentException ex) {
            showTradeMessage(ex.getMessage(), true);
        }
    }

    private void showTradeMessage(String msg, boolean isError) {
        lblTradeMessage.setText(msg);
        lblTradeMessage.setForeground(isError ? ThemeManager.getRed() : ThemeManager.getGreen());
        
        // Timer to clear message after 4 seconds
        Timer t = new Timer(4000, e -> lblTradeMessage.setText(" "));
        t.setRepeats(false);
        t.start();
    }

    private void handleLogout() {
        if (simulationTimer != null) {
            simulationTimer.stop();
        }
        ThemeManager.unregisterListener(this);
        this.dispose();
        logoutHandler.run();
    }

    // --- Dynamic Data Sync ---

    private void startSimulationTimer() {
        // Refresh market and prices every 4 seconds
        simulationTimer = new Timer(4000, e -> refreshMarketTick());
        simulationTimer.start();
        
        // Initial load
        refreshDataViews();
    }

    private void refreshMarketTick() {
        marketManager.simulateFluctuations();
        refreshDataViews();
    }

    private synchronized void refreshDataViews() {
        Portfolio portfolio = currentUser.getPortfolio();
        Map<String, Double> prices = marketManager.getPriceMap();

        // 1. Update Stat Labels
        double cash = portfolio.getBalance();
        double portfolioVal = portfolio.calculateTotalValue(prices);
        double pnl = portfolio.calculateTotalProfitLoss(prices);

        lblBalanceVal.setText(CURRENCY_FORMAT.format(cash));
        lblPortfolioVal.setText(CURRENCY_FORMAT.format(portfolioVal));

        if (pnl > 0.0) {
            lblGainLossVal.setText("+" + CURRENCY_FORMAT.format(pnl));
            lblGainLossVal.setForeground(ThemeManager.getGreen());
        } else if (pnl < 0.0) {
            lblGainLossVal.setText("-" + CURRENCY_FORMAT.format(Math.abs(pnl)));
            lblGainLossVal.setForeground(ThemeManager.getRed());
        } else {
            lblGainLossVal.setText(CURRENCY_FORMAT.format(0.0));
            lblGainLossVal.setForeground(ThemeManager.getSubtext());
        }

        // 2. Refill Market Watch table
        int selectedRowMarket = tblMarket.getSelectedRow();
        modelMarket.setRowCount(0);
        List<Stock> stockList = marketManager.getStocks();
        for (Stock stock : stockList) {
            modelMarket.addRow(new Object[]{
                stock.getSymbol(),
                stock.getCompanyName(),
                CURRENCY_FORMAT.format(stock.getCurrentPrice()),
                stock.getDailyChangePercent()
            });
        }
        if (selectedRowMarket >= 0 && selectedRowMarket < tblMarket.getRowCount()) {
            tblMarket.setRowSelectionInterval(selectedRowMarket, selectedRowMarket);
        }

        // 3. Refill Portfolio holdings table
        int selectedRowPortfolio = tblPortfolio.getSelectedRow();
        modelPortfolio.setRowCount(0);
        Map<String, Holding> holdings = portfolio.getHoldings();
        for (Holding holding : holdings.values()) {
            Double curPrice = prices.get(holding.getSymbol());
            double currentPriceVal = curPrice != null ? curPrice : holding.getAveragePurchasePrice();
            double marketValue = holding.getQuantity() * currentPriceVal;
            double holdingPnl = (currentPriceVal - holding.getAveragePurchasePrice()) * holding.getQuantity();

            modelPortfolio.addRow(new Object[]{
                holding.getSymbol(),
                holding.getQuantity(),
                CURRENCY_FORMAT.format(holding.getAveragePurchasePrice()),
                CURRENCY_FORMAT.format(currentPriceVal),
                CURRENCY_FORMAT.format(marketValue),
                holdingPnl
            });
        }
        if (selectedRowPortfolio >= 0 && selectedRowPortfolio < tblPortfolio.getRowCount()) {
            tblPortfolio.setRowSelectionInterval(selectedRowPortfolio, selectedRowPortfolio);
        }

        // 4. Refill History Table
        modelHistory.setRowCount(0);
        List<Transaction> txList = portfolio.getTransactions();
        // Insert in reverse chronological order
        for (int i = txList.size() - 1; i >= 0; i--) {
            Transaction tx = txList.get(i);
            double total = tx.getQuantity() * tx.getPrice();
            modelHistory.addRow(new Object[]{
                tx.getDateTime(),
                tx.getType().name(),
                tx.getSymbol(),
                tx.getQuantity(),
                CURRENCY_FORMAT.format(tx.getPrice()),
                CURRENCY_FORMAT.format(total)
            });
        }

        // 5. Dynamic order pricing update (e.g. if price changed under the cursor)
        updateEstTotal();
    }

    private void switchView(String cardName, JButton activeBtn) {
        contentCardLayout.show(contentPanel, cardName);

        // Reset nav button states
        resetSidebarButtonState(btnOverview);
        resetSidebarButtonState(btnMarket);
        resetSidebarButtonState(btnPortfolio);
        resetSidebarButtonState(btnHistory);

        // Set active button styles
        activeBtn.setBackground(ThemeManager.ACCENT);
        activeBtn.setForeground(Color.WHITE);
    }

    // --- Component Creation & Styling Customizers ---

    private JButton createSidebarButton(String text, String cardName) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                if (getBackground().equals(ThemeManager.ACCENT)) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(ThemeManager.ACCENT);
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                    g2.dispose();
                }
                super.paintComponent(g);
            }
        };
        btn.setFont(ThemeManager.FONT_BODY_BOLD);
        btn.setForeground(ThemeManager.getSubtext());
        btn.setBackground(new Color(0, 0, 0, 0));
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn.setPreferredSize(new Dimension(170, 38));
        btn.setMaximumSize(new Dimension(170, 38));

        btn.addActionListener(e -> switchView(cardName, btn));
        return btn;
    }

    private void resetSidebarButtonState(JButton btn) {
        btn.setBackground(new Color(0,0,0,0));
        btn.setForeground(ThemeManager.getSubtext());
    }

    private JButton createAccentButton(String text, boolean secondary) {
        Color baseColor = secondary ? ThemeManager.ACCENT_SECONDARY : ThemeManager.ACCENT;
        Color hoverColor = secondary ? ThemeManager.ACCENT_SECONDARY.darker() : ThemeManager.ACCENT_HOVER;

        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isPressed()) {
                    g2.setColor(hoverColor.darker());
                } else if (getModel().isRollover()) {
                    g2.setColor(hoverColor);
                } else {
                    g2.setColor(baseColor);
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
        button.setPreferredSize(new Dimension(140, 36));
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
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

                g2.setColor(ThemeManager.getText());
                int w = getWidth();
                int h = getHeight();
                if (tooltip.contains("Theme")) {
                    if (ThemeManager.isDarkMode()) {
                        g2.fillArc(w/4, h/4, w/2, h/2, -90, 180);
                        g2.setColor(ThemeManager.getTableHeaderBg());
                        g2.fillOval(w/3 + 1, h/4 + 1, w/2 - 2, h/2 - 2);
                    } else {
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
                } else {
                    // Circle + arrow refresh icon representation
                    g2.setStroke(new BasicStroke(2f));
                    g2.drawArc(w/4, h/4, w/2, h/2, 45, 270);
                    // Arrow head
                    int[] xPoints = {w/2, w/2 + 6, w/2 + 2};
                    int[] yPoints = {h/4 - 4, h/4 + 2, h/4 + 6};
                    g2.fillPolygon(xPoints, yPoints, 3);
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

    private JPanel createStatCard(String title) {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(ThemeManager.getCardBg());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.setColor(ThemeManager.getBorder());
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setLayout(new GridLayout(2, 1, 0, 5));
        card.setBorder(new EmptyBorder(15, 20, 15, 20));
        card.setPreferredSize(new Dimension(240, 90));

        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(ThemeManager.FONT_SMALL);
        titleLbl.setForeground(ThemeManager.getSubtext());

        JLabel valLbl = new JLabel("$0.00");
        valLbl.setFont(ThemeManager.FONT_TITLE);

        card.add(titleLbl);
        card.add(valLbl);
        card.putClientProperty("valLabel", valLbl);
        card.putClientProperty("titleLabel", titleLbl);

        return card;
    }

    private void styleJTable(JTable table) {
        table.setRowHeight(32);
        table.setFont(ThemeManager.FONT_BODY);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setFillsViewportHeight(true);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Customize header
        JTableHeader header = table.getTableHeader();
        header.setFont(ThemeManager.FONT_BODY_BOLD);
        header.setReorderingAllowed(false);
        header.setPreferredSize(new Dimension(header.getWidth(), 36));

        // Default cell align left for symbols, currency formats right-aligned
        DefaultTableCellRenderer cellRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean isSelected, boolean hasFocus, int r, int c) {
                Component comp = super.getTableCellRendererComponent(t, v, isSelected, hasFocus, r, c);
                setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));

                if (isSelected) {
                    comp.setBackground(ThemeManager.ACCENT.darker());
                    comp.setForeground(Color.WHITE);
                } else {
                    comp.setBackground(r % 2 == 0 ? ThemeManager.getCardBg() : ThemeManager.getPrimaryBg());
                    comp.setForeground(ThemeManager.getText());
                }
                return comp;
            }
        };
        table.setDefaultRenderer(Object.class, cellRenderer);
        table.setDefaultRenderer(Number.class, cellRenderer);
        table.setDefaultRenderer(Integer.class, cellRenderer);
    }

    // --- Custom Table Cell Renderers ---

    private class ChangePercentCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Component comp = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            setHorizontalAlignment(SwingConstants.RIGHT);
            setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));

            if (value instanceof Double) {
                double pct = (Double) value;
                String text = PERCENT_FORMAT.format(pct / 100.0);
                setText(text);

                if (!isSelected) {
                    if (pct > 0) {
                        comp.setForeground(ThemeManager.getGreen());
                    } else if (pct < 0) {
                        comp.setForeground(ThemeManager.getRed());
                    } else {
                        comp.setForeground(ThemeManager.getSubtext());
                    }
                }
            }
            return comp;
        }
    }

    private class PnlValueCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Component comp = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            setHorizontalAlignment(SwingConstants.RIGHT);
            setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));

            if (value instanceof Double) {
                double pnl = (Double) value;
                String text = (pnl >= 0 ? "+" : "-") + CURRENCY_FORMAT.format(Math.abs(pnl));
                setText(text);

                if (!isSelected) {
                    if (pnl > 0) {
                        comp.setForeground(ThemeManager.getGreen());
                    } else if (pnl < 0) {
                        comp.setForeground(ThemeManager.getRed());
                    } else {
                        comp.setForeground(ThemeManager.getSubtext());
                    }
                }
            }
            return comp;
        }
    }

    private class TransactionTypeCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Component comp = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            setHorizontalAlignment(SwingConstants.CENTER);
            setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));

            if (value instanceof String) {
                String type = (String) value;
                setText(type);

                if (!isSelected) {
                    if ("BUY".equalsIgnoreCase(type)) {
                        comp.setForeground(ThemeManager.getGreen());
                        setFont(ThemeManager.FONT_BODY_BOLD);
                    } else {
                        comp.setForeground(ThemeManager.getRed());
                        setFont(ThemeManager.FONT_BODY_BOLD);
                    }
                }
            }
            return comp;
        }
    }

    // --- Dynamic Theme Switching ---

    private void applyCurrentTheme() {
        Color primaryBg = ThemeManager.getPrimaryBg();
        Color cardBg = ThemeManager.getCardBg();
        Color text = ThemeManager.getText();
        Color subtext = ThemeManager.getSubtext();
        Color border = ThemeManager.getBorder();

        getContentPane().setBackground(primaryBg);
        sidebarPanel.setBackground(cardBg);
        sidebarPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, border));

        headerPanel.setBackground(cardBg);
        headerPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, border));

        // Update labels and controls in Sidebar
        for (Component child : sidebarPanel.getComponents()) {
            if (child instanceof JLabel) {
                child.setForeground(text);
            }
        }

        // Update labels in Header
        lblUserGreeting.setForeground(text);

        // Update card background states
        contentPanel.setBackground(primaryBg);

        // Update stat cards
        updateCardTheme(cardBalance, cardBg, border, text, subtext);
        updateCardTheme(cardPortfolioValue, cardBg, border, text, subtext);
        updateCardTheme(cardGainLoss, cardBg, border, null, subtext); // Pnl determines value label foreground

        // Update text fields border
        updateTradeFieldTheme(txtSymbol, cardBg, text, border);
        updateTradeFieldTheme(txtQuantity, cardBg, text, border);
        lblEstTotal.setForeground(text);

        // Style JTables & Headers
        styleJTableHeader(tblMarket);
        styleJTableHeader(tblPortfolio);
        styleJTableHeader(tblHistory);

        repaint();
    }

    private void updateCardTheme(JPanel card, Color bg, Color border, Color valColor, Color titleColor) {
        if (card == null) return;
        JLabel titleLbl = (JLabel) card.getClientProperty("titleLabel");
        JLabel valLbl = (JLabel) card.getClientProperty("valLabel");

        if (titleLbl != null) titleLbl.setForeground(titleColor);
        if (valLbl != null && valColor != null) valLbl.setForeground(valColor);
    }

    private void updateTradeFieldTheme(JTextField field, Color bg, Color fg, Color border) {
        if (field == null) return;
        field.setBackground(bg);
        field.setForeground(fg);
        field.setCaretColor(fg);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(border, 1, true),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
    }

    private void styleJTableHeader(JTable table) {
        if (table == null) return;
        JTableHeader header = table.getTableHeader();
        header.setBackground(ThemeManager.getTableHeaderBg());
        header.setForeground(ThemeManager.getText());
        header.setBorder(new LineBorder(ThemeManager.getBorder()));
    }

    @Override
    public void onThemeChanged() {
        SwingUtilities.invokeLater(this::applyCurrentTheme);
    }
}
