package com.quizbyte.exception;

/**
 * Root checked exception for all application-level errors in Quizbyte.
 * Demonstrates clean OOP exception hierarchy and error encapsulation.
 */
public class QuizbyteException extends Exception {

    public QuizbyteException(String message) {
        super(message);
    }

    public QuizbyteException(String message, Throwable cause) {
        super(message, cause);
    }
}
