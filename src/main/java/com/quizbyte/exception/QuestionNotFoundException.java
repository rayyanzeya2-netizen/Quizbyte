package com.quizbyte.exception;

/**
 * Thrown when no questions exist for a selected subject or a requested question
 * cannot be located in the database.
 */
public class QuestionNotFoundException extends QuizbyteException {

    public QuestionNotFoundException(String message) {
        super(message);
    }

    public QuestionNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
