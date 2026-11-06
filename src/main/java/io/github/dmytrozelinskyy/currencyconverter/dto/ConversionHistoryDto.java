package io.github.dmytrozelinskyy.currencyconverter.dto;

import io.github.dmytrozelinskyy.currencyconverter.model.ConversionHistory;

import java.math.BigDecimal;
import java.time.Instant;

public record ConversionHistoryDto (
        String from,
        String to,
        BigDecimal amount,
        BigDecimal result,
        Instant convertedAt
) {
    public static ConversionHistoryDto from(ConversionHistory h) {
        return new ConversionHistoryDto(
                h.getCurrencyFrom(), h.getCurrencyTo(),
                h.getAmount(), h.getResult(),
                h.getConvertedAt());
    }
}
