package io.github.dmytrozelinskyy.currencyconverter.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, updatable = false, length = 25)
    private String username;

    @Column(name = "api_key", nullable = false, unique = true, updatable = false)
    private String apiKey;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
