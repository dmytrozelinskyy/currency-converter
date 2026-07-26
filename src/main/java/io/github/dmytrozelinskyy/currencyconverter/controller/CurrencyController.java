package io.github.dmytrozelinskyy.currencyconverter.controller;

import io.github.dmytrozelinskyy.currencyconverter.service.CurrencyConversionService;
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
            @RequestParam String from,
            @RequestParam String to,
            @RequestParam BigDecimal amount
    )
    {
        return conversionService.convert(from, to, amount);
    }
}
