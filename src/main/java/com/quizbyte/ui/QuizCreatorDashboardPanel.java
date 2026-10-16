package com.quizbyte.ui;

import com.quizbyte.dao.MessageDAO;
import com.quizbyte.dao.QuestionDAO;
import com.quizbyte.exception.DatabaseOperationException;
import com.quizbyte.model.Message;
import com.quizbyte.model.Question;
import com.quizbyte.model.User;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Dashboard panel for Quiz Creators.
 * Allows authoring questions (submitted as PENDING for admin approval),
 * tracking approval statuses, and replying to participant inquiries.
 */
public class QuizCreatorDashboardPanel extends JPanel {

    private static final String VIEW_TABLE = "TABLE";
    private static final String VIEW_EMPTY = "EMPTY";

    private final MainFrame mainFrame;
    private final QuestionDAO questionDAO;
    private final MessageDAO messageDAO;

    private JLabel welcomeLabel;
    private JLabel roleBadgeLabel;

    // Tab 1: Create Question Components
    private JTextField subjectField;
    private JTextArea questionArea;
    private JTextField optionAField;
    private JTextField optionBField;
    private JTextField optionCField;
    private JTextField optionDField;
    private JComboBox<String> correctOptionComboBox;
    private JSpinner durationSpinner;
    private JButton submitQuestionBtn;

    // Tab 2: My Questions Components
    private CardLayout questionsCardLayout;
    private JPanel questionsCardPanel;
    private JTable questionsTable;
    private DefaultTableModel questionsModel;

    // Tab 3: Participant Inquiries Components
    private CardLayout inquiriesCardLayout;
    private JPanel inquiriesCardPanel;
    private JTable inquiriesTable;
    private DefaultTableModel inquiriesModel;

    public QuizCreatorDashboardPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        this.questionDAO = mainFrame.getQuestionDAO();
        this.messageDAO = mainFrame.getMessageDAO();

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

        welcomeLabel = new JLabel("Welcome, Quiz Creator!");
        welcomeLabel.setFont(UITheme.FONT_TITLE);
        welcomeLabel.setForeground(UITheme.SLATE_DARK);

        roleBadgeLabel = new JLabel("[QUIZ CREATOR]");
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

    private JTabbedPane buildTabbedContent() {
        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(UITheme.FONT_BODY_BOLD);
        tabs.setBackground(Color.WHITE);

        tabs.addTab("  Create New Question  ", buildCreateQuestionTab());
        tabs.addTab("  My Questions & Approval Status  ", buildMyQuestionsTab());
        tabs.addTab("  Participant Inquiries  ", buildInquiriesTab());

        tabs.addChangeListener(e -> {
            int idx = tabs.getSelectedIndex();
            if (idx == 1) {
                loadMyQuestionsAsync();
            } else if (idx == 2) {
                loadInquiriesAsync();
            }
        });

        return tabs;
    }

    private JPanel buildCreateQuestionTab() {
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

        durationSpinner = new JSpinner(new SpinnerNumberModel(30, 10, 300, 5));
        durationSpinner.setFont(UITheme.FONT_BODY);

        JLabel noticeLabel = new JLabel("Questions submitted will be marked PENDING until approved by an Administrator.");
        noticeLabel.setFont(UITheme.FONT_SMALL);
        noticeLabel.setForeground(new Color(0xD97706)); // Amber color

        submitQuestionBtn = UITheme.createSuccessButton("Submit for Approval");
        submitQuestionBtn.addActionListener(e -> submitQuestion());

        JButton clearBtn = UITheme.createSecondaryButton("Clear Form");
        clearBtn.addActionListener(e -> clearForm());

        int row = 0;
        addFormRow(formCard, gbc, row++, "Subject (e.g. Java, OOP, DBMS):", subjectField);
        addFormRow(formCard, gbc, row++, "Question Text:", questionScroll);
        addFormRow(formCard, gbc, row++, "Option [A]:", optionAField);
        addFormRow(formCard, gbc, row++, "Option [B]:", optionBField);
        addFormRow(formCard, gbc, row++, "Option [C]:", optionCField);
        addFormRow(formCard, gbc, row++, "Option [D]:", optionDField);
        addFormRow(formCard, gbc, row++, "Correct Option:", correctOptionComboBox);
        addFormRow(formCard, gbc, row++, "Duration (seconds):", durationSpinner);

        gbc.gridx = 1;
        gbc.gridy = row++;
        formCard.add(noticeLabel, gbc);

        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        buttonsPanel.setOpaque(false);
        buttonsPanel.add(submitQuestionBtn);
        buttonsPanel.add(clearBtn);

        gbc.gridx = 1;
        gbc.gridy = row;
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

    private JPanel buildMyQuestionsTab() {
        JPanel panel = new JPanel(new BorderLayout(0, 12));
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(16, 16, 16, 16));

        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setOpaque(false);
        JLabel title = new JLabel("My Authored Questions");
        title.setFont(UITheme.FONT_SUBTITLE);
        title.setForeground(UITheme.SLATE_DARK);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setOpaque(false);

        JButton deleteBtn = UITheme.createDangerButton("Delete Question");
        deleteBtn.addActionListener(e -> deleteSelectedQuestion());

        JButton refreshBtn = UITheme.createSecondaryButton("Refresh List");
        refreshBtn.addActionListener(e -> loadMyQuestionsAsync());

        actions.add(deleteBtn);
        actions.add(refreshBtn);

        topBar.add(title, BorderLayout.WEST);
        topBar.add(actions, BorderLayout.EAST);

        questionsCardLayout = new CardLayout();
        questionsCardPanel = new JPanel(questionsCardLayout);
        questionsCardPanel.setBackground(Color.WHITE);

        String[] cols = {"ID", "Subject", "Question Text", "Correct", "Duration (s)", "Status"};
        questionsModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };

