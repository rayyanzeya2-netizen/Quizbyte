package com.quizbyte.dao;

import com.quizbyte.config.DatabaseConfig;
import com.quizbyte.exception.DatabaseOperationException;
import com.quizbyte.model.QuizReminder;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object for QuizReminder entity.
 * Supports study reminders and test preparation planning for participants.
 */
public class ReminderDAO implements BaseDAO<QuizReminder> {

    @Override
    public boolean save(QuizReminder reminder) throws DatabaseOperationException {
        return addReminder(reminder);
    }

    @Override
    public Optional<QuizReminder> findById(int id) throws DatabaseOperationException {
        String sql = "SELECT reminder_id, user_id, subject, notes, target_date, created_at FROM quiz_reminders WHERE reminder_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(new QuizReminder(
                            rs.getInt("reminder_id"),
                            rs.getInt("user_id"),
                            rs.getString("subject"),
                            rs.getString("notes"),
                            rs.getDate("target_date"),
                            rs.getTimestamp("created_at")
                    ));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Error fetching reminder ID: " + id, e);
        }
        return Optional.empty();
    }

    @Override
    public List<QuizReminder> findAll() throws DatabaseOperationException {
        List<QuizReminder> list = new ArrayList<>();
        String sql = "SELECT reminder_id, user_id, subject, notes, target_date, created_at FROM quiz_reminders ORDER BY target_date ASC";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                list.add(new QuizReminder(
                        rs.getInt("reminder_id"),
                        rs.getInt("user_id"),
                        rs.getString("subject"),
                        rs.getString("notes"),
                        rs.getDate("target_date"),
                        rs.getTimestamp("created_at")
                ));
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Error fetching all quiz reminders", e);
        }
        return list;
    }

    /**
     * Inserts a new study reminder for a participant.
     */
    public boolean addReminder(QuizReminder reminder) throws DatabaseOperationException {
        String sql = "INSERT INTO quiz_reminders (user_id, subject, notes, target_date) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, reminder.getUserId());
            stmt.setString(2, reminder.getSubject());
            stmt.setString(3, reminder.getNotes());
            stmt.setDate(4, reminder.getTargetDate());

            int affected = stmt.executeUpdate();
            if (affected > 0) {
                try (ResultSet keys = stmt.getGeneratedKeys()) {
                    if (keys.next()) {
                        reminder.setReminderId(keys.getInt(1));
                    }
                }
                return true;
            }
            return false;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to persist quiz reminder", e);
        }
    }

    /**
     * Retrieves all study reminders for a specific user, ordered by target date.
     */
    public List<QuizReminder> getRemindersByUser(int userId) throws DatabaseOperationException {
        List<QuizReminder> list = new ArrayList<>();
        String sql = "SELECT reminder_id, user_id, subject, notes, target_date, created_at " +
                     "FROM quiz_reminders WHERE user_id = ? ORDER BY target_date ASC";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(new QuizReminder(
                            rs.getInt("reminder_id"),
                            rs.getInt("user_id"),
                            rs.getString("subject"),
                            rs.getString("notes"),
                            rs.getDate("target_date"),
                            rs.getTimestamp("created_at")
                    ));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Error fetching reminders for user ID: " + userId, e);
        }
        return list;
    }

    /**
     * Deletes a reminder by ID.
     */
    public boolean deleteReminder(int reminderId) throws DatabaseOperationException {
        String sql = "DELETE FROM quiz_reminders WHERE reminder_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, reminderId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to delete reminder ID: " + reminderId, e);
        }
    }
}
