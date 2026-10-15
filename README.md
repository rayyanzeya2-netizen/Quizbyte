# Quizbyte - Java Online Quiz Platform

Quizbyte is an enterprise-grade, console-based online quiz platform built using **Java 17**, **Maven**, and **MySQL** via standard **JDBC**. It follows the industry-standard **DAO (Data Access Object) Pattern**, strict **Object-Oriented Programming (OOP)** principles, and clean separation of concerns.

---

## 🌟 Key Features

- **Role-Based Access Control (RBAC):**
  - **Student Portal:**
    - Browse available quiz subjects (e.g., Java, Object-Oriented Programming, Database Management Systems).
    - Take interactive, randomized quizzes with immediate feedback and scoring.
    - Automated grade calculation (`A+`, `A`, `B`, `C`, `D`, `F`) and performance evaluation remarks.
    - View personal historical quiz attempts with timestamps.
    - View the global real-time leaderboard.
  - **Administrator Portal:**
    - Add new multiple-choice questions dynamically across any subject.
    - View and inspect all registered system users and their roles.
    - Audit global score history and student performance records.
- **Robust Security & Data Integrity:**
  - Standard JDBC `PreparedStatement` utilized in every query to eliminate SQL injection vulnerabilities.
  - Automatic resource management using Java `try-with-resources`.
  - Referential integrity with foreign keys and cascade deletion.
- **Resilient CLI Experience:**
  - Rich ASCII art banners and clean table formatting.
  - Crash-proof input parsing using dedicated scanner input validators.
  - Automatic startup database connectivity diagnostics.

---

## 🏗️ Architecture & Directory Structure

Quizbyte adheres to a clean layered architecture:

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
                    ├── Main.java         # CLI presentation & menu routing
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
                    └── service/          # Business logic layer
                        ├── AuthService.java
                        └── QuizService.java
```

---

## 🛠️ Prerequisites & Tech Stack

- **Java Development Kit (JDK):** Version 17 or higher
- **Apache Maven:** Version 3.8+
- **MySQL Server:** Version 8.0+
- **JDBC Driver:** `com.mysql:mysql-connector-j:8.3.0`

---

## 🚀 Setup & Execution Guide

### Step 1: Initialize MySQL Database
Make sure your MySQL service is active (e.g. `MySQL80`), then open MySQL CLI or MySQL Workbench and run `database/schema.sql`:

```bash
# Using MySQL Command Line Client:
mysql -u root -p < database/schema.sql
```

This will:
1. Create the `quiz_db` database.
2. Create the `users`, `questions`, and `quiz_results` tables with constraints.
3. Seed the initial admin account, student account, and question bank.

**Default Seed Credentials:**
| Role | Username | Password |
| :--- | :--- | :--- |
| **Admin** | `admin` | `admin123` |
| **Student** | `student1` | `pass123` |

---

### Step 2: Configure Database Credentials

The default credentials in `src/main/java/com/quizbyte/config/DatabaseConfig.java` are:
- **URL:** `jdbc:mysql://localhost:3306/quiz_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC`
- **Username:** `root`
- **Password:** `root`

If your local MySQL root password is different, either:
1. Edit `DEFAULT_PASSWORD` in `DatabaseConfig.java`, **OR**
2. Set runtime environment variables without touching the code:
   ```powershell
   $env:DB_USER = "root"
   $env:DB_PASSWORD = "your_mysql_password"
   ```

---

### Step 3: Build & Compile with Maven

Verify and compile the project:

```bash
mvn clean test-compile
```

---

### Step 4: Run the Application

Launch Quizbyte using the Maven Exec plugin:

```bash
mvn exec:java
```

Alternatively, compile and package into a JAR:
```bash
mvn clean package
java -cp "target/quizbyte-1.0-SNAPSHOT.jar;target/dependency/*" com.quizbyte.Main
```

---

## 🎓 Academic Evaluation Highlights

1. **Object-Oriented Programming (OOP) Principles:**
   - **Encapsulation:** Model fields are private with accessors/mutators (`User`, `Question`, `QuizResult`).
   - **Separation of Concerns:** Business logic (`service`), persistence logic (`dao`), models (`model`), and UI runner (`Main`) are strictly decoupled.
   - **Polymorphism & Dynamic Dispatch:** Used across standard interfaces, switch expressions, and Java Collections framework.
2. **Database Best Practices:**
   - Explicit database transaction handling and automatic resource closure (`try-with-resources`).
   - Prevention of SQL Injection attacks via parameterized queries (`PreparedStatement`).
   - Relational integrity with Foreign Keys and `ON DELETE CASCADE`.
3. **Defensive Programming:**
   - Full input sanitization preventing application crashes on improper numeric or character CLI input.
   - Graceful offline detection if MySQL database is not reachable.
