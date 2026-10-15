package com.quizbyte;

import com.quizbyte.config.DatabaseConfig;
import com.quizbyte.dao.UserDAO;
import com.quizbyte.model.User;
import com.quizbyte.service.AuthService;
import com.quizbyte.service.QuizService;

import java.util.List;
import java.util.Scanner;

/**
 * Quizbyte - Interactive Console-based Online Quiz Platform
 * Main entry point of the CLI application.
 */
public class Main {

    private static final Scanner scanner = new Scanner(System.in);
    private static final AuthService authService = new AuthService();
    private static final QuizService quizService = new QuizService();
    private static final UserDAO userDAO = new UserDAO();

    public static void main(String[] args) {
        printWelcomeBanner();
        checkDatabaseStatus();

        boolean running = true;
        while (running) {
            if (!authService.isLoggedIn()) {
                running = handleMainMenu();
            } else if (authService.isAdmin()) {
                handleAdminMenu();
            } else {
                handleStudentMenu();
            }
        }

        printExitBanner();
        scanner.close();
    }

    /**
     * Displays the primary unauthenticated menu.
     *
     * @return true to continue running, false to exit
     */
    private static boolean handleMainMenu() {
        System.out.println("\n+-------------------------------------------------------+");
        System.out.println("|                      MAIN MENU                        |");
        System.out.println("+-------------------------------------------------------+");
        System.out.println("|  [1] Login                                            |");
        System.out.println("|  [2] Register Student                                 |");
        System.out.println("|  [3] Exit                                             |");
        System.out.println("+-------------------------------------------------------+");

        int choice = readIntChoice("Select an option (1-3): ", 1, 3);
        switch (choice) {
            case 1 -> performLogin();
            case 2 -> performRegistration();
            case 3 -> {
                return false;
            }
        }
        return true;
    }

    /**
     * Handles student portal options.
     */
    private static void handleStudentMenu() {
        User user = authService.getCurrentUser();
        System.out.println("\n+-------------------------------------------------------+");
        System.out.printf("|  STUDENT DASHBOARD - Welcome, %-23s |\n", user.getUsername());
        System.out.println("+-------------------------------------------------------+");
        System.out.println("|  [1] Take a Quiz                                      |");
        System.out.println("|  [2] View My Past Scores                              |");
        System.out.println("|  [3] View Global Leaderboard                          |");
        System.out.println("|  [4] Logout                                           |");
        System.out.println("+-------------------------------------------------------+");

        int choice = readIntChoice("Select an option (1-4): ", 1, 4);
        switch (choice) {
            case 1 -> quizService.conductQuiz(user, scanner);
            case 2 -> quizService.displayUserHistory(user.getUserId(), user.getUsername());
            case 3 -> quizService.displayLeaderboard();
            case 4 -> authService.logout();
        }
    }

    /**
     * Handles admin portal options.
     */
    private static void handleAdminMenu() {
        User user = authService.getCurrentUser();
        System.out.println("\n+-------------------------------------------------------+");
        System.out.printf("|  ADMINISTRATOR DASHBOARD - Welcome, %-17s |\n", user.getUsername());
        System.out.println("+-------------------------------------------------------+");
        System.out.println("|  [1] Add New Question                                 |");
        System.out.println("|  [2] View All Registered Users                        |");
        System.out.println("|  [3] View Global Score History                        |");
        System.out.println("|  [4] Logout                                           |");
        System.out.println("+-------------------------------------------------------+");

        int choice = readIntChoice("Select an option (1-4): ", 1, 4);
        switch (choice) {
            case 1 -> quizService.addNewQuestion(scanner);
            case 2 -> viewAllUsers();
            case 3 -> quizService.displayGlobalHistory();
            case 4 -> authService.logout();
        }
    }

    /**
     * Prompts for login credentials and authenticates.
     */
    private static void performLogin() {
        System.out.println("\n--- USER LOGIN ---");
        System.out.print("Username: ");
        String username = scanner.nextLine().trim();
        System.out.print("Password: ");
        String password = scanner.nextLine().trim();

        if (authService.login(username, password)) {
            User user = authService.getCurrentUser();
            System.out.printf("%n[Success] Logged in successfully as '%s' (Role: %s)%n", 
                    user.getUsername(), user.getRole());
        } else {
            System.out.println("%n(!) Login failed: Invalid username or password.");
        }
    }

