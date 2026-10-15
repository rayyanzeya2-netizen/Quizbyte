package com.quizbyte.ui;

import com.quizbyte.dao.QuestionDAO;
import com.quizbyte.dao.ResultDAO;
import com.quizbyte.dao.UserDAO;
import com.quizbyte.model.Question;
import com.quizbyte.model.QuizResult;
import com.quizbyte.service.AuthService;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * Root JFrame controller managing view transitions via CardLayout.
 * Central coordinator for authentication state and navigation across panels.
 */
public class MainFrame extends JFrame {

    public static final String CARD_LOGIN = "LOGIN";
    public static final String CARD_STUDENT = "STUDENT_DASHBOARD";
    public static final String CARD_QUIZ = "QUIZ";
    public static final String CARD_RESULT = "RESULT";
    public static final String CARD_ADMIN = "ADMIN_DASHBOARD";

    private final AuthService authService;
    private final QuestionDAO questionDAO;
    private final ResultDAO resultDAO;
    private final UserDAO userDAO;

    private final CardLayout cardLayout;
    private final JPanel cardsContainer;

    private LoginPanel loginPanel;
    private StudentDashboardPanel studentDashboardPanel;
    private QuizPanel quizPanel;
    private ResultSummaryPanel resultSummaryPanel;
    private AdminDashboardPanel adminDashboardPanel;

    public MainFrame() {
        super("Quizbyte - Interactive Online Quiz Platform");

        // Initialize Data Access & Services
        this.authService = new AuthService();
        this.questionDAO = new QuestionDAO();
        this.resultDAO = new ResultDAO();
        this.userDAO = new UserDAO();

        // Frame Setup
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1024, 720);
        setMinimumSize(new Dimension(880, 620));
        setLocationRelativeTo(null);

        // Container with CardLayout
        this.cardLayout = new CardLayout();
        this.cardsContainer = new JPanel(cardLayout);
        this.cardsContainer.setBackground(UITheme.BG_LIGHT);

        initCards();

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(cardsContainer, BorderLayout.CENTER);

        showLogin();
    }

    private void initCards() {
        this.loginPanel = new LoginPanel(this);
        this.studentDashboardPanel = new StudentDashboardPanel(this);
        this.quizPanel = new QuizPanel(this);
        this.resultSummaryPanel = new ResultSummaryPanel(this);
        this.adminDashboardPanel = new AdminDashboardPanel(this);

        cardsContainer.add(loginPanel, CARD_LOGIN);
        cardsContainer.add(studentDashboardPanel, CARD_STUDENT);
        cardsContainer.add(quizPanel, CARD_QUIZ);
        cardsContainer.add(resultSummaryPanel, CARD_RESULT);
        cardsContainer.add(adminDashboardPanel, CARD_ADMIN);
    }

    public void showLogin() {
        loginPanel.resetFields();
        cardLayout.show(cardsContainer, CARD_LOGIN);
    }

    public void showStudentDashboard() {
        if (!authService.isLoggedIn() || !authService.isStudent()) {
            showLogin();
            return;
        }
        studentDashboardPanel.refreshData();
        cardLayout.show(cardsContainer, CARD_STUDENT);
    }

    public void showAdminDashboard() {
        if (!authService.isLoggedIn() || !authService.isAdmin()) {
            showLogin();
            return;
        }
        adminDashboardPanel.refreshData();
        cardLayout.show(cardsContainer, CARD_ADMIN);
    }

    public void startQuiz(String subject) {
        if (subject == null || subject.isBlank()) {
            JOptionPane.showMessageDialog(this, "Please select a valid subject.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Fetch questions asynchronously
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        SwingWorker<List<Question>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Question> doInBackground() throws Exception {
                return questionDAO.getQuestionsBySubject(subject, 5);
            }

            @Override
            protected void done() {
                setCursor(Cursor.getDefaultCursor());
                try {
                    List<Question> questions = get();
                    if (questions == null || questions.isEmpty()) {
                        JOptionPane.showMessageDialog(MainFrame.this,
                                "No questions available for " + subject + ". Please ask an Admin to add questions.",
                                "Notice", JOptionPane.INFORMATION_MESSAGE);
                        return;
                    }
                    quizPanel.loadQuiz(authService.getCurrentUser(), subject, questions);
                    cardLayout.show(cardsContainer, CARD_QUIZ);
                } catch (java.util.concurrent.ExecutionException e) {
                    Throwable cause = e.getCause();
                    if (cause instanceof com.quizbyte.exception.QuestionNotFoundException) {
                        JOptionPane.showMessageDialog(MainFrame.this,
                                cause.getMessage(), "Topic Unavailable", JOptionPane.INFORMATION_MESSAGE);
                    } else if (cause instanceof com.quizbyte.exception.DatabaseOperationException) {
                        JOptionPane.showMessageDialog(MainFrame.this,
                                "Database Error: " + cause.getMessage(), "Database Failure", JOptionPane.ERROR_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(MainFrame.this,
                                "Failed to load quiz: " + cause.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        };
        worker.execute();
    }

    public void showResult(QuizResult result, String subject) {
        resultSummaryPanel.displayResult(result, subject);
        cardLayout.show(cardsContainer, CARD_RESULT);
    }

    public void logout() {
        authService.logout();
        showLogin();
    }

    // Getters for services & DAOs
    public AuthService getAuthService() {
        return authService;
    }

    public QuestionDAO getQuestionDAO() {
        return questionDAO;
    }

    public ResultDAO getResultDAO() {
        return resultDAO;
    }

    public UserDAO getUserDAO() {
        return userDAO;
    }
}
