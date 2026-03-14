package com.banking.system.service;

import com.banking.system.model.Account;
import com.banking.system.model.User;
import com.banking.system.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;

    public List<Account> getUserAccounts(User user) {
        return accountRepository.findByUserOrderByCreatedAtDesc(user);
    }

    public Account getAccountById(Long id, User user) {
        return accountRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new IllegalArgumentException("Account not found"));
    }

    public Account getAccountByNumber(String accountNumber) {
        return accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new IllegalArgumentException("Account not found: " + accountNumber));
    }

    @Transactional
    public Account createAccount(User user, Account.AccountType accountType, String currency) {
        Account account = new Account();
        account.setUser(user);
        account.setAccountType(accountType);
        account.setCurrency(currency.toUpperCase());
        account.setBalance(BigDecimal.ZERO);
        account.setAccountNumber(generateAccountNumber());
        account.setActive(true);
        return accountRepository.save(account);
    }

    @Transactional
    public Account deposit(Long accountId, User user, BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Deposit amount must be positive");
        }
        Account account = getAccountById(accountId, user);
        account.setBalance(account.getBalance().add(amount));
        return accountRepository.save(account);
    }

    @Transactional
    public Account withdraw(Long accountId, User user, BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Withdrawal amount must be positive");
        }
        Account account = getAccountById(accountId, user);
        if (account.getBalance().compareTo(amount) < 0) {
            throw new IllegalArgumentException("Insufficient funds");
        }
        account.setBalance(account.getBalance().subtract(amount));
        return accountRepository.save(account);
    }

    @Transactional
    public void debitAccount(Account account, BigDecimal amount) {
        if (account.getBalance().compareTo(amount) < 0) {
            throw new IllegalArgumentException("Insufficient funds in account: " + account.getAccountNumber());
        }
        account.setBalance(account.getBalance().subtract(amount));
        accountRepository.save(account);
    }

    @Transactional
    public void creditAccount(Account account, BigDecimal amount) {
        account.setBalance(account.getBalance().add(amount));
        accountRepository.save(account);
    }

    private String generateAccountNumber() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyMMddHHmmss"));
        String random = String.format("%04d", new Random().nextInt(10000));
        return "ACC" + timestamp + random;
    }
}
