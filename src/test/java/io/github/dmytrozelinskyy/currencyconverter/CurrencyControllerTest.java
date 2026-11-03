package io.github.dmytrozelinskyy.currencyconverter;

import io.github.dmytrozelinskyy.currencyconverter.config.SecurityConfig;
import io.github.dmytrozelinskyy.currencyconverter.controller.CurrencyController;
import io.github.dmytrozelinskyy.currencyconverter.exception.CurrencyNotSupportedException;
import io.github.dmytrozelinskyy.currencyconverter.exception.ExchangeRateUnavailableException;
import io.github.dmytrozelinskyy.currencyconverter.service.CurrencyConversionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;


import java.math.BigDecimal;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CurrencyController.class)
@Import(SecurityConfig.class)
public class CurrencyControllerTest {
   @Autowired
   private MockMvc mockMvc;

   @MockitoBean
   private CurrencyConversionService conversionService;

   @Test
   void convert_validRequest_returnsConvertedAmount() throws Exception {
      when(conversionService.convert("USD", "EUR", new BigDecimal("100")))
              .thenReturn(new BigDecimal("92.35"));

      mockMvc.perform(get("/api/convert")
      .param("from", "USD").param("to", "EUR").param("amount", "100"))
              .andExpect(status().isOk())
              .andExpect(content().string("92.35"));
   }

   @Test
   void convert_lowerCaseCodes_areAccepted() throws Exception {
      when(conversionService.convert("usd", "eur", new BigDecimal("5")))
              .thenReturn(new BigDecimal("4.60"));

      mockMvc.perform(get("/api/convert")
                      .param("from", "usd").param("to", "eur").param("amount", "5"))
              .andExpect(status().isOk());
   }

   @Test
   void convert_invalidCurrencyCode_returns400WithFieldError() throws Exception {
      mockMvc.perform(get("/api/convert")
              .param("from", "US").param("to", "EUR").param("amount", "100"))
              .andExpect(status().isBadRequest())
              .andExpect(jsonPath("$.title").value("Invalid request"))
              .andExpect(jsonPath("$.status").value(400))
              .andExpect(jsonPath("$.errors.from").exists());

      verify(conversionService, never()).convert(any(), any(), any());
   }

   @Test
   void convert_nonLetterCurrencyCode_returns400() throws Exception {
      mockMvc.perform(get("/api/convert")
                      .param("from", "U5D").param("to", "EUR").param("amount", "100"))
              .andExpect(status().isBadRequest())
              .andExpect(jsonPath("$.errors.from").exists());
   }

   @Test
   void convert_zeroAmount_returns400() throws Exception {
      mockMvc.perform(get("/api/convert")
                      .param("from", "USD").param("to", "EUR").param("amount", "0"))
              .andExpect(status().isBadRequest())
              .andExpect(jsonPath("$.errors.amount").value("must be greater than 0"));
   }

   @Test
   void convert_negativeAmount_returns400() throws Exception {
      mockMvc.perform(get("/api/convert")
                      .param("from", "USD").param("to", "EUR").param("amount", "-5"))
              .andExpect(status().isBadRequest())
              .andExpect(jsonPath("$.errors.amount").exists());
   }

   @Test
   void convert_nonNumericAmount_returns400() throws Exception {
      mockMvc.perform(get("/api/convert")
                      .param("from", "USD").param("to", "EUR").param("amount", "abc"))
              .andExpect(status().isBadRequest())
              .andExpect(jsonPath("$.status").value(400));
   }

   @Test
   void convert_missingParameter_returns400() throws Exception {
      mockMvc.perform(get("/api/convert")
                      .param("from", "USD").param("amount", "100"))
              .andExpect(status().isBadRequest())
              .andExpect(jsonPath("$.status").value(400));
   }

   @Test
   void convert_unsupportedCurrency_returns400WithMessage() throws Exception {
      when(conversionService.convert(eq("USD"), eq("XYZ"), any()))
              .thenThrow(new CurrencyNotSupportedException("No rate was found for currency: XYZ"));
      mockMvc.perform(get("/api/convert")
                      .param("from", "USD").param("to", "XYZ").param("amount", "100"))
              .andExpect(status().isBadRequest())
              .andExpect(jsonPath("$.title").value("Currency not supported."))
              .andExpect(jsonPath("$.detail").value("No rate was found for currency: XYZ"));
   }

   @Test
   void convert_providerDown_returns503WithoutLeakingInternals() throws Exception {
      when(conversionService.convert(any(), any(), any()))
              .thenThrow(new ExchangeRateUnavailableException("connect timed out to api.frankfurter.dev"));
      mockMvc.perform(get("/api/convert")
                      .param("from", "USD").param("to", "EUR").param("amount", "100"))
              .andExpect(status().isServiceUnavailable())
              .andExpect(jsonPath("$.title").value("Exchange rate service unavailable."))
              .andExpect(jsonPath("$.detail").value(
                      "Exchange rates are temporarily unavailable. Please try again in a moment."));
   }

   @Test
   void convert_unexpectedError_returns500WithGenericMessage() throws Exception {
      when(conversionService.convert(any(), any(), any()))
              .thenThrow(new IllegalStateException("secret internal detail"));
      mockMvc.perform(get("/api/convert")
                      .param("from", "USD").param("to", "EUR").param("amount", "100"))
              .andExpect(status().isInternalServerError())
              .andExpect(jsonPath("$.detail").value("Something went wrong on our side."));
   }
}
