package com.quizbyte.ui;

import com.quizbyte.dao.QuestionDAO;
import com.quizbyte.dao.ResultDAO;
import com.quizbyte.exception.DatabaseOperationException;
import com.quizbyte.model.QuizResult;
import com.quizbyte.model.User;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Dashboard panel for logged-in students.
 * Integrates OOP polymorphic role badges, Collections framework tables,
 * and asynchronous background database workers.
 */
public class StudentDashboardPanel extends JPanel {

    private final MainFrame mainFrame;
    private final QuestionDAO questionDAO;
    private final ResultDAO resultDAO;

    private JLabel welcomeLabel;
    private JLabel roleBadgeLabel;
    private JComboBox<String> subjectComboBox;
    private JLabel questionCountLabel;
    private JButton startQuizButton;

    private JTable pastAttemptsTable;
    private DefaultTableModel pastAttemptsModel;

    private JTable leaderboardTable;
    private DefaultTableModel leaderboardModel;

    public StudentDashboardPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        this.questionDAO = mainFrame.getQuestionDAO();
        this.resultDAO = mainFrame.getResultDAO();

        setLayout(new BorderLayout(0, 16));
        setBackground(UITheme.BG_LIGHT);
        setBorder(new EmptyBorder(20, 28, 20, 28));

