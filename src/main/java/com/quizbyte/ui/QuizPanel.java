package com.quizbyte.ui;

import com.quizbyte.dao.ResultDAO;
import com.quizbyte.exception.DatabaseOperationException;
import com.quizbyte.model.Question;
import com.quizbyte.model.QuizResult;
import com.quizbyte.model.User;
import com.quizbyte.service.QuizTimerService;
import com.quizbyte.util.ThreadSafeLeaderboardCache;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Interactive Quiz runner panel.
 * Demonstrates Multithreading (dedicated QuizTimerService worker thread),
 * Collections Framework (Map<Integer, Character> student answer mapping, List<Question>),
 * and clean standard text labels with zero unsupported font glyphs.
 */
public class QuizPanel extends JPanel {

    private static final int SECONDS_PER_QUESTION = 30;

    private final MainFrame mainFrame;
    private final ResultDAO resultDAO;
    private final QuizTimerService timerService;
    private final ThreadSafeLeaderboardCache cache = ThreadSafeLeaderboardCache.getInstance();

    // Active Quiz State
    private User currentUser;
    private String subject;
    private List<Question> questions;
    private int currentIndex;
    private int currentScore;

    // Collections Rubric: Map storing question_id -> student chosen answer character
    private final Map<Integer, Character> studentAnswers;

    // UI Components
    private JLabel subjectLabel;
    private JLabel questionCounterLabel;
    private JLabel timerBadgeLabel;
    private JProgressBar progressBar;

    private JTextArea questionTextArea;
    private JRadioButton optionARadio;
    private JRadioButton optionBRadio;
    private JRadioButton optionCRadio;
    private JRadioButton optionDRadio;
    private ButtonGroup optionsGroup;

    private JButton nextButton;
    private JButton quitButton;

    public QuizPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        this.resultDAO = mainFrame.getResultDAO();
        this.timerService = new QuizTimerService();
        this.studentAnswers = new HashMap<>();

        setLayout(new BorderLayout(0, 16));
        setBackground(UITheme.BG_LIGHT);
        setBorder(new EmptyBorder(20, 32, 20, 32));

