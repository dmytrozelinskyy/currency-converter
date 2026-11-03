package io.github.dmytrozelinskyy.currencyconverter.controller;

import io.github.dmytrozelinskyy.currencyconverter.service.CurrencyConversionService;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Pattern;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

@RestController
public class CurrencyController {

    private final CurrencyConversionService conversionService;

    public CurrencyController(CurrencyConversionService conversionService) {
        this.conversionService = conversionService;
    }

    @GetMapping("/api/convert")
    public BigDecimal convert(
            @RequestParam
            @Pattern(regexp = "[A-Za-z]{3}", message = "must be a 3-letter currency code, e.g. USD")
            String from,
            @RequestParam
            @Pattern(regexp = "[A-Za-z]{3}", message = "must be a 3-letter currency code, e.g. EUR")
            String to,
            @RequestParam
            @DecimalMin(value = "0", inclusive = false, message = "must be greater than 0")
            BigDecimal amount
    )
    {
        return conversionService.convert(from, to, amount);
    }
}
