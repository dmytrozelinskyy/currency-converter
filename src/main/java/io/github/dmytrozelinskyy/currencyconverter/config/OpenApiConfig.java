package io.github.dmytrozelinskyy.currencyconverter.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
    @Bean
    public OpenAPI currencyConverterOpenApi() {
        return new OpenAPI().info(new Info()
                .title("Currency Converter API")
                .version("v1")
                .description("Converts amounts between currencies using Frankfurter exchange rates."));
    }
}