        add(buildTopHeaderPanel(), BorderLayout.NORTH);
        add(buildQuestionCenterPanel(), BorderLayout.CENTER);
        add(buildBottomControlPanel(), BorderLayout.SOUTH);
    }

    private JPanel buildTopHeaderPanel() {
        JPanel container = new JPanel(new BorderLayout(0, 8));
        container.setOpaque(false);

        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setOpaque(false);

        subjectLabel = new JLabel("Subject: Java");
        subjectLabel.setFont(UITheme.FONT_TITLE);
        subjectLabel.setForeground(UITheme.SLATE_DARK);

        questionCounterLabel = new JLabel("Question 1 of 5", SwingConstants.CENTER);
        questionCounterLabel.setFont(UITheme.FONT_SUBTITLE);
        questionCounterLabel.setForeground(UITheme.TEXT_MUTED);

        timerBadgeLabel = new JLabel("Time: 30s", SwingConstants.RIGHT);
        timerBadgeLabel.setFont(UITheme.FONT_TITLE);
        timerBadgeLabel.setForeground(UITheme.PRIMARY);

        topBar.add(subjectLabel, BorderLayout.WEST);
        topBar.add(questionCounterLabel, BorderLayout.CENTER);
        topBar.add(timerBadgeLabel, BorderLayout.EAST);

        progressBar = new JProgressBar(0, 100);
        progressBar.setValue(0);
        progressBar.setPreferredSize(new Dimension(Integer.MAX_VALUE, 8));
        progressBar.setForeground(UITheme.PRIMARY);
        progressBar.setBackground(new Color(0xE2, 0xE8, 0xF0));
        progressBar.setBorderPainted(false);

        container.add(topBar, BorderLayout.NORTH);
        container.add(progressBar, BorderLayout.SOUTH);
        return container;
    }

    private JPanel buildQuestionCenterPanel() {
        JPanel card = UITheme.createCardPanel();
        card.setLayout(new BorderLayout(0, 18));

        questionTextArea = new JTextArea("Question prompt goes here...");
        questionTextArea.setFont(new Font("Segoe UI", Font.BOLD, 16));
        questionTextArea.setForeground(UITheme.TEXT_MAIN);
        questionTextArea.setBackground(Color.WHITE);
        questionTextArea.setLineWrap(true);
        questionTextArea.setWrapStyleWord(true);
        questionTextArea.setEditable(false);
        questionTextArea.setFocusable(false);
        questionTextArea.setBorder(new EmptyBorder(10, 8, 10, 8));

        JPanel optionsPanel = new JPanel(new GridLayout(4, 1, 0, 10));
        optionsPanel.setOpaque(false);

        optionARadio = createOptionRadioButton('A');
        optionBRadio = createOptionRadioButton('B');
        optionCRadio = createOptionRadioButton('C');
        optionDRadio = createOptionRadioButton('D');

        optionsGroup = new ButtonGroup();
        optionsGroup.add(optionARadio);
        optionsGroup.add(optionBRadio);
        optionsGroup.add(optionCRadio);
        optionsGroup.add(optionDRadio);

        optionsPanel.add(optionARadio);
        optionsPanel.add(optionBRadio);
        optionsPanel.add(optionCRadio);
        optionsPanel.add(optionDRadio);

        card.add(questionTextArea, BorderLayout.NORTH);
        card.add(optionsPanel, BorderLayout.CENTER);

        return card;
    }

    private JRadioButton createOptionRadioButton(char letter) {
        JRadioButton radio = new JRadioButton("Option " + letter);
        radio.setFont(UITheme.FONT_BODY);
        radio.setForeground(UITheme.TEXT_MAIN);
        radio.setBackground(Color.WHITE);
        radio.setFocusPainted(false);
        radio.setCursor(new Cursor(Cursor.HAND_CURSOR));
        radio.setBorder(new CompoundBorder(
                new LineBorder(UITheme.BORDER_COLOR, 1, true),
                new EmptyBorder(10, 14, 10, 14)
        ));
        radio.setBorderPainted(true);
        return radio;
    }

    private JPanel buildBottomControlPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);

        quitButton = UITheme.createDangerButton("Cancel / Quit Quiz");
        quitButton.addActionListener(e -> confirmQuit());

        nextButton = UITheme.createPrimaryButton("Next Question");
        nextButton.addActionListener(e -> handleNextQuestion(false));

        panel.add(quitButton, BorderLayout.WEST);
        panel.add(nextButton, BorderLayout.EAST);
        return panel;
    }

    /**
     * Initializes a new quiz session with questions.
     */
    public void loadQuiz(User user, String subject, List<Question> questions) {
        this.currentUser = user;
        this.subject = subject;
        this.questions = questions;
        this.currentIndex = 0;
        this.currentScore = 0;
        this.studentAnswers.clear();

        // Increment active concurrent quiz counter
        cache.incrementActiveQuizzes();

        subjectLabel.setText("Subject: " + subject);
        progressBar.setMaximum(questions.size());

        displayCurrentQuestion();
    }

    private void displayCurrentQuestion() {
        if (currentIndex >= questions.size()) {
            finishQuiz();
            return;
        }

        Question q = questions.get(currentIndex);
        questionCounterLabel.setText(String.format("Question %d of %d", currentIndex + 1, questions.size()));
        progressBar.setValue(currentIndex + 1);

        questionTextArea.setText(q.getQuestionText());

        optionARadio.setText("<html><b>[A]</b> &nbsp; " + escapeHtml(q.getOptionA()) + "</html>");
        optionBRadio.setText("<html><b>[B]</b> &nbsp; " + escapeHtml(q.getOptionB()) + "</html>");
        optionCRadio.setText("<html><b>[C]</b> &nbsp; " + escapeHtml(q.getOptionC()) + "</html>");
        optionDRadio.setText("<html><b>[D]</b> &nbsp; " + escapeHtml(q.getOptionD()) + "</html>");

        optionsGroup.clearSelection();

        if (currentIndex == questions.size() - 1) {
            nextButton.setText("Submit Quiz");
        } else {
            nextButton.setText("Next Question");
        }

        // Start dedicated worker thread timer
        startWorkerTimer();
    }

    private void startWorkerTimer() {
        int duration = SECONDS_PER_QUESTION;
        if (questions != null && currentIndex < questions.size()) {
            int qDur = questions.get(currentIndex).getDurationSeconds();
            if (qDur > 0) {
                duration = qDur;
            }
        }
        timerService.startTimer(duration, new QuizTimerService.TimerCallback() {
            @Override
            public void onTick(int secondsRemaining) {
                timerBadgeLabel.setText("Time: " + secondsRemaining + "s");
                if (secondsRemaining > 10) {
                    timerBadgeLabel.setForeground(UITheme.PRIMARY);
                } else if (secondsRemaining > 5) {
                    timerBadgeLabel.setForeground(UITheme.WARNING);
                } else {
                    timerBadgeLabel.setForeground(UITheme.DANGER);
                }
            }

            @Override
            public void onTimeout() {
                JOptionPane.showMessageDialog(QuizPanel.this,
                        "Time's up for Question " + (currentIndex + 1) + "! Proceeding automatically.",
                        "Time Expired", JOptionPane.WARNING_MESSAGE);
                handleNextQuestion(true);
            }
        });
    }

    private void handleNextQuestion(boolean timedOut) {
        timerService.stopTimer();

        char selectedOption = ' ';
        if (optionARadio.isSelected()) selectedOption = 'A';
        else if (optionBRadio.isSelected()) selectedOption = 'B';
        else if (optionCRadio.isSelected()) selectedOption = 'C';
        else if (optionDRadio.isSelected()) selectedOption = 'D';

        if (!timedOut && selectedOption == ' ') {
            JOptionPane.showMessageDialog(this,
                    "Please select an option before proceeding (or wait for the timer to expire).",
                    "Selection Required", JOptionPane.WARNING_MESSAGE);
            startWorkerTimer();
            return;
        }

        Question currentQuestion = questions.get(currentIndex);
        studentAnswers.put(currentQuestion.getQuestionId(), selectedOption);

        if (selectedOption != ' ' && selectedOption == currentQuestion.getCorrectOption()) {
            currentScore++;
        }

        currentIndex++;
        displayCurrentQuestion();
    }

    private void finishQuiz() {
        timerService.stopTimer();
        cache.decrementActiveQuizzes();

        int total = questions.size();
        double percentage = ((double) currentScore / total) * 100.0;
        QuizResult result = new QuizResult(currentUser.getUserId(), subject, currentScore, total, percentage);

        // Asynchronous persistence with SwingWorker
        SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
            @Override
            protected Boolean doInBackground() throws DatabaseOperationException {
                return resultDAO.saveResult(result);
            }

            @Override
            protected void done() {
                mainFrame.showResult(result, subject);
            }
        };
        worker.execute();
    }

    private void confirmQuit() {
        int choice = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to quit the quiz? Current progress will be lost.",
                "Confirm Quit", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (choice == JOptionPane.YES_OPTION) {
            timerService.stopTimer();
            cache.decrementActiveQuizzes();
            mainFrame.showStudentDashboard();
        }
    }

    private String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}
