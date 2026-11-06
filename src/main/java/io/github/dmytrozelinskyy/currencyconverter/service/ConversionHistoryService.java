package io.github.dmytrozelinskyy.currencyconverter.service;

import io.github.dmytrozelinskyy.currencyconverter.dto.ConversionHistoryDto;
import io.github.dmytrozelinskyy.currencyconverter.model.ConversionHistory;
import io.github.dmytrozelinskyy.currencyconverter.repositories.ConversionHistoryRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class ConversionHistoryService {
    private static final Logger log = LoggerFactory.getLogger(ConversionHistoryService.class);

    private final ConversionHistoryRepository repository;

    public ConversionHistoryService(ConversionHistoryRepository repository) { this.repository = repository; }

    public void record(String from, String to, BigDecimal amount, BigDecimal result) {
        try {
            repository.save(
                    new ConversionHistory(from, to, amount, result));
        } catch (RuntimeException e) {
            log.warn("Could not save conversion history.", e);
        }
    }

    public Page<ConversionHistoryDto> recent(Pageable pageable) {
        return repository
                .findAllByOrderByConvertedAtDesc(pageable)
                .map(ConversionHistoryDto::from);
    }
}
