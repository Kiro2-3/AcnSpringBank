package com.banking.system.controller;

import com.banking.system.model.Account;
import com.banking.system.model.Transaction;
import com.banking.system.model.User;
import com.banking.system.service.AccountService;
import com.banking.system.service.CurrencyService;
import com.banking.system.service.TransactionService;
import com.banking.system.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final UserService userService;
    private final AccountService accountService;
    private final TransactionService transactionService;
    private final CurrencyService currencyService;

    @GetMapping
    public String dashboard(Authentication authentication, Model model) {
        User user = userService.findByUsername(authentication.getName());
        List<Account> accounts = accountService.getUserAccounts(user);
        List<Transaction> recentTransactions = transactionService.getRecentTransactions(user, 5);

        // Calculate total balance in USD
        BigDecimal totalBalanceUsd = accounts.stream()
                .filter(Account::isActive)
                .map(acc -> {
                    if ("USD".equals(acc.getCurrency())) {
                        return acc.getBalance();
                    }
                    return currencyService.convert(acc.getBalance(), acc.getCurrency(), "USD");
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);

        model.addAttribute("user", user);
        model.addAttribute("accounts", accounts);
        model.addAttribute("recentTransactions", recentTransactions);
        model.addAttribute("totalBalanceUsd", totalBalanceUsd);
        model.addAttribute("accountCount", accounts.size());
        model.addAttribute("supportedCurrencies", currencyService.getSupportedCurrencies());

        return "dashboard/index";
    }
}
