package com.banking.accountservice.exception;

public class NoAccountsForUserId extends RuntimeException{
    public NoAccountsForUserId(String message) {
        super(message);
    }
}
