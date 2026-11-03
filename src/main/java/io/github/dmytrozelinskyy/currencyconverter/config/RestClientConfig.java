package io.github.dmytrozelinskyy.currencyconverter.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.time.Duration;

@Configuration
public class RestClientConfig {

   @Bean
   public RestClient exchangeRestClient(
           @Value("${exchange.api.base-url}") String baseUrl,
           @Value("${exchange.api.timeout}") long timeoutMillis) {
      SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
      factory.setConnectTimeout(Duration.ofMillis(timeoutMillis));
      factory.setReadTimeout(Duration.ofMillis(timeoutMillis));

      return RestClient.builder()
              .baseUrl(baseUrl)
              .requestFactory(factory)
              .build();
   }
}
