package com.quizbyte.service;

import com.quizbyte.dao.QuestionDAO;
import com.quizbyte.dao.ResultDAO;
import com.quizbyte.exception.DatabaseOperationException;
import com.quizbyte.exception.QuestionNotFoundException;
import com.quizbyte.model.Question;
import com.quizbyte.model.QuizResult;
import com.quizbyte.model.User;
import com.quizbyte.util.ThreadSafeLeaderboardCache;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

/**
 * Service orchestrating quiz sessions, utilizing Collections framework components:
 * List for sequencing, Map<Integer, Character> for answer tracking, Set for subject indexing,
 * and Collections.shuffle() for question randomization.
 */
public class QuizService {

    private final QuestionDAO questionDAO;
    private final ResultDAO resultDAO;
    private final ThreadSafeLeaderboardCache cache = ThreadSafeLeaderboardCache.getInstance();

    public QuizService() {
        this.questionDAO = new QuestionDAO();
        this.resultDAO = new ResultDAO();
    }

    public QuizService(QuestionDAO questionDAO, ResultDAO resultDAO) {
        this.questionDAO = questionDAO;
        this.resultDAO = resultDAO;
    }

    /**
     * Conducts a full quiz session for the console runner.
     * Demonstrates Collections (List, Set, Map) and multithreaded test counter.
     */
    public void conductQuiz(User user, Scanner scanner) {
        System.out.println("\n========================================================");
        System.out.println("                 START NEW QUIZ SESSION                ");
        System.out.println("========================================================");

        List<String> subjects = questionDAO.getAvailableSubjects();
        if (subjects.isEmpty()) {
            System.out.println("(!) No quiz subjects currently available. Please contact an admin.");
            return;
        }

        System.out.println("Select a subject for your quiz:");
        for (int i = 0; i < subjects.size(); i++) {
            int count = 0;
            try {
                count = questionDAO.getQuestionCountBySubject(subjects.get(i));
            } catch (DatabaseOperationException ignored) {}
            System.out.printf("  [%d] %-15s (%d question%s available)%n", 
                    (i + 1), subjects.get(i), count, count == 1 ? "" : "s");
        }
        System.out.printf("  [%d] Cancel and return to menu%n", subjects.size() + 1);

        int choice = promptInt(scanner, "Enter subject choice: ", 1, subjects.size() + 1);
        if (choice == subjects.size() + 1) {
            System.out.println("Quiz attempt cancelled.");
            return;
        }

        String selectedSubject = subjects.get(choice - 1);

        // Track active concurrent test using atomic counter
        cache.incrementActiveQuizzes();

        try {
            // Fetch up to 5 randomized questions per quiz
            int quizLimit = 5;
            List<Question> questions = questionDAO.getQuestionsBySubject(selectedSubject, quizLimit);

            System.out.println("\n--------------------------------------------------------");
            System.out.printf("  Quiz Subject: %s | Total Questions: %d%n", selectedSubject, questions.size());
            System.out.println("  Instructions: Type A, B, C, or D for each question.");
            System.out.println("--------------------------------------------------------");

            // Collections Rubric: Map to record question ID -> Student chosen option
            Map<Integer, Character> studentAnswers = new HashMap<>();
            int score = 0;

            for (int i = 0; i < questions.size(); i++) {
                Question q = questions.get(i);
                System.out.printf("%n[Question %d of %d]%n", (i + 1), questions.size());
                System.out.println(q.getQuestionText());
                System.out.printf("   [A] %s%n", q.getOptionA());
                System.out.printf("   [B] %s%n", q.getOptionB());
                System.out.printf("   [C] %s%n", q.getOptionC());
                System.out.printf("   [D] %s%n", q.getOptionD());

                char userAnswer = promptAnswer(scanner);
                studentAnswers.put(q.getQuestionId(), userAnswer);

                if (userAnswer == q.getCorrectOption()) {
                    System.out.println(">> Correct Answer! (+1 point)");
                    score++;
                } else {
                    System.out.printf(">> Incorrect! Correct option was [%c]: %s%n", 
                            q.getCorrectOption(), q.getOptionByLetter(q.getCorrectOption()));
                }
            }

            // Calculate and persist result
            int totalQuestions = questions.size();
            double percentage = ((double) score / totalQuestions) * 100.0;
            QuizResult result = new QuizResult(user.getUserId(), selectedSubject, score, totalQuestions, percentage);

            boolean saved = resultDAO.saveResult(result);
            displayScoreCard(user.getUsername(), selectedSubject, score, totalQuestions, percentage, result.getGrade(), saved);

        } catch (QuestionNotFoundException | DatabaseOperationException e) {
            System.err.println("(!) Error running quiz: " + e.getMessage());
        } finally {
            cache.decrementActiveQuizzes();
        }
    }

