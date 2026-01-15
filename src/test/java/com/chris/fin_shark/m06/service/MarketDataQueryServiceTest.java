package com.chris.fin_shark.m06.service;

import com.chris.fin_shark.m06.converter.InstitutionalTradingConverter;
import com.chris.fin_shark.m06.converter.MarginTradingConverter;
import com.chris.fin_shark.m06.converter.StockPriceConverter;
import com.chris.fin_shark.m06.domain.InstitutionalTrading;
import com.chris.fin_shark.m06.domain.MarginTrading;
import com.chris.fin_shark.m06.domain.StockPrice;
import com.chris.fin_shark.m06.dto.InstitutionalTradingDTO;
import com.chris.fin_shark.m06.dto.MarginTradingDTO;
import com.chris.fin_shark.m06.dto.StockPriceDTO;
import com.chris.fin_shark.m06.exception.StockNotFoundException;
import com.chris.fin_shark.m06.mapper.StockPriceMapper;
import com.chris.fin_shark.m06.repository.InstitutionalTradingRepository;
import com.chris.fin_shark.m06.repository.MarginTradingRepository;
import com.chris.fin_shark.m06.repository.StockPriceRepository;
import com.chris.fin_shark.m06.repository.StockRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * MarketDataQueryService 單元測試
 * <p>
 * 測試市場資料查詢服務的核心業務邏輯
 * </p>
 *
 * @author chris
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("市場資料查詢服務測試")
class MarketDataQueryServiceTest {

    @Mock
    private StockRepository stockRepository;

    @Mock
    private StockPriceRepository stockPriceRepository;

    @Mock
    private StockPriceMapper stockPriceMapper;

    @Mock
    private StockPriceConverter stockPriceConverter;

    @Mock
    private InstitutionalTradingRepository institutionalTradingRepository;

    @Mock
    private MarginTradingRepository marginTradingRepository;

    @Mock
    private InstitutionalTradingConverter institutionalTradingConverter;

    @Mock
    private MarginTradingConverter marginTradingConverter;

    @InjectMocks
    private MarketDataQueryService marketDataQueryService;

    private String testStockId;
    private LocalDate testDate;
    private StockPrice testStockPrice;
    private StockPriceDTO testStockPriceDTO;

    @BeforeEach
    void setUp() {
        testStockId = "2330";
        testDate = LocalDate.of(2024, 12, 24);

        testStockPrice = new StockPrice();
        testStockPrice.setStockId(testStockId);
        testStockPrice.setTradeDate(testDate);
        testStockPrice.setOpenPrice(BigDecimal.valueOf(580));
        testStockPrice.setHighPrice(BigDecimal.valueOf(585));
        testStockPrice.setLowPrice(BigDecimal.valueOf(578));
        testStockPrice.setClosePrice(BigDecimal.valueOf(583));
        testStockPrice.setVolume(25000000L);

        testStockPriceDTO = StockPriceDTO.builder()
                .stockId(testStockId)
                .tradeDate(testDate)
                .openPrice(BigDecimal.valueOf(580))
                .highPrice(BigDecimal.valueOf(585))
                .lowPrice(BigDecimal.valueOf(578))
                .closePrice(BigDecimal.valueOf(583))
                .volume(25000000L)
                .build();
    }

    @Test
    @DisplayName("queryStockPrices - 股票不存在時應拋出 StockNotFoundException")
    void queryStockPrices_shouldThrowWhenStockNotFound() {
        // Given
        when(stockRepository.existsByStockId("9999")).thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> marketDataQueryService.queryStockPrices(
                "9999", null, null, 30))
                .isInstanceOf(StockNotFoundException.class);

