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

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/")
public class TransactionController {

    @Autowired
    private TransactionService transactionService;

    @PostMapping("/transactions/transfer/initiation")
    public ResponseEntity<TransactionResponseDto> initiateTransaction(@Valid @RequestBody TransactionInitiationDto transactionInitiationDto) {
        TransactionResponseDto response = transactionService.initiateTransaction(transactionInitiationDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/transactions/transfer/execute")
    public ResponseEntity<TransactionResponseDto> executeTransaction(@Valid @RequestBody TransactionExecutionDto transactionExecutionDto)
    {
        TransactionResponseDto response = transactionService.executeTransaction(transactionExecutionDto);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/accounts/{accountId}/transactions")
    public ResponseEntity<?> getTransactionHistory(@PathVariable UUID accountId) {
        List<TransactionHistoryDto> response= transactionService.getTransactionHistory(accountId);
        return ResponseEntity.ok(response);
    }

}

