package com.banking.system.service;

import com.banking.system.dto.CurrencyExchangeRequest;
import com.banking.system.dto.TransferRequest;
import com.banking.system.model.Account;
import com.banking.system.model.Transaction;
import com.banking.system.model.User;
import com.banking.system.repository.AccountRepository;
import com.banking.system.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Random;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final AccountService accountService;
    private final CurrencyService currencyService;

    public List<Transaction> getUserTransactions(User user) {
        return transactionRepository.findByUserOrderByCreatedAtDesc(user);
    }

    public List<Transaction> getRecentTransactions(User user, int limit) {
        return transactionRepository.findRecentByUser(user, PageRequest.of(0, limit));
    }

    public List<Transaction> getAccountTransactions(User user, Account account) {
        return transactionRepository.findByUserAndAccount(user, account);
    }

    @Transactional
    public Transaction deposit(User user, Long accountId, BigDecimal amount, String description) {
        Account account = accountService.getAccountById(accountId, user);

        accountService.creditAccount(account, amount);

        Transaction transaction = new Transaction();
        transaction.setUser(user);
        transaction.setTransactionType(Transaction.TransactionType.DEPOSIT);
        transaction.setAmount(amount);
        transaction.setSourceCurrency(account.getCurrency());
        transaction.setDestinationCurrency(account.getCurrency());
        transaction.setExchangeRate(BigDecimal.ONE);
        transaction.setConvertedAmount(amount);
        transaction.setDescription(description != null ? description : "Deposit");
        transaction.setDestinationAccount(account);
        transaction.setStatus(Transaction.TransactionStatus.COMPLETED);
        transaction.setReferenceNumber(generateReferenceNumber());
        transaction.setCompletedAt(LocalDateTime.now());

        return transactionRepository.save(transaction);
    }

    @Transactional
    public Transaction withdraw(User user, Long accountId, BigDecimal amount, String description) {
        Account account = accountService.getAccountById(accountId, user);

        accountService.debitAccount(account, amount);

        Transaction transaction = new Transaction();
        transaction.setUser(user);
        transaction.setTransactionType(Transaction.TransactionType.WITHDRAWAL);
        transaction.setAmount(amount);
        transaction.setSourceCurrency(account.getCurrency());
        transaction.setDestinationCurrency(account.getCurrency());
        transaction.setExchangeRate(BigDecimal.ONE);
        transaction.setConvertedAmount(amount);
        transaction.setDescription(description != null ? description : "Withdrawal");
        transaction.setSourceAccount(account);
        transaction.setStatus(Transaction.TransactionStatus.COMPLETED);
        transaction.setReferenceNumber(generateReferenceNumber());
        transaction.setCompletedAt(LocalDateTime.now());

        return transactionRepository.save(transaction);
    }

    @Transactional
    public Transaction transfer(User user, TransferRequest request) {
        Account sourceAccount = accountService.getAccountById(request.getSourceAccountId(), user);
        Account destinationAccount = accountService.getAccountByNumber(request.getDestinationAccountNumber());

        if (sourceAccount.getAccountNumber().equals(destinationAccount.getAccountNumber())) {
            throw new IllegalArgumentException("Cannot transfer to the same account");
        }

        BigDecimal amount = request.getAmount();
        accountService.debitAccount(sourceAccount, amount);

        BigDecimal convertedAmount = amount;
        BigDecimal exchangeRate = BigDecimal.ONE;

        if (!sourceAccount.getCurrency().equals(destinationAccount.getCurrency())) {
            exchangeRate = currencyService.getExchangeRate(
                    sourceAccount.getCurrency(), destinationAccount.getCurrency());
            convertedAmount = currencyService.convert(amount,
                    sourceAccount.getCurrency(), destinationAccount.getCurrency());
        }

        accountService.creditAccount(destinationAccount, convertedAmount);

        Transaction transaction = new Transaction();
        transaction.setUser(user);
        transaction.setTransactionType(Transaction.TransactionType.TRANSFER);
        transaction.setAmount(amount);
        transaction.setSourceCurrency(sourceAccount.getCurrency());
        transaction.setDestinationCurrency(destinationAccount.getCurrency());
        transaction.setExchangeRate(exchangeRate);
        transaction.setConvertedAmount(convertedAmount);
        transaction.setDescription(request.getDescription() != null ? request.getDescription() : "Transfer");
        transaction.setSourceAccount(sourceAccount);
        transaction.setDestinationAccount(destinationAccount);
        transaction.setStatus(Transaction.TransactionStatus.COMPLETED);
        transaction.setReferenceNumber(generateReferenceNumber());
        transaction.setCompletedAt(LocalDateTime.now());

        return transactionRepository.save(transaction);
    }

    @Transactional
    public Transaction exchangeCurrency(User user, CurrencyExchangeRequest request) {
        Account sourceAccount = accountService.getAccountById(request.getSourceAccountId(), user);
        Account destinationAccount = accountService.getAccountById(request.getDestinationAccountId(), user);

        BigDecimal amount = request.getAmount();
        BigDecimal exchangeRate = currencyService.getExchangeRate(
                request.getFromCurrency(), request.getToCurrency());
        BigDecimal convertedAmount = currencyService.convert(amount,
                request.getFromCurrency(), request.getToCurrency());

        accountService.debitAccount(sourceAccount, amount);
        accountService.creditAccount(destinationAccount, convertedAmount);

        Transaction transaction = new Transaction();
        transaction.setUser(user);
        transaction.setTransactionType(Transaction.TransactionType.CURRENCY_EXCHANGE);
        transaction.setAmount(amount);
        transaction.setSourceCurrency(request.getFromCurrency());
        transaction.setDestinationCurrency(request.getToCurrency());
        transaction.setExchangeRate(exchangeRate);
        transaction.setConvertedAmount(convertedAmount);
        transaction.setDescription("Currency exchange: " + request.getFromCurrency() + " → " + request.getToCurrency());
        transaction.setSourceAccount(sourceAccount);
        transaction.setDestinationAccount(destinationAccount);
        transaction.setStatus(Transaction.TransactionStatus.COMPLETED);
        transaction.setReferenceNumber(generateReferenceNumber());
        transaction.setCompletedAt(LocalDateTime.now());

        return transactionRepository.save(transaction);
    }

    private String generateReferenceNumber() {
        return "TXN" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + String.format("%04d", new Random().nextInt(10000));
    }
}
