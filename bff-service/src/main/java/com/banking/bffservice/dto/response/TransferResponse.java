package com.banking.bffservice.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonPropertyOrder({"transactionId", "fromAccountId", "toAccountId", "amount", "description", "timestamp"})
public class TransferResponse {
    private UUID transactionId;

    @JsonProperty("fromAccountId")
    private UUID fromAccountId;

    @JsonProperty("toAccountId")
    private UUID toAccountId;

    private BigDecimal amount;
    private String description;
    private LocalDateTime timestamp;

    // Constructor that matches the order of the JSON
//    public TransferResponse(UUID transactionId, UUID fromAccountId, UUID toAccountId,
//                            BigDecimal amount, String description, LocalDateTime timestamp) {
//        this.transactionId = transactionId;
//        this.fromAccountId = fromAccountId;
//        this.toAccountId = toAccountId;
//        this.amount = amount;
//        this.description = description;
//        this.timestamp = timestamp;
//    }
}
