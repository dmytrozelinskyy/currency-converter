package io.github.dmytrozelinskyy.currencyconverter.controller;

import io.github.dmytrozelinskyy.currencyconverter.dto.CurrencyDto;
import io.github.dmytrozelinskyy.currencyconverter.service.CurrencyListService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class CurrencyListController {
    private CurrencyListService service;

    public CurrencyListController(CurrencyListService service) {
        this.service = service;
    }

    @Operation(summary = "")
    @GetMapping("/api/currencies")
    public List<CurrencyDto> currencies() {
        return service.getCurrencies();
    }
}
