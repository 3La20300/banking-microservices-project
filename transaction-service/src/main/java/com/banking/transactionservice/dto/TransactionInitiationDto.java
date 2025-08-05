package com.banking.transactionservice.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransactionInitiationDto {

    @NotNull(message = "From account ID cannot be null")
    private UUID fromAccountId;

    @NotNull(message = "To account ID cannot be null")
    private UUID toAccountId;

    @NotNull(message = "Amount cannot be null")
    private BigDecimal amount;


    private String description;



}
