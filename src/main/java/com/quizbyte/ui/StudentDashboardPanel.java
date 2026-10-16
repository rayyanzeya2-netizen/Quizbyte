package com.quizbyte.ui;

import com.quizbyte.dao.MessageDAO;
import com.quizbyte.dao.QuestionDAO;
import com.quizbyte.dao.ReminderDAO;
import com.quizbyte.dao.ResultDAO;
import com.quizbyte.dao.UserDAO;
import com.quizbyte.exception.DatabaseOperationException;
import com.quizbyte.model.Message;
import com.quizbyte.model.QuizReminder;
import com.quizbyte.model.QuizResult;
import com.quizbyte.model.User;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.List;

/**
 * Dashboard panel for logged-in participants/students.
 * Includes quiz launcher, performance history, creator messaging inquiries,
 * study reminders planner, and global leaderboard.
 */
public class StudentDashboardPanel extends JPanel {

    private static final String VIEW_TABLE = "TABLE";
    private static final String VIEW_EMPTY = "EMPTY";

    private final MainFrame mainFrame;
    private final QuestionDAO questionDAO;
    private final ResultDAO resultDAO;
    private final UserDAO userDAO;
    private final MessageDAO messageDAO;
    private final ReminderDAO reminderDAO;

    private JLabel welcomeLabel;
    private JLabel roleBadgeLabel;

    // Tab 1: Quiz Launcher
    private JComboBox<String> subjectComboBox;
    private JLabel questionCountLabel;
    private JButton startQuizButton;

    // Tab 2: Past Attempts Components
    private CardLayout pastAttemptsCardLayout;
    private JPanel pastAttemptsCardPanel;
    private JTable pastAttemptsTable;
    private DefaultTableModel pastAttemptsModel;

    // Tab 3: Creator Inquiries Components
    private JComboBox<UserItem> creatorPicker;
    private JTextArea inquiryTextArea;
    private CardLayout inquiriesCardLayout;
    private JPanel inquiriesCardPanel;
    private JTable inquiriesTable;
    private DefaultTableModel inquiriesModel;

    // Tab 4: Study Reminders Components
    private JTextField reminderSubjectField;
    private JTextField reminderDateField;
    private JTextField reminderNotesField;
    private CardLayout remindersCardLayout;
    private JPanel remindersCardPanel;
    private JTable remindersTable;
    private DefaultTableModel remindersModel;

    // Tab 5: Leaderboard Components
    private CardLayout leaderboardCardLayout;
    private JPanel leaderboardCardPanel;
    private JTable leaderboardTable;
    private DefaultTableModel leaderboardModel;

    public StudentDashboardPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        this.questionDAO = mainFrame.getQuestionDAO();
        this.resultDAO = mainFrame.getResultDAO();
        this.userDAO = mainFrame.getUserDAO();
        this.messageDAO = mainFrame.getMessageDAO();
        this.reminderDAO = mainFrame.getReminderDAO();

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

        welcomeLabel = new JLabel("Welcome, Participant!");
        welcomeLabel.setFont(UITheme.FONT_TITLE);
        welcomeLabel.setForeground(UITheme.SLATE_DARK);

        roleBadgeLabel = new JLabel("[PARTICIPANT]");
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

        // Subject picker & Quiz Launcher Card
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

        startQuizButton = UITheme.createPrimaryButton("Start Quiz");
        startQuizButton.addActionListener(e -> {
            String selectedSubject = (String) subjectComboBox.getSelectedItem();
            if (selectedSubject != null) {
                mainFrame.startQuiz(selectedSubject);
            }
        });

        JButton refreshBtn = UITheme.createSecondaryButton("Refresh Topics");
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
        tabbedPane.addTab("  Creator Inquiries  ", buildInquiriesPanel());
        tabbedPane.addTab("  Study Reminders  ", buildRemindersPanel());
        tabbedPane.addTab("  Global Leaderboard  ", buildLeaderboardPanel());

        tabbedPane.addChangeListener(e -> {
            int idx = tabbedPane.getSelectedIndex();
            if (idx == 1) {
                loadInquiriesAsync();
                loadCreatorsAsync();
            } else if (idx == 2) {
                loadRemindersAsync();
            } else if (idx == 3) {
                loadLeaderboardAsync();
            }
        });

        container.add(launcherCard, BorderLayout.NORTH);
        container.add(tabbedPane, BorderLayout.CENTER);