    /**
     * Prompts for new student registration details.
     */
    private static void performRegistration() {
        System.out.println("\n--- STUDENT REGISTRATION ---");
        System.out.print("Choose Username (min 3 chars): ");
        String username = scanner.nextLine().trim();
        System.out.print("Choose Password (min 4 chars): ");
        String password = scanner.nextLine().trim();

        if (authService.register(username, password, "STUDENT")) {
            System.out.println("\n[Success] Registration completed! You may now log in.");
        } else {
            System.out.println("\n(!) Registration could not be completed. Please check the requirements.");
        }
    }

    /**
     * Displays all registered users in a tabular format (Admin feature).
     */
    private static void viewAllUsers() {
        System.out.println("\n===================================================================");
        System.out.println("                      REGISTERED SYSTEM USERS                      ");
        System.out.println("===================================================================");
        List<User> users = userDAO.getAllUsers();
        if (users.isEmpty()) {
            System.out.println("  No users found in database.");
        } else {
            System.out.printf("%-8s | %-25s | %-12s%n", "User ID", "Username", "Role");
            System.out.println("-------------------------------------------------------------------");
            for (User u : users) {
                System.out.printf("%-8d | %-25s | %-12s%n", u.getUserId(), u.getUsername(), u.getRole());
            }
        }
        System.out.println("===================================================================");
    }

    /**
     * Verifies MySQL database connection on startup with friendly diagnostics.
     */
    private static void checkDatabaseStatus() {
        System.out.print("[Database] Checking MySQL connectivity... ");
        if (DatabaseConfig.testConnection()) {
            System.out.println("CONNECTED! (Ready)");
        } else {
            System.out.println("UNREACHABLE / NOT INITIALIZED");
            System.out.println("-------------------------------------------------------------------");
            System.out.println("(!) Warning: Could not connect to MySQL at: " + DatabaseConfig.getUrl());
            System.out.println("    Default User: " + DatabaseConfig.getUser());
            System.out.println("    Please ensure:");
            System.out.println("    1. MySQL server is running (e.g. MySQL80 service).");
            System.out.println("    2. 'quiz_db' database is initialized via: database/schema.sql");
            System.out.println("    3. If password differs from 'root', adjust in DatabaseConfig.java");
            System.out.println("       or pass env variables DB_USER and DB_PASSWORD.");
            System.out.println("-------------------------------------------------------------------");
        }
    }

    /**
     * Safe integer input reader preventing runtime crashes on invalid format.
     */
    private static int readIntChoice(String prompt, int min, int max) {
        while (true) {
            System.out.print(prompt);
            String line = scanner.nextLine().trim();
            try {
                int choice = Integer.parseInt(line);
                if (choice >= min && choice <= max) {
                    return choice;
                }
                System.out.printf("(!) Please enter a choice between %d and %d.%n", min, max);
            } catch (NumberFormatException e) {
                System.out.println("(!) Invalid input. Please enter a valid number.");
            }
        }
    }

    private static void printWelcomeBanner() {
        System.out.println("""
 ===============================================================================
   ___  _   _ _____ _____ ______ __   __ _____ _____ 
  / _ \\| | | |_   _/___  /| ___ \\\\ \\ / /|_   _|  ___|
 / /_\\ \\ | | | | |    / / | |_/ / \\ V /   | | | |__  
 |  _  | | | | | |   / /  | ___ \\  \\ /    | | |  __| 
 | | | | |_| |_| |_./ /___| |_/ /  | |    | | | |___ 
 \\_| |_/\\___/ \\___/\\_____/\\____/   \\_/    \\_/ \\____/ 
                                                    
               Console-based Online Quiz Platform v1.0
                  Powered by Java 17 & MySQL JDBC
 ===============================================================================
 """);
    }

    private static void printExitBanner() {
        System.out.println("""
 ===============================================================================
       Thank you for using Quizbyte! Keep learning and challenging yourself.
 ===============================================================================
 """);
    }
}
