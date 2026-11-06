package io.github.dmytrozelinskyy.currencyconverter.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "conversion_histories")
public class ConversionHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, updatable = false, length = 3)
    private String currencyFrom;

    @Column(nullable = false, updatable = false, length = 3)
    private String currencyTo;

    @Column(nullable = false, updatable = false, precision = 19, scale = 4)
    private BigDecimal amount;

    @Column(nullable = false, updatable = false, precision = 19, scale = 4)
    private BigDecimal result;

    @Column(nullable = false, updatable = false)
    private Instant convertedAt;

    protected ConversionHistory() { }

    public ConversionHistory(String currencyFrom, String currencyTo, BigDecimal amount, BigDecimal result) {
        this.currencyFrom = currencyFrom;
        this.currencyTo = currencyTo;
        this.amount = amount;
        this.result = result;
        this.convertedAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public String getCurrencyFrom() {
        return currencyFrom;
    }

    public String getCurrencyTo() {
        return currencyTo;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public BigDecimal getResult() {
        return result;
    }

    public Instant getConvertedAt() {
        return convertedAt;
    }
}