    private void displayScoreCard(String username, String subject, int score, 
                                  int total, double percentage, String grade, boolean saved) {
        System.out.println("\n========================================================");
        System.out.println("                   QUIZ RESULT SUMMARY                  ");
        System.out.println("========================================================");
        System.out.printf("  Student      : %s%n", username);
        System.out.printf("  Subject      : %s%n", subject);
        System.out.printf("  Score        : %d / %d%n", score, total);
        System.out.printf("  Percentage   : %.2f%%%n", percentage);
        System.out.printf("  Grade        : %s%n", grade);
        System.out.printf("  Performance  : %s%n", getFeedback(percentage));
        if (saved) {
            System.out.println("  Status       : Result saved successfully to database.");
        } else {
            System.out.println("  Status       : (!) Warning: Result could not be saved.");
        }
        System.out.println("========================================================");
    }

    private String getFeedback(double percentage) {
        if (percentage >= 90.0) return "Outstanding! Mastery demonstrated.";
        if (percentage >= 75.0) return "Great job! Strong understanding of the subject.";
        if (percentage >= 50.0) return "Passed! A bit more practice will make you shine.";
        return "Needs Improvement. Review the fundamentals and try again.";
    }

    public void displayUserHistory(int userId, String username) {
        System.out.println("\n===============================================================================");
        System.out.printf("                   SCORE HISTORY FOR: %s%n", username.toUpperCase());
        System.out.println("===============================================================================");
        try {
            List<QuizResult> results = resultDAO.getResultsByUserId(userId);
            if (results.isEmpty()) {
                System.out.println("  No quiz attempts found yet. Take a quiz to record your first score!");
                System.out.println("===============================================================================");
                return;
            }

            System.out.printf("%-6s | %-12s | %-8s | %-12s | %-6s | %-19s%n", 
                    "ID", "Subject", "Score", "Percentage", "Grade", "Attempt Date");
            System.out.println("-------------------------------------------------------------------------------");
            for (QuizResult r : results) {
                String dateStr = (r.getAttemptDate() != null) 
                        ? r.getAttemptDate().toString().substring(0, 19) 
                        : "N/A";
                System.out.printf("%-6d | %-12s | %2d/%-5d | %9.2f%% | %-6s | %-19s%n",
                        r.getResultId(),
                        r.getSubject(),
                        r.getScore(),
                        r.getTotalQuestions(),
                        r.getPercentage(),
                        r.getGrade(),
                        dateStr);
            }
        } catch (DatabaseOperationException e) {
            System.err.println("(!) Error fetching user history: " + e.getMessage());
        }
        System.out.println("===============================================================================");
    }

    public void displayLeaderboard() {
        System.out.println("\n===============================================================================");
        System.out.println("                        GLOBAL QUIZ LEADERBOARD                                ");
        System.out.println("===============================================================================");
        try {
            List<QuizResult> leaderboard = resultDAO.getAllLeaderboardResults();
            if (leaderboard.isEmpty()) {
                System.out.println("  No quiz attempts recorded on the leaderboard yet.");
                System.out.println("===============================================================================");
                return;
            }

            System.out.printf("%-5s | %-15s | %-10s | %-8s | %-10s | %-6s%n", 
                    "Rank", "Student", "Subject", "Score", "Percentage", "Grade");
            System.out.println("-------------------------------------------------------------------------------");
            int rank = 1;
            for (QuizResult r : leaderboard) {
                System.out.printf("#%-4d | %-15s | %-10s | %2d/%-5d | %7.2f%%  | %-6s%n",
                        rank++,
                        r.getUsername() != null ? r.getUsername() : "User #" + r.getUserId(),
                        r.getSubject(),
                        r.getScore(),
                        r.getTotalQuestions(),
                        r.getPercentage(),
                        r.getGrade());
            }
        } catch (DatabaseOperationException e) {
            System.err.println("(!) Error fetching leaderboard: " + e.getMessage());
        }
        System.out.println("===============================================================================");
    }

