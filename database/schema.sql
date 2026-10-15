-- ========================================================
-- Quizbyte Database Schema and Seed Data
-- Database Engine: MySQL 8.x
-- ========================================================

-- 1. Create and Use Database
CREATE DATABASE IF NOT EXISTS quiz_db;
USE quiz_db;

-- 2. Drop existing tables if re-initializing (Order preserves FK dependencies)
DROP TABLE IF EXISTS quiz_results;
DROP TABLE IF EXISTS questions;
DROP TABLE IF EXISTS users;

-- 3. Users Table
CREATE TABLE users (
    user_id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    role ENUM('STUDENT', 'ADMIN') DEFAULT 'STUDENT',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 4. Questions Table
CREATE TABLE questions (
    question_id INT AUTO_INCREMENT PRIMARY KEY,
    subject VARCHAR(50) NOT NULL,
    question_text TEXT NOT NULL,
    option_a VARCHAR(255) NOT NULL,
    option_b VARCHAR(255) NOT NULL,
    option_c VARCHAR(255) NOT NULL,
    option_d VARCHAR(255) NOT NULL,
    correct_option CHAR(1) NOT NULL,
    CONSTRAINT chk_correct_option CHECK (correct_option IN ('A', 'B', 'C', 'D'))
);

-- 5. Quiz Results Table
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

-- ========================================================
-- Seed Data
-- ========================================================

-- Default Users: 1 Admin and 1 Student
INSERT INTO users (username, password, role) VALUES
('admin', 'admin123', 'ADMIN'),
('student1', 'pass123', 'STUDENT');

-- Seed Questions: Java & OOP
INSERT INTO questions (subject, question_text, option_a, option_b, option_c, option_d, correct_option) VALUES
-- Java Questions
('Java', 'Which of the following is not a Java primitive data type?', 'int', 'float', 'String', 'boolean', 'C'),
('Java', 'What is the default value of a boolean variable in a Java class field?', 'true', 'false', '0', 'null', 'B'),
('Java', 'Which keyword is used by a class to implement an interface?', 'extends', 'implements', 'inherit', 'instanceof', 'B'),
('Java', 'Which component of the JDK is responsible for executing Java bytecode?', 'JVM', 'JIT compiler', 'javac', 'javadoc', 'A'),
('Java', 'Which of these collection classes allows duplicate elements and maintains insertion order?', 'HashSet', 'TreeSet', 'ArrayList', 'HashMap', 'C'),
('Java', 'What does the static keyword signify when applied to a method in Java?', 'It can only access non-static variables', 'It belongs to the class rather than instances', 'It cannot be overloaded', 'It is thread-safe by default', 'B'),

-- OOP Questions
('OOP', 'Which OOP principle involves wrapping code and data together into a single unit?', 'Polymorphism', 'Abstraction', 'Encapsulation', 'Inheritance', 'C'),
('OOP', 'What type of polymorphism is achieved through method overloading in Java?', 'Compile-time polymorphism', 'Runtime polymorphism', 'Dynamic binding', 'Virtual method dispatch', 'A'),
('OOP', 'Can a Java abstract class have constructors?', 'No, abstract classes cannot be instantiated so they have no constructors', 'Yes, called during subclass instantiation via super()', 'Only if all its methods are concrete', 'Only in Java 17 and above', 'B'),
('OOP', 'Which relationship best describes inheritance in Object-Oriented Programming?', 'HAS-A relationship', 'IS-A relationship', 'USES-A relationship', 'PART-OF relationship', 'B'),
('OOP', 'What is the primary purpose of dynamic method dispatch (runtime polymorphism)?', 'To resolve method calls at compile time', 'To allow a subclass method to override a superclass method dynamically at runtime', 'To make methods execute faster', 'To prevent inheritance', 'B'),

-- DBMS Questions
('DBMS', 'Which normal form eliminates partial dependency in relational database design?', 'First Normal Form (1NF)', 'Second Normal Form (2NF)', 'Third Normal Form (3NF)', 'Boyce-Codd Normal Form (BCNF)', 'B'),
('DBMS', 'What SQL command is used to remove all records from a table without logging individual row deletions?', 'DELETE', 'DROP', 'TRUNCATE', 'REMOVE', 'C'),
('DBMS', 'Which ACID property guarantees that all operations of a transaction succeed or all fail?', 'Atomicity', 'Consistency', 'Isolation', 'Durability', 'A');
