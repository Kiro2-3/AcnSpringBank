package com.banking.system.config;

import com.banking.system.model.Account;
import com.banking.system.model.User;
import com.banking.system.repository.UserRepository;
import com.banking.system.service.AccountService;
import com.banking.system.service.CurrencyService;
import com.banking.system.service.TransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final AccountService accountService;
    private final TransactionService transactionService;
    private final CurrencyService currencyService;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        // Initialize exchange rates with fallback values at startup
        currencyService.initializeFallbackRates();

        if (userRepository.count() == 0) {
            log.info("Initializing demo data...");

            // Create demo user
            User demoUser = new User();
            demoUser.setUsername("demo");
            demoUser.setEmail("demo@banking.com");
            demoUser.setPassword(passwordEncoder.encode("demo123"));
            demoUser.setFullName("Demo User");
            demoUser.setPhoneNumber("+1-555-0100");
            demoUser.setRole(User.Role.USER);
            userRepository.save(demoUser);

            // Create admin user
            User adminUser = new User();
            adminUser.setUsername("admin");
            adminUser.setEmail("admin@banking.com");
            adminUser.setPassword(passwordEncoder.encode("admin123"));
            adminUser.setFullName("System Administrator");
            adminUser.setPhoneNumber("+1-555-0199");
            adminUser.setRole(User.Role.ADMIN);
            userRepository.save(adminUser);

            // Create accounts for demo user
            Account usdAccount = accountService.createAccount(demoUser, Account.AccountType.CHECKING, "USD");
            Account eurAccount = accountService.createAccount(demoUser, Account.AccountType.SAVINGS, "EUR");
            Account phpAccount = accountService.createAccount(demoUser, Account.AccountType.SAVINGS, "PHP");

            // Add demo balances
            transactionService.deposit(demoUser, usdAccount.getId(), new BigDecimal("5000.00"), "Initial deposit");
            transactionService.deposit(demoUser, eurAccount.getId(), new BigDecimal("3500.00"), "Initial deposit");
            transactionService.deposit(demoUser, phpAccount.getId(), new BigDecimal("150000.00"), "Initial deposit");

            log.info("Demo data initialized successfully.");
            log.info("Demo login: username=demo, password=demo123");
            log.info("Admin login: username=admin, password=admin123");
        }
    }
}
