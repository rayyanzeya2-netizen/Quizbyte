package com.quizbyte.model;

import java.sql.Timestamp;

/**
 * Model representing a student's quiz attempt result.
 */
public class QuizResult {
    private int resultId;
    private int userId;
    private String subject;
    private int score;
    private int totalQuestions;
    private double percentage;
    private Timestamp attemptDate;

    // Optional field populated during JOINs for Leaderboard and Score History displays
    private String username;

    public QuizResult() {
    }

    public QuizResult(int userId, String subject, int score, int totalQuestions, double percentage) {
        this.userId = userId;
        this.subject = subject;
        this.score = score;
        this.totalQuestions = totalQuestions;
        this.percentage = percentage;
    }

    public QuizResult(int resultId, int userId, String subject, int score, 
                      int totalQuestions, double percentage, Timestamp attemptDate) {
        this.resultId = resultId;
        this.userId = userId;
        this.subject = subject;
        this.score = score;
        this.totalQuestions = totalQuestions;
        this.percentage = percentage;
        this.attemptDate = attemptDate;
    }

    public QuizResult(int resultId, int userId, String username, String subject, 
                      int score, int totalQuestions, double percentage, Timestamp attemptDate) {
        this.resultId = resultId;
        this.userId = userId;
        this.username = username;
        this.subject = subject;
        this.score = score;
        this.totalQuestions = totalQuestions;
        this.percentage = percentage;
        this.attemptDate = attemptDate;
    }

    public int getResultId() {
        return resultId;
    }

    public void setResultId(int resultId) {
        this.resultId = resultId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public int getTotalQuestions() {
        return totalQuestions;
    }

    public void setTotalQuestions(int totalQuestions) {
        this.totalQuestions = totalQuestions;
    }

    public double getPercentage() {
        return percentage;
    }

    public void setPercentage(double percentage) {
        this.percentage = percentage;
    }

    public Timestamp getAttemptDate() {
        return attemptDate;
    }

    public void setAttemptDate(Timestamp attemptDate) {
        this.attemptDate = attemptDate;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getGrade() {
        if (percentage >= 90.0) return "A+";
        if (percentage >= 80.0) return "A";
        if (percentage >= 70.0) return "B";
        if (percentage >= 60.0) return "C";
        if (percentage >= 50.0) return "D";
        return "F";
    }

    @Override
    public String toString() {
        return "QuizResult{" +
                "resultId=" + resultId +
                ", userId=" + userId +
                ", username='" + username + '\'' +
                ", subject='" + subject + '\'' +
                ", score=" + score +
                ", totalQuestions=" + totalQuestions +
                ", percentage=" + percentage + "%" +
                ", grade='" + getGrade() + '\'' +
                ", attemptDate=" + attemptDate +
                '}';
    }
}
