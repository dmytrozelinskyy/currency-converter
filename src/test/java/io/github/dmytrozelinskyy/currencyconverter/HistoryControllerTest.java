package io.github.dmytrozelinskyy.currencyconverter;

import io.github.dmytrozelinskyy.currencyconverter.config.SecurityConfig;
import io.github.dmytrozelinskyy.currencyconverter.controller.HistoryController;
import io.github.dmytrozelinskyy.currencyconverter.dto.ConversionHistoryDto;
import io.github.dmytrozelinskyy.currencyconverter.service.ConversionHistoryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@WebMvcTest(HistoryController.class)
@Import(SecurityConfig.class)
public class HistoryControllerTest {
    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    ConversionHistoryService conversionHistoryService;

    @Test
    void returnsPageOfHistory() throws Exception{
        ConversionHistoryDto dto = new ConversionHistoryDto( "USD", "EUR", BigDecimal.TEN, new BigDecimal("9.2"), Instant.parse("2026-11-21T10:00:00Z"));
        when(conversionHistoryService.recent(any()))
                .thenReturn(new PageImpl<>(List.of(dto)));
        mockMvc.perform(get("/api/history"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].from").value("USD"));
    }

    @Test
    void emptyHistory_returnsEmptyContent() throws Exception {
        when(conversionHistoryService.recent(any()))
                .thenReturn(new PageImpl<>(List.of()));
        mockMvc.perform(get("/api/history"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isEmpty());
    }
}
