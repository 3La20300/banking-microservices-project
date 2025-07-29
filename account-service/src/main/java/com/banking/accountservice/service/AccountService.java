package com.banking.accountservice.service;

import com.banking.accountservice.dto.AccountCreationDto;
import com.banking.accountservice.dto.AccountResponseDto;
import com.banking.accountservice.exception.AccountNotFoundException;
import com.banking.accountservice.exception.InvalidAccountDataException;
import com.banking.accountservice.exception.NoAccountsForUserId;
import com.banking.accountservice.model.Account;
import com.banking.accountservice.repository.AccountRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class AccountService {

    @Autowired
    private AccountRepository accountRepository;

    //1. Create a new account
    @Transactional
    public AccountResponseDto createAccount(AccountCreationDto accountCreationDto) {
        //The method compareTo(BigDecimal.ZERO) returns:
        //
        //-1 if the number is less than 0
        //
        //0 if it is equal to 0
        //
        //1 if it is greater than 0
        if (accountCreationDto.getInitialBalance().compareTo(BigDecimal.ZERO) < 0) {
            throw new InvalidAccountDataException("Initial balance cannot be negative.");
        }
        // Validate account type
        if (accountCreationDto.getAccountType() == null ||
            !(accountCreationDto.getAccountType() == Account.AccountType.SAVINGS ||
              accountCreationDto.getAccountType() == Account.AccountType.CHECKING)) {
            throw new com.banking.accountservice.exception.InvalidAccountTypeException("Invalid account type. Allowed values: SAVINGS, CHECKING.");
        }
        Account account= new Account();
        account.setUserId(accountCreationDto.getUserId());
        account.setAccountType(accountCreationDto.getAccountType());
        //Account Number is Generated
        String accountNumber;
        do {
            accountNumber = generateAccountNumber();
        } while (accountRepository.findByAccountNumber(accountNumber).isPresent());

        account.setAccountNumber(accountNumber);
        account.setBalance(accountCreationDto.getInitialBalance());
        account.setStatus(Account.AccountStatus.ACTIVE);

        Account savedAccount = accountRepository.save(account);


        return new AccountResponseDto(savedAccount.getAccountId(),
                savedAccount.getAccountNumber(),
                "Account created successfully");

    }
    //2.Get Account by ID
    public AccountResponseDto getAccountById(UUID accountId) throws AccountNotFoundException {
        Account account= accountRepository.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException("Account Not Found"));
        return new AccountResponseDto(
                account.getAccountId(),
                account.getUserId(),
                account.getAccountNumber(),
                account.getAccountType(),
                account.getBalance(),
                account.getStatus(),
                account.getCreatedAt()
        );
    }

    //3: Get all accounts for a user
    public List<AccountResponseDto> getAccountsByUserId(UUID userId) {
        List<Account> accounts = accountRepository.findByUserId(userId);
        if (accounts.isEmpty()) {
            throw new NoAccountsForUserId("No accounts found for user ID : " + userId);
        }
        return accounts.stream()
                .map(acc -> new AccountResponseDto(
                        acc.getAccountId(),
                        acc.getAccountNumber(),
                        acc.getAccountType(),
                        acc.getBalance(),
                        acc.getStatus()
                ))
                .collect(Collectors.toList());
    }
        //4. Scheduled job to inactivate stale accounts
        @Scheduled(fixedRate=300000)
        public void inactivateStaleAccounts()
        {
            LocalDateTime cutoff = LocalDateTime.now().minusHours(24);
            List<Account> activeAcoounts= accountRepository.findAll().stream()
                    .filter(acc->acc.getStatus()== Account.AccountStatus.ACTIVE)
                    .filter(acc -> acc.getUpdatedAt().isBefore(cutoff))
                    .collect(Collectors.toList());

            for(Account acc:activeAcoounts)
            {
                acc.setStatus(Account.AccountStatus.INACTIVE);
                accountRepository.save(acc);
            }
        }

        private String generateAccountNumber(){
        return String.valueOf((long) (Math.random()*1_000_000_0000L));
        }


}
