package com.banking.accountservice.controller;


import com.banking.accountservice.dto.AccountCreationDto;
import com.banking.accountservice.dto.AccountResponseDto;
import com.banking.accountservice.service.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/accounts")
public class AccountController {
    @Autowired
    AccountService accountService;

    //1. Create a new Account
    @PostMapping
    public ResponseEntity<AccountResponseDto> createAccount(@RequestBody AccountCreationDto account) {
        AccountResponseDto response =accountService.createAccount(account);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    //2. Get account By ID
    @GetMapping("/{accountId")
    public ResponseEntity<AccountResponseDto> getAccountById(@PathVariable UUID accountId) {
        AccountResponseDto response =accountService.getAccountById(accountId);
        return ResponseEntity.ok(response);
    }

    //3. Get All Account for a User
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<AccountResponseDto>> getAccountByUserId(@PathVariable UUID userId) {
        List<AccountResponseDto> response =accountService.getAccountsByUserId(userId);
        return ResponseEntity.ok(response);
    }
}
