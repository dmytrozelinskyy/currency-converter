package io.github.dmytrozelinskyy.currencyconverter.client;

import io.github.dmytrozelinskyy.currencyconverter.dto.FrankfurterResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;

@Component
public class ExchangeRateClient {

    private final RestClient restClient;

    public ExchangeRateClient(@Value("${exchange.api.base-url }") String baseUrl) {
        this.restClient = RestClient.builder().baseUrl(baseUrl).build();
    }

    public FrankfurterResponse getConversion(String from, String to, BigDecimal amount) {
        return restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/latest")
                        .queryParam("amount", amount)
                        .queryParam("from", from)
                        .queryParam("to", to)
                        .build())
                .retrieve()
                .body(FrankfurterResponse.class);
    }
}
