package com.banking.accountservice.repository;

import com.banking.accountservice.model.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AccountRepository extends JpaRepository<Account,UUID> {
    List<Account> findByUserId(UUID userId);

//Already provided in JpaRepository Library
//    Optional<Account> findAccountByAccountId(UUID accountId);
    Optional<Account> findByAccountNumber(String accountNumber);
}
