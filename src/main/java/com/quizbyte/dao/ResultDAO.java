package com.quizbyte.dao;

import com.quizbyte.config.DatabaseConfig;
import com.quizbyte.exception.DatabaseOperationException;
import com.quizbyte.model.QuizResult;
import com.quizbyte.util.ThreadSafeLeaderboardCache;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object for QuizResult entity.
 * Implements generic BaseDAO<QuizResult> contract and synchronizes with
 * ThreadSafeLeaderboardCache to eliminate race conditions on score updates.
 */
public class ResultDAO implements BaseDAO<QuizResult> {

    private final ThreadSafeLeaderboardCache cache = ThreadSafeLeaderboardCache.getInstance();

    // =========================================================================
    // BaseDAO<QuizResult> CONTRACT IMPLEMENTATIONS
    // =========================================================================

    @Override
    public boolean save(QuizResult result) throws DatabaseOperationException {
        return saveResult(result);
    }

    @Override
    public Optional<QuizResult> findById(int id) throws DatabaseOperationException {
        String sql = "SELECT r.result_id, r.user_id, u.username, r.subject, r.score, r.total_questions, " +
                     "r.percentage, r.attempt_date " +
                     "FROM quiz_results r " +
                     "JOIN users u ON r.user_id = u.user_id " +
                     "WHERE r.result_id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(new QuizResult(
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
            throw new DatabaseOperationException("Error fetching result by ID: " + id, e);
        }
        return Optional.empty();
    }

    @Override
    public List<QuizResult> findAll() throws DatabaseOperationException {
        return getAllResults();
    }

    // =========================================================================
    // RESULT SPECIFIC OPERATIONS
    // =========================================================================

    /**
     * Saves a quiz attempt and invalidates the thread-safe leaderboard cache.
     *
     * @param result QuizResult object to insert
     * @return true if successfully saved, false otherwise
     * @throws DatabaseOperationException if insert fails
     */
    public boolean saveResult(QuizResult result) throws DatabaseOperationException {
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
                // Invalidate thread-safe cache upon score write
                cache.invalidateCache();
                return true;
            }
            return false;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Error persisting quiz attempt result", e);
        }
    }

    /**
     * Retrieves all results for a specific student.
     */
    public List<QuizResult> getResultsByUserId(int userId) throws DatabaseOperationException {
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
            throw new DatabaseOperationException("Error fetching past quiz history for user ID: " + userId, e);
        }
        return results;
    }

    /**
     * Retrieves top leaderboard records, using the synchronized thread-safe cache
     * when valid to optimize database throughput and demonstrate thread safety.
     */
    public List<QuizResult> getAllLeaderboardResults() throws DatabaseOperationException {
        if (cache.isCacheValid()) {
            return cache.getCachedLeaderboard();
        }

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
            throw new DatabaseOperationException("Error retrieving global leaderboard results", e);
        }

        // Update the synchronized cache
        cache.updateCache(leaderboard);
        return leaderboard;
    }

    /**
     * Retrieves all results across the system for admin audit.
     */
    public List<QuizResult> getAllResults() throws DatabaseOperationException {
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
            throw new DatabaseOperationException("Error retrieving global score audit records", e);
        }
        return list;
    }
}
