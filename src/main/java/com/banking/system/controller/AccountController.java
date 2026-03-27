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
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.List;

@Controller
@RequestMapping("/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final UserService userService;
    private final AccountService accountService;
    private final TransactionService transactionService;
    private final CurrencyService currencyService;

    @GetMapping
    public String listAccounts(Authentication authentication, Model model) {
        User user = userService.findByUsername(authentication.getName());
        List<Account> accounts = accountService.getUserAccounts(user);
        model.addAttribute("user", user);
        model.addAttribute("accounts", accounts);
        model.addAttribute("supportedCurrencies", currencyService.getSupportedCurrencies());
        model.addAttribute("accountTypes", Account.AccountType.values());
        return "account/list";
    }

    @PostMapping("/create")
    public String createAccount(Authentication authentication,
                                @RequestParam String accountType,
                                @RequestParam String currency,
                                RedirectAttributes redirectAttributes) {
        User user = userService.findByUsername(authentication.getName());
        try {
            Account.AccountType type = Account.AccountType.valueOf(accountType.toUpperCase());
            accountService.createAccount(user, type, currency);
            redirectAttributes.addFlashAttribute("success", "Account created successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to create account: " + e.getMessage());
        }
        return "redirect:/accounts";
    }

    @GetMapping("/{id}")
    public String accountDetails(@PathVariable Long id,
                                 Authentication authentication,
                                 Model model) {
        User user = userService.findByUsername(authentication.getName());
        Account account = accountService.getAccountById(id, user);
        List<Transaction> transactions = transactionService.getAccountTransactions(user, account);
        model.addAttribute("user", user);
        model.addAttribute("account", account);
        model.addAttribute("transactions", transactions);
        return "account/details";
    }

    @PostMapping("/{id}/deposit")
    public String deposit(@PathVariable Long id,
                          Authentication authentication,
                          @RequestParam BigDecimal amount,
                          @RequestParam(required = false) String description,
                          RedirectAttributes redirectAttributes) {
        User user = userService.findByUsername(authentication.getName());
        try {
            transactionService.deposit(user, id, amount, description);
            redirectAttributes.addFlashAttribute("success", "Deposit successful!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Deposit failed: " + e.getMessage());
        }
        return "redirect:/accounts/" + id;
    }

    @PostMapping("/{id}/withdraw")
    public String withdraw(@PathVariable Long id,
                           Authentication authentication,
                           @RequestParam BigDecimal amount,
                           @RequestParam(required = false) String description,
                           RedirectAttributes redirectAttributes) {
        User user = userService.findByUsername(authentication.getName());
        try {
            transactionService.withdraw(user, id, amount, description);
            redirectAttributes.addFlashAttribute("success", "Withdrawal successful!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Withdrawal failed: " + e.getMessage());
        }
        return "redirect:/accounts/" + id;
    }

    @GetMapping("/settings")
    public String accountSettings(Authentication authentication, Model model) {
        User user = userService.findByUsername(authentication.getName());
        model.addAttribute("user", user);
        model.addAttribute("darkTheme", user.isDarkTheme());
        return "account/settings";
    }

    @PostMapping("/settings")
    public String updateAccountSettings(Authentication authentication,
                                       @RequestParam String email,
                                       @RequestParam(required = false) boolean darkTheme,
                                       RedirectAttributes redirectAttributes) {
        User user = userService.findByUsername(authentication.getName());
        user.setEmail(email);
        user.setDarkTheme(darkTheme);
        userService.save(user);
        redirectAttributes.addFlashAttribute("success", "Settings updated successfully!");
        return "redirect:/accounts/settings";
    }
}
