# Quizbyte - Interactive Desktop Quiz Platform (Java Swing GUI)

Quizbyte is an enterprise-grade, desktop-based online quiz platform built using **Java 17**, **Java Swing (`javax.swing.*`)**, **Maven**, and **MySQL** via standard **JDBC**. It follows the industry-standard **DAO (Data Access Object) Pattern**, strict **Object-Oriented Programming (OOP)** principles, and clean separation of concerns.

---

## 🌟 Key Features

- **Modern Graphical User Interface (Java Swing):**
  - Native OS Look & Feel integration (`UIManager.getSystemLookAndFeelClassName()`).
  - Cohesive design system (`UITheme`) with slate (`#1E293B`), royal blue (`#2563EB`), and emerald green (`#10B981`) color palettes.
  - Seamless, single-window transitions via `CardLayout` without cluttering popup windows.
  - Multi-threaded responsiveness using `SwingWorker` for non-blocking asynchronous database operations.
- **Role-Based Portals:**
  - **Authentication Screen (`LoginPanel`):**
    - Sign In with username, password, and role picker (Student / Admin).
    - Integrated registration toggle to create new student accounts with instant validation.
  - **Student Portal (`StudentDashboardPanel`):**
    - Subject selection dropdown (Java, OOP, DBMS, etc.) with real-time question count badges.
    - One-click quiz launcher.
    - Historical quiz attempts table (`JTable`) displaying scores, percentages, grades, and timestamps.
    - Real-time global leaderboard table (`JTable`) ranking top scores across all students.
  - **Interactive Quiz Runner (`QuizPanel`):**
    - Subject header with visual linear progress bar (`JProgressBar`).
    - Live countdown timer (30s per question) with dynamic color warning (Green &rarr; Amber &rarr; Red).
    - Word-wrapped question prompts and stylized option radio buttons [A], [B], [C], [D].
    - Automatic progression on timer expiration or user submission.
  - **Scorecard & Results (`ResultSummaryPanel`):**
    - Dynamic performance badge displaying computed letter grade (`A+`, `A`, `B`, `C`, `D`, `F`).
    - Stat cards for score breakdown, accuracy percentage, and contextual feedback messages.
    - Options to retake the quiz immediately or return to the student dashboard.
  - **Administrator Portal (`AdminDashboardPanel`):**
    - Tab 1: Interactive form to create and add new multiple-choice questions into the question bank.
    - Tab 2: System users registry table (`JTable`) listing all registered accounts and roles.
    - Tab 3: Global score audit table (`JTable`) tracking all quiz attempts across the platform.
- **Robust Security & Data Integrity:**
  - Parameterized queries (`PreparedStatement`) in all DAO layers to prevent SQL injection.
  - Automatic resource closure with `try-with-resources`.
  - Referential integrity with foreign keys and cascade deletion on `quiz_results`.

---

## 🏗️ Architecture & Directory Structure

```
Quizbyte/
├── pom.xml                               # Maven project configuration & dependencies
├── README.md                             # Project documentation & run guide
├── .gitignore                            # Git exclusions
├── database/
│   └── schema.sql                        # DDL schema, tables & seed records
└── src/
    └── main/
        └── java/
            └── com/
                └── quizbyte/
                    ├── Main.java         # Application entry point launching Swing GUI
                    ├── config/
                    │   └── DatabaseConfig.java # Centralized JDBC connection manager
                    ├── model/            # Plain Old Java Objects (POJO) / Entities
                    │   ├── User.java
                    │   ├── Question.java
                    │   └── QuizResult.java
                    ├── dao/              # Data Access Objects (SQL queries & JDBC)
                    │   ├── UserDAO.java
                    │   ├── QuestionDAO.java
                    │   └── ResultDAO.java
                    ├── service/          # Business logic layer
                    │   ├── AuthService.java
                    │   └── QuizService.java
                    └── ui/               # Modern Swing Desktop GUI Layer
                        ├── UITheme.java              # Styling constants, colors & component factories
                        ├── MainFrame.java            # Master JFrame with CardLayout view switcher
                        ├── LoginPanel.java           # Authentication & student registration
                        ├── StudentDashboardPanel.java# Subject pick, past attempts & leaderboard
                        ├── QuizPanel.java            # Live question runner, radio inputs & timer
                        ├── ResultSummaryPanel.java   # Scorecard, grade badges & retake controls
                        └── AdminDashboardPanel.java  # Question editor, user audit & global history
```

---

## 🛠️ Prerequisites & Tech Stack

- **Java Development Kit (JDK):** Version 17 or higher
- **UI Framework:** Java Swing (`javax.swing.*`) & AWT (`java.awt.*`)
- **Build Tool:** Apache Maven 3.8+
- **Database:** MySQL Server 8.0+
- **JDBC Driver:** `com.mysql:mysql-connector-j:8.3.0`

---

## 🚀 Setup & Execution Guide

### Step 1: Initialize MySQL Database
Ensure your MySQL service is active (e.g. `MySQL80`), then run the database setup script:

```bash
# Using MySQL Command Line Client:
mysql -u root -p < database/schema.sql
```

**Default Seed Credentials:**
| Role | Username | Password |
| :--- | :--- | :--- |
| **Admin** | `admin` | `admin123` |
| **Student** | `student1` | `pass123` |

---

### Step 2: Configure Database Credentials

Default credentials in `DatabaseConfig.java`:
- **URL:** `jdbc:mysql://localhost:3306/quiz_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC`
- **Username:** `root`
- **Password:** `root`

Override credentials anytime via environment variables:
```powershell
$env:DB_USER = "root"
$env:DB_PASSWORD = "your_mysql_password"
```

---

### Step 3: Build & Compile

```bash
mvn clean compile
```

---

### Step 4: Run Desktop GUI Application

Launch the desktop GUI window directly using Maven:

```bash
mvn exec:java
```

---

## 🎓 Academic Evaluation Highlights

1. **Object-Oriented Programming (OOP) Principles:**
   - **Encapsulation:** Pure POJO models with private fields and public accessors/mutators.
   - **Layered Architecture:** Strict separation between Presentation (`ui`), Business Logic (`service`), Data Access (`dao`), and Domain Models (`model`).
   - **Event-Driven Programming:** Robust listener pattern implementations (`ActionListener`, `DocumentListener`, `MouseAdapter`) driving Swing UI interactions.
2. **Concurrency & Thread Safety:**
   - Initialization and rendering dispatched safely on the **Event Dispatch Thread (EDT)** via `SwingUtilities.invokeLater`.
   - Long-running database operations offloaded to background threads using `SwingWorker` to ensure a 60 FPS, non-blocking UI.
3. **Database Best Practices:**
   - Parameterized SQL execution via `PreparedStatement` ensuring absolute protection against SQL Injection.
   - Connection lifecycle management with automatic resource cleanup (`try-with-resources`).
