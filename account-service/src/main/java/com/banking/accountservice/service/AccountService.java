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
//        @Scheduled(fixedRate=300000)//5 minutes
//        @Scheduled(fixedRate=60000)//1 minute
//        @Scheduled(cron = "0 */3 * * * *") // Every 3rd minute 3rd minute on every Day Like Companies reset at 12:00 AM

        @Scheduled(fixedRate = 180000) // 3 minutes (in milliseconds)
        public void inactivateStaleAccounts()
        {
            System.out.println("Running inactivateStaleAccounts job at " + LocalDateTime.now());

            //I know Requirenment asked for 24 hours, but for testing purpose I am using 1 minute
            LocalDateTime cutoff = LocalDateTime.now().minusMinutes(3);
            System.out.println("Looking for accounts with no transactions since: " + cutoff);

            // Get all accounts first to see what we're working with
            List<Account> allAccounts = accountRepository.findAll();
            System.out.println("Total accounts in database: " + allAccounts.size());

            // Get active accounts
            List<Account> activeAccounts = allAccounts.stream()
                    .filter(acc -> acc.getStatus() == Account.AccountStatus.ACTIVE)
                    .collect(Collectors.toList());

            System.out.println("Total ACTIVE accounts: " + activeAccounts.size());

            // Log details for each active account
            for (Account acc : activeAccounts) {
                LocalDateTime lastTxDate = acc.getLastTransactionDate();
                if (lastTxDate == null) {
                    lastTxDate = acc.getCreatedAt();
                    System.out.println("Account " + acc.getAccountId() + " has NULL lastTransactionDate, using createdAt: " + lastTxDate);
                } else {
                    System.out.println("Account " + acc.getAccountId() + " lastTransactionDate: " + lastTxDate);
                }

                if (lastTxDate.isBefore(cutoff)) {
                    System.out.println("Account " + acc.getAccountId() + " qualifies for inactivation (last tx: " + lastTxDate + ")");
                } else {
                    System.out.println("Account " + acc.getAccountId() + " is still active (last tx: " + lastTxDate + ")");
                }
            }

            // Filter for stale accounts
            List<Account> staleAccounts = activeAccounts.stream()
                    .filter(acc -> {
                        LocalDateTime lastTxDate = acc.getLastTransactionDate();
                        // If lastTransactionDate is null, fall back to createdAt date
                        if (lastTxDate == null) {
                            lastTxDate = acc.getCreatedAt();
                        }
                        return lastTxDate.isBefore(cutoff);
                    })
                    .collect(Collectors.toList());

            System.out.println("Stale accounts found: " + staleAccounts.size());

            int count = 0;
            if (staleAccounts.isEmpty()) {
                System.out.println("No stale accounts found to inactivate.");
                return;
            }
            for (Account acc : staleAccounts)
            {
                System.out.println("Inactivating account: " + acc.getAccountId() + ", last transaction: " +
                                   (acc.getLastTransactionDate() != null ? acc.getLastTransactionDate() : acc.getCreatedAt()));
                acc.setStatus(Account.AccountStatus.INACTIVE);
                accountRepository.save(acc);
                count++;
            }

            // Log how many accounts were inactivated
            System.out.println("Inactivated " + count + " stale accounts that had no transactions for 1+ minute");
        }

        //4. Update account balance
        @Transactional
        public AccountResponseDto updateAccountBalance(UUID accountId, BigDecimal amount) {
            Account account = accountRepository.findById(accountId)
                    .orElseThrow(() -> new AccountNotFoundException("Account not found with ID: " + accountId));

            // Update the balance
            BigDecimal currentBalance = account.getBalance();
            BigDecimal newBalance;

            // Handle debit and credit operations correctly: Already handled in TransactionService but just in case
            if (amount.compareTo(BigDecimal.ZERO) < 0) {
                // This is a debit operation (withdrawal or transfer from)
                if (currentBalance.compareTo(amount.abs()) < 0) {
                    throw new InvalidAccountDataException("Insufficient funds for this operation");
                }
                newBalance = currentBalance.add(amount); // amount is negative
            } else {
                // This is a credit operation (deposit or transfer to)
                newBalance = currentBalance.add(amount);
            }

            // Safety check to ensure balance doesn't go below zero: Already handled in TransactionService but just in case
            if (newBalance.compareTo(BigDecimal.ZERO) < 0) {
                throw new InvalidAccountDataException("Transaction would result in negative balance");
            }

            account.setBalance(newBalance);

            // Update the last transaction date when balance changes due to a transaction
            account.setLastTransactionDate(LocalDateTime.now());

//            account.setUpdatedAt(LocalDateTime.now());

            Account updatedAccount = accountRepository.save(account);

            if (updatedAccount == null) {
                throw new InvalidAccountDataException("Failed to update account balance");
            }

            return new AccountResponseDto(
                    updatedAccount.getAccountId(),
                    updatedAccount.getAccountNumber(),
                    updatedAccount.getAccountType(),
                    updatedAccount.getBalance(),
                    updatedAccount.getStatus()
            );
        }

        //5. Transfer between accounts
        @Transactional
        public void transferBetweenAccounts(UUID fromAccountId, UUID toAccountId, BigDecimal amount) {
            // Validation
            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                throw new InvalidAccountDataException("Transfer amount must be greater than zero");
            }

            if (fromAccountId.equals(toAccountId)) {
                throw new InvalidAccountDataException("Cannot transfer to the same account");
            }

            // Get source account
            Account fromAccount = accountRepository.findById(fromAccountId)
                    .orElseThrow(() -> new AccountNotFoundException("Source account not found with ID: " + fromAccountId));

            // Get destination account
            Account toAccount = accountRepository.findById(toAccountId)
                    .orElseThrow(() -> new AccountNotFoundException("Destination account not found with ID: " + toAccountId));

            // Check if both accounts are active
            if (fromAccount.getStatus() != Account.AccountStatus.ACTIVE ||
                toAccount.getStatus() != Account.AccountStatus.ACTIVE) {
                throw new InvalidAccountDataException("One or both accounts are not active");
            }

            // Check sufficient funds
            if (fromAccount.getBalance().compareTo(amount) < 0) {
                throw new InvalidAccountDataException("Insufficient funds in source account");
            }

            // Perform the transfer
            fromAccount.setBalance(fromAccount.getBalance().subtract(amount));
            toAccount.setBalance(toAccount.getBalance().add(amount));

            // Update transaction dates
            LocalDateTime now = LocalDateTime.now();
            fromAccount.setLastTransactionDate(now);
            toAccount.setLastTransactionDate(now);

            // Save both accounts
            accountRepository.save(fromAccount);
            accountRepository.save(toAccount);
        }
        private String generateAccountNumber(){
        return String.valueOf((long) (Math.random()*1_000_000_0000L));
        }


}
