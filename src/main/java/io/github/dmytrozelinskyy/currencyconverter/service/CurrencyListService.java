package io.github.dmytrozelinskyy.currencyconverter.service;

import io.github.dmytrozelinskyy.currencyconverter.client.ExchangeRateClient;
import io.github.dmytrozelinskyy.currencyconverter.dto.CurrencyDto;
import io.github.dmytrozelinskyy.currencyconverter.exception.ExchangeRateUnavailableException;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;

@Service
public class CurrencyListService {
    private static final Duration TTL = Duration.ofHours(24);

    private record Snapshot(List<CurrencyDto> items, Instant at ) { }
    private final ExchangeRateClient client;
    private volatile Snapshot cache;

    public CurrencyListService(ExchangeRateClient client)  {
        this.client = client;
    }

    public List<CurrencyDto> getCurrencies() {
        Snapshot current = cache;
        if (current != null && current.at().plus(TTL).isAfter(Instant.now()))
            return current.items();

        try {
            List<CurrencyDto> fresh = client.getCurrencies()
                    .entrySet().stream()
                    .map(e -> new CurrencyDto(e.getKey(), e.getValue()))
                    .sorted(Comparator.comparing(CurrencyDto::code))
                    .toList();
            cache = new Snapshot(fresh, Instant.now());
            return fresh;
        } catch (ExchangeRateUnavailableException ex) {
            if (current != null) return current.items();
            throw ex;
        }
    }
}