        questionsTable = new JTable(questionsModel);
        UITheme.styleTable(questionsTable);

        JScrollPane scroll = new JScrollPane(questionsTable);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(Color.WHITE);

        JPanel emptyPanel = UITheme.createEmptyPlaceholder("No authored questions found. Submit a question to see its status here.");

        questionsCardPanel.add(scroll, VIEW_TABLE);
        questionsCardPanel.add(emptyPanel, VIEW_EMPTY);
        questionsCardLayout.show(questionsCardPanel, VIEW_EMPTY);

        panel.add(topBar, BorderLayout.NORTH);
        panel.add(questionsCardPanel, BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildInquiriesTab() {
        JPanel panel = new JPanel(new BorderLayout(0, 12));
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(16, 16, 16, 16));

        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setOpaque(false);
        JLabel title = new JLabel("Inquiries from Participants");
        title.setFont(UITheme.FONT_SUBTITLE);
        title.setForeground(UITheme.SLATE_DARK);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setOpaque(false);

        JButton replyBtn = UITheme.createPrimaryButton("Reply to Inquiry");
        replyBtn.addActionListener(e -> replyToSelectedInquiry());

        JButton refreshBtn = UITheme.createSecondaryButton("Refresh Inquiries");
        refreshBtn.addActionListener(e -> loadInquiriesAsync());

        actions.add(replyBtn);
        actions.add(refreshBtn);

        topBar.add(title, BorderLayout.WEST);
        topBar.add(actions, BorderLayout.EAST);

        inquiriesCardLayout = new CardLayout();
        inquiriesCardPanel = new JPanel(inquiriesCardLayout);
        inquiriesCardPanel.setBackground(Color.WHITE);

        String[] cols = {"Msg ID", "From Participant", "Quiz ID", "Inquiry Message", "Your Reply", "Date"};
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

        JPanel emptyPanel = UITheme.createEmptyPlaceholder("No participant inquiries received yet.");

        inquiriesCardPanel.add(scroll, VIEW_TABLE);
        inquiriesCardPanel.add(emptyPanel, VIEW_EMPTY);
        inquiriesCardLayout.show(inquiriesCardPanel, VIEW_EMPTY);

        panel.add(topBar, BorderLayout.NORTH);
        panel.add(inquiriesCardPanel, BorderLayout.CENTER);
        return panel;
    }

