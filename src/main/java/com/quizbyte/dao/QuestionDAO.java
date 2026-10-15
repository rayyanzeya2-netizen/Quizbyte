package com.quizbyte.dao;

import com.quizbyte.config.DatabaseConfig;
import com.quizbyte.model.Question;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Question entity.
 * Handles database operations for retrieving and inserting quiz questions.
 */
public class QuestionDAO {

    /**
     * Retrieves questions for a specific subject up to the specified limit.
     * Uses ORDER BY RAND() to ensure questions appear randomly in each attempt.
     *
     * @param subject subject category (e.g. 'Java', 'OOP', 'DBMS')
     * @param limit maximum number of questions to retrieve
     * @return List of Question objects
     */
    public List<Question> getQuestionsBySubject(String subject, int limit) {
        List<Question> questions = new ArrayList<>();
        String sql = "SELECT question_id, subject, question_text, option_a, option_b, option_c, option_d, correct_option " +
                     "FROM questions WHERE subject = ? ORDER BY RAND() LIMIT ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, subject);
            stmt.setInt(2, limit);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String correctStr = rs.getString("correct_option");
                    char correctChar = (correctStr != null && !correctStr.isEmpty()) 
                            ? correctStr.charAt(0) 
                            : ' ';

                    questions.add(new Question(
                            rs.getInt("question_id"),
                            rs.getString("subject"),
                            rs.getString("question_text"),
                            rs.getString("option_a"),
                            rs.getString("option_b"),
                            rs.getString("option_c"),
                            rs.getString("option_d"),
                            correctChar
                    ));
                }
            }
        } catch (SQLException e) {
            System.err.println("[QuestionDAO] Error retrieving questions for subject '" + subject + "': " + e.getMessage());
        }
        return questions;
    }

    /**
     * Adds a new question into the database.
     *
     * @param question Question object to insert
     * @return true if successfully added, false otherwise
     */
    public boolean addQuestion(Question question) {
        String sql = "INSERT INTO questions (subject, question_text, option_a, option_b, option_c, option_d, correct_option) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, question.getSubject());
            stmt.setString(2, question.getQuestionText());
            stmt.setString(3, question.getOptionA());
            stmt.setString(4, question.getOptionB());
            stmt.setString(5, question.getOptionC());
            stmt.setString(6, question.getOptionD());
            stmt.setString(7, String.valueOf(Character.toUpperCase(question.getCorrectOption())));

            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        question.setQuestionId(generatedKeys.getInt(1));
                    }
                }
                return true;
            }
            return false;
        } catch (SQLException e) {
            System.err.println("[QuestionDAO] Error adding new question: " + e.getMessage());
            return false;
        }
    }

    /**
     * Retrieves all distinct subjects available in the questions repository.
     *
     * @return List of distinct subject names
     */
    public List<String> getAvailableSubjects() {
        List<String> subjects = new ArrayList<>();
        String sql = "SELECT DISTINCT subject FROM questions ORDER BY subject ASC";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                subjects.add(rs.getString("subject"));
            }
        } catch (SQLException e) {
            System.err.println("[QuestionDAO] Error fetching available subjects: " + e.getMessage());
        }
        return subjects;
    }

    /**
     * Retrieves the count of questions available for a given subject.
     *
     * @param subject subject name
     * @return number of available questions
     */
    public int getQuestionCountBySubject(String subject) {
        String sql = "SELECT COUNT(*) FROM questions WHERE subject = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, subject);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            System.err.println("[QuestionDAO] Error counting questions: " + e.getMessage());
        }
        return 0;
    }

    /**
     * Retrieves all questions in the database (useful for admin overview).
     *
     * @return List of all Question objects
     */
    public List<Question> getAllQuestions() {
        List<Question> list = new ArrayList<>();
        String sql = "SELECT question_id, subject, question_text, option_a, option_b, option_c, option_d, correct_option " +
                     "FROM questions ORDER BY subject, question_id";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                String correctStr = rs.getString("correct_option");
                char correctChar = (correctStr != null && !correctStr.isEmpty()) ? correctStr.charAt(0) : ' ';
                list.add(new Question(
                        rs.getInt("question_id"),
                        rs.getString("subject"),
                        rs.getString("question_text"),
                        rs.getString("option_a"),
                        rs.getString("option_b"),
                        rs.getString("option_c"),
                        rs.getString("option_d"),
                        correctChar
                ));
            }
        } catch (SQLException e) {
            System.err.println("[QuestionDAO] Error fetching all questions: " + e.getMessage());
        }
        return list;
    }
}
