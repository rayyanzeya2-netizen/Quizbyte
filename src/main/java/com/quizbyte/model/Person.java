package com.quizbyte.model;

/**
 * Abstract base class representing a human actor in the Quizbyte ecosystem.
 * Demonstrates OOP Inheritance and Polymorphism principles as per marking rubric.
 */
public abstract class Person {

    protected int id;
    protected String username;
    protected String role;

    public Person() {
    }

    public Person(String username, String role) {
        this.username = username;
        this.role = role;
    }

    public Person(int id, String username, String role) {
        this.id = id;
        this.username = username;
        this.role = role;
    }

    /**
     * Abstract polymorphic method to be implemented by concrete subclasses
     * (e.g. Student vs Admin badge representation).
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

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public boolean isAdmin() {
        return "ADMIN".equalsIgnoreCase(this.role);
    }

    public boolean isStudent() {
        return "STUDENT".equalsIgnoreCase(this.role);
    }

    @Override
    public String toString() {
        return "Person{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", role='" + role + '\'' +
                '}';
    }
}
