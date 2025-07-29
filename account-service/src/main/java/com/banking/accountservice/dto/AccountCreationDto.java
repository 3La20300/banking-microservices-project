package com.banking.accountservice.dto;

import com.banking.accountservice.model.Account;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public class AccountCreationDto {
    @NotNull(message = "UserId cannot be blank")
    private UUID userId;

    @NotNull(message = "Account Type cannot be empty")
    private Account.AccountType accountType;

    @NotNull(message = "Initial Balance cannot be empty")
    private BigDecimal initialBalance;

    public AccountCreationDto() {}

    public AccountCreationDto(UUID userId, Account.AccountType accountType, BigDecimal initialBalance) {
        this.userId = userId;
        this.accountType = accountType;
        this.initialBalance = initialBalance;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public Account.AccountType getAccountType() {
        return accountType;
    }

    public void setAccountType(Account.AccountType accountType) {
        this.accountType = accountType;
    }

    public BigDecimal getInitialBalance() {
        return initialBalance;
    }

    public void setInitialBalance(BigDecimal intialBalance) {
        this.initialBalance = intialBalance;
    }
}
