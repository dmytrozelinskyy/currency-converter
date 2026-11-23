package io.github.dmytrozelinskyy.currencyconverter.service;

import io.github.dmytrozelinskyy.currencyconverter.client.ExchangeRateClient;
import io.github.dmytrozelinskyy.currencyconverter.dto.FrankfurterResponse;
import io.github.dmytrozelinskyy.currencyconverter.exception.CurrencyNotSupportedException;
import io.github.dmytrozelinskyy.currencyconverter.exception.ExchangeRateUnavailableException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Locale;


@Service
public class CurrencyConversionService {
    private final ExchangeRateClient exchangeRateClient;
    private final ConversionHistoryService conversionHistoryService;

    public CurrencyConversionService(ExchangeRateClient exchangeRateClient, ConversionHistoryService conversionHistoryService) {
        this.exchangeRateClient = exchangeRateClient;
        this.conversionHistoryService = conversionHistoryService;
    }

    public BigDecimal convert(String from, String to, BigDecimal amount) {
        String source = from.toUpperCase(Locale.ROOT);
        String target = to.toUpperCase(Locale.ROOT);

        if (source.equals(target))
            return amount;

        FrankfurterResponse response = exchangeRateClient.getConversion(source, target, amount);

        if (response == null || response.rates() == null)
            throw new ExchangeRateUnavailableException("Exchange rate provider returned an empty response");

        BigDecimal converted = response.rates().get(target);

        if (converted == null)
            throw new CurrencyNotSupportedException("No rate was found for currency: " + target);

        conversionHistoryService.record(source, target, amount, converted);
        return converted;
    }
}
