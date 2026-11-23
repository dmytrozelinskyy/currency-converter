package io.github.dmytrozelinskyy.currencyconverter;

import io.github.dmytrozelinskyy.currencyconverter.client.ExchangeRateClient;
import io.github.dmytrozelinskyy.currencyconverter.dto.FrankfurterResponse;
import io.github.dmytrozelinskyy.currencyconverter.exception.ExchangeRateUnavailableException;
import io.github.dmytrozelinskyy.currencyconverter.model.ConversionHistory;
import io.github.dmytrozelinskyy.currencyconverter.repositories.ConversionHistoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
public class ApplicationIntegrationTest {

    @Autowired
    MockMvc MockMvc;
    @Autowired
    ConversionHistoryRepository conversionHistoryRepository;

    @MockitoBean
    ExchangeRateClient exchangeRateClient;
    @Autowired
    private MockMvc mockMvc;

    @BeforeEach
    void cleanHistory() {
        conversionHistoryRepository.deleteAll();
    }

    @Test
    void successfulConversion_isReturnedAndStoredInHistory() throws Exception {
        when(exchangeRateClient.getConversion(
                eq("USD"), eq("EUR"), any()))
                .thenReturn(response("USD", "EUR", "92.35"));

        mockMvc.perform(get("/api/convert")
                .param("from", "usd")
                .param("to", "eur")
                .param("amount", "100"))
                .andExpect(status().isOk())
                .andExpect(content().string("92.35"));

        mockMvc.perform(get("/api/history"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].from").value("USD"))
                .andExpect(jsonPath("$.content[0].to").value("EUR"));
    }

    @Test
    void providerDown_returns503_andStoresNothing() throws Exception {
        when(exchangeRateClient.getConversion(any(), any(), any()))
                .thenThrow(new ExchangeRateUnavailableException("down"));

        mockMvc.perform(get("/api/convert")
                        .param("from", "USD")
                        .param("to", "EUR")
                        .param("amount", "100"))
                .andExpect(status().isServiceUnavailable());

        assertThat(conversionHistoryRepository.count()).isZero();
    }

    @Test
    void sameCurrency_storesNothing() throws Exception {
        mockMvc.perform(get("/api/convert")
                        .param("from", "EUR")
                        .param("to", "EUR")
                        .param("amount", "5"))
                .andExpect(status().isOk());

        assertThat(conversionHistoryRepository.count()).isZero();
    }

    @Test
    void invalidInput_returns400_andStoresNothing() throws Exception {
        mockMvc.perform(get("/api/convert")
                        .param("from", "US")
                        .param("to", "EUR")
                        .param("amount", "-1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.from").exists())
                .andExpect(jsonPath("$.errors.amount").exists());

        assertThat(conversionHistoryRepository.count()).isZero();
    }

    @Test
    void history_isNewestFirst_andPageSizeIsCapped() throws Exception {
        conversionHistoryRepository.save(new ConversionHistory(
                "USD", "EUR", BigDecimal.ONE, BigDecimal.ONE
        ));
        Thread.sleep(5);
        conversionHistoryRepository.save(new ConversionHistory(
                "GBP", "PLN", BigDecimal.ONE, BigDecimal.ONE
        ));

        mockMvc.perform(get("/api/history").param("size", "1000"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].from").value("GBP"))
                .andExpect(jsonPath("$.page.size").value(100));
    }

    @Test
    void currencies_andReturnedSortedByCode() throws Exception {
        when(exchangeRateClient.getCurrencies()).thenReturn(
                Map.of("USD", "United States Dollar",
                        "AUD", "Australian Dollar")
        );

        mockMvc.perform(get("/api/currencies"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].code").value("AUD"))
                .andExpect(jsonPath("$[1].code").value("USD"));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "/", "/index.html", "/history.html", "/currencies.html", "/v3/api-docs", "/api/history"
    })
    void publicPaths_needNoLogin(String path) throws Exception {
        mockMvc.perform(get(path))
                .andExpect(status().isOk());
    }

    @Test
    void otherPaths_requireLogin() throws Exception {
        mockMvc.perform(get("/admin"))
                .andExpect(status().isUnauthorized());
    }

    private static FrankfurterResponse response(
            String base, String target, String value
    ) {
        return new FrankfurterResponse(
                new BigDecimal("100"), base, LocalDate.of(2026, 11, 23), Map.of(target, new BigDecimal(value))
        );
    }
}