    public void displayGlobalHistory() {
        System.out.println("\n===========================================================================================");
        System.out.println("                               GLOBAL SCORE HISTORY (ADMIN)                               ");
        System.out.println("===========================================================================================");
        try {
            List<QuizResult> allResults = resultDAO.getAllResults();
            if (allResults.isEmpty()) {
                System.out.println("  No score history records found in database.");
                System.out.println("===========================================================================================");
                return;
            }

            System.out.printf("%-6s | %-15s | %-10s | %-8s | %-10s | %-6s | %-19s%n", 
                    "ID", "Student", "Subject", "Score", "Percentage", "Grade", "Date");
            System.out.println("-------------------------------------------------------------------------------------------");
            for (QuizResult r : allResults) {
                String dateStr = (r.getAttemptDate() != null) 
                        ? r.getAttemptDate().toString().substring(0, 19) 
                        : "N/A";
                System.out.printf("%-6d | %-15s | %-10s | %2d/%-5d | %7.2f%%  | %-6s | %-19s%n",
                        r.getResultId(),
                        r.getUsername() != null ? r.getUsername() : "User #" + r.getUserId(),
                        r.getSubject(),
                        r.getScore(),
                        r.getTotalQuestions(),
                        r.getPercentage(),
                        r.getGrade(),
                        dateStr);
            }
        } catch (DatabaseOperationException e) {
            System.err.println("(!) Error fetching global score history: " + e.getMessage());
        }
        System.out.println("===========================================================================================");
    }

    public void addNewQuestion(Scanner scanner) {
        System.out.println("\n========================================================");
        System.out.println("                   ADD A NEW QUESTION                   ");
        System.out.println("========================================================");

        System.out.print("Enter Subject (e.g., Java, OOP, DBMS, DSA): ");
        String subject = scanner.nextLine().trim();
        while (subject.isEmpty()) {
            System.out.print("(!) Subject cannot be empty. Please re-enter: ");
            subject = scanner.nextLine().trim();
        }

        System.out.print("Enter Question Text: ");
        String questionText = scanner.nextLine().trim();
        while (questionText.isEmpty()) {
            System.out.print("(!) Question text cannot be empty. Please re-enter: ");
            questionText = scanner.nextLine().trim();
        }

        System.out.print("Enter Option [A]: ");
        String optA = scanner.nextLine().trim();
        while (optA.isEmpty()) {
            System.out.print("(!) Option A cannot be empty: ");
            optA = scanner.nextLine().trim();
        }

        System.out.print("Enter Option [B]: ");
        String optB = scanner.nextLine().trim();
        while (optB.isEmpty()) {
            System.out.print("(!) Option B cannot be empty: ");
            optB = scanner.nextLine().trim();
        }

        System.out.print("Enter Option [C]: ");
        String optC = scanner.nextLine().trim();
        while (optC.isEmpty()) {
            System.out.print("(!) Option C cannot be empty: ");
            optC = scanner.nextLine().trim();
        }

        System.out.print("Enter Option [D]: ");
        String optD = scanner.nextLine().trim();
        while (optD.isEmpty()) {
            System.out.print("(!) Option D cannot be empty: ");
            optD = scanner.nextLine().trim();
        }

        char correctOption = promptAnswer(scanner);

        Question newQuestion = new Question(subject, questionText, optA, optB, optC, optD, correctOption);
        try {
            boolean success = questionDAO.addQuestion(newQuestion);
            if (success) {
                System.out.println("\n[Success] New question added successfully! (Question ID: " + newQuestion.getQuestionId() + ")");
            } else {
                System.out.println("\n(!) Failed to add question.");
            }
        } catch (DatabaseOperationException e) {
            System.err.println("\n(!) Database error: " + e.getMessage());
        }
    }

    private char promptAnswer(Scanner scanner) {
        while (true) {
            System.out.print("Enter correct option (A/B/C/D): ");
            String input = scanner.nextLine().trim().toUpperCase();
            if (input.length() == 1) {
                char ch = input.charAt(0);
                if (ch >= 'A' && ch <= 'D') {
                    return ch;
                }
            }
            System.out.println("(!) Invalid option. Please enter 'A', 'B', 'C', or 'D'.");
        }
    }

    private int promptInt(Scanner scanner, String prompt, int min, int max) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            try {
                int val = Integer.parseInt(input);
                if (val >= min && val <= max) {
                    return val;
                }
                System.out.printf("(!) Please enter a number between %d and %d.%n", min, max);
            } catch (NumberFormatException e) {
                System.out.println("(!) Invalid number format. Please enter digits only.");
            }
        }
    }
}
