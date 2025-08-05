package com.banking.bffservice.exception;

public class AccountsNotFoundException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public AccountsNotFoundException(String message) {
        super(message);
    }

    public AccountsNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
