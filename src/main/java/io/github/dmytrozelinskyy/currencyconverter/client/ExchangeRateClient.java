package io.github.dmytrozelinskyy.currencyconverter.client;

import io.github.dmytrozelinskyy.currencyconverter.dto.FrankfurterResponse;
import io.github.dmytrozelinskyy.currencyconverter.exception.ExchangeRateUnavailableException;
import io.github.dmytrozelinskyy.currencyconverter.exception.CurrencyNotSupportedException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;


import java.math.BigDecimal;

@Component
public class ExchangeRateClient {

    private final RestClient restClient;

    public ExchangeRateClient(RestClient restClient) {
        this.restClient = restClient;
    }

    public FrankfurterResponse getConversion(String from, String to, BigDecimal amount) {
        try {
            return restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/latest")
                            .queryParam("amount", amount)
                            .queryParam("from", from)
                            .queryParam("to", to)
                            .build())
                    .retrieve()
                    .onStatus(status -> status.value() == 404 || status.value() == 422,
                        (request, response) -> {
                            throw new CurrencyNotSupportedException("Unsupported currency code in '" + from + "' -> '" + to +"'.");
                        })
                    .onStatus(status -> status.isError(),
                        (request, response) -> {
                            throw new ExchangeRateUnavailableException("Exchange rate provider returned HTTP " + response.getStatusCode().value());
                        })
                    .body(FrankfurterResponse.class);
        } catch (ResourceAccessException e) {
            throw new ExchangeRateUnavailableException("Could not reach the exchange rate provider", e);
        }
    }
}
