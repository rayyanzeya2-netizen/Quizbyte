package com.quizbyte.service;

import com.quizbyte.dao.UserDAO;
import com.quizbyte.model.User;

/**
 * Service class handling user authentication, session state, and registration validation.
 */
public class AuthService {

    private final UserDAO userDAO;
    private User currentUser;

    public AuthService() {
        this.userDAO = new UserDAO();
        this.currentUser = null;
    }

    public AuthService(UserDAO userDAO) {
        this.userDAO = userDAO;
        this.currentUser = null;
    }

    /**
     * Authenticates a user and sets the current active session.
     *
     * @param username user username
     * @param password user password
     * @return true if login succeeds, false otherwise
     */
    public boolean login(String username, String password) {
        if (username == null || username.trim().isEmpty() || password == null || password.trim().isEmpty()) {
            System.out.println("(!) Username and password cannot be empty.");
            return false;
        }

        User user = userDAO.login(username.trim(), password);
        if (user != null) {
            this.currentUser = user;
            return true;
        }
        return false;
    }

    /**
     * Validates and registers a new student or admin.
     *
     * @param username desired username
     * @param password desired password
     * @param role role ('STUDENT' or 'ADMIN')
     * @return true if registration succeeds, false otherwise
     */
    public boolean register(String username, String password, String role) {
        if (username == null || username.trim().length() < 3) {
            System.out.println("(!) Username must be at least 3 characters long.");
            return false;
        }

        if (password == null || password.trim().length() < 4) {
            System.out.println("(!) Password must be at least 4 characters long.");
            return false;
        }

        String sanitizedUsername = username.trim();
        if (userDAO.userExists(sanitizedUsername)) {
            System.out.println("(!) Username '" + sanitizedUsername + "' is already taken. Please choose another.");
            return false;
        }

        String assignedRole = (role != null && !role.trim().isEmpty()) ? role.trim().toUpperCase() : "STUDENT";
        User newUser = new User(sanitizedUsername, password, assignedRole);
        return userDAO.registerUser(newUser);
    }

    /**
     * Ends the current user session.
     */
    public void logout() {
        if (currentUser != null) {
            System.out.println("[Session] User '" + currentUser.getUsername() + "' logged out successfully.");
            currentUser = null;
        }
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public boolean isLoggedIn() {
        return currentUser != null;
    }

    public boolean isAdmin() {
        return currentUser != null && currentUser.isAdmin();
    }

    public boolean isStudent() {
        return currentUser != null && currentUser.isStudent();
    }
}
