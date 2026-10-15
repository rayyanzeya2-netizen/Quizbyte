package com.quizbyte.dao;

import com.quizbyte.config.DatabaseConfig;
import com.quizbyte.model.QuizResult;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for QuizResult entity.
 * Handles persisting quiz results, fetching user-specific history,
 * and generating leaderboard standings.
 */
public class ResultDAO {

    /**
     * Saves a quiz attempt result to the database.
     *
     * @param result QuizResult object containing score, percentage, user_id, and subject
     * @return true if successfully saved, false otherwise
     */
    public boolean saveResult(QuizResult result) {
        String sql = "INSERT INTO quiz_results (user_id, subject, score, total_questions, percentage) " +
                     "VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, result.getUserId());
            stmt.setString(2, result.getSubject());
            stmt.setInt(3, result.getScore());
            stmt.setInt(4, result.getTotalQuestions());
            stmt.setDouble(5, result.getPercentage());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        result.setResultId(generatedKeys.getInt(1));
                    }
                }
                return true;
            }
            return false;
        } catch (SQLException e) {
            System.err.println("[ResultDAO] Error saving quiz result: " + e.getMessage());
            return false;
        }
    }

    /**
     * Retrieves all quiz results for a specific user, ordered by attempt date descending.
     *
     * @param userId user ID
     * @return List of QuizResult objects
     */
    public List<QuizResult> getResultsByUserId(int userId) {
        List<QuizResult> results = new ArrayList<>();
        String sql = "SELECT r.result_id, r.user_id, u.username, r.subject, r.score, r.total_questions, " +
                     "r.percentage, r.attempt_date " +
                     "FROM quiz_results r " +
                     "JOIN users u ON r.user_id = u.user_id " +
                     "WHERE r.user_id = ? " +
                     "ORDER BY r.attempt_date DESC";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    results.add(new QuizResult(
                            rs.getInt("result_id"),
                            rs.getInt("user_id"),
                            rs.getString("username"),
                            rs.getString("subject"),
                            rs.getInt("score"),
                            rs.getInt("total_questions"),
                            rs.getDouble("percentage"),
                            rs.getTimestamp("attempt_date")
                    ));
                }
            }
        } catch (SQLException e) {
            System.err.println("[ResultDAO] Error retrieving user results: " + e.getMessage());
        }
        return results;
    }

    /**
     * Retrieves the top leaderboard results across all users.
     * Ranked by percentage descending, score descending, and recent attempt date.
     *
     * @return List of QuizResult objects representing the leaderboard
     */
    public List<QuizResult> getAllLeaderboardResults() {
        List<QuizResult> leaderboard = new ArrayList<>();
        String sql = "SELECT r.result_id, r.user_id, u.username, r.subject, r.score, r.total_questions, " +
                     "r.percentage, r.attempt_date " +
                     "FROM quiz_results r " +
                     "JOIN users u ON r.user_id = u.user_id " +
                     "ORDER BY r.percentage DESC, r.score DESC, r.attempt_date DESC " +
                     "LIMIT 10";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                leaderboard.add(new QuizResult(
                        rs.getInt("result_id"),
                        rs.getInt("user_id"),
                        rs.getString("username"),
                        rs.getString("subject"),
                        rs.getInt("score"),
                        rs.getInt("total_questions"),
                        rs.getDouble("percentage"),
                        rs.getTimestamp("attempt_date")
                ));
            }
        } catch (SQLException e) {
            System.err.println("[ResultDAO] Error retrieving leaderboard: " + e.getMessage());
        }
        return leaderboard;
    }

    /**
     * Retrieves all quiz results across all users (for Admin global audit).
     *
     * @return List of QuizResult objects
     */
    public List<QuizResult> getAllResults() {
        List<QuizResult> list = new ArrayList<>();
        String sql = "SELECT r.result_id, r.user_id, u.username, r.subject, r.score, r.total_questions, " +
                     "r.percentage, r.attempt_date " +
                     "FROM quiz_results r " +
                     "JOIN users u ON r.user_id = u.user_id " +
                     "ORDER BY r.attempt_date DESC";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                list.add(new QuizResult(
                        rs.getInt("result_id"),
                        rs.getInt("user_id"),
                        rs.getString("username"),
                        rs.getString("subject"),
                        rs.getInt("score"),
                        rs.getInt("total_questions"),
                        rs.getDouble("percentage"),
                        rs.getTimestamp("attempt_date")
                ));
            }
        } catch (SQLException e) {
            System.err.println("[ResultDAO] Error retrieving all results: " + e.getMessage());
        }
        return list;
    }
}
