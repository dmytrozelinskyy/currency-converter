package io.github.dmytrozelinskyy.currencyconverter;

import io.github.dmytrozelinskyy.currencyconverter.dto.ConversionHistoryDto;
import io.github.dmytrozelinskyy.currencyconverter.model.ConversionHistory;
import io.github.dmytrozelinskyy.currencyconverter.repositories.ConversionHistoryRepository;
import io.github.dmytrozelinskyy.currencyconverter.service.ConversionHistoryService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ConversionHistoryServiceTest {

    @Mock
    ConversionHistoryRepository conversionHistoryRepository;

    @InjectMocks
    ConversionHistoryService conversionHistoryService;

    @Test
    void record_savesEntity() {
        conversionHistoryService.record("USD", "EUR", new BigDecimal("100"), new BigDecimal("92.35"));

        ArgumentCaptor<ConversionHistory> captor = ArgumentCaptor.forClass(ConversionHistory.class);
        verify(conversionHistoryRepository).save(captor.capture());
        assertThat(captor.getValue().getCurrencyTo()).isEqualTo("EUR");
    }

    @Test
    void record_swallowsRepositoryFailure() {
        when(conversionHistoryRepository.save(any())).thenThrow(new RuntimeException("db down"));

        assertThatCode(() -> conversionHistoryService.record("USD", "EUR", BigDecimal.TEN, BigDecimal.ONE)).doesNotThrowAnyException();
    }

    @Test
    void recent_mapsEntitiesToDtos() {
        ConversionHistory entity = new ConversionHistory("USD", "EUR", BigDecimal.TEN, BigDecimal.ONE);
        when(conversionHistoryRepository.findAllByOrderByConvertedAtDesc(any())).thenReturn(new PageImpl<>(List.of(entity)));

        Page<ConversionHistoryDto> page = conversionHistoryService.recent(PageRequest.of(0, 20));
        assertThat(page.getContent().getFirst().from()).isEqualTo("USD");
    }
}
