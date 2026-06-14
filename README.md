# Apex Stock Trader - Swing Desktop Application

A complete, premium Stock Trading Simulation System built with **Java Swing** using a custom design language (supporting dynamic Light and Dark modes) and without any external frameworks, Maven, or Gradle.

## Key Features

1. **User Authentication**: Register new accounts, encrypt password hashes locally, and log in securely.
2. **Real-time Price Simulation**: Automatic price updates every 4 seconds mimicking real market fluctuations (+/- 2%).
3. **Responsive GUI Dashboard**:
   - **Market Watch**: Interactive table with visual indicators for gains (green) and losses (red). Fills order form on row click.
   - **Portfolio holdings**: Dynamic calculations for average cost basis, current market value, and unrealized profit/loss.
   - **Transaction logs**: Reverse chronological transaction audit history.
   - **Account summary**: Cash balance and portfolio value cards.
4. **Dynamic Light & Dark Theme Toggling**: Change UI appearance instantly with unified, premium color schemes using an observer pattern.
5. **Local Data Persistence**: Saves user credentials, cash balances, current holdings, and transaction logs in custom parsed CSV files.

---

## OOP Design Application

- **Encapsulation**: All models (`User`, `Stock`, `Holding`, `Transaction`, `Portfolio`) hide internal details behind `private` fields and expose thread-safe, `synchronized` accessors (getters/setters).
- **Inheritance & Polymorphism**: Used heavily in custom Swing UI elements. Custom table renderers (e.g. `ChangePercentCellRenderer`, `PnlValueCellRenderer`) inherit from Swing's `DefaultTableCellRenderer` and override rendering to format percentages, currencies, and colors dynamically.
- **Abstraction**: Abstracted database interfaces and theme notification systems (`ThemeChangeListener`).
- **Collections**: Leveraged `LinkedHashMap` for ordered holdings mappings, `HashMap` for user databases, and `ArrayList` for transactional histories.

---

## Project Structure

```
StockTradingPlatform/
├── .vscode/
│   └── launch.json                # VS Code launch configuration
├── src/
│   └── com/
│       └── stockplatform/
│           ├── Main.java          # Entry point and lifecyle hook
│           ├── model/
│           │   ├── User.java          # User credentials & state
│           │   ├── Stock.java         # Live stock model
│           │   ├── Holding.java       # User share holding representation
│           │   ├── Transaction.java   # Trade record
│           │   └── Portfolio.java     # User balance & asset management
│           ├── service/
│           │   ├── MarketManager.java  # Market listing & pricing updates
│           │   └── FileManager.java    # Text CSV serializer/deserializer
│           └── ui/
│               ├── ThemeManager.java   # Centralized theme definitions
│               ├── LoginFrame.java     # Login/Register Card GUI
│               └── DashboardFrame.java # Core Trading Console GUI
├── data/                          # Automatically generated storage
│   ├── users.csv                  
│   ├── portfolios.csv             
│   └── transactions.csv           
└── README.md                      # Documentation
```

---

## How to Run in VS Code

### Prerequisites
- **Java Development Kit (JDK)**: Version 11 or higher.
- **VS Code Extensions**:
  - `Extension Pack for Java` (by Microsoft)

### Execution Steps
1. Open VS Code and open the **`StockTradingPlatform`** directory as your workspace.
2. In the bottom-left/Run panel or using `F5`, select **`Launch StockTradingPlatform`** from the Run configurations dropdown.
3. Press **`F5`** (or click Run/Debug) to compile and launch the desktop application.
4. If running from command prompt, run:
   ```bash
   # From the project root
   javac -d bin -sourcepath src src/com/stockplatform/Main.java
   java -cp bin com.stockplatform.Main
   ```

---

## Screenshots

*(Screenshots placeholder section)*
## Screenshots

| Login Screen | Dashboard |
|--------------|-----------|
| ![](screenshots/login.png) | ![](screenshots/dashboard.png) |

| Portfolio | Transactions |
|-----------|-------------|
| ![](screenshots/portfolio.png) | ![](screenshots/transactions.png) |
