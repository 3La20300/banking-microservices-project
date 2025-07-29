package com.banking.bffservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Transaction {
    private UUID transactionId;
    private UUID accountId;
    private BigDecimal amount;
    private String type;
    private String description;
    private LocalDateTime timestamp;
    private BigDecimal currentBalance;
}
