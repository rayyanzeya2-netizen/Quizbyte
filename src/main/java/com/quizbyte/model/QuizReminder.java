package com.quizbyte.model;

import java.sql.Date;
import java.sql.Timestamp;

/**
 * Model representing a planned study or quiz reminder for participants.
 */
public class QuizReminder {

    private int reminderId;
    private int userId;
    private String subject;
    private String notes;
    private Date targetDate;
    private Timestamp createdAt;

    public QuizReminder() {
    }

    public QuizReminder(int userId, String subject, String notes, Date targetDate) {
        this.userId = userId;
        this.subject = subject;
        this.notes = notes;
        this.targetDate = targetDate;
    }

    public QuizReminder(int reminderId, int userId, String subject, String notes, Date targetDate, Timestamp createdAt) {
        this.reminderId = reminderId;
        this.userId = userId;
        this.subject = subject;
        this.notes = notes;
        this.targetDate = targetDate;
        this.createdAt = createdAt;
    }

    public int getReminderId() {
        return reminderId;
    }

    public void setReminderId(int reminderId) {
        this.reminderId = reminderId;
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

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public Date getTargetDate() {
        return targetDate;
    }

    public void setTargetDate(Date targetDate) {
        this.targetDate = targetDate;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "QuizReminder{" +
                "reminderId=" + reminderId +
                ", userId=" + userId +
                ", subject='" + subject + '\'' +
                ", notes='" + notes + '\'' +
                ", targetDate=" + targetDate +
                '}';
    }
}