        add(buildHeaderPanel(), BorderLayout.NORTH);
        add(buildContentPanel(), BorderLayout.CENTER);
    }

    private JPanel buildHeaderPanel() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JPanel leftBox = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        leftBox.setOpaque(false);

        welcomeLabel = new JLabel("Welcome, Student!");
        welcomeLabel.setFont(UITheme.FONT_TITLE);
        welcomeLabel.setForeground(UITheme.SLATE_DARK);

        // Polymorphic badge representation from Person/User model
        roleBadgeLabel = new JLabel("[🎓 STUDENT]");
        roleBadgeLabel.setFont(UITheme.FONT_BODY_BOLD);
        roleBadgeLabel.setForeground(UITheme.PRIMARY);

        leftBox.add(welcomeLabel);
        leftBox.add(roleBadgeLabel);

        JButton logoutBtn = UITheme.createDangerButton("Log Out");
        logoutBtn.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(mainFrame, 
                    "Are you sure you want to log out?", "Confirm Logout", 
                    JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                mainFrame.logout();
            }
        });

        header.add(leftBox, BorderLayout.WEST);
        header.add(logoutBtn, BorderLayout.EAST);
        return header;
    }

    private JPanel buildContentPanel() {
        JPanel container = new JPanel(new BorderLayout(0, 16));
        container.setOpaque(false);

        JPanel launcherCard = UITheme.createCardPanel();
        launcherCard.setLayout(new FlowLayout(FlowLayout.LEFT, 16, 12));

        JLabel selectLabel = new JLabel("Select Subject:");
        selectLabel.setFont(UITheme.FONT_BODY_BOLD);
        selectLabel.setForeground(UITheme.TEXT_MAIN);

        subjectComboBox = new JComboBox<>();
        subjectComboBox.setFont(UITheme.FONT_BODY);
        subjectComboBox.setPreferredSize(new Dimension(180, 36));
        subjectComboBox.addActionListener(e -> updateQuestionCountLabel());

        questionCountLabel = new JLabel("Available: 0 questions");
        questionCountLabel.setFont(UITheme.FONT_SMALL);
        questionCountLabel.setForeground(UITheme.TEXT_MUTED);

        startQuizButton = UITheme.createSuccessButton("▶  Start Quiz");
        startQuizButton.addActionListener(e -> {
            String selectedSubject = (String) subjectComboBox.getSelectedItem();
            if (selectedSubject != null) {
                mainFrame.startQuiz(selectedSubject);
            }
        });

        JButton refreshBtn = UITheme.createSecondaryButton("↻  Refresh");
        refreshBtn.addActionListener(e -> refreshData());

        launcherCard.add(selectLabel);
        launcherCard.add(subjectComboBox);
        launcherCard.add(questionCountLabel);
        launcherCard.add(Box.createHorizontalStrut(10));
        launcherCard.add(startQuizButton);
        launcherCard.add(refreshBtn);

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(UITheme.FONT_BODY_BOLD);
        tabbedPane.setBackground(Color.WHITE);

        tabbedPane.addTab("  My Past Attempts  ", buildPastAttemptsPanel());
        tabbedPane.addTab("  Global Leaderboard  ", buildLeaderboardPanel());

        container.add(launcherCard, BorderLayout.NORTH);
        container.add(tabbedPane, BorderLayout.CENTER);

        return container;
    }

    private JPanel buildPastAttemptsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(12, 12, 12, 12));

        String[] columns = {"Result ID", "Subject", "Score", "Total", "Percentage", "Grade", "Attempt Date"};
        pastAttemptsModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        pastAttemptsTable = new JTable(pastAttemptsModel);
        UITheme.styleTable(pastAttemptsTable);

        JScrollPane scrollPane = new JScrollPane(pastAttemptsTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(Color.WHITE);

        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildLeaderboardPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(12, 12, 12, 12));

        String[] columns = {"Rank", "Student", "Subject", "Score", "Percentage", "Grade"};
        leaderboardModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        leaderboardTable = new JTable(leaderboardModel);
        UITheme.styleTable(leaderboardTable);

        JScrollPane scrollPane = new JScrollPane(leaderboardTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(Color.WHITE);

        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }

    public void refreshData() {
        User user = mainFrame.getAuthService().getCurrentUser();
        if (user != null) {
            welcomeLabel.setText("Welcome back, " + user.getUsername() + "!");
            roleBadgeLabel.setText(user.getRoleBadge()); // Polymorphic call
        }

        loadSubjectsAsync();
        loadPastAttemptsAsync(user != null ? user.getUserId() : -1);
        loadLeaderboardAsync();
    }

    private void loadSubjectsAsync() {
        SwingWorker<List<String>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<String> doInBackground() {
                return questionDAO.getAvailableSubjects();
            }

            @Override
            protected void done() {
                try {
                    List<String> subjects = get();
                    String previousSelection = (String) subjectComboBox.getSelectedItem();
                    subjectComboBox.removeAllItems();

                    for (String s : subjects) {
                        subjectComboBox.addItem(s);
                    }

                    if (previousSelection != null && subjects.contains(previousSelection)) {
                        subjectComboBox.setSelectedItem(previousSelection);
                    } else if (!subjects.isEmpty()) {
                        subjectComboBox.setSelectedIndex(0);
                    }

                    updateQuestionCountLabel();
                } catch (Exception ignored) {}
            }
        };
        worker.execute();
    }

    private void updateQuestionCountLabel() {
        String selected = (String) subjectComboBox.getSelectedItem();
        if (selected == null) {
            questionCountLabel.setText("Available: 0 questions");
            startQuizButton.setEnabled(false);
            return;
        }

        SwingWorker<Integer, Void> worker = new SwingWorker<>() {
            @Override
            protected Integer doInBackground() throws DatabaseOperationException {
                return questionDAO.getQuestionCountBySubject(selected);
            }

            @Override
            protected void done() {
                try {
                    int count = get();
                    questionCountLabel.setText("Available: " + count + " question" + (count == 1 ? "" : "s"));
                    startQuizButton.setEnabled(count > 0);
                } catch (Exception ignored) {
                    startQuizButton.setEnabled(false);
                }
            }
        };
        worker.execute();
    }

    private void loadPastAttemptsAsync(int userId) {
        if (userId <= 0) return;

        SwingWorker<List<QuizResult>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<QuizResult> doInBackground() throws DatabaseOperationException {
                return resultDAO.getResultsByUserId(userId);
            }

            @Override
            protected void done() {
                try {
                    List<QuizResult> results = get();
                    pastAttemptsModel.setRowCount(0);
                    for (QuizResult r : results) {
                        String dateStr = (r.getAttemptDate() != null)
                                ? r.getAttemptDate().toString().substring(0, 19)
                                : "N/A";
                        pastAttemptsModel.addRow(new Object[]{
                                "#" + r.getResultId(),
                                r.getSubject(),
                                r.getScore(),
                                r.getTotalQuestions(),
                                String.format("%.2f%%", r.getPercentage()),
                                r.getGrade(),
                                dateStr
                        });
                    }
                } catch (Exception ignored) {}
            }
        };
        worker.execute();
    }

    private void loadLeaderboardAsync() {
        SwingWorker<List<QuizResult>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<QuizResult> doInBackground() throws DatabaseOperationException {
                return resultDAO.getAllLeaderboardResults();
            }

            @Override
            protected void done() {
                try {
                    List<QuizResult> leaderboard = get();
                    leaderboardModel.setRowCount(0);
                    int rank = 1;
                    for (QuizResult r : leaderboard) {
                        leaderboardModel.addRow(new Object[]{
                                "#" + (rank++),
                                r.getUsername() != null ? r.getUsername() : "User " + r.getUserId(),
                                r.getSubject(),
                                r.getScore() + " / " + r.getTotalQuestions(),
                                String.format("%.2f%%", r.getPercentage()),
                                r.getGrade()
                        });
                    }
                } catch (Exception ignored) {}
            }
        };
        worker.execute();
    }
}
