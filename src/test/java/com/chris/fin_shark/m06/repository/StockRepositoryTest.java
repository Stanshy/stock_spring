package com.chris.fin_shark.m06.repository;

import com.chris.fin_shark.m06.domain.Stock;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * StockRepository 測試
 *
 * @author chris
 * @since 1.0.0
 */
@DataJpaTest
@ActiveProfiles("test")
class StockRepositoryTest {

    @Autowired
    private StockRepository stockRepository;

    @Test
    void testSaveAndFindById() {
        // Given
        Stock stock = Stock.builder()
                .stockId("2330")
                .stockName("台積電")
                .marketType("TWSE")
                .industry("半導體")
                .listingDate(LocalDate.of(1994, 9, 5))
                .isActive(true)
                .parValue(BigDecimal.valueOf(10))
                .issuedShares(25930380458L)
                .build();

        // When
        Stock saved = stockRepository.save(stock);
        Optional<Stock> found = stockRepository.findById("2330");

        // Then
        assertThat(saved.getStockId()).isEqualTo("2330");
        assertThat(found).isPresent();
        assertThat(found.get().getStockName()).isEqualTo("台積電");
    }

    @Test
    void testFindByMarketType() {
        // Given
        stockRepository.save(Stock.builder()
                .stockId("2330").stockName("台積電").marketType("TWSE")
                .isActive(true).listingDate(LocalDate.now()).build());
        stockRepository.save(Stock.builder()
                .stockId("2454").stockName("聯發科").marketType("TWSE")
                .isActive(true).listingDate(LocalDate.now()).build());
        stockRepository.save(Stock.builder()
                .stockId("6505").stockName("台塑化").marketType("OTC")
                .isActive(true).listingDate(LocalDate.now()).build());

        // When
        List<Stock> twseStocks = stockRepository.findByMarketType("TWSE");

        // Then
        assertThat(twseStocks).hasSize(2);
        assertThat(twseStocks).extracting("stockId").containsExactlyInAnyOrder("2330", "2454");
    }

    @Test
    void testFindByIsActiveTrue() {
        // Given
        stockRepository.save(Stock.builder()
                .stockId("2330").stockName("台積電").marketType("TWSE")
                .isActive(true).listingDate(LocalDate.now()).build());
        stockRepository.save(Stock.builder()
                .stockId("2454").stockName("聯發科").marketType("TWSE")
                .isActive(false).listingDate(LocalDate.now()).build());

        // When
        List<Stock> activeStocks = stockRepository.findByIsActiveTrue();

        // Then
        assertThat(activeStocks).hasSize(1);
        assertThat(activeStocks.get(0).getStockId()).isEqualTo("2330");
    }

    @Test
    void testExistsByStockId() {
        // Given
        stockRepository.save(Stock.builder()
                .stockId("2330").stockName("台積電").marketType("TWSE")
                .isActive(true).listingDate(LocalDate.now()).build());

        // When & Then
        assertThat(stockRepository.existsByStockId("2330")).isTrue();
        assertThat(stockRepository.existsByStockId("9999")).isFalse();
    }
}
