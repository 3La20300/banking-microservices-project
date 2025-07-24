package com.banking.accountservice.exception;

// No need for it as Type is defined as enum so Spring will Handle this Automatically !
public class InvalidAccountTypeException extends RuntimeException {
    public InvalidAccountTypeException(String message) {
        super(message);
    }
}
