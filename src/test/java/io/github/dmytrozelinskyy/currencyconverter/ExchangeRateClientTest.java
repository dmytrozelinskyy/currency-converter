package io.github.dmytrozelinskyy.currencyconverter;

import io.github.dmytrozelinskyy.currencyconverter.client.ExchangeRateClient;
import io.github.dmytrozelinskyy.currencyconverter.dto.FrankfurterResponse;
import io.github.dmytrozelinskyy.currencyconverter.exception.CurrencyNotSupportedException;
import io.github.dmytrozelinskyy.currencyconverter.exception.ExchangeRateUnavailableException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

import java.io.IOException;
import java.math.BigDecimal;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

public class ExchangeRateClientTest {
   private static final String URL = "https://api.test/v1/latest?amount=100&from=USD&to=EUR";

   private MockRestServiceServer mockServer;
   private ExchangeRateClient exchangeRateClient;

   @BeforeEach
   void setUp() {
      RestClient.Builder builder = RestClient.builder().baseUrl("https://api.test/v1");
      mockServer = MockRestServiceServer.bindTo(builder).build();
      exchangeRateClient = new ExchangeRateClient(builder.build());
   }

   @Test
   void getConversion_parsesSuccessfulResponse() {
      mockServer.expect(requestTo(URL))
              .andExpect(method(HttpMethod.GET))
              .andRespond(withSuccess("""
                      {"amount":100.0,"base":"USD","data":"2026-11-02","rates":{"EUR":92.35}}
                      """, MediaType.APPLICATION_JSON));

      FrankfurterResponse response = exchangeRateClient.getConversion("USD", "EUR", new BigDecimal("100"));

      assertThat(response.base()).isEqualTo("USD");
      assertThat(response.rates().get("EUR")).isEqualByComparingTo("92.35");
      mockServer.verify();
   }

   @Test
   void getConversion_notFound_throwsCurrencyNotSupported() {
      mockServer.expect(requestTo(URL)).andRespond(withStatus(HttpStatus.NOT_FOUND));

      assertThatThrownBy(() -> exchangeRateClient.getConversion("USD", "EUR", new BigDecimal("100"))).isInstanceOf(CurrencyNotSupportedException.class);
   }

   @Test
   void getConversion_unprocessableEntity_throwsCurrencyNotSupported() {
      mockServer.expect(requestTo(URL)).andRespond(withStatus(HttpStatus.UNPROCESSABLE_ENTITY));

      assertThatThrownBy(() -> exchangeRateClient.getConversion("USD", "EUR", new BigDecimal("100"))).isInstanceOf(CurrencyNotSupportedException.class);
   }

   @Test
   void getConversion_serverError_throwsRateUnavailable() {
      mockServer.expect(requestTo(URL)).andRespond(withStatus(HttpStatus.INTERNAL_SERVER_ERROR));

      assertThatThrownBy(() -> exchangeRateClient.getConversion("USD", "EUR", new BigDecimal("100"))).isInstanceOf(ExchangeRateUnavailableException.class);
   }

   @Test
   void getConversion_tooManyRequests_throwsRateUnavailable() {
      mockServer.expect(requestTo(URL)).andRespond(withStatus(HttpStatus.TOO_MANY_REQUESTS));

      assertThatThrownBy(() -> exchangeRateClient.getConversion("USD", "EUR", new BigDecimal("100"))).isInstanceOf(ExchangeRateUnavailableException.class);
   }

   @Test
   void getConversion_networkFailure_throwsRateUnavailable() {
      mockServer.expect(requestTo(URL)).andRespond(withException(new IOException("connection refused")));

      assertThatThrownBy(() -> exchangeRateClient.getConversion("USD", "EUR", new BigDecimal("100")))
              .isInstanceOf(ExchangeRateUnavailableException.class)
              .hasCauseInstanceOf(ResourceAccessException.class);
   }
}
