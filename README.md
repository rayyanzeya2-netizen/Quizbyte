# Quizbyte - Academic Desktop Online Quiz Platform (Java Swing & MySQL)

Quizbyte is an enterprise-grade, desktop-based online quiz platform built using **Java 17**, **Java Swing (`javax.swing.*`)**, **Maven**, and **MySQL** via standard **JDBC**. 

The application is architected strictly around clean **Object-Oriented Programming (OOP)**, **Generics**, **Multithreading & Synchronization**, and the **DAO (Data Access Object) Pattern**, meeting every criterion of university software evaluation rubrics.

---

## 👥 Three-Role Platform Architecture

Quizbyte supports a full 3-role authorization model:

| Role | Responsibilities & Capabilities |
| :--- | :--- |
| **Administrator (`ADMIN`)** | Manages system users (role changes, account suspension/activation, deletion), reviews the **Content Approval Queue** (approving or rejecting question submissions from creators), inspects **Global Performance Reports** with aggregated metrics, and manages **System Settings**. |
| **Quiz Creator (`QUIZ_CREATOR`)** | Authors questions with custom options and per-question duration timers, tracks approval statuses (**PENDING**, **APPROVED**, **REJECTED**), and responds to inquiries from participants. |
| **Participant (`PARTICIPANT`)** | Takes quizzes on approved subjects, tracks personal score history with letter grades, communicates with quiz creators via inquiry messaging, schedules study reminders, and competes on the global leaderboard. |

---

## 📊 Rubric Compliance Matrix

This matrix maps each academic marking criterion directly to the codebase implementation:

