package com.quizbyte.model;

/**
 * Abstract base class representing a human actor in the Quizbyte ecosystem.
 * Supports the 3-role specification: ADMIN, QUIZ_CREATOR, PARTICIPANT.
 * Demonstrates OOP Inheritance and Polymorphism principles as per marking rubric.
 */
public abstract class Person {

    protected int id;
    protected String username;
    protected String email;
    protected String role;
    protected String status; // 'ACTIVE', 'SUSPENDED'

    public Person() {
        this.status = "ACTIVE";
    }

    public Person(String username, String role) {
        this.username = username;
        this.role = role;
        this.status = "ACTIVE";
    }

    public Person(int id, String username, String role) {
        this.id = id;
        this.username = username;
        this.role = role;
        this.status = "ACTIVE";
    }

    public Person(int id, String username, String email, String role, String status) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.role = role;
        this.status = (status != null) ? status : "ACTIVE";
    }

    /**
     * Abstract polymorphic method to be implemented by concrete subclasses.
     */
    public abstract void displayRoleBadge();

    /**
     * Polymorphic badge descriptor string for GUI and logging.
     */
    public abstract String getRoleBadge();

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean isAdmin() {
        return "ADMIN".equalsIgnoreCase(this.role);
    }

    public boolean isQuizCreator() {
        return "QUIZ_CREATOR".equalsIgnoreCase(this.role);
    }

    public boolean isParticipant() {
        return "PARTICIPANT".equalsIgnoreCase(this.role) || "STUDENT".equalsIgnoreCase(this.role);
    }

    public boolean isStudent() {
        return isParticipant();
    }

    public boolean isActive() {
        return "ACTIVE".equalsIgnoreCase(this.status);
    }

    public boolean isSuspended() {
        return "SUSPENDED".equalsIgnoreCase(this.status);
    }

    @Override
    public String toString() {
        return "Person{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", role='" + role + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}
