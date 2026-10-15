package com.quizbyte.exception;

/**
 * Thrown when a database access error, query failure, or connection drop
 * occurs within the DAO or persistence layer.
 */
public class DatabaseOperationException extends QuizbyteException {

    public DatabaseOperationException(String message) {
        super(message);
    }

    public DatabaseOperationException(String message, Throwable cause) {
        super(message, cause);
    }
}
