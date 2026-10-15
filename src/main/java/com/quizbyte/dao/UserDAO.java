package com.quizbyte.dao;

import com.quizbyte.config.DatabaseConfig;
import com.quizbyte.exception.DatabaseOperationException;
import com.quizbyte.model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object for User entity.
 * Implements generic BaseDAO<User> contract and JDBC best practices
 * (PreparedStatement, try-with-resources, explicit custom exception handling).
 */
public class UserDAO implements BaseDAO<User> {

    // =========================================================================
    // BaseDAO<User> CONTRACT IMPLEMENTATIONS
    // =========================================================================

    @Override
    public boolean save(User user) throws DatabaseOperationException {
        return registerUser(user);
    }

    @Override
    public Optional<User> findById(int id) throws DatabaseOperationException {
        User user = getUserById(id);
        return Optional.ofNullable(user);
    }

    @Override
    public List<User> findAll() throws DatabaseOperationException {
        return getAllUsers();
    }

    // =========================================================================
    // USER SPECIFIC OPERATIONS
    // =========================================================================

    /**
     * Checks if a user already exists with the given username.
     *
     * @param username username to verify
     * @return true if user exists, false otherwise
     * @throws DatabaseOperationException if database query fails
     */
    public boolean userExists(String username) throws DatabaseOperationException {
        String sql = "SELECT 1 FROM users WHERE username = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to verify if username exists: " + username, e);
        }
    }

    /**
     * Registers a new user into the database.
     *
     * @param user User object containing username, password, and role
     * @return true if successfully inserted, false otherwise
     * @throws DatabaseOperationException if database query fails
     */
    public boolean registerUser(User user) throws DatabaseOperationException {
        String sql = "INSERT INTO users (username, password, role) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, user.getUsername());
            stmt.setString(2, user.getPassword());
            stmt.setString(3, user.getRole() != null ? user.getRole().toUpperCase() : "STUDENT");

            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        user.setUserId(generatedKeys.getInt(1));
                    }
                }
                return true;
            }
            return false;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to register new user: " + user.getUsername(), e);
        }
    }

    /**
     * Authenticates a user by username and password.
     *
     * @param username user's username
     * @param password user's password
     * @return User object if authenticated, or null if invalid credentials
     * @throws DatabaseOperationException if query fails
     */
    public User login(String username, String password) throws DatabaseOperationException {
        String sql = "SELECT user_id, username, password, role FROM users WHERE username = ? AND password = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            stmt.setString(2, password);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new User(
                            rs.getInt("user_id"),
                            rs.getString("username"),
                            rs.getString("password"),
                            rs.getString("role")
                    );
                }
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Database error during login for user: " + username, e);
        }
        return null;
    }

    /**
     * Retrieves all registered users in the system.
     *
     * @return List of all User objects
     * @throws DatabaseOperationException if query fails
     */
    public List<User> getAllUsers() throws DatabaseOperationException {
        List<User> users = new ArrayList<>();
        String sql = "SELECT user_id, username, password, role FROM users ORDER BY user_id ASC";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                users.add(new User(
                        rs.getInt("user_id"),
                        rs.getString("username"),
                        rs.getString("password"),
                        rs.getString("role")
                ));
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Error retrieving registered users list", e);
        }
        return users;
    }

    /**
     * Retrieves a user by their user ID.
     *
     * @param userId user ID
     * @return User object or null
     * @throws DatabaseOperationException if query fails
     */
    public User getUserById(int userId) throws DatabaseOperationException {
        String sql = "SELECT user_id, username, password, role FROM users WHERE user_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new User(
                            rs.getInt("user_id"),
                            rs.getString("username"),
                            rs.getString("password"),
                            rs.getString("role")
                    );
                }
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Error fetching user by ID: " + userId, e);
        }
        return null;
    }
}
