package com.quizbyte.model;

/**
 * Concrete model representing a registered user in Quizbyte.
 * Supports the 3-role specification: ADMIN, QUIZ_CREATOR, and PARTICIPANT.
 * Demonstrates OOP Inheritance by extending the abstract Person class.
 */
public class User extends Person {

    private String password;

    public User() {
        super();
    }

    public User(String username, String password, String role) {
        super(username, role);
        this.password = password;
    }

    public User(int userId, String username, String password, String role) {
        super(userId, username, role);
        this.password = password;
    }

    public User(int userId, String username, String password, String email, String role, String status) {
        super(userId, username, email, role, status);
        this.password = password;
    }

    // Polymorphic implementation of Person's abstract method
    @Override
    public void displayRoleBadge() {
        System.out.println(getRoleBadge());
    }

    @Override
    public String getRoleBadge() {
        if (isAdmin()) {
            return "[ADMINISTRATOR]";
        } else if (isQuizCreator()) {
            return "[QUIZ CREATOR]";
        } else {
            return "[PARTICIPANT]";
        }
    }

    // Backward-compatible alias getters/setters for userId
    public int getUserId() {
        return getId();
    }

    public void setUserId(int userId) {
        setId(userId);
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + getId() +
                ", username='" + getUsername() + '\'' +
                ", email='" + getEmail() + '\'' +
                ", role='" + getRole() + '\'' +
                ", status='" + getStatus() + '\'' +
                ", badge='" + getRoleBadge() + '\'' +
                '}';
    }
}
