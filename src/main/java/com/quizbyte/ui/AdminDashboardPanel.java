package com.quizbyte.ui;

import com.quizbyte.dao.QuestionDAO;
import com.quizbyte.dao.ResultDAO;
import com.quizbyte.dao.UserDAO;
import com.quizbyte.model.Question;
import com.quizbyte.model.QuizResult;
import com.quizbyte.model.User;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Administrator dashboard for managing questions, inspecting registered users,
 * and reviewing global quiz score history.
 */
public class AdminDashboardPanel extends JPanel {

    private final MainFrame mainFrame;
    private final QuestionDAO questionDAO;
    private final UserDAO userDAO;
    private final ResultDAO resultDAO;

    private JLabel adminWelcomeLabel;

    // Tab 1: Add Question Form Components
    private JTextField subjectField;
    private JTextArea questionArea;
    private JTextField optionAField;
    private JTextField optionBField;
    private JTextField optionCField;
    private JTextField optionDField;
    private JComboBox<String> correctOptionComboBox;
    private JButton saveQuestionBtn;

    // Tab 2: Users Table Components
    private JTable usersTable;
    private DefaultTableModel usersModel;

    // Tab 3: Global Score History Components
    private JTable scoreHistoryTable;
    private DefaultTableModel scoreHistoryModel;

    public AdminDashboardPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        this.questionDAO = mainFrame.getQuestionDAO();
        this.userDAO = mainFrame.getUserDAO();
        this.resultDAO = mainFrame.getResultDAO();

        setLayout(new BorderLayout(0, 16));
        setBackground(UITheme.BG_LIGHT);
        setBorder(new EmptyBorder(20, 28, 20, 28));