| Rubric Criterion | Marks | Implementation Details | Key Classes & Methods |
| :--- | :---: | :--- | :--- |
| **Interfaces & Generic Abstraction** | 3 | Generic CRUD interface defining contracts across domain entities | [`BaseDAO<T>`](file:///c:/Users/rayya/OneDrive/Documents/Quizbyte/src/main/java/com/quizbyte/dao/BaseDAO.java)<br>Implemented by `UserDAO`, `QuestionDAO`, `ResultDAO`, `MessageDAO`, `ReminderDAO` |
| **Inheritance & Polymorphism** | 4 | Abstract base class with concrete subclasses and polymorphic methods | Abstract [`Person`](file:///c:/Users/rayya/OneDrive/Documents/Quizbyte/src/main/java/com/quizbyte/model/Person.java)<br>Subclass [`User`](file:///c:/Users/rayya/OneDrive/Documents/Quizbyte/src/main/java/com/quizbyte/model/User.java)<br>Polymorphic `displayRoleBadge()` & `getRoleBadge()` across all 3 roles |
| **Custom Exception Hierarchy** | 3 | Checked exception hierarchy separating business and technical errors | [`QuizbyteException`](file:///c:/Users/rayya/OneDrive/Documents/Quizbyte/src/main/java/com/quizbyte/exception/QuizbyteException.java)<br>[`AuthenticationException`](file:///c:/Users/rayya/OneDrive/Documents/Quizbyte/src/main/java/com/quizbyte/exception/AuthenticationException.java) (including suspended accounts)<br>[`DatabaseOperationException`](file:///c:/Users/rayya/OneDrive/Documents/Quizbyte/src/main/java/com/quizbyte/exception/DatabaseOperationException.java)<br>[`QuestionNotFoundException`](file:///c:/Users/rayya/OneDrive/Documents/Quizbyte/src/main/java/com/quizbyte/exception/QuestionNotFoundException.java) |
| **Collections & Generics** | 6 | Use of Generic classes, `List`, `Map`, `Set`, and `Collections.shuffle()` | Generic [`ApiResponse<T>`](file:///c:/Users/rayya/OneDrive/Documents/Quizbyte/src/main/java/com/quizbyte/util/ApiResponse.java)<br>`List<Question>` (Sequencing in `QuizService`)<br>`Map<Integer, Character>` (Answers in `QuizPanel`)<br>`Set<String>` (Distinct approved subjects in `QuestionDAO`)<br>`Collections.shuffle()` (Randomization in `QuestionDAO`) |
| **Multithreading & Worker Threads** | 2 | Dedicated background timer thread pool for countdowns | [`QuizTimerService`](file:///c:/Users/rayya/OneDrive/Documents/Quizbyte/src/main/java/com/quizbyte/service/QuizTimerService.java)<br>Dispatches ticks safely via `SwingUtilities.invokeLater()` |
| **Thread Safety & Synchronization** | 2 | Concurrency control preventing race conditions on score writes | [`ThreadSafeLeaderboardCache`](file:///c:/Users/rayya/OneDrive/Documents/Quizbyte/src/main/java/com/quizbyte/util/ThreadSafeLeaderboardCache.java)<br>`AtomicInteger activeQuizCounter`<br>`ReentrantReadWriteLock` for cache synchronization |
| **Database DAO Layer (Modular)** | 7 | Pure DAO separation encapsulating SQL queries | [`UserDAO`](file:///c:/Users/rayya/OneDrive/Documents/Quizbyte/src/main/java/com/quizbyte/dao/UserDAO.java), [`QuestionDAO`](file:///c:/Users/rayya/OneDrive/Documents/Quizbyte/src/main/java/com/quizbyte/dao/QuestionDAO.java), [`ResultDAO`](file:///c:/Users/rayya/OneDrive/Documents/Quizbyte/src/main/java/com/quizbyte/dao/ResultDAO.java), [`MessageDAO`](file:///c:/Users/rayya/OneDrive/Documents/Quizbyte/src/main/java/com/quizbyte/dao/MessageDAO.java), [`ReminderDAO`](file:///c:/Users/rayya/OneDrive/Documents/Quizbyte/src/main/java/com/quizbyte/dao/ReminderDAO.java) |
| **JDBC Implementation & Security** | 3 | Parameterized queries and automatic connection closure | `PreparedStatement` on all SQL queries (zero SQL Injection risk)<br>`try-with-resources` on all Connections, Statements, ResultSets<br>Centralized [`DatabaseConfig`](file:///c:/Users/rayya/OneDrive/Documents/Quizbyte/src/main/java/com/quizbyte/config/DatabaseConfig.java) |

---

## 🌟 Application Features

- **Modern Desktop GUI (Java Swing):**
  - Native look & feel integration (`UIManager.getSystemLookAndFeelClassName()`).
  - Cohesive design system ([`UITheme`](file:///c:/Users/rayya/OneDrive/Documents/Quizbyte/src/main/java/com/quizbyte/ui/UITheme.java)) featuring slate, royal blue, emerald, and crimson accents.
  - Seamless, single-frame view transitions using `CardLayout` in [`MainFrame`](file:///c:/Users/rayya/OneDrive/Documents/Quizbyte/src/main/java/com/quizbyte/ui/MainFrame.java).
  - Multi-threaded responsiveness with `SwingWorker` ensuring zero UI freezing during database operations.
- **Participant Dashboard:**
  - Dynamic subject picker filtered to **Approved** topics only with real-time question count badges.
  - One-click quiz launch with progress tracking.
  - Interactive table of past quiz attempts with timestamps, percentage, and letter grades.
  - **Creator Inquiries:** Send direct messages to quiz creators and view received replies.
  - **Study Reminders Planner:** Add target study dates and study notes with deletion support.
  - Real-time global leaderboard ranking top scores.
- **Quiz Creator Dashboard:**
  - **Author Questions:** Submit questions with custom 4 options, correct answer, and per-question duration (default 30s).
  - **Status Tracker:** Live table of authored questions showing status (**PENDING**, **APPROVED**, **REJECTED**).
  - **Participant Inquiries:** Review incoming questions and submit direct replies.
- **Administrator Dashboard:**
  - **User Management:** View all users, change user roles, toggle account status (`ACTIVE` &harr; `SUSPENDED`), and delete users.
  - **Content Approval Queue:** Review creator-submitted questions and click **Approve** or **Reject**.
  - **Global Performance Reports:** Aggregated platform metrics (Total Attempts, Average Score %, Question Count) plus full audit score history.
  - **System Settings:** Configure platform parameters such as default time limit and maintenance mode.
- **Interactive Quiz Runner:**
  - Dynamic live countdown timer per question with color warnings (Blue &rarr; Amber &rarr; Red).
  - Linear visual progress bar (`JProgressBar`).
  - Radio options [A], [B], [C], [D] mapped using Java `Map<Integer, Character>`.
  - Automatic advance on timeout.
- **Scorecard & Results:**
  - Dynamic grade badge indicator (`A+`, `A`, `B`, `C`, `D`, `F`).
  - Statistical summary card with score breakdown and contextual performance evaluation.
  - Instant quiz retake action.

---

## 🏗️ Project Directory Structure

```
Quizbyte/
├── pom.xml                                      # Maven dependencies & build configuration
├── README.md                                    # Comprehensive academic documentation
├── .gitignore                                   # Git exclusions
├── database/
│   └── schema.sql                               # Database DDL schema, tables & seed records
├── docs/
│   └── Quizbyte_Project_Presentation.md         # Evaluation presentation slide deck
└── src/
    └── main/
        └── java/
            └── com/
                └── quizbyte/
                    ├── Main.java                # GUI application entry point (Swing EDT)
                    ├── config/
                    │   └── DatabaseConfig.java  # Centralized JDBC connection manager
                    ├── model/                   # Domain entities & OOP hierarchy
                    │   ├── Person.java          # Abstract base class
                    │   ├── User.java            # Concrete user extending Person (3 roles)
                    │   ├── Question.java        # Multiple-choice question with approval status
                    │   ├── QuizResult.java      # Score attempt entity & grade calculation
                    │   ├── Message.java         # Inquiries between participants & creators
                    │   └── QuizReminder.java    # Study reminders planner entity
                    ├── dao/                     # Data Access Objects & Generic Interface
                    │   ├── BaseDAO.java         # Generic BaseDAO<T> CRUD contract
                    │   ├── UserDAO.java         # BaseDAO<User> implementation
                    │   ├── QuestionDAO.java     # BaseDAO<Question> implementation
                    │   ├── ResultDAO.java       # BaseDAO<QuizResult> implementation
                    │   ├── MessageDAO.java      # BaseDAO<Message> implementation
                    │   ├── ReminderDAO.java     # BaseDAO<QuizReminder> implementation
                    │   └── SystemSettingsDAO.java# Key-value system settings manager
                    ├── exception/               # Checked Exception Hierarchy
                    │   ├── QuizbyteException.java
                    │   ├── AuthenticationException.java
                    │   ├── DatabaseOperationException.java
                    │   └── QuestionNotFoundException.java
                    ├── service/                 # Business logic & background schedulers
                    │   ├── AuthService.java     # Session tracking, roles, & suspension check
                    │   ├── QuizService.java     # Quiz sequencing & scoring orchestration
                    │   └── QuizTimerService.java# Dedicated timer thread pool (ScheduledExecutor)
                    ├── util/                    # Generics & Concurrency Utilities
                    │   ├── ApiResponse.java     # Generic ApiResponse<T> wrapper
                    │   └── ThreadSafeLeaderboardCache.java # AtomicInteger & ReentrantReadWriteLock
                    └── ui/                      # Modern Java Swing Presentation Layer
                        ├── UITheme.java         # Visual styles, palettes & component builders
                        ├── MainFrame.java       # Root window with CardLayout view switcher
                        ├── LoginPanel.java      # Sign-in & registration card (3-role support)
                        ├── StudentDashboardPanel.java # Participant quizzes, inquiries & reminders
                        ├── QuizCreatorDashboardPanel.java # Creator authoring, approval & inquiries
                        ├── AdminDashboardPanel.java # User management, content approval & reports
                        ├── QuizPanel.java       # Live timer, question text area & radio choices
                        └── ResultSummaryPanel.java   # Score summary & grade badge display
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
Ensure your MySQL service is running (e.g. `MySQL80`), then execute the database schema:

```bash
# Using MySQL Command Line Client:
mysql -u root -p < database/schema.sql
```

**Default Seed Credentials:**
| Role | Username | Password | Email |
| :--- | :--- | :--- | :--- |
| **Admin** | `admin` | `admin123` | `admin@quizbyte.com` |
| **Quiz Creator** | `creator1` | `creator123` | `creator1@quizbyte.com` |
| **Participant** | `participant1` | `pass123` | `participant1@quizbyte.com` |
| **Participant** | `student1` | `pass123` | `student1@quizbyte.com` |

---

### Step 2: Configure Database Credentials

The credentials in [`DatabaseConfig.java`](file:///c:/Users/rayya/OneDrive/Documents/Quizbyte/src/main/java/com/quizbyte/config/DatabaseConfig.java) can be adjusted directly or overridden via environment variables:

```powershell
$env:DB_USER = "root"
$env:DB_PASSWORD = "your_mysql_password"
```

---

### Step 3: Build & Compile

Verify and compile the project using Maven:

```bash
mvn clean compile
```

---

### Step 4: Run the Application

Launch the desktop application directly:

```bash
mvn exec:java
```

---

## 📑 Presentation Slide Deck

A presentation outline is included in [`docs/Quizbyte_Project_Presentation.md`](file:///c:/Users/rayya/OneDrive/Documents/Quizbyte/docs/Quizbyte_Project_Presentation.md), covering:
1. Title & Tech Stack
2. Problem Statement & Objectives
3. System Architecture & 3-Role Specification
4. Database Design & Mermaid ER Diagram
5. Rubric Highlights: OOP, Generics & Collections
6. Rubric Highlights: Multithreading & Synchronization
7. GUI Walkthrough: Admin, Creator, and Participant Dashboards
8. Conclusion & Future Scope
