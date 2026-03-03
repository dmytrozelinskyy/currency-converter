package io.github.dmytrozelinskyy.currencyconverter.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

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

    @Column(nullable = false, updatable = false)
    private LocalDateTime convertedAt;
}

