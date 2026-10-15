package com.quizbyte.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Database configuration and connection manager.
 * Provides a centralized point for establishing MySQL connections via JDBC.
 */
public class DatabaseConfig {

    // =========================================================================
    // DATABASE CONNECTION CREDENTIALS
    // =========================================================================
    // Adjust these constants if your local MySQL instance uses a different
    // host, port, username, or password. Alternatively, you can override them
    // at runtime by setting environment variables: DB_URL, DB_USER, DB_PASSWORD.
    // =========================================================================
    public static final String DEFAULT_URL = 
            "jdbc:mysql://localhost:3306/quiz_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    public static final String DEFAULT_USER = "root";
    public static final String DEFAULT_PASSWORD = "1234";

    private static final String URL;
    private static final String USER;
    private static final String PASSWORD;

    static {
        // Load MySQL JDBC Driver
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("[DatabaseConfig] Warning: MySQL JDBC Driver not found on classpath: " + e.getMessage());
        }

        // Allow environment variable or system property override, defaulting to constants
        String envUrl = System.getenv("DB_URL");
        String envUser = System.getenv("DB_USER");
        String envPass = System.getenv("DB_PASSWORD");

        URL = (envUrl != null && !envUrl.isBlank()) 
                ? envUrl 
                : System.getProperty("db.url", DEFAULT_URL);

        USER = (envUser != null && !envUser.isBlank()) 
                ? envUser 
                : System.getProperty("db.user", DEFAULT_USER);

        PASSWORD = (envPass != null) 
                ? envPass 
                : System.getProperty("db.password", DEFAULT_PASSWORD);
    }

    private DatabaseConfig() {
        // Prevent instantiation
    }

    /**
     * Obtains a new active connection to the MySQL database.
     *
     * @return Connection object
     * @throws SQLException if a database access error occurs
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    /**
     * Tests the database connection and returns true if successful.
     *
     * @return boolean true if connected, false otherwise
     */
    public static boolean testConnection() {
        try (Connection conn = getConnection()) {
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }

    public static String getUrl() {
        return URL;
    }

    public static String getUser() {
        return USER;
    }
}
