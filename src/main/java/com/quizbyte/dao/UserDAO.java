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
 * Implements generic BaseDAO<User> contract and full CRUD operations
 * for the 3-role platform (ADMIN, QUIZ_CREATOR, PARTICIPANT).
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
     * Registers a new user with email, role (ADMIN, QUIZ_CREATOR, PARTICIPANT), and status.
     */
    public boolean registerUser(User user) throws DatabaseOperationException {
        String sql = "INSERT INTO users (username, password, email, role, status) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, user.getUsername());
            stmt.setString(2, user.getPassword());
            stmt.setString(3, user.getEmail() != null ? user.getEmail() : (user.getUsername() + "@quizbyte.com"));
            stmt.setString(4, user.getRole() != null ? user.getRole().toUpperCase() : "PARTICIPANT");
            stmt.setString(5, user.getStatus() != null ? user.getStatus().toUpperCase() : "ACTIVE");

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
     */
    public User login(String username, String password) throws DatabaseOperationException {
        String sql = "SELECT user_id, username, password, email, role, status FROM users WHERE username = ? AND password = ?";
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
                            rs.getString("email"),
                            rs.getString("role"),
                            rs.getString("status")
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
     */
    public List<User> getAllUsers() throws DatabaseOperationException {
        List<User> users = new ArrayList<>();
        String sql = "SELECT user_id, username, password, email, role, status FROM users ORDER BY user_id ASC";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                users.add(new User(
                        rs.getInt("user_id"),
                        rs.getString("username"),
                        rs.getString("password"),
                        rs.getString("email"),
                        rs.getString("role"),
                        rs.getString("status")
                ));
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Error retrieving registered users list", e);
        }
        return users;
    }

    /**
     * Retrieves a user by their user ID.
     */
    public User getUserById(int userId) throws DatabaseOperationException {
        String sql = "SELECT user_id, username, password, email, role, status FROM users WHERE user_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new User(
                            rs.getInt("user_id"),
                            rs.getString("username"),
                            rs.getString("password"),
                            rs.getString("email"),
                            rs.getString("role"),
                            rs.getString("status")
                    );
                }
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Error fetching user by ID: " + userId, e);
        }
        return null;
    }

    /**
     * Deletes a user by their ID (cascades to quiz results and questions).
     */
    public boolean deleteUser(int userId) throws DatabaseOperationException {
        String sql = "DELETE FROM users WHERE user_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Error deleting user ID: " + userId, e);
        }
    }

    /**
     * Updates a user's role (ADMIN, QUIZ_CREATOR, PARTICIPANT).
     */
    public boolean updateUserRole(int userId, String newRole) throws DatabaseOperationException {
        String sql = "UPDATE users SET role = ? WHERE user_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, newRole.toUpperCase());
            stmt.setInt(2, userId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Error updating role for user ID: " + userId, e);
        }
    }

    /**
     * Updates a user's status (ACTIVE, SUSPENDED).
     */
    public boolean updateUserStatus(int userId, String newStatus) throws DatabaseOperationException {
        String sql = "UPDATE users SET status = ? WHERE user_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, newStatus.toUpperCase());
            stmt.setInt(2, userId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Error updating status for user ID: " + userId, e);
        }
    }

    /**
     * Retrieves all users having a specific role (e.g. QUIZ_CREATOR).
     */
    public List<User> getUsersByRole(String role) throws DatabaseOperationException {
        List<User> list = new ArrayList<>();
        String sql = "SELECT user_id, username, password, email, role, status FROM users WHERE role = ? AND status = 'ACTIVE' ORDER BY username ASC";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, role.toUpperCase());
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(new User(
                            rs.getInt("user_id"),
                            rs.getString("username"),
                            rs.getString("password"),
                            rs.getString("email"),
                            rs.getString("role"),
                            rs.getString("status")
                    ));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Error fetching users by role: " + role, e);
        }
        return list;
    }
}
