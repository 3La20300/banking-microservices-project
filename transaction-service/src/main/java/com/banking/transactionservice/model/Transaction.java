package com.banking.transactionservice.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
@Entity
@Table(name = "transactions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", columnDefinition = "uuid", updatable = false, nullable = false)
    private UUID transactionId;

    @Column(nullable = false, name = "from_Account_id")
//    @Column(nullable = false, name = "fromAccountId")
    private UUID fromAccountId;

    @Column(nullable = false, name = "to_account_id")
//    @Column(nullable = false, name = "toAccountId")
    private UUID toAccountId;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Column(nullable = true, length = 255)
    private String description; // e.g., "DEBIT", "CREDIT"

    @Column(nullable = false)
    private TransactionStatus status=TransactionStatus.INITIATED;

    public enum TransactionStatus {
        INITIATED,
        SUCCESS,
        FAILED
    }

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @PrePersist
    protected void onCreate() {
        this.timestamp = LocalDateTime.now();
    }
}