        return container;
    }

    // =========================================================================
    // TAB 1: PAST ATTEMPTS
    // =========================================================================
    private JPanel buildPastAttemptsPanel() {
        pastAttemptsCardLayout = new CardLayout();
        pastAttemptsCardPanel = new JPanel(pastAttemptsCardLayout);
        pastAttemptsCardPanel.setBackground(Color.WHITE);

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

        JPanel emptyPanel = UITheme.createEmptyPlaceholder("No past attempts found. Select a subject and start a quiz!");

        pastAttemptsCardPanel.add(scrollPane, VIEW_TABLE);
        pastAttemptsCardPanel.add(emptyPanel, VIEW_EMPTY);

        pastAttemptsCardLayout.show(pastAttemptsCardPanel, VIEW_EMPTY);
        return pastAttemptsCardPanel;
    }

    // =========================================================================
    // TAB 2: CREATOR INQUIRIES
    // =========================================================================
    private JPanel buildInquiriesPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 12));
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(16, 16, 16, 16));

        // Form to send message
        JPanel formCard = UITheme.createCardPanel();
        formCard.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 8, 4, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        creatorPicker = new JComboBox<>();
        creatorPicker.setFont(UITheme.FONT_BODY);
        creatorPicker.setBackground(Color.WHITE);

        inquiryTextArea = new JTextArea(2, 25);
        inquiryTextArea.setFont(UITheme.FONT_BODY);
        inquiryTextArea.setLineWrap(true);
        inquiryTextArea.setWrapStyleWord(true);
        JScrollPane textScroll = new JScrollPane(inquiryTextArea);

        JButton sendBtn = UITheme.createPrimaryButton("Send Message to Creator");
        sendBtn.addActionListener(e -> sendInquiry());

        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.2;
        formCard.add(new JLabel("Select Quiz Creator:"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.8;
        formCard.add(creatorPicker, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.2;
        formCard.add(new JLabel("Inquiry / Feedback:"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.8;
        formCard.add(textScroll, gbc);

        gbc.gridx = 1; gbc.gridy = 2;
        formCard.add(sendBtn, gbc);

        // Inquiries Table
        inquiriesCardLayout = new CardLayout();
        inquiriesCardPanel = new JPanel(inquiriesCardLayout);
        inquiriesCardPanel.setBackground(Color.WHITE);

        String[] cols = {"Msg ID", "Recipient", "Quiz ID", "Your Message", "Creator Reply", "Sent Date"};
        inquiriesModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };

        inquiriesTable = new JTable(inquiriesModel);
        UITheme.styleTable(inquiriesTable);

        JScrollPane scroll = new JScrollPane(inquiriesTable);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(Color.WHITE);

        JPanel emptyPanel = UITheme.createEmptyPlaceholder("No inquiries sent yet. Reach out to a quiz creator above!");

        inquiriesCardPanel.add(scroll, VIEW_TABLE);
        inquiriesCardPanel.add(emptyPanel, VIEW_EMPTY);
        inquiriesCardLayout.show(inquiriesCardPanel, VIEW_EMPTY);

        panel.add(formCard, BorderLayout.NORTH);
        panel.add(inquiriesCardPanel, BorderLayout.CENTER);
        return panel;
    }

    private void sendInquiry() {
        User student = mainFrame.getAuthService().getCurrentUser();
        if (student == null) return;

        UserItem selectedCreator = (UserItem) creatorPicker.getSelectedItem();
        if (selectedCreator == null) {
            JOptionPane.showMessageDialog(this, "No quiz creator available to message.",
                    "Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String text = inquiryTextArea.getText().trim();
        if (text.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter your message before sending.",
                    "Input Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Message msg = new Message(student.getUserId(), selectedCreator.userId, null, text);

        SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
            @Override
            protected Boolean doInBackground() throws DatabaseOperationException {
                return messageDAO.sendMessage(msg);
            }

            @Override
            protected void done() {
                try {
                    boolean ok = get();
                    if (ok) {
                        JOptionPane.showMessageDialog(StudentDashboardPanel.this,
                                "Message successfully sent to " + selectedCreator.username + "!",
                                "Message Sent", JOptionPane.INFORMATION_MESSAGE);
                        inquiryTextArea.setText("");
                        loadInquiriesAsync();
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(StudentDashboardPanel.this,
                            "Failed to send message: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    // =========================================================================
    // TAB 3: STUDY REMINDERS
    // =========================================================================
    private JPanel buildRemindersPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 12));
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(16, 16, 16, 16));

        // Add Reminder Form
        JPanel formCard = UITheme.createCardPanel();
        formCard.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 8, 4, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        reminderSubjectField = UITheme.createTextField(15);
        reminderDateField = UITheme.createTextField(12);
        reminderDateField.setToolTipText("Format: YYYY-MM-DD");
        reminderNotesField = UITheme.createTextField(25);

        // Pre-fill current date + 3 days as hint
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        long threeDaysLater = System.currentTimeMillis() + (3L * 24 * 60 * 60 * 1000);
        reminderDateField.setText(sdf.format(new java.util.Date(threeDaysLater)));

        JButton addReminderBtn = UITheme.createSuccessButton("Set Reminder");
        addReminderBtn.addActionListener(e -> addReminder());

        JButton deleteReminderBtn = UITheme.createDangerButton("Delete Reminder");
        deleteReminderBtn.addActionListener(e -> deleteSelectedReminder());

        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.15;
        formCard.add(new JLabel("Subject:"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.35;
        formCard.add(reminderSubjectField, gbc);

        gbc.gridx = 2; gbc.weightx = 0.15;
        formCard.add(new JLabel("Target Date (YYYY-MM-DD):"), gbc);
        gbc.gridx = 3; gbc.weightx = 0.35;
        formCard.add(reminderDateField, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.15;
        formCard.add(new JLabel("Study Notes:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 3; gbc.weightx = 0.85;
        formCard.add(reminderNotesField, gbc);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        btnPanel.setOpaque(false);
        btnPanel.add(addReminderBtn);
        btnPanel.add(deleteReminderBtn);

        gbc.gridx = 1; gbc.gridy = 2; gbc.gridwidth = 3;
        formCard.add(btnPanel, gbc);

        // Reminders Table
        remindersCardLayout = new CardLayout();
        remindersCardPanel = new JPanel(remindersCardLayout);
        remindersCardPanel.setBackground(Color.WHITE);

        String[] cols = {"Reminder ID", "Subject", "Notes", "Target Date"};
        remindersModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };

        remindersTable = new JTable(remindersModel);
        UITheme.styleTable(remindersTable);

        JScrollPane scroll = new JScrollPane(remindersTable);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(Color.WHITE);

        JPanel emptyPanel = UITheme.createEmptyPlaceholder("No study reminders scheduled. Add your first study plan above!");

        remindersCardPanel.add(scroll, VIEW_TABLE);
        remindersCardPanel.add(emptyPanel, VIEW_EMPTY);
        remindersCardLayout.show(remindersCardPanel, VIEW_EMPTY);

        panel.add(formCard, BorderLayout.NORTH);
        panel.add(remindersCardPanel, BorderLayout.CENTER);
        return panel;
    }

    private void addReminder() {
        User user = mainFrame.getAuthService().getCurrentUser();
        if (user == null) return;

        String subject = reminderSubjectField.getText().trim();
        String dateStr = reminderDateField.getText().trim();
        String notes = reminderNotesField.getText().trim();

        if (subject.isEmpty() || dateStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter both Subject and Target Date (YYYY-MM-DD).",
                    "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Date targetDate;
        try {
            targetDate = Date.valueOf(dateStr);
        } catch (IllegalArgumentException e) {
            JOptionPane.showMessageDialog(this, "Invalid date format. Please use YYYY-MM-DD (e.g. 2026-11-01).",
                    "Format Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        QuizReminder reminder = new QuizReminder(user.getUserId(), subject, notes, targetDate);

        SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
            @Override
            protected Boolean doInBackground() throws DatabaseOperationException {
                return reminderDAO.addReminder(reminder);
            }

            @Override
            protected void done() {
                try {
                    boolean ok = get();
                    if (ok) {
                        JOptionPane.showMessageDialog(StudentDashboardPanel.this,
                                "Quiz reminder saved for " + dateStr + "!", "Reminder Created", JOptionPane.INFORMATION_MESSAGE);
                        reminderSubjectField.setText("");
                        reminderNotesField.setText("");
                        loadRemindersAsync();
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(StudentDashboardPanel.this,
                            "Failed to save reminder: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    private void deleteSelectedReminder() {
        int row = remindersTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a reminder to delete.",
                    "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int reminderId = (Integer) remindersModel.getValueAt(row, 0);

        SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
            @Override
            protected Boolean doInBackground() throws DatabaseOperationException {
                return reminderDAO.deleteReminder(reminderId);
            }

            @Override
            protected void done() {
                try {
                    boolean ok = get();
                    if (ok) {
                        loadRemindersAsync();
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(StudentDashboardPanel.this,
                            "Failed to delete reminder: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    // =========================================================================
    // TAB 4: LEADERBOARD
    // =========================================================================
    private JPanel buildLeaderboardPanel() {
        leaderboardCardLayout = new CardLayout();
        leaderboardCardPanel = new JPanel(leaderboardCardLayout);
        leaderboardCardPanel.setBackground(Color.WHITE);

        String[] columns = {"Rank", "Participant", "Subject", "Score", "Percentage", "Grade"};
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

        JPanel emptyPanel = UITheme.createEmptyPlaceholder("No quiz attempts recorded on the leaderboard yet.");

        leaderboardCardPanel.add(scrollPane, VIEW_TABLE);
        leaderboardCardPanel.add(emptyPanel, VIEW_EMPTY);

        leaderboardCardLayout.show(leaderboardCardPanel, VIEW_EMPTY);
        return leaderboardCardPanel;
    }

    // =========================================================================
    // DATA LOADING HELPERS
    // =========================================================================
    public void refreshData() {
        User user = mainFrame.getAuthService().getCurrentUser();
        if (user != null) {
            welcomeLabel.setText("Welcome back, " + user.getUsername() + "!");
            roleBadgeLabel.setText(user.getRoleBadge());
        }

        loadSubjectsAsync();
        loadPastAttemptsAsync(user != null ? user.getUserId() : -1);
        loadInquiriesAsync();
        loadCreatorsAsync();
        loadRemindersAsync();
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
                    if (results == null || results.isEmpty()) {
                        pastAttemptsCardLayout.show(pastAttemptsCardPanel, VIEW_EMPTY);
                    } else {
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
                        pastAttemptsCardLayout.show(pastAttemptsCardPanel, VIEW_TABLE);
                    }
                } catch (Exception ignored) {
                    pastAttemptsCardLayout.show(pastAttemptsCardPanel, VIEW_EMPTY);
                }
            }
        };
        worker.execute();
    }

    private void loadCreatorsAsync() {
        SwingWorker<List<User>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<User> doInBackground() throws DatabaseOperationException {
                return userDAO.getUsersByRole("QUIZ_CREATOR");
            }

            @Override
            protected void done() {
                try {
                    List<User> creators = get();
                    creatorPicker.removeAllItems();
                    for (User c : creators) {
                        creatorPicker.addItem(new UserItem(c.getUserId(), c.getUsername()));
                    }
                } catch (Exception ignored) {}
            }
        };
        worker.execute();
    }

    private void loadInquiriesAsync() {
        User user = mainFrame.getAuthService().getCurrentUser();
        if (user == null) return;

        SwingWorker<List<Message>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Message> doInBackground() throws DatabaseOperationException {
                return messageDAO.getMessagesForUser(user.getUserId());
            }

            @Override
            protected void done() {
                try {
                    List<Message> list = get();
                    inquiriesModel.setRowCount(0);
                    if (list == null || list.isEmpty()) {
                        inquiriesCardLayout.show(inquiriesCardPanel, VIEW_EMPTY);
                    } else {
                        for (Message m : list) {
                            String dateStr = (m.getCreatedAt() != null)
                                    ? m.getCreatedAt().toString().substring(0, 19)
                                    : "N/A";
                            inquiriesModel.addRow(new Object[]{
                                    m.getMessageId(),
                                    m.getReceiverUsername(),
                                    m.getQuizId() != null ? "#" + m.getQuizId() : "General",
                                    m.getMessageText(),
                                    m.getReplyText() != null ? m.getReplyText() : "(Awaiting Reply)",
                                    dateStr
                            });
                        }
                        inquiriesCardLayout.show(inquiriesCardPanel, VIEW_TABLE);
                    }
                } catch (Exception ignored) {
                    inquiriesCardLayout.show(inquiriesCardPanel, VIEW_EMPTY);
                }
            }
        };
        worker.execute();
    }

    private void loadRemindersAsync() {
        User user = mainFrame.getAuthService().getCurrentUser();
        if (user == null) return;

        SwingWorker<List<QuizReminder>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<QuizReminder> doInBackground() throws DatabaseOperationException {
                return reminderDAO.getRemindersByUser(user.getUserId());
            }

            @Override
            protected void done() {
                try {
                    List<QuizReminder> list = get();
                    remindersModel.setRowCount(0);
                    if (list == null || list.isEmpty()) {
                        remindersCardLayout.show(remindersCardPanel, VIEW_EMPTY);
                    } else {
                        for (QuizReminder r : list) {
                            remindersModel.addRow(new Object[]{
                                    r.getReminderId(),
                                    r.getSubject(),
                                    r.getNotes() != null ? r.getNotes() : "",
                                    r.getTargetDate().toString()
                            });
                        }
                        remindersCardLayout.show(remindersCardPanel, VIEW_TABLE);
                    }
                } catch (Exception ignored) {
                    remindersCardLayout.show(remindersCardPanel, VIEW_EMPTY);
                }
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
                    if (leaderboard == null || leaderboard.isEmpty()) {
                        leaderboardCardLayout.show(leaderboardCardPanel, VIEW_EMPTY);
                    } else {
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
                        leaderboardCardLayout.show(leaderboardCardPanel, VIEW_TABLE);
                    }
                } catch (Exception ignored) {
                    leaderboardCardLayout.show(leaderboardCardPanel, VIEW_EMPTY);
                }
            }
        };
        worker.execute();
    }

    // Helper record/class for combobox display
    private static class UserItem {
        final int userId;
        final String username;

        UserItem(int userId, String username) {
            this.userId = userId;
            this.username = username;
        }

        @Override
        public String toString() {
            return username + " (ID: #" + userId + ")";
        }
    }
}
