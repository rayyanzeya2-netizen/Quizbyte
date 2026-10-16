package com.quizbyte.ui;

import com.quizbyte.dao.QuestionDAO;
import com.quizbyte.dao.ResultDAO;
import com.quizbyte.dao.SystemSettingsDAO;
import com.quizbyte.dao.UserDAO;
import com.quizbyte.exception.DatabaseOperationException;
import com.quizbyte.model.Question;
import com.quizbyte.model.QuizResult;
import com.quizbyte.model.User;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.Map;

/**
 * Administrator dashboard for managing users, approving/rejecting creator-submitted questions,
 * inspecting global performance reports, and configuring system settings.
 */
public class AdminDashboardPanel extends JPanel {

    private static final String VIEW_TABLE = "TABLE";
    private static final String VIEW_EMPTY = "EMPTY";

    private final MainFrame mainFrame;
    private final QuestionDAO questionDAO;
    private final UserDAO userDAO;
    private final ResultDAO resultDAO;
    private final SystemSettingsDAO settingsDAO;

    private JLabel adminWelcomeLabel;

    // Tab 1: User Management Components
    private CardLayout usersCardLayout;
    private JPanel usersCardPanel;
    private JTable usersTable;
    private DefaultTableModel usersModel;

    // Tab 2: Content Approval Components
    private CardLayout approvalCardLayout;
    private JPanel approvalCardPanel;
    private JTable approvalTable;
    private DefaultTableModel approvalModel;

    // Tab 3: Global Performance Reports Components
    private JLabel totalAttemptsValLabel;
    private JLabel avgScoreValLabel;
    private JLabel totalQuestionsValLabel;
    private CardLayout scoreHistoryCardLayout;
    private JPanel scoreHistoryCardPanel;
    private JTable scoreHistoryTable;
    private DefaultTableModel scoreHistoryModel;

    // Tab 4: System Settings Components
    private JTextField timeLimitField;
    private JComboBox<String> maintenanceModeComboBox;

    public AdminDashboardPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        this.questionDAO = mainFrame.getQuestionDAO();
        this.userDAO = mainFrame.getUserDAO();
        this.resultDAO = mainFrame.getResultDAO();
        this.settingsDAO = mainFrame.getSystemSettingsDAO();

        setLayout(new BorderLayout(0, 16));
        setBackground(UITheme.BG_LIGHT);
        setBorder(new EmptyBorder(20, 28, 20, 28));

