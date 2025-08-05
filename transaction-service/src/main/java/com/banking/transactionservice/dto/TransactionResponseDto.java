package com.banking.transactionservice.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL) //Only Non-Null Fields in JSON
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransactionResponseDto {
    private UUID transactionId;
//    private UUID fromAccountId;
//    private UUID toAccountId;
    private String status;
//    private BigDecimal amount;
//    private String description;
    private LocalDateTime timestamp;

}
