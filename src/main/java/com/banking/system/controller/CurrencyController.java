package com.banking.system.controller;

import com.banking.system.dto.CurrencyExchangeRequest;
import com.banking.system.model.Account;
import com.banking.system.model.User;
import com.banking.system.service.AccountService;
import com.banking.system.service.CurrencyService;
import com.banking.system.service.TransactionService;
import com.banking.system.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/currency")
@RequiredArgsConstructor
public class CurrencyController {

    private final UserService userService;
    private final CurrencyService currencyService;
    private final AccountService accountService;
    private final TransactionService transactionService;

    @GetMapping
    public String currencyPage(Authentication authentication, Model model) {
        User user = userService.findByUsername(authentication.getName());
        List<Account> accounts = accountService.getUserAccounts(user);
        String[] currencies = currencyService.getSupportedCurrencies();

        model.addAttribute("user", user);
        model.addAttribute("accounts", accounts);
        model.addAttribute("currencies", currencies);
        model.addAttribute("exchangeRequest", new CurrencyExchangeRequest());
        model.addAttribute("ratesFromUsd", currencyService.getRatesForCurrency("USD"));

        return "currency/index";
    }

    @GetMapping("/rates")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getRates(@RequestParam(defaultValue = "USD") String base) {
        Map<String, Object> response = new HashMap<>();
        response.put("base", base);
        response.put("rates", currencyService.getRatesForCurrency(base));
        return ResponseEntity.ok(response);
    }

    @GetMapping("/convert")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> convert(
            @RequestParam String from,
            @RequestParam String to,
            @RequestParam BigDecimal amount) {
        Map<String, Object> response = new HashMap<>();
        BigDecimal rate = currencyService.getExchangeRate(from, to);
        BigDecimal convertedAmount = currencyService.convert(amount, from, to);
        response.put("from", from);
        response.put("to", to);
        response.put("amount", amount);
        response.put("rate", rate);
        response.put("convertedAmount", convertedAmount);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/exchange")
    public String exchangeCurrency(Authentication authentication,
                                   @Valid @ModelAttribute("exchangeRequest") CurrencyExchangeRequest request,
                                   BindingResult bindingResult,
                                   RedirectAttributes redirectAttributes,
                                   Model model) {
        User user = userService.findByUsername(authentication.getName());

        if (bindingResult.hasErrors()) {
            List<Account> accounts = accountService.getUserAccounts(user);
            model.addAttribute("accounts", accounts);
            model.addAttribute("currencies", currencyService.getSupportedCurrencies());
            model.addAttribute("user", user);
            model.addAttribute("ratesFromUsd", currencyService.getRatesForCurrency("USD"));
            return "currency/index";
        }

        try {
            transactionService.exchangeCurrency(user, request);
            redirectAttributes.addFlashAttribute("success",
                    "Currency exchange completed! " + request.getAmount() + " "
                    + request.getFromCurrency() + " → " + request.getToCurrency());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Exchange failed: " + e.getMessage());
        }

        return "redirect:/currency";
    }
}
