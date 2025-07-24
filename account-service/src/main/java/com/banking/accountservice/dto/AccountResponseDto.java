package com.banking.accountservice.dto;

import com.banking.accountservice.model.Account;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class AccountResponseDto {
    private UUID accountId;
    private UUID userId;
    private String accountNumber;
    private Account.AccountType accountType;
    private BigDecimal balance;
    private Account.AccountStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private String message;

    public AccountResponseDto() {}

    // Respone: POST /accounts: Creates a new account.
    public AccountResponseDto(UUID accountId, String accountNumber, String message ){
        this.accountId = accountId;
        this.accountNumber = accountNumber;
        this.message = message;
    }

    //Response: GET /accounts/{accountId}: Retrieves details of a specific bank account.
    public AccountResponseDto(UUID accountId,
                              UUID userId,
                              String accountNumber,
                              Account.AccountType accountType,
                              BigDecimal balance,
                              Account.AccountStatus status,
                              LocalDateTime createdAt
                              ){
        this.accountId = accountId;
        this.userId = userId;
        this.accountNumber = accountNumber;
        this.accountType = accountType;
        this.balance = balance;
        this.status = status;
        this.createdAt = createdAt;
    }

    public AccountResponseDto( UUID accountId,
                               String accountNumber,
                               Account.AccountType accountType,
                               BigDecimal balance,
                               Account.AccountStatus status){
        this.accountId = accountId;
        this.accountNumber = accountNumber;
        this.accountType = accountType;
        this.balance = balance;
        this.status = status;
    }

    public UUID getAccountId() {
        return accountId;
    }

    public void setAccountId(UUID accountId) {
        this.accountId = accountId;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public Account.AccountType getAccountType() {
        return accountType;
    }

    public void setAccountType(Account.AccountType accountType) {
        this.accountType = accountType;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public Account.AccountStatus getStatus() {
        return status;
    }

    public void setStatus(Account.AccountStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
