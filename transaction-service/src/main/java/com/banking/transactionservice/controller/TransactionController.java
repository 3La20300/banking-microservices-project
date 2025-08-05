package com.banking.transactionservice.controller;

import com.banking.transactionservice.dto.TransactionExecutionDto;
import com.banking.transactionservice.dto.TransactionHistoryDto;
import com.banking.transactionservice.dto.TransactionInitiationDto;
import com.banking.transactionservice.dto.TransactionResponseDto;
import com.banking.transactionservice.service.TransactionService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/")
public class TransactionController {

    @Autowired
    private TransactionService transactionService;

    @PostMapping("/transactions/transfer/initiation")
    public Mono<ResponseEntity<TransactionResponseDto>> initiateTransaction(@Valid @RequestBody TransactionInitiationDto transactionInitiationDto) {
        return transactionService.initiateTransaction(transactionInitiationDto)
                .map(response -> ResponseEntity.status(HttpStatus.CREATED).body(response));
    }

    @PostMapping("/transactions/transfer/execution")
    public Mono<ResponseEntity<TransactionResponseDto>> executeTransaction(@Valid @RequestBody TransactionExecutionDto executionRequest)
    {
//        UUID transactionId = UUID.fromString(executionRequest.get("transactionId"));
        UUID transactionId = executionRequest.getTransactionId();
        return transactionService.executeTransaction(transactionId)
                .map(response -> ResponseEntity.ok(response));
    }

    @GetMapping("/accounts/{accountId}/transactions")
    public ResponseEntity<?> getTransactionHistory(@PathVariable UUID accountId) {
        List<TransactionHistoryDto> response = transactionService.getTransactionHistory(accountId);
        return ResponseEntity.ok(response);
    }
}
