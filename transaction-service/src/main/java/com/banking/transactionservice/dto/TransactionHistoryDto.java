package com.banking.transactionservice.dto;


import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL) // Only include non-null fields in JSON
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransactionHistoryDto {

    private UUID transactionId;
    private UUID accountId;
    private BigDecimal amount;
    private String description; // Optional description of the transaction
    private LocalDateTime timestamp; // ISO 8601 format
    private BigDecimal currentBalance; // Current balance after the transaction

}
