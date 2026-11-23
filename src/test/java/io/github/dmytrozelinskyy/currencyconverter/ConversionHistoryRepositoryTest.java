package io.github.dmytrozelinskyy.currencyconverter;

import io.github.dmytrozelinskyy.currencyconverter.model.ConversionHistory;
import io.github.dmytrozelinskyy.currencyconverter.repositories.ConversionHistoryRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TestcontainersConfiguration.class)
public class ConversionHistoryRepositoryTest {
    @Autowired
    ConversionHistoryRepository conversionHistoryRepository;

    @Test
    void returnsNewestFirst() throws Exception {
        conversionHistoryRepository.save(new ConversionHistory(
                "USD", "EUR", BigDecimal.ONE, BigDecimal.ONE
        ));
        Thread.sleep(5);
        conversionHistoryRepository.save(new ConversionHistory(
                "GBP", "PLN", BigDecimal.ONE, BigDecimal.ONE
        ));

        Page<ConversionHistory> page = conversionHistoryRepository.findAllByOrderByConvertedAtDesc(PageRequest.of(0, 10));

        assertThat(page.getContent().getFirst().getCurrencyFrom()).isEqualTo("GBP");
    }
}
