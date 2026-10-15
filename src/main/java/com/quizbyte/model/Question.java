package com.quizbyte.model;

/**
 * Model representing a quiz question with 4 multiple choice options.
 */
public class Question {
    private int questionId;
    private String subject;
    private String questionText;
    private String optionA;
    private String optionB;
    private String optionC;
    private String optionD;
    private char correctOption; // 'A', 'B', 'C', or 'D'

    public Question() {
    }

    public Question(String subject, String questionText, String optionA, String optionB, 
                    String optionC, String optionD, char correctOption) {
        this.subject = subject;
        this.questionText = questionText;
        this.optionA = optionA;
        this.optionB = optionB;
        this.optionC = optionC;
        this.optionD = optionD;
        this.correctOption = Character.toUpperCase(correctOption);
    }

    public Question(int questionId, String subject, String questionText, String optionA, 
                    String optionB, String optionC, String optionD, char correctOption) {
        this.questionId = questionId;
        this.subject = subject;
        this.questionText = questionText;
        this.optionA = optionA;
        this.optionB = optionB;
        this.optionC = optionC;
        this.optionD = optionD;
        this.correctOption = Character.toUpperCase(correctOption);
    }

    public int getQuestionId() {
        return questionId;
    }

    public void setQuestionId(int questionId) {
        this.questionId = questionId;
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
                ", subject='" + subject + '\'' +
                ", questionText='" + questionText + '\'' +
                ", optionA='" + optionA + '\'' +
                ", optionB='" + optionB + '\'' +
                ", optionC='" + optionC + '\'' +
                ", optionD='" + optionD + '\'' +
                ", correctOption=" + correctOption +
                '}';
    }
}
