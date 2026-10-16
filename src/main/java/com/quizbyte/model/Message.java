package com.quizbyte.model;

import java.sql.Timestamp;

/**
 * Model representing an inquiry or feedback message exchanged between
 * participants and quiz creators / administrators.
 */
public class Message {

    private int messageId;
    private int senderId;
    private int receiverId;
    private Integer quizId;
    private String messageText;
    private String replyText;
    private Timestamp createdAt;

    // Transient helper fields for UI display
    private String senderUsername;
    private String receiverUsername;

    public Message() {
    }

    public Message(int senderId, int receiverId, String messageText) {
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.messageText = messageText;
    }

    public Message(int senderId, int receiverId, Integer quizId, String messageText) {
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.quizId = quizId;
        this.messageText = messageText;
    }

    public Message(int messageId, int senderId, int receiverId, Integer quizId, 
                   String messageText, String replyText, Timestamp createdAt) {
        this.messageId = messageId;
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.quizId = quizId;
        this.messageText = messageText;
        this.replyText = replyText;
        this.createdAt = createdAt;
    }

    public Message(int messageId, int senderId, String senderUsername, int receiverId, 
                   String receiverUsername, Integer quizId, String messageText, String replyText, Timestamp createdAt) {
        this.messageId = messageId;
        this.senderId = senderId;
        this.senderUsername = senderUsername;
        this.receiverId = receiverId;
        this.receiverUsername = receiverUsername;
        this.quizId = quizId;
        this.messageText = messageText;
        this.replyText = replyText;
        this.createdAt = createdAt;
    }

    public int getMessageId() {
        return messageId;
    }

    public void setMessageId(int messageId) {
        this.messageId = messageId;
    }

    public int getSenderId() {
        return senderId;
    }

    public void setSenderId(int senderId) {
        this.senderId = senderId;
    }

    public int getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(int receiverId) {
        this.receiverId = receiverId;
    }

    public Integer getQuizId() {
        return quizId;
    }

    public void setQuizId(Integer quizId) {
        this.quizId = quizId;
    }

    public String getMessageText() {
        return messageText;
    }

    public void setMessageText(String messageText) {
        this.messageText = messageText;
    }

    public String getReplyText() {
        return replyText;
    }

    public void setReplyText(String replyText) {
        this.replyText = replyText;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public String getSenderUsername() {
        return senderUsername;
    }

    public void setSenderUsername(String senderUsername) {
        this.senderUsername = senderUsername;
    }

    public String getReceiverUsername() {
        return receiverUsername;
    }

    public void setReceiverUsername(String receiverUsername) {
        this.receiverUsername = receiverUsername;
    }

    public boolean isReplied() {
        return replyText != null && !replyText.trim().isEmpty();
    }

    @Override
    public String toString() {
        return "Message{" +
                "messageId=" + messageId +
                ", senderId=" + senderId +
                ", senderUsername='" + senderUsername + '\'' +
                ", receiverId=" + receiverId +
                ", receiverUsername='" + receiverUsername + '\'' +
                ", messageText='" + messageText + '\'' +
                ", replyText='" + replyText + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}
