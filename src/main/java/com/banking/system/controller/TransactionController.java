package com.banking.system.controller;

import com.banking.system.dto.TransferRequest;
import com.banking.system.model.Account;
import com.banking.system.model.Transaction;
import com.banking.system.model.User;
import com.banking.system.service.AccountService;
import com.banking.system.service.TransactionService;
import com.banking.system.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final UserService userService;
    private final TransactionService transactionService;
    private final AccountService accountService;

    @GetMapping
    public String listTransactions(Authentication authentication, Model model) {
        User user = userService.findByUsername(authentication.getName());
        List<Transaction> transactions = transactionService.getUserTransactions(user);
        List<Account> accounts = accountService.getUserAccounts(user);
        model.addAttribute("user", user);
        model.addAttribute("transactions", transactions);
        model.addAttribute("accounts", accounts);
        model.addAttribute("transferRequest", new TransferRequest());
        return "transaction/list";
    }

    @PostMapping("/transfer")
    public String transfer(Authentication authentication,
                           @Valid @ModelAttribute("transferRequest") TransferRequest request,
                           BindingResult bindingResult,
                           RedirectAttributes redirectAttributes,
                           Model model) {
        User user = userService.findByUsername(authentication.getName());

        if (bindingResult.hasErrors()) {
            List<Transaction> transactions = transactionService.getUserTransactions(user);
            List<Account> accounts = accountService.getUserAccounts(user);
            model.addAttribute("transactions", transactions);
            model.addAttribute("accounts", accounts);
            model.addAttribute("user", user);
            return "transaction/list";
        }

        try {
            transactionService.transfer(user, request);
            redirectAttributes.addFlashAttribute("success", "Transfer completed successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Transfer failed: " + e.getMessage());
        }
        return "redirect:/transactions";
    }
}
