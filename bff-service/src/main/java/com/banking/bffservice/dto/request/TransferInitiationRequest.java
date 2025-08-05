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
public class TransferInitiationRequest {
    @NotNull(message = "From account ID is required")
    @JsonProperty("fromAccountId") // maps to the JSON property "fromAccountId" to match the API contract as database field // is "from_account_id"
    private UUID fromAccountId;

    @NotNull(message = "To account ID is required")
    @JsonProperty("toAccountId") // maps to the JSON property "toAccountId" to match the API contract
    private UUID toAccountId;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    private BigDecimal amount;

    private String description;
}
