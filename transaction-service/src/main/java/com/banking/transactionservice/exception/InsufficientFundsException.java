package com.banking.transactionservice.exception;

/**
 * Exception thrown when a transaction cannot be completed due to insufficient funds
 * in the source account.
 */
public class InsufficientFundsException extends RuntimeException {

    public InsufficientFundsException(String message) {
        super(message);
    }

    public InsufficientFundsException(String message, Throwable cause) {
        super(message, cause);
    }
}
