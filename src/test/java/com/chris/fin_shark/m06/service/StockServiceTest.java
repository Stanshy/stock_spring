package com.chris.fin_shark.m06.service;

import com.chris.fin_shark.m06.domain.Stock;
import com.chris.fin_shark.m06.dto.StockDTO;
import com.chris.fin_shark.m06.dto.request.StockCreateRequest;
import com.chris.fin_shark.m06.exception.StockNotFoundException;
import com.chris.fin_shark.m06.converter.StockConverter;
import com.chris.fin_shark.m06.repository.StockRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * StockService 測試
 *
 * @author chris
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
class StockServiceTest {

    @Mock
    private StockRepository stockRepository;

    @Mock
    private StockConverter stockConverter;

    @InjectMocks
    private StockService stockService;

    @Test
    void testGetStockById_Success() {
        // Given
        Stock stock = Stock.builder()
                .stockId("2330")
                .stockName("台積電")
                .build();

        StockDTO dto = StockDTO.builder()
                .stockId("2330")
                .stockName("台積電")
                .build();

        when(stockRepository.findById("2330")).thenReturn(Optional.of(stock));
        when(stockConverter.toDTO(stock)).thenReturn(dto);

        // When
        StockDTO result = stockService.getStockById("2330");

        // Then
        assertThat(result.getStockId()).isEqualTo("2330");
        assertThat(result.getStockName()).isEqualTo("台積電");
        verify(stockRepository).findById("2330");
        verify(stockConverter).toDTO(stock);
    }

    @Test
    void testGetStockById_NotFound() {
        // Given
        when(stockRepository.findById("9999")).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> stockService.getStockById("9999"))
                .isInstanceOf(StockNotFoundException.class);

        verify(stockRepository).findById("9999");
        verifyNoInteractions(stockConverter);
    }

    @Test
    void testCreateStock_Success() {
        // Given
        StockCreateRequest request = StockCreateRequest.builder()
                .stockId("2330")
                .stockName("台積電")
                .marketType("TWSE")
                .listingDate(LocalDate.now())
                .parValue(BigDecimal.TEN)
                .issuedShares(25930380458L)
                .build();

        Stock stock = Stock.builder()
                .stockId("2330")
                .stockName("台積電")
                .build();

        StockDTO dto = StockDTO.builder()
                .stockId("2330")
                .stockName("台積電")
                .build();

        when(stockRepository.existsByStockId("2330")).thenReturn(false);
        when(stockConverter.toEntity(request)).thenReturn(stock);
        when(stockRepository.save(stock)).thenReturn(stock);
        when(stockConverter.toDTO(stock)).thenReturn(dto);

        // When
        StockDTO result = stockService.createStock(request);

        // Then
        assertThat(result.getStockId()).isEqualTo("2330");
        verify(stockRepository).existsByStockId("2330");
        verify(stockRepository).save(stock);
    }
}
