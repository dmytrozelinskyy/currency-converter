package io.github.dmytrozelinskyy.currencyconverter.service;

import io.github.dmytrozelinskyy.currencyconverter.client.ExchangeRateClient;
import io.github.dmytrozelinskyy.currencyconverter.dto.FrankfurterResponse;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class CurrencyConversionService {
    private final ExchangeRateClient exchangeRateClient;

    public CurrencyConversionService(ExchangeRateClient exchangeRateClient) {
        this.exchangeRateClient = exchangeRateClient;
    }

    public BigDecimal convert(String from, String to, BigDecimal amount) {
        FrankfurterResponse frankfurterResponse = exchangeRateClient.getConversion(from, to, amount);
        BigDecimal rateResult = frankfurterResponse.rates().get(to);

        if (rateResult == null)
            throw new IllegalArgumentException("No rate was found for such currency: " + to);
        return rateResult;
    }
}
