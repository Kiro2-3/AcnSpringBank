package com.banking.system.service;

import com.banking.system.model.ExchangeRate;
import com.banking.system.repository.ExchangeRateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class CurrencyService {

    private final ExchangeRateRepository exchangeRateRepository;
    private final RestTemplate restTemplate;

    private static final String[] SUPPORTED_CURRENCIES = {
        "USD", "EUR", "GBP", "JPY", "CNY", "AUD", "CAD", "CHF",
        "HKD", "SGD", "PHP", "KRW", "INR", "BRL", "MXN"
    };

    private static final String FREE_API_URL =
        "https://open.er-api.com/v6/latest/USD";

    public String[] getSupportedCurrencies() {
        return SUPPORTED_CURRENCIES;
    }

    public BigDecimal getExchangeRate(String fromCurrency, String toCurrency) {
        if (fromCurrency.equalsIgnoreCase(toCurrency)) {
            return BigDecimal.ONE;
        }

        Optional<ExchangeRate> rateOpt = exchangeRateRepository
                .findByFromCurrencyAndToCurrency(fromCurrency.toUpperCase(), toCurrency.toUpperCase());

        if (rateOpt.isPresent()) {
            return rateOpt.get().getRate();
        }

        // Fallback: try to compute via USD as base
        Optional<ExchangeRate> fromUsd = exchangeRateRepository
                .findByFromCurrencyAndToCurrency("USD", fromCurrency.toUpperCase());
        Optional<ExchangeRate> toUsd = exchangeRateRepository
                .findByFromCurrencyAndToCurrency("USD", toCurrency.toUpperCase());

        if (fromUsd.isPresent() && toUsd.isPresent()) {
            BigDecimal rateFromUsd = fromUsd.get().getRate();
            BigDecimal rateToUsd = toUsd.get().getRate();
            if (rateFromUsd.compareTo(BigDecimal.ZERO) != 0) {
                return rateToUsd.divide(rateFromUsd, 6, RoundingMode.HALF_UP);
            }
        }

        // If no rates available in DB, return fallback static rates
        return getFallbackRate(fromCurrency, toCurrency);
    }

    public BigDecimal convert(BigDecimal amount, String fromCurrency, String toCurrency) {
        BigDecimal rate = getExchangeRate(fromCurrency, toCurrency);
        return amount.multiply(rate).setScale(4, RoundingMode.HALF_UP);
    }

    public Map<String, BigDecimal> getRatesForCurrency(String baseCurrency) {
        Map<String, BigDecimal> rates = new LinkedHashMap<>();
        for (String currency : SUPPORTED_CURRENCIES) {
            if (!currency.equals(baseCurrency)) {
                rates.put(currency, getExchangeRate(baseCurrency, currency));
            }
        }
        return rates;
    }

    @Scheduled(fixedDelay = 3600000) // Refresh every hour
    @Transactional
    public void refreshExchangeRates() {
        try {
            log.info("Refreshing exchange rates from external API...");
            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.getForObject(FREE_API_URL, Map.class);

            if (response != null && "success".equals(response.get("result"))) {
                @SuppressWarnings("unchecked")
                Map<String, Object> rawRates = (Map<String, Object>) response.get("rates");

                if (rawRates != null) {
                    for (String currency : SUPPORTED_CURRENCIES) {
                        Object rateObj = rawRates.get(currency);
                        if (rateObj != null) {
                            BigDecimal rate = new BigDecimal(rateObj.toString());
                            saveOrUpdateRate("USD", currency, rate);
                        }
                    }
                    log.info("Exchange rates refreshed successfully.");
                }
            }
        } catch (Exception e) {
            log.warn("Could not refresh exchange rates from external API: {}. Rates unchanged.", e.getMessage());
        }
    }

    @Transactional
    public void initializeFallbackRates() {
        Map<String, BigDecimal> usdRates = getStaticUsdRates();
        for (Map.Entry<String, BigDecimal> entry : usdRates.entrySet()) {
            saveOrUpdateRate("USD", entry.getKey(), entry.getValue());
        }
        log.info("Fallback exchange rates initialized.");
    }

    private void saveOrUpdateRate(String from, String to, BigDecimal rate) {
        ExchangeRate exchangeRate = exchangeRateRepository
                .findByFromCurrencyAndToCurrency(from, to)
                .orElse(new ExchangeRate());
        exchangeRate.setFromCurrency(from);
        exchangeRate.setToCurrency(to);
        exchangeRate.setRate(rate);
        exchangeRate.setUpdatedAt(LocalDateTime.now());
        exchangeRateRepository.save(exchangeRate);
    }

    private BigDecimal getFallbackRate(String fromCurrency, String toCurrency) {
        Map<String, BigDecimal> usdRates = getStaticUsdRates();
        BigDecimal fromRate = usdRates.getOrDefault(fromCurrency.toUpperCase(), BigDecimal.ONE);
        BigDecimal toRate = usdRates.getOrDefault(toCurrency.toUpperCase(), BigDecimal.ONE);

        if ("USD".equals(fromCurrency.toUpperCase())) {
            return toRate;
        } else if ("USD".equals(toCurrency.toUpperCase())) {
            return BigDecimal.ONE.divide(fromRate, 6, RoundingMode.HALF_UP);
        } else {
            return toRate.divide(fromRate, 6, RoundingMode.HALF_UP);
        }
    }

    private Map<String, BigDecimal> getStaticUsdRates() {
        Map<String, BigDecimal> rates = new LinkedHashMap<>();
        rates.put("USD", BigDecimal.ONE);
        rates.put("EUR", new BigDecimal("0.92"));
        rates.put("GBP", new BigDecimal("0.79"));
        rates.put("JPY", new BigDecimal("149.50"));
        rates.put("CNY", new BigDecimal("7.24"));
        rates.put("AUD", new BigDecimal("1.53"));
        rates.put("CAD", new BigDecimal("1.36"));
        rates.put("CHF", new BigDecimal("0.90"));
        rates.put("HKD", new BigDecimal("7.82"));
        rates.put("SGD", new BigDecimal("1.34"));
        rates.put("PHP", new BigDecimal("55.80"));
        rates.put("KRW", new BigDecimal("1325.00"));
        rates.put("INR", new BigDecimal("83.12"));
        rates.put("BRL", new BigDecimal("4.97"));
        rates.put("MXN", new BigDecimal("17.15"));
        return rates;
    }
}