        add(buildHeaderPanel(), BorderLayout.NORTH);
        add(buildTabbedContent(), BorderLayout.CENTER);
    }

    private JPanel buildHeaderPanel() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        adminWelcomeLabel = new JLabel("Administrator Dashboard - Welcome, Admin!");
        adminWelcomeLabel.setFont(UITheme.FONT_TITLE);
        adminWelcomeLabel.setForeground(UITheme.SLATE_DARK);

        JButton logoutBtn = UITheme.createDangerButton("Log Out");
        logoutBtn.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(mainFrame,
                    "Are you sure you want to log out of Admin Dashboard?", "Confirm Logout",
                    JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                mainFrame.logout();
            }
        });

        header.add(adminWelcomeLabel, BorderLayout.WEST);
        header.add(logoutBtn, BorderLayout.EAST);
        return header;
    }

    private JTabbedPane buildTabbedContent() {
        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(UITheme.FONT_BODY_BOLD);
        tabs.setBackground(Color.WHITE);

        tabs.addTab("  ➕ Add New Question  ", buildAddQuestionTab());
        tabs.addTab("  👥 Registered Users  ", buildUsersTab());
        tabs.addTab("  📊 Global Score History  ", buildScoreHistoryTab());

        tabs.addChangeListener(e -> {
            int selectedIndex = tabs.getSelectedIndex();
            if (selectedIndex == 1) {
                loadUsersAsync();
            } else if (selectedIndex == 2) {
                loadScoreHistoryAsync();
            }
        });

        return tabs;
    }

    private JPanel buildAddQuestionTab() {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(Color.WHITE);
        wrapper.setBorder(new EmptyBorder(20, 24, 20, 24));

        JPanel formCard = UITheme.createCardPanel();
        formCard.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 8, 6, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        subjectField = UITheme.createTextField(25);
        questionArea = new JTextArea(3, 25);
        questionArea.setFont(UITheme.FONT_BODY);
        questionArea.setLineWrap(true);
        questionArea.setWrapStyleWord(true);
        JScrollPane questionScroll = new JScrollPane(questionArea);
        questionScroll.setBorder(subjectField.getBorder());

        optionAField = UITheme.createTextField(25);
        optionBField = UITheme.createTextField(25);
        optionCField = UITheme.createTextField(25);
        optionDField = UITheme.createTextField(25);

        correctOptionComboBox = new JComboBox<>(new String[]{"A", "B", "C", "D"});
        correctOptionComboBox.setFont(UITheme.FONT_BODY);
        correctOptionComboBox.setBackground(Color.WHITE);

        saveQuestionBtn = UITheme.createSuccessButton("Save Question to Bank");
        saveQuestionBtn.addActionListener(e -> saveNewQuestion());

        JButton clearBtn = UITheme.createSecondaryButton("Clear Form");
        clearBtn.addActionListener(e -> clearQuestionForm());

        // Layout rows
        int row = 0;
        addFormRow(formCard, gbc, row++, "Subject (e.g. Java, OOP, DBMS):", subjectField);
        addFormRow(formCard, gbc, row++, "Question Text:", questionScroll);
        addFormRow(formCard, gbc, row++, "Option [A]:", optionAField);
        addFormRow(formCard, gbc, row++, "Option [B]:", optionBField);
        addFormRow(formCard, gbc, row++, "Option [C]:", optionCField);
        addFormRow(formCard, gbc, row++, "Option [D]:", optionDField);
        addFormRow(formCard, gbc, row++, "Correct Answer:", correctOptionComboBox);

        // Buttons row
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        buttonsPanel.setOpaque(false);
        buttonsPanel.add(saveQuestionBtn);
        buttonsPanel.add(clearBtn);

        gbc.gridx = 1;
        gbc.gridy = row;
        gbc.gridwidth = 1;
        formCard.add(buttonsPanel, gbc);

        JScrollPane containerScroll = new JScrollPane(formCard);
        containerScroll.setBorder(BorderFactory.createEmptyBorder());
        containerScroll.getViewport().setBackground(Color.WHITE);

        wrapper.add(containerScroll, BorderLayout.CENTER);
        return wrapper;
    }

    private void addFormRow(JPanel panel, GridBagConstraints gbc, int row, String labelText, JComponent field) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 1;
        gbc.weightx = 0.25;
        JLabel label = new JLabel(labelText);
        label.setFont(UITheme.FONT_BODY_BOLD);
        label.setForeground(UITheme.TEXT_MAIN);
        panel.add(label, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.75;
        panel.add(field, gbc);
    }

    private JPanel buildUsersTab() {
        JPanel panel = new JPanel(new BorderLayout(0, 12));
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(16, 16, 16, 16));

        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setOpaque(false);
        JLabel title = new JLabel("System Users Registry");
        title.setFont(UITheme.FONT_SUBTITLE);
        title.setForeground(UITheme.SLATE_DARK);

        JButton refreshUsersBtn = UITheme.createSecondaryButton("↻  Refresh List");
        refreshUsersBtn.addActionListener(e -> loadUsersAsync());

        topBar.add(title, BorderLayout.WEST);
        topBar.add(refreshUsersBtn, BorderLayout.EAST);

        String[] cols = {"User ID", "Username", "Role"};
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

        panel.add(topBar, BorderLayout.NORTH);
        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildScoreHistoryTab() {
        JPanel panel = new JPanel(new BorderLayout(0, 12));
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(16, 16, 16, 16));

        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setOpaque(false);
        JLabel title = new JLabel("Global Student Attempt History");
        title.setFont(UITheme.FONT_SUBTITLE);
        title.setForeground(UITheme.SLATE_DARK);

        JButton refreshScoresBtn = UITheme.createSecondaryButton("↻  Refresh Records");
        refreshScoresBtn.addActionListener(e -> loadScoreHistoryAsync());

        topBar.add(title, BorderLayout.WEST);
        topBar.add(refreshScoresBtn, BorderLayout.EAST);

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

        panel.add(topBar, BorderLayout.NORTH);
        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

    private void saveNewQuestion() {
        String subject = subjectField.getText().trim();
        String questionText = questionArea.getText().trim();
        String optA = optionAField.getText().trim();
        String optB = optionBField.getText().trim();
        String optC = optionCField.getText().trim();
        String optD = optionDField.getText().trim();
        String correct = (String) correctOptionComboBox.getSelectedItem();

        if (subject.isEmpty() || questionText.isEmpty() || optA.isEmpty() || 
            optB.isEmpty() || optC.isEmpty() || optD.isEmpty() || correct == null) {
            JOptionPane.showMessageDialog(this,
                    "All fields are required. Please fill in the subject, question text, and all 4 options.",
                    "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        char correctChar = correct.charAt(0);
        Question newQuestion = new Question(subject, questionText, optA, optB, optC, optD, correctChar);

        saveQuestionBtn.setEnabled(false);
        saveQuestionBtn.setText("Saving...");

        SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
            @Override
            protected Boolean doInBackground() {
                return questionDAO.addQuestion(newQuestion);
            }

            @Override
            protected void done() {
                saveQuestionBtn.setEnabled(true);
                saveQuestionBtn.setText("Save Question to Bank");
                try {
                    boolean success = get();
                    if (success) {
                        JOptionPane.showMessageDialog(AdminDashboardPanel.this,
                                "Question saved successfully! (Question ID: #" + newQuestion.getQuestionId() + ")",
                                "Success", JOptionPane.INFORMATION_MESSAGE);
                        clearQuestionForm();
                    } else {
                        JOptionPane.showMessageDialog(AdminDashboardPanel.this,
                                "Failed to save question to the database.",
                                "Database Error", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(AdminDashboardPanel.this,
                            "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    private void clearQuestionForm() {
        subjectField.setText("");
        questionArea.setText("");
        optionAField.setText("");
        optionBField.setText("");
        optionCField.setText("");
        optionDField.setText("");
        correctOptionComboBox.setSelectedIndex(0);
    }

    public void refreshData() {
        User admin = mainFrame.getAuthService().getCurrentUser();
        if (admin != null) {
            adminWelcomeLabel.setText("Administrator Dashboard - Welcome, " + admin.getUsername() + "!");
        }
        loadUsersAsync();
        loadScoreHistoryAsync();
    }

    private void loadUsersAsync() {
        SwingWorker<List<User>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<User> doInBackground() {
                return userDAO.getAllUsers();
            }

            @Override
            protected void done() {
                try {
                    List<User> users = get();
                    usersModel.setRowCount(0);
                    for (User u : users) {
                        usersModel.addRow(new Object[]{
                                "#" + u.getUserId(),
                                u.getUsername(),
                                u.getRole()
                        });
                    }
                } catch (Exception ignored) {}
            }
        };
        worker.execute();
    }

    private void loadScoreHistoryAsync() {
        SwingWorker<List<QuizResult>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<QuizResult> doInBackground() {
                return resultDAO.getAllResults();
            }

            @Override
            protected void done() {
                try {
                    List<QuizResult> list = get();
                    scoreHistoryModel.setRowCount(0);
                    for (QuizResult r : list) {
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
                } catch (Exception ignored) {}
            }
        };
        worker.execute();
    }
}
