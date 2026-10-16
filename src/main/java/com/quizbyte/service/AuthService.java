package com.quizbyte.service;

import com.quizbyte.dao.UserDAO;
import com.quizbyte.exception.AuthenticationException;
import com.quizbyte.exception.DatabaseOperationException;
import com.quizbyte.model.User;

/**
 * Service managing user authentication, registration, validation, and session state.
 * Employs custom exception handling, role-based authorization for the 3-role platform
 * (ADMIN, QUIZ_CREATOR, PARTICIPANT), and account suspension checks.
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
     * Authenticates a user against database records and checks for suspended account status.
     *
     * @param username username entered
     * @param password password entered
     * @return authenticated User instance
     * @throws AuthenticationException    if credentials are invalid or account is suspended
     * @throws DatabaseOperationException if database query fails
     */
    public User login(String username, String password) throws AuthenticationException, DatabaseOperationException {
        if (username == null || username.trim().isEmpty() || password == null || password.trim().isEmpty()) {
            throw new AuthenticationException("Username and password must not be empty.");
        }

        User user = userDAO.login(username.trim(), password);
        if (user == null) {
            throw new AuthenticationException("Invalid username or password. Please verify your credentials.");
        }

        if (user.isSuspended()) {
            throw new AuthenticationException("Account is SUSPENDED. Please contact your system administrator.");
        }

        this.currentUser = user;
        return user;
    }

    /**
     * Overloaded register method with default PARTICIPANT role.
     */
    public boolean register(String username, String password) throws AuthenticationException, DatabaseOperationException {
        return register(username, password, "PARTICIPANT");
    }

    /**
     * Registers a new user with validation and custom exception signaling.
     *
     * @param username desired username
     * @param password desired password
     * @param role     user role ('PARTICIPANT', 'QUIZ_CREATOR', or 'ADMIN')
     * @return true if successfully created
     * @throws AuthenticationException    if validation constraints are violated
     * @throws DatabaseOperationException if database error occurs
     */
    public boolean register(String username, String password, String role) 
            throws AuthenticationException, DatabaseOperationException {

        if (username == null || username.trim().length() < 3) {
            throw new AuthenticationException("Username must be at least 3 characters long.");
        }

        if (password == null || password.trim().length() < 4) {
            throw new AuthenticationException("Password must be at least 4 characters long.");
        }

        String sanitizedUsername = username.trim();
        if (userDAO.userExists(sanitizedUsername)) {
            throw new AuthenticationException("Username '" + sanitizedUsername + "' is already taken. Please choose another.");
        }

        String assignedRole = (role != null && !role.trim().isEmpty()) ? role.trim().toUpperCase() : "PARTICIPANT";
        String email = sanitizedUsername.toLowerCase() + "@quizbyte.com";
        User newUser = new User(0, sanitizedUsername, password, email, assignedRole, "ACTIVE");
        return userDAO.registerUser(newUser);
    }

    /**
     * Clears current session.
     */
    public void logout() {
        if (currentUser != null) {
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

    public boolean isQuizCreator() {
        return currentUser != null && currentUser.isQuizCreator();
    }

    public boolean isParticipant() {
        return currentUser != null && currentUser.isParticipant();
    }

    public boolean isStudent() {
        return isParticipant();
    }
}
