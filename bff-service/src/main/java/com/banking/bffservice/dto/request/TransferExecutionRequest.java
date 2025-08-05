package com.banking.bffservice.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransferExecutionRequest {
    @NotNull(message = "Transaction ID is required")
    private UUID transactionId;

    @NotNull(message = "From account ID is required")
    @JsonProperty("fromAccountId")
    private UUID fromAccountId;

    @NotNull(message = "To account ID is required")
    @JsonProperty("toAccountId")
    private UUID toAccountId;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    private BigDecimal amount;

    private String description;
}
