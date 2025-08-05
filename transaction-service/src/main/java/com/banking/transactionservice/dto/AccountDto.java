package com.banking.transactionservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AccountDto {
    private UUID accountId;
    private String accountNumber;
    private String accountType;
    private BigDecimal balance;
    private String status;
    private LocalDateTime createdAt;
    private List<TransactionResponseDto> transactions;
}
