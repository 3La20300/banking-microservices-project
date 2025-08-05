package com.banking.accountservice.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "accounts")
public class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(columnDefinition = "uuid", updatable = false, nullable = false)
    private UUID accountId;

    @Column(name="user_id")
    private UUID userId;

    @NotBlank
    @Column(unique = true, nullable = false, length = 20)
    private String accountNumber;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name= "account_type" ,nullable = false)
    private AccountType accountType;

    @NotNull
    @Column(nullable = false ,precision=15, scale=2)
    private BigDecimal balance=BigDecimal.ZERO;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AccountStatus status=AccountStatus.ACTIVE;

    @NotNull
    @Column(name= "created_at" ,nullable = false)
    private LocalDateTime createdAt;

    @NotNull
    @Column(name="updated_at",nullable = false)
    private LocalDateTime updatedAt;

    @Column(name="last_transaction_date")
    private LocalDateTime lastTransactionDate;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.lastTransactionDate = LocalDateTime.now(); // Initialize with creation date
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public enum AccountType {
        SAVINGS, CHECKING
    }

    public enum AccountStatus {
        ACTIVE, INACTIVE
    }

    public Account(){}

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID user_id) {
        this.userId = user_id;
    }

    public UUID getAccountId() {
        return accountId;
    }

    public void setAccountId(UUID accountId) {
        this.accountId = accountId;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public AccountType getAccountType() {
        return accountType;
    }

    public void setAccountType(AccountType accountType) {
        this.accountType = accountType;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public AccountStatus getStatus() {
        return status;
    }

    public void setStatus(AccountStatus status) {
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

    public LocalDateTime getLastTransactionDate() {
        return lastTransactionDate;
    }

    public void setLastTransactionDate(LocalDateTime lastTransactionDate) {
        this.lastTransactionDate = lastTransactionDate;
    }
}
