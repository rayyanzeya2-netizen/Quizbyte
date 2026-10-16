package com.quizbyte.dao;

import com.quizbyte.config.DatabaseConfig;
import com.quizbyte.exception.DatabaseOperationException;
import com.quizbyte.model.Message;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object for Message entity.
 * Supports communication between participants and quiz creators / administrators.
 */
public class MessageDAO implements BaseDAO<Message> {

    @Override
    public boolean save(Message message) throws DatabaseOperationException {
        return sendMessage(message);
    }

    @Override
    public Optional<Message> findById(int id) throws DatabaseOperationException {
        String sql = "SELECT m.message_id, m.sender_id, u1.username AS sender_name, " +
                     "m.receiver_id, u2.username AS receiver_name, m.quiz_id, " +
                     "m.message_text, m.reply_text, m.created_at " +
                     "FROM messages m " +
                     "JOIN users u1 ON m.sender_id = u1.user_id " +
                     "JOIN users u2 ON m.receiver_id = u2.user_id " +
                     "WHERE m.message_id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Integer quizId = rs.getObject("quiz_id") != null ? rs.getInt("quiz_id") : null;
                    return Optional.of(new Message(
                            rs.getInt("message_id"),
                            rs.getInt("sender_id"),
                            rs.getString("sender_name"),
                            rs.getInt("receiver_id"),
                            rs.getString("receiver_name"),
                            quizId,
                            rs.getString("message_text"),
                            rs.getString("reply_text"),
                            rs.getTimestamp("created_at")
                    ));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Error fetching message ID: " + id, e);
        }
        return Optional.empty();
    }

    @Override
    public List<Message> findAll() throws DatabaseOperationException {
        List<Message> list = new ArrayList<>();
        String sql = "SELECT m.message_id, m.sender_id, u1.username AS sender_name, " +
                     "m.receiver_id, u2.username AS receiver_name, m.quiz_id, " +
                     "m.message_text, m.reply_text, m.created_at " +
                     "FROM messages m " +
                     "JOIN users u1 ON m.sender_id = u1.user_id " +
                     "JOIN users u2 ON m.receiver_id = u2.user_id " +
                     "ORDER BY m.created_at DESC";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Integer quizId = rs.getObject("quiz_id") != null ? rs.getInt("quiz_id") : null;
                list.add(new Message(
                        rs.getInt("message_id"),
                        rs.getInt("sender_id"),
                        rs.getString("sender_name"),
                        rs.getInt("receiver_id"),
                        rs.getString("receiver_name"),
                        quizId,
                        rs.getString("message_text"),
                        rs.getString("reply_text"),
                        rs.getTimestamp("created_at")
                ));
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Error fetching all messages", e);
        }
        return list;
    }

    /**
     * Sends a new inquiry message.
     */
    public boolean sendMessage(Message message) throws DatabaseOperationException {
        String sql = "INSERT INTO messages (sender_id, receiver_id, quiz_id, message_text) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, message.getSenderId());
            stmt.setInt(2, message.getReceiverId());
            if (message.getQuizId() != null) {
                stmt.setInt(3, message.getQuizId());
            } else {
                stmt.setNull(3, Types.INTEGER);
            }
            stmt.setString(4, message.getMessageText());

            int affected = stmt.executeUpdate();
            if (affected > 0) {
                try (ResultSet keys = stmt.getGeneratedKeys()) {
                    if (keys.next()) {
                        message.setMessageId(keys.getInt(1));
                    }
                }
                return true;
            }
            return false;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to persist inquiry message", e);
        }
    }

    /**
     * Retrieves all messages exchanged involving a specific user (either as sender or recipient).
     */
    public List<Message> getMessagesForUser(int userId) throws DatabaseOperationException {
        List<Message> list = new ArrayList<>();
        String sql = "SELECT m.message_id, m.sender_id, u1.username AS sender_name, " +
                     "m.receiver_id, u2.username AS receiver_name, m.quiz_id, " +
                     "m.message_text, m.reply_text, m.created_at " +
                     "FROM messages m " +
                     "JOIN users u1 ON m.sender_id = u1.user_id " +
                     "JOIN users u2 ON m.receiver_id = u2.user_id " +
                     "WHERE m.sender_id = ? OR m.receiver_id = ? " +
                     "ORDER BY m.created_at DESC";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            stmt.setInt(2, userId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Integer quizId = rs.getObject("quiz_id") != null ? rs.getInt("quiz_id") : null;
                    list.add(new Message(
                            rs.getInt("message_id"),
                            rs.getInt("sender_id"),
                            rs.getString("sender_name"),
                            rs.getInt("receiver_id"),
                            rs.getString("receiver_name"),
                            quizId,
                            rs.getString("message_text"),
                            rs.getString("reply_text"),
                            rs.getTimestamp("created_at")
                    ));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Error retrieving messages for user ID: " + userId, e);
        }
        return list;
    }

    /**
     * Retrieves all incoming inquiries directed to a specific creator or admin.
     */
    public List<Message> getMessagesForCreator(int creatorId) throws DatabaseOperationException {
        List<Message> list = new ArrayList<>();
        String sql = "SELECT m.message_id, m.sender_id, u1.username AS sender_name, " +
                     "m.receiver_id, u2.username AS receiver_name, m.quiz_id, " +
                     "m.message_text, m.reply_text, m.created_at " +
                     "FROM messages m " +
                     "JOIN users u1 ON m.sender_id = u1.user_id " +
                     "JOIN users u2 ON m.receiver_id = u2.user_id " +
                     "WHERE m.receiver_id = ? " +
                     "ORDER BY m.created_at DESC";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, creatorId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Integer quizId = rs.getObject("quiz_id") != null ? rs.getInt("quiz_id") : null;
                    list.add(new Message(
                            rs.getInt("message_id"),
                            rs.getInt("sender_id"),
                            rs.getString("sender_name"),
                            rs.getInt("receiver_id"),
                            rs.getString("receiver_name"),
                            quizId,
                            rs.getString("message_text"),
                            rs.getString("reply_text"),
                            rs.getTimestamp("created_at")
                    ));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Error retrieving messages for creator ID: " + creatorId, e);
        }
        return list;
    }

    /**
     * Submits a reply to an existing message.
     */
    public boolean replyToMessage(int messageId, String replyText) throws DatabaseOperationException {
        String sql = "UPDATE messages SET reply_text = ? WHERE message_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, replyText);
            stmt.setInt(2, messageId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to reply to message ID: " + messageId, e);
        }
    }
}