        verify(stockPriceRepository, never()).findByStockIdAndDateRange(any(), any(), any());
    }

    @Test
    @DisplayName("queryStockPrices - 使用日期範圍查詢應返回正確結果")
    void queryStockPrices_shouldQueryByDateRange() {
        // Given
        LocalDate startDate = LocalDate.of(2024, 12, 1);
        LocalDate endDate = LocalDate.of(2024, 12, 31);

        when(stockRepository.existsByStockId(testStockId)).thenReturn(true);
        when(stockPriceRepository.findByStockIdAndDateRange(testStockId, startDate, endDate))
                .thenReturn(List.of(testStockPrice));
        when(stockPriceConverter.toDTOList(any())).thenReturn(List.of(testStockPriceDTO));

        // When
        List<StockPriceDTO> result = marketDataQueryService.queryStockPrices(
                testStockId, startDate, endDate, 30);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getStockId()).isEqualTo(testStockId);
        verify(stockPriceRepository).findByStockIdAndDateRange(testStockId, startDate, endDate);
    }

    @Test
    @DisplayName("queryStockPrices - 使用天數查詢應計算正確的日期範圍")
    void queryStockPrices_shouldQueryByDays() {
        // Given
        when(stockRepository.existsByStockId(testStockId)).thenReturn(true);
        when(stockPriceRepository.findTopByStockIdOrderByTradeDateDesc(testStockId))
                .thenReturn(Optional.of(testStockPrice));
        when(stockPriceRepository.findByStockIdAndDateRange(eq(testStockId), any(), any()))
                .thenReturn(List.of(testStockPrice));
        when(stockPriceConverter.toDTOList(any())).thenReturn(List.of(testStockPriceDTO));

        // When
        List<StockPriceDTO> result = marketDataQueryService.queryStockPrices(
                testStockId, null, null, 30);

        // Then
        assertThat(result).hasSize(1);
        verify(stockPriceRepository).findTopByStockIdOrderByTradeDateDesc(testStockId);
        verify(stockPriceRepository).findByStockIdAndDateRange(
                eq(testStockId),
                eq(testDate.minusDays(29)),
                eq(testDate));
    }

    @Test
    @DisplayName("queryStockPrices - 無股價資料時應返回空列表")
    void queryStockPrices_shouldReturnEmptyWhenNoPriceData() {
        // Given
        when(stockRepository.existsByStockId(testStockId)).thenReturn(true);
        when(stockPriceRepository.findTopByStockIdOrderByTradeDateDesc(testStockId))
                .thenReturn(Optional.empty());

        // When
        List<StockPriceDTO> result = marketDataQueryService.queryStockPrices(
                testStockId, null, null, 30);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("getLatestPrice - 應返回最新股價")
    void getLatestPrice_shouldReturnLatestPrice() {
        // Given
        when(stockPriceRepository.findTopByStockIdOrderByTradeDateDesc(testStockId))
                .thenReturn(Optional.of(testStockPrice));
        when(stockPriceConverter.toDTO(testStockPrice)).thenReturn(testStockPriceDTO);

        // When
        StockPriceDTO result = marketDataQueryService.getLatestPrice(testStockId);

        // Then
        assertThat(result.getStockId()).isEqualTo(testStockId);
        assertThat(result.getTradeDate()).isEqualTo(testDate);
        assertThat(result.getClosePrice()).isEqualByComparingTo(BigDecimal.valueOf(583));
    }

    @Test
    @DisplayName("getLatestPrice - 無股價資料時應拋出異常")
    void getLatestPrice_shouldThrowWhenNoPriceData() {
        // Given
        when(stockPriceRepository.findTopByStockIdOrderByTradeDateDesc("9999"))
                .thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> marketDataQueryService.getLatestPrice("9999"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("No price data found");
    }

    @Test
    @DisplayName("queryInstitutionalTrading - 使用日期範圍查詢法人買賣超")
    void queryInstitutionalTrading_shouldQueryByDateRange() {
        // Given
        LocalDate startDate = LocalDate.of(2024, 12, 1);
        LocalDate endDate = LocalDate.of(2024, 12, 31);

        InstitutionalTrading trading = new InstitutionalTrading();
        trading.setStockId(testStockId);
        trading.setTradeDate(testDate);
        trading.setForeignNet(1000000L);

        InstitutionalTradingDTO tradingDTO = InstitutionalTradingDTO.builder()
                .stockId(testStockId)
                .tradeDate(testDate)
                .foreignNet(1000000L)
                .build();

        when(institutionalTradingRepository.findByStockIdAndDateRange(
                testStockId, startDate, endDate))
                .thenReturn(List.of(trading));
        when(institutionalTradingConverter.toDTOList(any())).thenReturn(List.of(tradingDTO));

        // When
        List<InstitutionalTradingDTO> result = marketDataQueryService.queryInstitutionalTrading(
                testStockId, startDate, endDate, 30);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getForeignNet()).isEqualTo(1000000L);
    }

    @Test
    @DisplayName("queryInstitutionalTrading - 使用天數查詢法人買賣超")
    void queryInstitutionalTrading_shouldQueryByDays() {
        // Given
        InstitutionalTrading trading = new InstitutionalTrading();
        trading.setStockId(testStockId);
        trading.setTradeDate(testDate);

        Page<InstitutionalTrading> page = new PageImpl<>(List.of(trading));

        when(institutionalTradingRepository.findByStockIdOrderByTradeDateDesc(
                eq(testStockId), any(Pageable.class)))
                .thenReturn(page);
        when(institutionalTradingConverter.toDTOList(any())).thenReturn(Collections.emptyList());

        // When
        List<InstitutionalTradingDTO> result = marketDataQueryService.queryInstitutionalTrading(
                testStockId, null, null, 30);

        // Then
        verify(institutionalTradingRepository).findByStockIdOrderByTradeDateDesc(
                eq(testStockId), any(Pageable.class));
    }

    @Test
    @DisplayName("queryMarginTrading - 使用日期範圍查詢融資融券")
    void queryMarginTrading_shouldQueryByDateRange() {
        // Given
        LocalDate startDate = LocalDate.of(2024, 12, 1);
        LocalDate endDate = LocalDate.of(2024, 12, 31);

        MarginTrading margin = new MarginTrading();
        margin.setStockId(testStockId);
        margin.setTradeDate(testDate);
        margin.setMarginBalance(5000000L);

        MarginTradingDTO marginDTO = MarginTradingDTO.builder()
                .stockId(testStockId)
                .tradeDate(testDate)
                .marginBalance(5000000L)
                .build();

        when(marginTradingRepository.findByStockIdAndDateRange(
                testStockId, startDate, endDate))
                .thenReturn(List.of(margin));
        when(marginTradingConverter.toDTOList(any())).thenReturn(List.of(marginDTO));

        // When
        List<MarginTradingDTO> result = marketDataQueryService.queryMarginTrading(
                testStockId, startDate, endDate, 30);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getMarginBalance()).isEqualTo(5000000L);
    }

    @Test
    @DisplayName("queryMarginTrading - 使用天數查詢融資融券")
    void queryMarginTrading_shouldQueryByDays() {
        // Given
        MarginTrading margin = new MarginTrading();
        margin.setStockId(testStockId);
        margin.setTradeDate(testDate);

        Page<MarginTrading> page = new PageImpl<>(List.of(margin));

        when(marginTradingRepository.findByStockIdOrderByTradeDateDesc(
                eq(testStockId), any(Pageable.class)))
                .thenReturn(page);
        when(marginTradingConverter.toDTOList(any())).thenReturn(Collections.emptyList());

        // When
        List<MarginTradingDTO> result = marketDataQueryService.queryMarginTrading(
                testStockId, null, null, 30);

        // Then
        verify(marginTradingRepository).findByStockIdOrderByTradeDateDesc(
                eq(testStockId), any(Pageable.class));
    }
}
