package io.github.dmytrozelinskyy.currencyconverter;

import io.github.dmytrozelinskyy.currencyconverter.client.ExchangeRateClient;
import io.github.dmytrozelinskyy.currencyconverter.dto.CurrencyDto;
import io.github.dmytrozelinskyy.currencyconverter.exception.ExchangeRateUnavailableException;
import io.github.dmytrozelinskyy.currencyconverter.service.CurrencyListService;
import org.apache.commons.lang3.mutable.Mutable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.*;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CurrencyListServiceTest {
    static class MutableClock extends Clock {
        private Instant now = Instant.parse("2026-11-18T11:37:00Z");

        void advance(Duration d) { now = now.plus(d); }

        @Override public Instant instant() { return now; }
        @Override public ZoneId getZone() { return ZoneOffset.UTC; }
        @Override public Clock withZone(ZoneId zone) { return this; }
    }

    @Mock
    ExchangeRateClient exchangeRateClient;
    MutableClock clock = new MutableClock();
    CurrencyListService currencyListService;

    @BeforeEach
    void setUp() {
        currencyListService = new CurrencyListService(exchangeRateClient, clock);
    }

    @Test
    void getCurrencies_mapsAndSortsByCode() {
        when(exchangeRateClient.getCurrencies()).thenReturn(Map.of(
                "USD", "United States Dollar",
                "AUD", "Australian Dollar",
                "EUR", "Euro"
        ));

        List<CurrencyDto> result = currencyListService.getCurrencies();

        assertThat(result).
                extracting(CurrencyDto::code)
                .containsExactly("AUD", "EUR", "USD");
        assertThat(result.getFirst().name())
                .isEqualTo("Australian Dollar");
    }

    @Test
    void getCurrencies_secondCallUsesCache() {
        when(exchangeRateClient.getCurrencies())
                .thenReturn(Map.of("EUR", "Euro"));
        currencyListService.getCurrencies();
        currencyListService.getCurrencies();

        verify(exchangeRateClient, times(1)).getCurrencies();
    }

    @Test
    void getCurrencies_noCacheAndProviderDown_throws() {
        when(exchangeRateClient.getCurrencies()).thenThrow(
                new ExchangeRateUnavailableException("down"));

        assertThatThrownBy(() -> currencyListService.getCurrencies())
                .isInstanceOf(ExchangeRateUnavailableException.class);
    }

    @Test
    void getCurrencies_refetchesAfterTtl() {
        when(exchangeRateClient.getCurrencies())
                .thenReturn(Map.of("EUR", "Euro"));

        currencyListService.getCurrencies();
        clock.advance(Duration.ofHours(25));
        currencyListService.getCurrencies();

        verify(exchangeRateClient, times(2)).getCurrencies();
    }

    @Test
    void getCurrencies_expiredCacheAndProviderDown_returnStaleList() {
        when(exchangeRateClient.getCurrencies())
                .thenReturn(Map.of("EUR", "Euro"))
                .thenThrow(new ExchangeRateUnavailableException("down"));

        currencyListService.getCurrencies();
        clock.advance(Duration.ofHours(25));

        assertThat(currencyListService.getCurrencies())
                .extracting(CurrencyDto::code)
                .containsExactly("EUR");
    }
}
