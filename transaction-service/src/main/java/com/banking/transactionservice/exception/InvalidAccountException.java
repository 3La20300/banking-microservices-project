package com.banking.transactionservice.exception;

/**
 * Exception thrown when an operation is attempted with an invalid or non-existent account ID.
 */
public class InvalidAccountException extends RuntimeException {

    public InvalidAccountException(String message) {
        super(message);
    }

    public InvalidAccountException(String message, Throwable cause) {
        super(message, cause);
    }
}
