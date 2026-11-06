package io.github.dmytrozelinskyy.currencyconverter.repositories;

import io.github.dmytrozelinskyy.currencyconverter.model.ConversionHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConversionHistoryRepository extends JpaRepository<ConversionHistory, Long> {
    Page<ConversionHistory> findAllByOrderByConvertedAtDesc(Pageable pageable);
}
