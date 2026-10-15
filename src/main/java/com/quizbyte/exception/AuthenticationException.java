package com.quizbyte.exception;

/**
 * Thrown when user authentication fails due to invalid credentials,
 * unauthorized role access, or registration validation issues.
 */
public class AuthenticationException extends QuizbyteException {

    public AuthenticationException(String message) {
        super(message);
    }

    public AuthenticationException(String message, Throwable cause) {
        super(message, cause);
    }
}
