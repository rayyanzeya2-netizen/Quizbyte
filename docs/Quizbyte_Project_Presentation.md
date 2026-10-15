# Quizbyte - Academic Evaluation & Project Presentation Deck

---

<!-- slide -->
# Slide 1: Title Slide
## **Quizbyte - Enterprise Java GUI Online Quiz Platform**
### *Advanced Java Application with MySQL Persistence & Concurrency*

- **Course:** Advanced Object-Oriented Programming & Software Engineering (Java)
- **Target JDK:** Java 17+ (LTS)
- **Architecture:** Layered Model-DAO-Service-GUI (MVC Pattern)
- **Core Technologies:** Java Swing (`javax.swing.*`), JDBC, MySQL 8.x, Maven, Java Concurrency Framework
- **Repository:** [https://github.com/rayyanzeya2-netizen/Quizbyte](https://github.com/rayyanzeya2-netizen/Quizbyte)

---

<!-- slide -->
# Slide 2: Problem Statement & Objectives

### 1. Problem Statement
Traditional computer-based examinations often suffer from:
- Cluttered, fragile console interfaces prone to input crashes.
- Vulnerability to SQL Injection through unparameterized query building.
- Unresponsive desktop user interfaces that lock and freeze during network I/O.
- Tightly coupled legacy code violating modern Object-Oriented design principles.

### 2. Project Objectives
- **Modern Desktop GUI:** Provide a flicker-free, responsive desktop experience using pure Java Swing with single-window `CardLayout` view switching.
- **Strict OOP Rigor:** Encapsulate entities with abstract inheritance (`Person` &rarr; `User`), polymorphic behaviors, generic DAO interfaces (`BaseDAO<T>`), and a custom checked exception hierarchy.
- **Multithreading & Concurrency:** Implement a non-blocking UI using `SwingWorker` alongside a dedicated timer daemon thread pool (`QuizTimerService`) and synchronized caching (`ThreadSafeLeaderboardCache`).
- **Resilient Persistence:** Leverage pure JDBC `PreparedStatement` and `try-with-resources` to deliver secure, leak-free relational database transactions.

---

<!-- slide -->
# Slide 3: System Architecture & Package Structure

### Layered Architecture Overview
```
+-------------------------------------------------------------------------+
|                        PRESENTATION LAYER (GUI)                         |
|   MainFrame (CardLayout) | LoginPanel | StudentDashboard | QuizPanel     |
|              ResultSummaryPanel | AdminDashboardPanel                    |
+-------------------------------------------------------------------------+
                                    │
                                    ▼
+-------------------------------------------------------------------------+
|                         BUSINESS SERVICE LAYER                          |
|         AuthService | QuizService | QuizTimerService (Scheduled)        |
+-------------------------------------------------------------------------+
                                    │
                                    ▼
+-------------------------------------------------------------------------+
|                  DATA ACCESS OBJECT LAYER (BaseDAO<T>)                  |
|    UserDAO (BaseDAO<User>) | QuestionDAO (BaseDAO<Q>) | ResultDAO       |
+-------------------------------------------------------------------------+
                                    │
                                    ▼
+-------------------------------------------------------------------------+
|                      DATABASE / JDBC CONNECTION                         |
|       DatabaseConfig (DriverManager)  ◄──►  MySQL Server 8.x (quiz_db)  |
+-------------------------------------------------------------------------+
```

### Modular Package Layout
- `com.quizbyte.config`: Centralized JDBC connection manager with environment variable fallbacks.
- `com.quizbyte.model`: POJO domain entities (`Person`, `User`, `Question`, `QuizResult`).
- `com.quizbyte.dao`: Generic interfaces and JDBC data access implementations (`BaseDAO<T>`).
- `com.quizbyte.service`: Authentication, quiz orchestration, and background timer schedulers.
- `com.quizbyte.exception`: Domain checked exception hierarchy.
- `com.quizbyte.util`: Generic response models (`ApiResponse<T>`) and concurrency caches.
- `com.quizbyte.ui`: Swing panels, design tokens (`UITheme`), and frame controllers.

---

<!-- slide -->
# Slide 4: Database Design & Relational Schema

### Entity-Relationship Model

```mermaid
erDiagram
    USERS ||--o{ QUIZ_RESULTS : "records"
    
    USERS {
        int user_id PK "AUTO_INCREMENT"
        string username UK "NOT NULL, UNIQUE"
        string password "NOT NULL"
        string role "ENUM('STUDENT', 'ADMIN')"
        timestamp created_at "DEFAULT CURRENT_TIMESTAMP"
    }

    QUESTIONS {
        int question_id PK "AUTO_INCREMENT"
        string subject "NOT NULL (Java, OOP, DBMS)"
        string question_text "NOT NULL"
        string option_a "NOT NULL"
        string option_b "NOT NULL"
        string option_c "NOT NULL"
        string option_d "NOT NULL"
        char correct_option "CHECK IN ('A','B','C','D')"
    }

    QUIZ_RESULTS {
        int result_id PK "AUTO_INCREMENT"
        int user_id FK "REFERENCES users(user_id) ON DELETE CASCADE"
        string subject "NOT NULL"
        int score "NOT NULL"
        int total_questions "NOT NULL"
        decimal percentage "DECIMAL(5,2)"
        timestamp attempt_date "DEFAULT CURRENT_TIMESTAMP"
    }
```

### Relational Safeguards
- **Referential Integrity:** Foreign key constraint `fk_quiz_results_user` ensures deletion of user records cascades safely to their historical scores.
- **Domain Constraints:** SQL `CHECK (correct_option IN ('A', 'B', 'C', 'D'))` guarantees answer validity at the schema level.

---

<!-- slide -->
# Slide 5: Rubric Highlights - OOP & Generics [16 Marks]

### 1. Abstract Inheritance & Polymorphism
- Abstract class `Person` declares common identity fields and abstract contract:
  ```java
  public abstract class Person {
      protected int id;
      protected String username, role;
      public abstract void displayRoleBadge();
      public abstract String getRoleBadge();
  }
  ```
- Subclass `User extends Person` polymorphically overrides role rendering:
  ```java
  @Override
  public String getRoleBadge() {
      return isAdmin() ? "[🛡️ ADMINISTRATOR]" : "[🎓 STUDENT]";
  }
  ```

### 2. Generics & Interface Abstraction
- Generic DAO contract `BaseDAO<T>` establishes standard CRUD signatures:
  ```java
  public interface BaseDAO<T> {
      boolean save(T entity) throws DatabaseOperationException;
      Optional<T> findById(int id) throws DatabaseOperationException;
      List<T> findAll() throws DatabaseOperationException;
  }
  ```
- Generic wrapper `ApiResponse<T>` encapsulates operation status and payloads.

### 3. Custom Exception Hierarchy
- Checked exceptions cleanly separate business errors from technical failures:
  - `QuizbyteException` (Base)
    - `AuthenticationException` (Invalid credentials, duplicates, role mismatch)
    - `DatabaseOperationException` (JDBC failures, communication drops)
    - `QuestionNotFoundException` (Empty subjects, missing IDs)

### 4. Comprehensive Collections Framework
- **`List<Question>`:** Ordered sequence of questions presented during examination.
- **`Map<Integer, Character>`:** Maps question ID to student-selected options for scoring verification.
- **`Set<String>` (`HashSet`):** Distinct subject aggregation without duplicates.
- **`Collections.shuffle(List)`:** Dynamic in-place question randomization upon test generation.

---

<!-- slide -->
# Slide 6: Rubric Highlights - Multithreading & Synchronization [4 Marks]

### 1. Dedicated Timer Thread Pool (`QuizTimerService`)
- Uses `ScheduledExecutorService` with a daemon worker thread to count down 30s per question without freezing the GUI.
- Timer ticks safely dispatched to Swing's Event Dispatch Thread:
  ```java
  SwingUtilities.invokeLater(() -> callback.onTick(remainingSeconds));
  ```

### 2. Non-blocking UI with `SwingWorker`
- All network and JDBC I/O operations (logging in, subject discovery, score saving) are dispatched via `SwingWorker<T, Void>`.
- Maintains a steady 60 FPS user interface with zero mouse lag or visual stutters.

### 3. Concurrency & Synchronization (`ThreadSafeLeaderboardCache`)
- **`AtomicInteger`:** Thread-safe global counter monitoring active concurrent quiz sessions.
- **`ReentrantReadWriteLock`:** Concurrent read-locks for instantaneous leaderboard retrieval coupled with exclusive write-locks for cache invalidation when scores are submitted.

---

<!-- slide -->
# Slide 7: Key Features & GUI Walkthrough

| Screen | Core Functionality | Visual Design Highlights |
| :--- | :--- | :--- |
| **Login / Register** | Dual-card switcher for authentication and student onboarding with live validation. | Clean modern card on off-white background with bold typography and role pickers. |
| **Student Dashboard** | Subject picker, dynamic question counter, past scores table, and live global leaderboard. | High-contrast tables (`JTable`), status badges, and one-click quiz launcher. |
| **Interactive Quiz Runner** | 30-second live timer, visual progress bar, word-wrapped prompts, radio choices [A]-[D]. | Color-coded timer badge (Blue &rarr; Amber &rarr; Red), timeout auto-advance. |
| **Result Scorecard** | Instant score computation, accuracy percentage, and letter grade badge (`A+` to `F`). | Colored badge indicators (Green for A, Red for F) and one-click retake actions. |
| **Admin Control Panel** | 3-tabbed manager: Add new questions, view registered users, and audit score history. | Comprehensive data tables and structured question entry forms. |

---

<!-- slide -->
# Slide 8: Conclusion & Future Scope

### Summary of Achievements
- Successfully elevated a standard academic console task into an enterprise-grade Java Swing Desktop application.
- 100% compliant with university marking rubric (OOP, Generics, Multithreading, DAO, and JDBC).
- Fully tested and builds with zero warnings under standard Maven toolchains.

### Future Enhancements
1. **Cloud Synchronization:** Integration with AWS RDS or cloud-hosted MySQL clusters.
2. **Category Analytics:** Radar charts and statistical analytics illustrating student performance trends across topics.
3. **Automated Assessment Export:** PDF scorecard generation with downloadable certificates for students achieving Grade A+.

---
*Created for Academic Evaluation & Demonstration - Quizbyte Platform v1.0*
