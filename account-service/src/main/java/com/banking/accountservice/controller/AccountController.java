package com.banking.accountservice.controller;


import com.banking.accountservice.dto.AccountCreationDto;
import com.banking.accountservice.dto.AccountResponseDto;
import com.banking.accountservice.service.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/")
public class AccountController {
    @Autowired
    AccountService accountService;

    //1. Create a new Account
    @PostMapping("/accounts")
    public ResponseEntity<AccountResponseDto> createAccount(@RequestBody AccountCreationDto account) {
        AccountResponseDto response = accountService.createAccount(account);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    //2. Get account By ID
    @GetMapping("/accounts/{accountId}")
    public ResponseEntity<AccountResponseDto> getAccountById(@PathVariable UUID accountId) {
        AccountResponseDto response = accountService.getAccountById(accountId);
        return ResponseEntity.ok(response);
    }

    //3. Get All Account for a User
    @GetMapping("/users/{userId}/accounts")
    public ResponseEntity<List<AccountResponseDto>> getAccountByUserId(@PathVariable UUID userId) {
        List<AccountResponseDto> response = accountService.getAccountsByUserId(userId);
        return ResponseEntity.ok(response);
    }

    //4. Update Account Balance
//    @PutMapping("/accounts/{accountId}/balance")
//    public ResponseEntity<AccountResponseDto> updateAccountBalance(
//            @PathVariable UUID accountId,
//            @RequestBody Map<String, BigDecimal> balanceUpdate) {
//
//        BigDecimal amount = balanceUpdate.get("amount");
//        AccountResponseDto response = accountService.updateAccountBalance(accountId, amount);
//        return ResponseEntity.ok(response);
//    }

    //5. Transfer between accounts (as per requirements)
    @PutMapping("/accounts/transfer")
    public ResponseEntity<Map<String, String>> transferBetweenAccounts(
            @RequestBody Map<String, Object> transferRequest) {

        UUID fromAccountId = UUID.fromString(transferRequest.get("fromAccountId").toString());
        UUID toAccountId = UUID.fromString(transferRequest.get("toAccountId").toString());
        BigDecimal amount = new BigDecimal(transferRequest.get("amount").toString());

        accountService.transferBetweenAccounts(fromAccountId, toAccountId, amount);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Account updated successfully.");
        return ResponseEntity.ok(response);
    }
}
