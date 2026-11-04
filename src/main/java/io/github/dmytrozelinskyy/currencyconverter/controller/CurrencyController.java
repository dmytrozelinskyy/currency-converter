package io.github.dmytrozelinskyy.currencyconverter.controller;

import io.github.dmytrozelinskyy.currencyconverter.service.CurrencyConversionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Pattern;
import org.springframework.http.ProblemDetail;
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
    @Operation(summary = "Convert an amount between two currencies")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Converted amount"),
            @ApiResponse(responseCode = "400", description = "Invalid input or unsupported currency",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "503", description = "Exchange rate provider unavailable",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
    })
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