        add(buildHeaderPanel(), BorderLayout.NORTH);
        add(buildTabbedContent(), BorderLayout.CENTER);
    }

    private JPanel buildHeaderPanel() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JPanel leftBox = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        leftBox.setOpaque(false);

        adminWelcomeLabel = new JLabel("Administrator Dashboard - Welcome, Admin!");
        adminWelcomeLabel.setFont(UITheme.FONT_TITLE);
        adminWelcomeLabel.setForeground(UITheme.SLATE_DARK);

        JLabel roleBadge = new JLabel("[ADMINISTRATOR]");
        roleBadge.setFont(UITheme.FONT_BODY_BOLD);
        roleBadge.setForeground(UITheme.DANGER);

        leftBox.add(adminWelcomeLabel);
        leftBox.add(roleBadge);

        JButton logoutBtn = UITheme.createDangerButton("Log Out");
        logoutBtn.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(mainFrame,
                    "Are you sure you want to log out of Admin Dashboard?", "Confirm Logout",
                    JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                mainFrame.logout();
            }
        });

        header.add(leftBox, BorderLayout.WEST);
        header.add(logoutBtn, BorderLayout.EAST);
        return header;
    }

    private JTabbedPane buildTabbedContent() {
        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(UITheme.FONT_BODY_BOLD);
        tabs.setBackground(Color.WHITE);

        tabs.addTab("  User Management  ", buildUsersTab());
        tabs.addTab("  Content Approval  ", buildContentApprovalTab());
        tabs.addTab("  Global Performance Reports  ", buildPerformanceReportsTab());
        tabs.addTab("  System Settings  ", buildSystemSettingsTab());

        tabs.addChangeListener(e -> {
            int selectedIndex = tabs.getSelectedIndex();
            if (selectedIndex == 0) {
                loadUsersAsync();
            } else if (selectedIndex == 1) {
                loadPendingQuestionsAsync();
            } else if (selectedIndex == 2) {
                loadPerformanceReportsAsync();
            } else if (selectedIndex == 3) {
                loadSettingsAsync();
            }
        });

        return tabs;
    }

    // =========================================================================
    // TAB 1: USER MANAGEMENT
    // =========================================================================
    private JPanel buildUsersTab() {
        JPanel panel = new JPanel(new BorderLayout(0, 12));
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(16, 16, 16, 16));

        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setOpaque(false);
        JLabel title = new JLabel("Registered System Users");
        title.setFont(UITheme.FONT_SUBTITLE);
        title.setForeground(UITheme.SLATE_DARK);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setOpaque(false);

        JButton changeRoleBtn = UITheme.createPrimaryButton("Change Role");
        changeRoleBtn.addActionListener(e -> handleChangeUserRole());

        JButton toggleStatusBtn = UITheme.createSecondaryButton("Toggle Status (Active/Suspended)");
        toggleStatusBtn.addActionListener(e -> handleToggleUserStatus());

        JButton deleteUserBtn = UITheme.createDangerButton("Delete User");
        deleteUserBtn.addActionListener(e -> handleDeleteUser());

        JButton refreshUsersBtn = UITheme.createSecondaryButton("Refresh List");
        refreshUsersBtn.addActionListener(e -> loadUsersAsync());

        actions.add(changeRoleBtn);
        actions.add(toggleStatusBtn);
        actions.add(deleteUserBtn);
        actions.add(refreshUsersBtn);

        topBar.add(title, BorderLayout.WEST);
        topBar.add(actions, BorderLayout.EAST);

        usersCardLayout = new CardLayout();
        usersCardPanel = new JPanel(usersCardLayout);
        usersCardPanel.setBackground(Color.WHITE);

        String[] cols = {"User ID", "Username", "Email", "Role", "Status"};
        usersModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };

        usersTable = new JTable(usersModel);
        UITheme.styleTable(usersTable);

        JScrollPane scroll = new JScrollPane(usersTable);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(Color.WHITE);

        JPanel emptyPanel = UITheme.createEmptyPlaceholder("No registered users found in database.");

        usersCardPanel.add(scroll, VIEW_TABLE);
        usersCardPanel.add(emptyPanel, VIEW_EMPTY);
        usersCardLayout.show(usersCardPanel, VIEW_EMPTY);

        panel.add(topBar, BorderLayout.NORTH);
        panel.add(usersCardPanel, BorderLayout.CENTER);
        return panel;
    }

    private void handleChangeUserRole() {
        int row = usersTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a user to update their role.",
                    "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int userId = (Integer) usersModel.getValueAt(row, 0);
        String username = (String) usersModel.getValueAt(row, 1);
        String currentRole = (String) usersModel.getValueAt(row, 3);

        String[] roles = {"PARTICIPANT", "QUIZ_CREATOR", "ADMIN"};
        String selectedRole = (String) JOptionPane.showInputDialog(
                this,
                "Select new role for user '" + username + "':",
                "Change User Role",
                JOptionPane.QUESTION_MESSAGE,
                null,
                roles,
                currentRole
        );

        if (selectedRole != null && !selectedRole.equalsIgnoreCase(currentRole)) {
            SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
                @Override
                protected Boolean doInBackground() throws DatabaseOperationException {
                    return userDAO.updateUserRole(userId, selectedRole);
                }

                @Override
                protected void done() {
                    try {
                        boolean ok = get();
                        if (ok) {
                            JOptionPane.showMessageDialog(AdminDashboardPanel.this,
                                    "User role updated to " + selectedRole, "Success", JOptionPane.INFORMATION_MESSAGE);
                            loadUsersAsync();
                        }
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(AdminDashboardPanel.this,
                                "Failed to update role: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            };
            worker.execute();
        }
    }

    private void handleToggleUserStatus() {
        int row = usersTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a user to change account status.",
                    "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int userId = (Integer) usersModel.getValueAt(row, 0);
        String username = (String) usersModel.getValueAt(row, 1);
        String currentStatus = (String) usersModel.getValueAt(row, 4);

        String newStatus = "ACTIVE".equalsIgnoreCase(currentStatus) ? "SUSPENDED" : "ACTIVE";

        int confirm = JOptionPane.showConfirmDialog(this,
                "Change account status for '" + username + "' to " + newStatus + "?",
                "Confirm Status Change", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
            @Override
            protected Boolean doInBackground() throws DatabaseOperationException {
                return userDAO.updateUserStatus(userId, newStatus);
            }

            @Override
            protected void done() {
                try {
                    boolean ok = get();
                    if (ok) {
                        JOptionPane.showMessageDialog(AdminDashboardPanel.this,
                                "User status updated to " + newStatus, "Success", JOptionPane.INFORMATION_MESSAGE);
                        loadUsersAsync();
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(AdminDashboardPanel.this,
                            "Failed to update status: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    private void handleDeleteUser() {
        int row = usersTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a user to delete.",
                    "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int userId = (Integer) usersModel.getValueAt(row, 0);
        String username = (String) usersModel.getValueAt(row, 1);

        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete user '" + username + "' (ID: " + userId + ")?\n" +
                "All associated records and results will be permanently removed.",
                "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
            @Override
            protected Boolean doInBackground() throws DatabaseOperationException {
                return userDAO.deleteUser(userId);
            }

            @Override
            protected void done() {
                try {
                    boolean ok = get();
                    if (ok) {
                        JOptionPane.showMessageDialog(AdminDashboardPanel.this,
                                "User deleted successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                        loadUsersAsync();
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(AdminDashboardPanel.this,
                            "Failed to delete user: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    // =========================================================================
    // TAB 2: CONTENT APPROVAL WORKFLOW
    // =========================================================================
    private JPanel buildContentApprovalTab() {
        JPanel panel = new JPanel(new BorderLayout(0, 12));
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(16, 16, 16, 16));

        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setOpaque(false);
        JLabel title = new JLabel("Pending Question Approval Queue");
        title.setFont(UITheme.FONT_SUBTITLE);
        title.setForeground(UITheme.SLATE_DARK);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setOpaque(false);

        JButton approveBtn = UITheme.createSuccessButton("Approve Question");
        approveBtn.addActionListener(e -> updateSelectedQuestionStatus("APPROVED"));

        JButton rejectBtn = UITheme.createDangerButton("Reject Question");
        rejectBtn.addActionListener(e -> updateSelectedQuestionStatus("REJECTED"));

        JButton refreshBtn = UITheme.createSecondaryButton("Refresh Queue");
        refreshBtn.addActionListener(e -> loadPendingQuestionsAsync());

        actions.add(approveBtn);
        actions.add(rejectBtn);
        actions.add(refreshBtn);

        topBar.add(title, BorderLayout.WEST);
        topBar.add(actions, BorderLayout.EAST);

        approvalCardLayout = new CardLayout();
        approvalCardPanel = new JPanel(approvalCardLayout);
        approvalCardPanel.setBackground(Color.WHITE);

        String[] cols = {"ID", "Creator ID", "Subject", "Question Text", "Correct", "Duration (s)", "Status"};
        approvalModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };

        approvalTable = new JTable(approvalModel);
        UITheme.styleTable(approvalTable);

        JScrollPane scroll = new JScrollPane(approvalTable);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(Color.WHITE);

        JPanel emptyPanel = UITheme.createEmptyPlaceholder("No pending questions awaiting approval. All submissions are up to date!");

        approvalCardPanel.add(scroll, VIEW_TABLE);
        approvalCardPanel.add(emptyPanel, VIEW_EMPTY);
        approvalCardLayout.show(approvalCardPanel, VIEW_EMPTY);

        panel.add(topBar, BorderLayout.NORTH);
        panel.add(approvalCardPanel, BorderLayout.CENTER);
        return panel;
    }

    private void updateSelectedQuestionStatus(String targetStatus) {
        int row = approvalTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a pending question to " + targetStatus.toLowerCase() + ".",
                    "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int questionId = (Integer) approvalModel.getValueAt(row, 0);

        SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
            @Override
            protected Boolean doInBackground() throws DatabaseOperationException {
                return questionDAO.updateQuestionStatus(questionId, targetStatus);
            }

            @Override
            protected void done() {
                try {
                    boolean ok = get();
                    if (ok) {
                        JOptionPane.showMessageDialog(AdminDashboardPanel.this,
                                "Question #" + questionId + " marked as " + targetStatus + ".",
                                "Status Updated", JOptionPane.INFORMATION_MESSAGE);
                        loadPendingQuestionsAsync();
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(AdminDashboardPanel.this,
                            "Failed to update question status: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    // =========================================================================
    // TAB 3: GLOBAL PERFORMANCE REPORTS
    // =========================================================================
    private JPanel buildPerformanceReportsTab() {
        JPanel panel = new JPanel(new BorderLayout(0, 16));
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(16, 16, 16, 16));

        // Aggregated Metrics Banner
        JPanel metricsPanel = new JPanel(new GridLayout(1, 3, 16, 0));
        metricsPanel.setOpaque(false);

        totalAttemptsValLabel = new JLabel("0", SwingConstants.CENTER);
        avgScoreValLabel = new JLabel("0.0%", SwingConstants.CENTER);
        totalQuestionsValLabel = new JLabel("0", SwingConstants.CENTER);

        metricsPanel.add(createMetricCard("Total Quizzes Attempted", totalAttemptsValLabel, UITheme.PRIMARY));
        metricsPanel.add(createMetricCard("Platform Average Score", avgScoreValLabel, UITheme.SUCCESS));
        metricsPanel.add(createMetricCard("Questions in Bank", totalQuestionsValLabel, UITheme.SLATE_DARK));

        // Table Header
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setOpaque(false);
        JLabel title = new JLabel("Global Student Attempt History");
        title.setFont(UITheme.FONT_SUBTITLE);
        title.setForeground(UITheme.SLATE_DARK);

        JButton refreshScoresBtn = UITheme.createSecondaryButton("Refresh Reports");
        refreshScoresBtn.addActionListener(e -> loadPerformanceReportsAsync());

        topBar.add(title, BorderLayout.WEST);
        topBar.add(refreshScoresBtn, BorderLayout.EAST);

        scoreHistoryCardLayout = new CardLayout();
        scoreHistoryCardPanel = new JPanel(scoreHistoryCardLayout);
        scoreHistoryCardPanel.setBackground(Color.WHITE);

        String[] cols = {"Attempt ID", "Student Name", "Subject", "Score", "Percentage", "Grade", "Attempt Date"};
        scoreHistoryModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };

        scoreHistoryTable = new JTable(scoreHistoryModel);
        UITheme.styleTable(scoreHistoryTable);

        JScrollPane scroll = new JScrollPane(scoreHistoryTable);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(Color.WHITE);

        JPanel emptyPanel = UITheme.createEmptyPlaceholder("No quiz attempt records found in database.");

        scoreHistoryCardPanel.add(scroll, VIEW_TABLE);
        scoreHistoryCardPanel.add(emptyPanel, VIEW_EMPTY);
        scoreHistoryCardLayout.show(scoreHistoryCardPanel, VIEW_EMPTY);

        JPanel centerPanel = new JPanel(new BorderLayout(0, 12));
        centerPanel.setOpaque(false);
        centerPanel.add(topBar, BorderLayout.NORTH);
        centerPanel.add(scoreHistoryCardPanel, BorderLayout.CENTER);

        panel.add(metricsPanel, BorderLayout.NORTH);
        panel.add(centerPanel, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createMetricCard(String title, JLabel valueLabel, Color accentColor) {
        JPanel card = UITheme.createCardPanel();
        card.setLayout(new BorderLayout(0, 6));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xE2E8F0), 1),
                new EmptyBorder(12, 16, 12, 16)
        ));

        JLabel titleLabel = new JLabel(title, SwingConstants.CENTER);
        titleLabel.setFont(UITheme.FONT_SMALL);
        titleLabel.setForeground(UITheme.TEXT_MUTED);

        valueLabel.setFont(UITheme.FONT_HEADER_LARGE);
        valueLabel.setForeground(accentColor);

        card.add(titleLabel, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        return card;
    }

    // =========================================================================
    // TAB 4: SYSTEM SETTINGS
    // =========================================================================
    private JPanel buildSystemSettingsTab() {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(Color.WHITE);
        wrapper.setBorder(new EmptyBorder(24, 28, 24, 28));

        JPanel formCard = UITheme.createCardPanel();
        formCard.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 10, 8, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        timeLimitField = UITheme.createTextField(15);
        maintenanceModeComboBox = new JComboBox<>(new String[]{"false", "true"});
        maintenanceModeComboBox.setFont(UITheme.FONT_BODY);
        maintenanceModeComboBox.setBackground(Color.WHITE);

        JButton saveBtn = UITheme.createSuccessButton("Save Settings");
        saveBtn.addActionListener(e -> saveSettings());

        int row = 0;
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0.3;
        JLabel timeLabel = new JLabel("Default Question Time Limit (seconds):");
        timeLabel.setFont(UITheme.FONT_BODY_BOLD);
        formCard.add(timeLabel, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.7;
        formCard.add(timeLimitField, gbc);

        row++;
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0.3;
        JLabel maintLabel = new JLabel("Maintenance Mode (block student access):");
        maintLabel.setFont(UITheme.FONT_BODY_BOLD);
        formCard.add(maintLabel, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.7;
        formCard.add(maintenanceModeComboBox, gbc);

        row++;
        gbc.gridx = 1;
        gbc.gridy = row;
        formCard.add(saveBtn, gbc);

        wrapper.add(formCard, BorderLayout.NORTH);
        return wrapper;
    }

    private void saveSettings() {
        String timeLimit = timeLimitField.getText().trim();
        String maintenance = (String) maintenanceModeComboBox.getSelectedItem();

        if (timeLimit.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a valid time limit in seconds.",
                    "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
            @Override
            protected Boolean doInBackground() throws DatabaseOperationException {
                boolean b1 = settingsDAO.updateSetting("DEFAULT_TIME_LIMIT", timeLimit);
                boolean b2 = settingsDAO.updateSetting("MAINTENANCE_MODE", maintenance);
                return b1 && b2;
            }

            @Override
            protected void done() {
                try {
                    boolean ok = get();
                    if (ok) {
                        JOptionPane.showMessageDialog(AdminDashboardPanel.this,
                                "System settings successfully updated!", "Settings Saved", JOptionPane.INFORMATION_MESSAGE);
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(AdminDashboardPanel.this,
                            "Failed to update settings: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    public void refreshData() {
        User admin = mainFrame.getAuthService().getCurrentUser();
        if (admin != null) {
            adminWelcomeLabel.setText("Administrator Dashboard - Welcome, " + admin.getUsername() + "!");
        }
        loadUsersAsync();
        loadPendingQuestionsAsync();
        loadPerformanceReportsAsync();
        loadSettingsAsync();
    }

    private void loadUsersAsync() {
        SwingWorker<List<User>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<User> doInBackground() throws DatabaseOperationException {
                return userDAO.getAllUsers();
            }

            @Override
            protected void done() {
                try {
                    List<User> users = get();
                    usersModel.setRowCount(0);
                    if (users == null || users.isEmpty()) {
                        usersCardLayout.show(usersCardPanel, VIEW_EMPTY);
                    } else {
                        for (User u : users) {
                            usersModel.addRow(new Object[]{
                                    u.getUserId(),
                                    u.getUsername(),
                                    u.getEmail() != null ? u.getEmail() : "N/A",
                                    u.getRole(),
                                    u.getStatus()
                            });
                        }
                        usersCardLayout.show(usersCardPanel, VIEW_TABLE);
                    }
                } catch (Exception ignored) {
                    usersCardLayout.show(usersCardPanel, VIEW_EMPTY);
                }
            }
        };
        worker.execute();
    }

    private void loadPendingQuestionsAsync() {
        SwingWorker<List<Question>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Question> doInBackground() throws DatabaseOperationException {
                return questionDAO.getPendingQuestions();
            }

            @Override
            protected void done() {
                try {
                    List<Question> list = get();
                    approvalModel.setRowCount(0);
                    if (list == null || list.isEmpty()) {
                        approvalCardLayout.show(approvalCardPanel, VIEW_EMPTY);
                    } else {
                        for (Question q : list) {
                            approvalModel.addRow(new Object[]{
                                    q.getQuestionId(),
                                    q.getCreatorId(),
                                    q.getSubject(),
                                    q.getQuestionText(),
                                    String.valueOf(q.getCorrectOption()),
                                    q.getDurationSeconds(),
                                    q.getStatus()
                            });
                        }
                        approvalCardLayout.show(approvalCardPanel, VIEW_TABLE);
                    }
                } catch (Exception ignored) {
                    approvalCardLayout.show(approvalCardPanel, VIEW_EMPTY);
                }
            }
        };
        worker.execute();
    }

    private void loadPerformanceReportsAsync() {
        SwingWorker<List<QuizResult>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<QuizResult> doInBackground() throws DatabaseOperationException {
                return resultDAO.getAllResults();
            }

            @Override
            protected void done() {
                try {
                    List<QuizResult> list = get();
                    scoreHistoryModel.setRowCount(0);

                    int totalAttempts = list != null ? list.size() : 0;
                    double totalScorePct = 0.0;

                    if (list == null || list.isEmpty()) {
                        scoreHistoryCardLayout.show(scoreHistoryCardPanel, VIEW_EMPTY);
                    } else {
                        for (QuizResult r : list) {
                            totalScorePct += r.getPercentage();
                            String dateStr = (r.getAttemptDate() != null)
                                    ? r.getAttemptDate().toString().substring(0, 19)
                                    : "N/A";
                            scoreHistoryModel.addRow(new Object[]{
                                    "#" + r.getResultId(),
                                    r.getUsername() != null ? r.getUsername() : "User #" + r.getUserId(),
                                    r.getSubject(),
                                    r.getScore() + " / " + r.getTotalQuestions(),
                                    String.format("%.2f%%", r.getPercentage()),
                                    r.getGrade(),
                                    dateStr
                            });
                        }
                        scoreHistoryCardLayout.show(scoreHistoryCardPanel, VIEW_TABLE);
                    }

                    totalAttemptsValLabel.setText(String.valueOf(totalAttempts));
                    double avgScore = totalAttempts > 0 ? (totalScorePct / totalAttempts) : 0.0;
                    avgScoreValLabel.setText(String.format("%.1f%%", avgScore));

                    loadTotalQuestionCountAsync();
                } catch (Exception ignored) {
                    scoreHistoryCardLayout.show(scoreHistoryCardPanel, VIEW_EMPTY);
                }
            }
        };
        worker.execute();
    }

    private void loadTotalQuestionCountAsync() {
        SwingWorker<Integer, Void> worker = new SwingWorker<>() {
            @Override
            protected Integer doInBackground() throws DatabaseOperationException {
                return questionDAO.getAllQuestions().size();
            }

            @Override
            protected void done() {
                try {
                    int count = get();
                    totalQuestionsValLabel.setText(String.valueOf(count));
                } catch (Exception ignored) {}
            }
        };
        worker.execute();
    }

    private void loadSettingsAsync() {
        SwingWorker<Map<String, String>, Void> worker = new SwingWorker<>() {
            @Override
            protected Map<String, String> doInBackground() throws DatabaseOperationException {
                return settingsDAO.getAllSettings();
            }

            @Override
            protected void done() {
                try {
                    Map<String, String> settings = get();
                    String timeLimit = settings.getOrDefault("DEFAULT_TIME_LIMIT", "30");
                    String maint = settings.getOrDefault("MAINTENANCE_MODE", "false");

                    timeLimitField.setText(timeLimit);
                    maintenanceModeComboBox.setSelectedItem(maint.equalsIgnoreCase("true") ? "true" : "false");
                } catch (Exception ignored) {}
            }
        };
        worker.execute();
    }
}