    private void submitQuestion() {
        User creator = mainFrame.getAuthService().getCurrentUser();
        if (creator == null) return;

        String subject = subjectField.getText().trim();
        String questionText = questionArea.getText().trim();
        String optA = optionAField.getText().trim();
        String optB = optionBField.getText().trim();
        String optC = optionCField.getText().trim();
        String optD = optionDField.getText().trim();
        String correct = (String) correctOptionComboBox.getSelectedItem();
        int duration = (Integer) durationSpinner.getValue();

        if (subject.isEmpty() || questionText.isEmpty() || optA.isEmpty() ||
            optB.isEmpty() || optC.isEmpty() || optD.isEmpty() || correct == null) {
            JOptionPane.showMessageDialog(this,
                    "All fields are required. Please fill in all options.",
                    "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        char correctChar = correct.charAt(0);
        Question newQuestion = new Question(
                0,
                creator.getUserId(),
                subject,
                questionText,
                optA,
                optB,
                optC,
                optD,
                correctChar,
                "PENDING",
                duration
        );

        submitQuestionBtn.setEnabled(false);
        submitQuestionBtn.setText("Submitting...");

        SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
            @Override
            protected Boolean doInBackground() throws DatabaseOperationException {
                return questionDAO.addQuestion(newQuestion);
            }

            @Override
            protected void done() {
                submitQuestionBtn.setEnabled(true);
                submitQuestionBtn.setText("Submit for Approval");
                try {
                    boolean success = get();
                    if (success) {
                        JOptionPane.showMessageDialog(QuizCreatorDashboardPanel.this,
                                "Question submitted successfully! Status is PENDING admin approval.",
                                "Submitted", JOptionPane.INFORMATION_MESSAGE);
                        clearForm();
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(QuizCreatorDashboardPanel.this,
                            "Database Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    private void deleteSelectedQuestion() {
        int row = questionsTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a question to delete.",
                    "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int questionId = (Integer) questionsModel.getValueAt(row, 0);
        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete question #" + questionId + "?",
                "Confirm Deletion", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
            @Override
            protected Boolean doInBackground() throws DatabaseOperationException {
                return questionDAO.deleteQuestion(questionId);
            }

            @Override
            protected void done() {
                try {
                    boolean deleted = get();
                    if (deleted) {
                        JOptionPane.showMessageDialog(QuizCreatorDashboardPanel.this,
                                "Question deleted successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                        loadMyQuestionsAsync();
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(QuizCreatorDashboardPanel.this,
                            "Failed to delete question: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    private void replyToSelectedInquiry() {
        int row = inquiriesTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select an inquiry to reply to.",
                    "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int messageId = (Integer) inquiriesModel.getValueAt(row, 0);
        String currentReply = (String) inquiriesModel.getValueAt(row, 4);

        String reply = JOptionPane.showInputDialog(this,
                "Enter your reply to participant:",
                currentReply != null && !currentReply.isEmpty() ? currentReply : "");

        if (reply == null || reply.trim().isEmpty()) return;

        SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
            @Override
            protected Boolean doInBackground() throws DatabaseOperationException {
                return messageDAO.replyToMessage(messageId, reply.trim());
            }

            @Override
            protected void done() {
                try {
                    boolean ok = get();
                    if (ok) {
                        JOptionPane.showMessageDialog(QuizCreatorDashboardPanel.this,
                                "Reply sent successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                        loadInquiriesAsync();
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(QuizCreatorDashboardPanel.this,
                            "Failed to send reply: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    private void clearForm() {
        subjectField.setText("");
        questionArea.setText("");
        optionAField.setText("");
        optionBField.setText("");
        optionCField.setText("");
        optionDField.setText("");
        correctOptionComboBox.setSelectedIndex(0);
        durationSpinner.setValue(30);
    }

    public void refreshData() {
        User creator = mainFrame.getAuthService().getCurrentUser();
        if (creator != null) {
            welcomeLabel.setText("Welcome back, " + creator.getUsername() + "!");
            roleBadgeLabel.setText(creator.getRoleBadge());
        }
        loadMyQuestionsAsync();
        loadInquiriesAsync();
    }

    private void loadMyQuestionsAsync() {
        User creator = mainFrame.getAuthService().getCurrentUser();
        if (creator == null) return;

        SwingWorker<List<Question>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Question> doInBackground() throws DatabaseOperationException {
                return questionDAO.getQuestionsByCreator(creator.getUserId());
            }

            @Override
            protected void done() {
                try {
                    List<Question> list = get();
                    questionsModel.setRowCount(0);
                    if (list == null || list.isEmpty()) {
                        questionsCardLayout.show(questionsCardPanel, VIEW_EMPTY);
                    } else {
                        for (Question q : list) {
                            questionsModel.addRow(new Object[]{
                                    q.getQuestionId(),
                                    q.getSubject(),
                                    q.getQuestionText(),
                                    String.valueOf(q.getCorrectOption()),
                                    q.getDurationSeconds(),
                                    q.getStatus()
                            });
                        }
                        questionsCardLayout.show(questionsCardPanel, VIEW_TABLE);
                    }
                } catch (Exception ignored) {
                    questionsCardLayout.show(questionsCardPanel, VIEW_EMPTY);
                }
            }
        };
        worker.execute();
    }

    private void loadInquiriesAsync() {
        User creator = mainFrame.getAuthService().getCurrentUser();
        if (creator == null) return;

        SwingWorker<List<Message>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Message> doInBackground() throws DatabaseOperationException {
                return messageDAO.getMessagesForCreator(creator.getUserId());
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
                                    m.getSenderUsername(),
                                    m.getQuizId() != null ? "#" + m.getQuizId() : "General",
                                    m.getMessageText(),
                                    m.getReplyText() != null ? m.getReplyText() : "(Pending)",
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
}
