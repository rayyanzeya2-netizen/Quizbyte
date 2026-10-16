package com.quizbyte.model;

/**
 * Model representing a quiz question with 4 multiple choice options,
 * approval status workflow (PENDING, APPROVED, REJECTED), creator attribution,
 * and duration setting.
 */
public class Question {
    private int questionId;
    private int creatorId;
    private String subject;
    private String questionText;
    private String optionA;
    private String optionB;
    private String optionC;
    private String optionD;
    private char correctOption; // 'A', 'B', 'C', or 'D'
    private String status;      // 'PENDING', 'APPROVED', 'REJECTED'
    private int durationSeconds;

    public Question() {
        this.status = "APPROVED";
        this.durationSeconds = 30;
    }

    public Question(String subject, String questionText, String optionA, String optionB, 
                    String optionC, String optionD, char correctOption) {
        this(1, subject, questionText, optionA, optionB, optionC, optionD, correctOption, "APPROVED", 30);
    }

    public Question(int creatorId, String subject, String questionText, String optionA, String optionB, 
                    String optionC, String optionD, char correctOption, String status, int durationSeconds) {
        this.creatorId = creatorId;
        this.subject = subject;
        this.questionText = questionText;
        this.optionA = optionA;
        this.optionB = optionB;
        this.optionC = optionC;
        this.optionD = optionD;
        this.correctOption = Character.toUpperCase(correctOption);
        this.status = (status != null) ? status.toUpperCase() : "APPROVED";
        this.durationSeconds = durationSeconds > 0 ? durationSeconds : 30;
    }

    public Question(int questionId, String subject, String questionText, String optionA, 
                    String optionB, String optionC, String optionD, char correctOption) {
        this(questionId, 1, subject, questionText, optionA, optionB, optionC, optionD, correctOption, "APPROVED", 30);
    }

    public Question(int questionId, int creatorId, String subject, String questionText, String optionA, 
                    String optionB, String optionC, String optionD, char correctOption, String status, int durationSeconds) {
        this.questionId = questionId;
        this.creatorId = creatorId;
        this.subject = subject;
        this.questionText = questionText;
        this.optionA = optionA;
        this.optionB = optionB;
        this.optionC = optionC;
        this.optionD = optionD;
        this.correctOption = Character.toUpperCase(correctOption);
        this.status = (status != null) ? status.toUpperCase() : "APPROVED";
        this.durationSeconds = durationSeconds > 0 ? durationSeconds : 30;
    }

    public int getQuestionId() {
        return questionId;
    }

    public void setQuestionId(int questionId) {
        this.questionId = questionId;
    }

    public int getCreatorId() {
        return creatorId;
    }

    public void setCreatorId(int creatorId) {
        this.creatorId = creatorId;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getQuestionText() {
        return questionText;
    }

    public void setQuestionText(String questionText) {
        this.questionText = questionText;
    }

    public String getOptionA() {
        return optionA;
    }

    public void setOptionA(String optionA) {
        this.optionA = optionA;
    }

    public String getOptionB() {
        return optionB;
    }

    public void setOptionB(String optionB) {
        this.optionB = optionB;
    }

    public String getOptionC() {
        return optionC;
    }

    public void setOptionC(String optionC) {
        this.optionC = optionC;
    }

    public String getOptionD() {
        return optionD;
    }

    public void setOptionD(String optionD) {
        this.optionD = optionD;
    }

    public char getCorrectOption() {
        return correctOption;
    }

    public void setCorrectOption(char correctOption) {
        this.correctOption = Character.toUpperCase(correctOption);
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = (status != null) ? status.toUpperCase() : "APPROVED";
    }

    public boolean isApproved() {
        return "APPROVED".equalsIgnoreCase(this.status);
    }

    public boolean isPending() {
        return "PENDING".equalsIgnoreCase(this.status);
    }

    public boolean isRejected() {
        return "REJECTED".equalsIgnoreCase(this.status);
    }

    public int getDurationSeconds() {
        return durationSeconds;
    }

    public void setDurationSeconds(int durationSeconds) {
        this.durationSeconds = durationSeconds;
    }

    /**
     * Retrieves the option string corresponding to a choice letter ('A', 'B', 'C', or 'D').
     */
    public String getOptionByLetter(char letter) {
        return switch (Character.toUpperCase(letter)) {
            case 'A' -> optionA;
            case 'B' -> optionB;
            case 'C' -> optionC;
            case 'D' -> optionD;
            default -> null;
        };
    }

    @Override
    public String toString() {
        return "Question{" +
                "questionId=" + questionId +
                ", creatorId=" + creatorId +
                ", subject='" + subject + '\'' +
                ", questionText='" + questionText + '\'' +
                ", correctOption=" + correctOption +
                ", status='" + status + '\'' +
                ", durationSeconds=" + durationSeconds +
                '}';
    }
}
