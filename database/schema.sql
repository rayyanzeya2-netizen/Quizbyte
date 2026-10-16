-- ========================================================
-- Quizbyte Database Schema and Seed Data (3-Role Specification)
-- Database Engine: MySQL 8.x
-- Roles: ADMIN, QUIZ_CREATOR, PARTICIPANT
-- ========================================================

CREATE DATABASE IF NOT EXISTS quiz_db;
USE quiz_db;

-- Drop existing tables preserving foreign key dependencies
DROP TABLE IF EXISTS quiz_reminders;
DROP TABLE IF EXISTS messages;
DROP TABLE IF EXISTS system_settings;
DROP TABLE IF EXISTS quiz_results;
DROP TABLE IF EXISTS questions;
DROP TABLE IF EXISTS users;

-- 1. Users Table with 3 Roles, Email, and Account Status
CREATE TABLE users (
    user_id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    email VARCHAR(100) DEFAULT NULL,
    role ENUM('ADMIN', 'QUIZ_CREATOR', 'PARTICIPANT') DEFAULT 'PARTICIPANT',
    status ENUM('ACTIVE', 'SUSPENDED') DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 2. Questions Table with Creator Link, Status (Approval Workflow), and Duration
CREATE TABLE questions (
    question_id INT AUTO_INCREMENT PRIMARY KEY,
    creator_id INT NOT NULL,
    subject VARCHAR(50) NOT NULL,
    question_text TEXT NOT NULL,
    option_a VARCHAR(255) NOT NULL,
    option_b VARCHAR(255) NOT NULL,
    option_c VARCHAR(255) NOT NULL,
    option_d VARCHAR(255) NOT NULL,
    correct_option CHAR(1) NOT NULL,
    status ENUM('PENDING', 'APPROVED', 'REJECTED') DEFAULT 'APPROVED',
    duration_seconds INT DEFAULT 30,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_correct_option CHECK (correct_option IN ('A', 'B', 'C', 'D')),
    CONSTRAINT fk_questions_creator
        FOREIGN KEY (creator_id) REFERENCES users(user_id)
        ON DELETE CASCADE
);

-- 3. Quiz Results Table
CREATE TABLE quiz_results (
    result_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    subject VARCHAR(50) NOT NULL,
    score INT NOT NULL,
    total_questions INT NOT NULL,
    percentage DECIMAL(5, 2) NOT NULL,
    attempt_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_quiz_results_user
        FOREIGN KEY (user_id) REFERENCES users(user_id)
        ON DELETE CASCADE
);

-- 4. Messages / Inquiries Table between Participants and Creators/Admins
CREATE TABLE messages (
    message_id INT AUTO_INCREMENT PRIMARY KEY,
    sender_id INT NOT NULL,
    receiver_id INT NOT NULL,
    quiz_id INT DEFAULT NULL,
    message_text TEXT NOT NULL,
    reply_text TEXT DEFAULT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_messages_sender
        FOREIGN KEY (sender_id) REFERENCES users(user_id)
        ON DELETE CASCADE,
    CONSTRAINT fk_messages_receiver
        FOREIGN KEY (receiver_id) REFERENCES users(user_id)
        ON DELETE CASCADE
);

-- 5. Quiz Reminders Table for Participant Planners
CREATE TABLE quiz_reminders (
    reminder_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    subject VARCHAR(50) NOT NULL,
    notes VARCHAR(255) DEFAULT NULL,
    target_date DATE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_reminders_user
        FOREIGN KEY (user_id) REFERENCES users(user_id)
        ON DELETE CASCADE
);

-- 6. System Settings Table
CREATE TABLE system_settings (
    setting_key VARCHAR(50) PRIMARY KEY,
    setting_value VARCHAR(100) NOT NULL
);

-- ========================================================
-- Seed Data
-- ========================================================

-- Seed System Settings
INSERT INTO system_settings (setting_key, setting_value) VALUES
('DEFAULT_TIME_LIMIT', '30'),
('MAINTENANCE_MODE', 'false');

-- Seed Users across all 3 Roles
INSERT INTO users (username, password, email, role, status) VALUES
('admin', 'admin123', 'admin@quizbyte.com', 'ADMIN', 'ACTIVE'),
('creator1', 'creator123', 'creator1@quizbyte.com', 'QUIZ_CREATOR', 'ACTIVE'),
('participant1', 'pass123', 'participant1@quizbyte.com', 'PARTICIPANT', 'ACTIVE'),
('student1', 'pass123', 'student1@quizbyte.com', 'PARTICIPANT', 'ACTIVE');

-- Seed Questions authored by creator1 (user_id = 2) or admin (user_id = 1)
INSERT INTO questions (creator_id, subject, question_text, option_a, option_b, option_c, option_d, correct_option, status, duration_seconds) VALUES
-- Java Questions
(2, 'Java', 'Which of the following is not a Java primitive data type?', 'int', 'float', 'String', 'boolean', 'C', 'APPROVED', 30),
(2, 'Java', 'What is the default value of a boolean variable in a Java class field?', 'true', 'false', '0', 'null', 'B', 'APPROVED', 30),
(2, 'Java', 'Which keyword is used by a class to implement an interface?', 'extends', 'implements', 'inherit', 'instanceof', 'B', 'APPROVED', 30),
(2, 'Java', 'Which component of the JDK is responsible for executing Java bytecode?', 'JVM', 'JIT compiler', 'javac', 'javadoc', 'A', 'APPROVED', 30),
(2, 'Java', 'Which of these collection classes allows duplicate elements and maintains insertion order?', 'HashSet', 'TreeSet', 'ArrayList', 'HashMap', 'C', 'APPROVED', 30),
(2, 'Java', 'What does the static keyword signify when applied to a method in Java?', 'It can only access non-static variables', 'It belongs to the class rather than instances', 'It cannot be overloaded', 'It is thread-safe by default', 'B', 'APPROVED', 30),

-- OOP Questions
(2, 'OOP', 'Which OOP principle involves wrapping code and data together into a single unit?', 'Polymorphism', 'Abstraction', 'Encapsulation', 'Inheritance', 'C', 'APPROVED', 30),
(2, 'OOP', 'What type of polymorphism is achieved through method overloading in Java?', 'Compile-time polymorphism', 'Runtime polymorphism', 'Dynamic binding', 'Virtual method dispatch', 'A', 'APPROVED', 30),
(2, 'OOP', 'Can a Java abstract class have constructors?', 'No, abstract classes cannot be instantiated so they have no constructors', 'Yes, called during subclass instantiation via super()', 'Only if all its methods are concrete', 'Only in Java 17 and above', 'B', 'APPROVED', 30),
(2, 'OOP', 'Which relationship best describes inheritance in Object-Oriented Programming?', 'HAS-A relationship', 'IS-A relationship', 'USES-A relationship', 'PART-OF relationship', 'B', 'APPROVED', 30),
(2, 'OOP', 'What is the primary purpose of dynamic method dispatch (runtime polymorphism)?', 'To resolve method calls at compile time', 'To allow a subclass method to override a superclass method dynamically at runtime', 'To make methods execute faster', 'To prevent inheritance', 'B', 'APPROVED', 30),

-- DBMS Questions
(2, 'DBMS', 'Which normal form eliminates partial dependency in relational database design?', 'First Normal Form (1NF)', 'Second Normal Form (2NF)', 'Third Normal Form (3NF)', 'Boyce-Codd Normal Form (BCNF)', 'B', 'APPROVED', 30),
(2, 'DBMS', 'What SQL command is used to remove all records from a table without logging individual row deletions?', 'DELETE', 'DROP', 'TRUNCATE', 'REMOVE', 'C', 'APPROVED', 30),
(2, 'DBMS', 'Which ACID property guarantees that all operations of a transaction succeed or all fail?', 'Atomicity', 'Consistency', 'Isolation', 'Durability', 'A', 'APPROVED', 30),

-- Sample PENDING Question for Content Approval Demonstration
(2, 'Java', 'What is the size of an int variable in Java?', '16 bit', '32 bit', '64 bit', 'Depends on OS', 'B', 'PENDING', 30);
