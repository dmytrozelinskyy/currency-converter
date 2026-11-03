package io.github.dmytrozelinskyy.currencyconverter;

import io.github.dmytrozelinskyy.currencyconverter.client.ExchangeRateClient;
import io.github.dmytrozelinskyy.currencyconverter.dto.FrankfurterResponse;
import io.github.dmytrozelinskyy.currencyconverter.exception.CurrencyNotSupportedException;
import io.github.dmytrozelinskyy.currencyconverter.exception.ExchangeRateUnavailableException;
import io.github.dmytrozelinskyy.currencyconverter.service.CurrencyConversionService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CurrencyConversionServiceTest {
   @Mock
   private ExchangeRateClient exchangeRateClient;

   @InjectMocks
   private CurrencyConversionService currencyConversionService;

   @Test
   void convert_returnsRateFromProvider() {
      BigDecimal amount = new BigDecimal("100");
      when(exchangeRateClient.getConversion("USD", "EUR", amount))
              .thenReturn(response("USD", "EUR", "92.35"));

      BigDecimal result = currencyConversionService.convert("USD", "EUR", amount);

      assertThat(result).isEqualByComparingTo("92.35");
   }

   @Test
   void convert_normalizesCurrencyCodesToUpperCase() {
      BigDecimal amount = new BigDecimal("10");
      when(exchangeRateClient.getConversion("USD", "PLN", amount))
              .thenReturn(response("USD", "PLN", "39.10"));

      BigDecimal result = currencyConversionService.convert("usd", "pln", amount);

      assertThat(result).isEqualByComparingTo("39.10");
      verify(exchangeRateClient).getConversion("USD", "PLN", amount);
   }

   @Test
   void convert_sameCurrency_returnsAmountWithoutCallingProvider() {
      BigDecimal amount = new BigDecimal("20.50");

      BigDecimal result = currencyConversionService.convert("eur", "EUR", amount);

      assertThat(result).isEqualByComparingTo("20.50");
      verifyNoInteractions(exchangeRateClient);
   }

   @Test
   void convert_rateMissingForTarget_throwsCurrencyNotSupported() {
      BigDecimal amount = new BigDecimal("100");
      when(exchangeRateClient.getConversion("USD", "XYZ", amount)).thenReturn(response("USD", "EUR", "92.35"));

      assertThatThrownBy(() -> currencyConversionService.convert("USD", "XYZ", amount))
              .isInstanceOf(CurrencyNotSupportedException.class)
              .hasMessageContaining("XYZ");
   }

   @Test
   void convert_emptyProviderResponse_throwsRateUnavailable() {
      BigDecimal amount = new BigDecimal("100");
      when(exchangeRateClient.getConversion("USD", "EUR", amount)).thenReturn(null);

      assertThatThrownBy(() -> currencyConversionService.convert("USD", "EUR", amount))
              .isInstanceOf(ExchangeRateUnavailableException.class);
   }

   @Test
   void convert_providerFailure_isPropagated() {
      BigDecimal amount = new BigDecimal("100");
      when(exchangeRateClient.getConversion("USD", "EUR", amount))
              .thenThrow(new ExchangeRateUnavailableException("down"));

      assertThatThrownBy(() -> currencyConversionService.convert("USD", "EUR", amount))
              .isInstanceOf(ExchangeRateUnavailableException.class);
   }

   private static FrankfurterResponse response(String base, String target, String value) {
      return new FrankfurterResponse(new BigDecimal("100"), base, LocalDate.of(2026, 11, 3),
              Map.of(target, new BigDecimal(value)));
   }
}
