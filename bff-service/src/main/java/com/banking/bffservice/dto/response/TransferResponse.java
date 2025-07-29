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
public class TransferResponse {
    private UUID transactionId;
    private UUID from_accountId;
    private String to_accountId;
    private BigDecimal amount;
    private String description;
    private LocalDateTime timestamp;

}
