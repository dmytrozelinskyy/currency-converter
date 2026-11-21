package io.github.dmytrozelinskyy.currencyconverter;

import io.github.dmytrozelinskyy.currencyconverter.config.SecurityConfig;
import io.github.dmytrozelinskyy.currencyconverter.controller.CurrencyListController;
import io.github.dmytrozelinskyy.currencyconverter.dto.CurrencyDto;
import io.github.dmytrozelinskyy.currencyconverter.exception.ExchangeRateUnavailableException;
import io.github.dmytrozelinskyy.currencyconverter.service.CurrencyListService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@WebMvcTest(CurrencyListController.class)
@Import(SecurityConfig.class)
public class CurrencyListControllerTest {

    @Autowired
    MockMvc mockMvc;
    @MockitoBean
    CurrencyListService currencyListService;

    @Test
    void currencies_returnsList() throws Exception
    {
        when(currencyListService.getCurrencies()).thenReturn(List.of(
                new CurrencyDto("EUR", "Euro"),
                new CurrencyDto("USD", "United States Dollar")));

        mockMvc.perform(get("/api/currencies"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].code").value("EUR"))
                .andExpect(jsonPath("$[0].name").value("Euro"));
    }

    @Test
    void currencies_providerDown_returns503() throws Exception {
        when(currencyListService.getCurrencies()).thenThrow(
                new ExchangeRateUnavailableException("down"));

        mockMvc.perform(get("/api/currencies"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.title")
                        .value("Exchange rate service unavailable."));
    }
}
