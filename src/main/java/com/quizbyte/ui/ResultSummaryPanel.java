package com.quizbyte.ui;

import com.quizbyte.model.QuizResult;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;

/**
 * Result display panel presenting the scorecard, grade badge,
 * score percentage breakdown, and navigation options.
 * Utilizes high-contrast buttons and standard typography without broken glyphs.
 */
public class ResultSummaryPanel extends JPanel {

    private final MainFrame mainFrame;
    private String currentSubject;

    private JLabel titleLabel;
    private JLabel subjectLabel;
    private JLabel gradeBadgeLabel;
    private JLabel scoreLabel;
    private JLabel percentageLabel;
    private JLabel feedbackLabel;

    public ResultSummaryPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;

        setLayout(new GridBagLayout());
        setBackground(UITheme.BG_LIGHT);

        JPanel scorecardCard = buildScorecardCard();
        add(scorecardCard);
    }

    private JPanel buildScorecardCard() {
        JPanel card = UITheme.createCardPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setPreferredSize(new Dimension(500, 520));

        titleLabel = new JLabel("Quiz Completed!");
        titleLabel.setFont(UITheme.FONT_HEADER_LARGE);
        titleLabel.setForeground(UITheme.SLATE_DARK);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        subjectLabel = new JLabel("Subject: Java");
        subjectLabel.setFont(UITheme.FONT_SUBTITLE);
        subjectLabel.setForeground(UITheme.TEXT_MUTED);
        subjectLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Grade Badge
        gradeBadgeLabel = new JLabel("A", SwingConstants.CENTER);
        gradeBadgeLabel.setFont(UITheme.FONT_SCORE_BADGE);
        gradeBadgeLabel.setForeground(Color.WHITE);
        gradeBadgeLabel.setOpaque(true);
        gradeBadgeLabel.setBackground(UITheme.SUCCESS);
        gradeBadgeLabel.setPreferredSize(new Dimension(100, 100));
        gradeBadgeLabel.setMaximumSize(new Dimension(100, 100));
        gradeBadgeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        gradeBadgeLabel.setBorder(new LineBorder(Color.WHITE, 4, true));

        // Stats Panel
        JPanel statsPanel = new JPanel(new GridLayout(1, 2, 16, 0));
        statsPanel.setOpaque(false);
        statsPanel.setMaximumSize(new Dimension(420, 80));

        JPanel scoreBox = createStatBox("Score", scoreLabel = new JLabel("0 / 0", SwingConstants.CENTER));
        JPanel pctBox = createStatBox("Percentage", percentageLabel = new JLabel("0.00%", SwingConstants.CENTER));

        statsPanel.add(scoreBox);
        statsPanel.add(pctBox);

        feedbackLabel = new JLabel("Great job!", SwingConstants.CENTER);
        feedbackLabel.setFont(UITheme.FONT_BODY_BOLD);
        feedbackLabel.setForeground(UITheme.TEXT_MAIN);
        feedbackLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Buttons
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 14, 0));
        buttonsPanel.setOpaque(false);
        buttonsPanel.setMaximumSize(new Dimension(440, 50));

        JButton retakeBtn = UITheme.createSecondaryButton("Retake Quiz");
        retakeBtn.addActionListener(e -> {
            if (currentSubject != null) {
                mainFrame.startQuiz(currentSubject);
            }
        });

        JButton dashboardBtn = UITheme.createPrimaryButton("Return to Dashboard");
        dashboardBtn.addActionListener(e -> mainFrame.showStudentDashboard());

        buttonsPanel.add(retakeBtn);
        buttonsPanel.add(dashboardBtn);

        // Assemble
        card.add(Box.createVerticalStrut(12));
        card.add(titleLabel);
        card.add(Box.createVerticalStrut(4));
        card.add(subjectLabel);
        card.add(Box.createVerticalStrut(20));
        card.add(gradeBadgeLabel);
        card.add(Box.createVerticalStrut(24));
        card.add(statsPanel);
        card.add(Box.createVerticalStrut(18));
        card.add(feedbackLabel);
        card.add(Box.createVerticalStrut(24));
        card.add(buttonsPanel);

        return card;
    }

    private JPanel createStatBox(String title, JLabel valueLabel) {
        JPanel box = new JPanel(new BorderLayout(0, 4));
        box.setBackground(UITheme.BG_LIGHT);
        box.setBorder(new EmptyBorder(10, 12, 10, 12));

        JLabel titleLbl = new JLabel(title, SwingConstants.CENTER);
        titleLbl.setFont(UITheme.FONT_SMALL);
        titleLbl.setForeground(UITheme.TEXT_MUTED);

        valueLabel.setFont(UITheme.FONT_TITLE);
        valueLabel.setForeground(UITheme.TEXT_MAIN);

        box.add(titleLbl, BorderLayout.NORTH);
        box.add(valueLabel, BorderLayout.CENTER);
        return box;
    }

    /**
     * Updates display fields with fresh quiz results.
     */
    public void displayResult(QuizResult result, String subject) {
        this.currentSubject = subject;

        subjectLabel.setText("Subject: " + subject);
        scoreLabel.setText(result.getScore() + " / " + result.getTotalQuestions());
        percentageLabel.setText(String.format("%.2f%%", result.getPercentage()));

        String grade = result.getGrade();
        gradeBadgeLabel.setText(grade);

        if (grade.startsWith("A")) {
            gradeBadgeLabel.setBackground(UITheme.SUCCESS);
        } else if (grade.startsWith("B")) {
            gradeBadgeLabel.setBackground(UITheme.PRIMARY);
        } else if (grade.startsWith("C") || grade.startsWith("D")) {
            gradeBadgeLabel.setBackground(UITheme.WARNING);
        } else {
            gradeBadgeLabel.setBackground(UITheme.DANGER);
        }

        feedbackLabel.setText(getEvaluationText(result.getPercentage()));
    }

    private String getEvaluationText(double percentage) {
        if (percentage >= 90.0) return "Outstanding! Excellent mastery of " + currentSubject + ".";
        if (percentage >= 75.0) return "Great Job! Strong understanding demonstrated.";
        if (percentage >= 50.0) return "Passed! A little more practice will make it perfect.";
        return "Keep Practicing! Review the core concepts and try again.";
    }
}
