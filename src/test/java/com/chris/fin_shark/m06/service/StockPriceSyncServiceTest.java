package com.chris.fin_shark.m06.service;

import com.chris.fin_shark.client.twse.TwseApiClient;
import com.chris.fin_shark.common.domain.JobExecution;
import com.chris.fin_shark.common.dto.external.TwseStockPriceData;
import com.chris.fin_shark.common.enums.JobStatus;
import com.chris.fin_shark.common.enums.TriggerType;
import com.chris.fin_shark.m06.domain.Stock;
import com.chris.fin_shark.m06.domain.StockPrice;
import com.chris.fin_shark.m06.repository.JobExecutionRepository;
import com.chris.fin_shark.m06.repository.StockPriceRepository;
import com.chris.fin_shark.m06.repository.StockRepository;
import com.chris.fin_shark.m06.repository.TradingCalendarRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * StockPriceSyncService 單元測試
 *
 * @author chris
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("股價同步服務測試")
class StockPriceSyncServiceTest {

    @Mock
    private TwseApiClient twseApiClient;

    @Mock
    private StockRepository stockRepository;

    @Mock
    private StockPriceRepository stockPriceRepository;

    @Mock
    private TradingCalendarRepository tradingCalendarRepository;

    @Mock
    private JobExecutionRepository jobExecutionRepository;

    @InjectMocks
    private StockPriceSyncService stockPriceSyncService;

    private LocalDate testDate;
    private Stock testStock;
    private TwseStockPriceData testPriceData;

    @BeforeEach
    void setUp() {
        testDate = LocalDate.of(2024, 12, 24);

        testStock = Stock.builder()
                .stockId("2330")
                .stockName("台積電")
                .marketType("TWSE")
                .isActive(true)
                .build();

        testPriceData = TwseStockPriceData.builder()
                .tradeDate(testDate)
                .openPrice(BigDecimal.valueOf(580))
                .highPrice(BigDecimal.valueOf(585))
                .lowPrice(BigDecimal.valueOf(578))
                .closePrice(BigDecimal.valueOf(583))
                .volume(25000000L)
                .turnover(BigDecimal.valueOf(14575000000L))
                .transactions(45000)
                .changePrice(BigDecimal.valueOf(3))
                .build();
    }

    @Test
    @DisplayName("非交易日應跳過同步並返回 CANCELLED 狀態")
    void shouldSkipSyncOnNonTradingDay() {
        // Given
        when(tradingCalendarRepository.isTradingDay(testDate)).thenReturn(false);
        when(jobExecutionRepository.save(any(JobExecution.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        JobExecution result = stockPriceSyncService.syncStockPricesForDate(
                testDate, TriggerType.SCHEDULED);

        // Then
        assertThat(result.getJobStatus()).isEqualTo(JobStatus.CANCELLED.getCode());
        assertThat(result.getErrorMessage()).contains("非交易日");
        verify(stockRepository, never()).findByIsActiveTrue();
        verify(twseApiClient, never()).getStockMonthlyPrices(any(), any());
    }

    @Test
    @DisplayName("交易日應成功同步股價資料")
    void shouldSyncStockPricesOnTradingDay() throws InterruptedException {
        // Given
        when(tradingCalendarRepository.isTradingDay(testDate)).thenReturn(true);
        when(stockRepository.findByIsActiveTrue()).thenReturn(List.of(testStock));
        when(twseApiClient.getStockMonthlyPrices(eq("2330"), eq(testDate)))
                .thenReturn(List.of(testPriceData));
        when(jobExecutionRepository.save(any(JobExecution.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        JobExecution result = stockPriceSyncService.syncStockPricesForDate(
                testDate, TriggerType.MANUAL);

        // Then
        assertThat(result.getJobStatus()).isEqualTo(JobStatus.SUCCESS.getCode());
        assertThat(result.getSuccessItems()).isEqualTo(1);
        assertThat(result.getFailedItems()).isEqualTo(0);

        verify(stockPriceRepository).save(any(StockPrice.class));
    }

    @Test
    @DisplayName("無活躍股票時應正常完成並返回 SUCCESS")
    void shouldSucceedWithNoActiveStocks() {
        // Given
        when(tradingCalendarRepository.isTradingDay(testDate)).thenReturn(true);
        when(stockRepository.findByIsActiveTrue()).thenReturn(Collections.emptyList());
        when(jobExecutionRepository.save(any(JobExecution.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        JobExecution result = stockPriceSyncService.syncStockPricesForDate(
                testDate, TriggerType.SCHEDULED);

        // Then
        assertThat(result.getJobStatus()).isEqualTo(JobStatus.SUCCESS.getCode());
        assertThat(result.getTotalItems()).isEqualTo(0);
        assertThat(result.getSuccessItems()).isEqualTo(0);
        assertThat(result.getFailedItems()).isEqualTo(0);

        verify(twseApiClient, never()).getStockMonthlyPrices(any(), any());
    }

    @Test
    @DisplayName("API 回應空資料時仍計為成功")
    void shouldCountAsSuccessWhenApiReturnsEmpty() throws InterruptedException {
        // Given
        when(tradingCalendarRepository.isTradingDay(testDate)).thenReturn(true);
        when(stockRepository.findByIsActiveTrue()).thenReturn(List.of(testStock));
        when(twseApiClient.getStockMonthlyPrices(eq("2330"), eq(testDate)))
                .thenReturn(Collections.emptyList());
        when(jobExecutionRepository.save(any(JobExecution.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        JobExecution result = stockPriceSyncService.syncStockPricesForDate(
                testDate, TriggerType.MANUAL);

        // Then
        assertThat(result.getJobStatus()).isEqualTo(JobStatus.SUCCESS.getCode());
        assertThat(result.getSuccessItems()).isEqualTo(1);
        verify(stockPriceRepository, never()).save(any(StockPrice.class));
    }

    @Test
    @DisplayName("單一股票同步失敗不影響其他股票")
    void shouldContinueOnSingleStockFailure() throws InterruptedException {
        // Given
        Stock stock1 = Stock.builder().stockId("2330").stockName("台積電").isActive(true).build();
        Stock stock2 = Stock.builder().stockId("2317").stockName("鴻海").isActive(true).build();

        when(tradingCalendarRepository.isTradingDay(testDate)).thenReturn(true);
        when(stockRepository.findByIsActiveTrue()).thenReturn(List.of(stock1, stock2));

        when(twseApiClient.getStockMonthlyPrices(eq("2330"), eq(testDate)))
                .thenThrow(new RuntimeException("API Error"));

        TwseStockPriceData stock2Data = TwseStockPriceData.builder()
                .tradeDate(testDate)
                .openPrice(BigDecimal.valueOf(100))
                .highPrice(BigDecimal.valueOf(102))
                .lowPrice(BigDecimal.valueOf(99))
                .closePrice(BigDecimal.valueOf(101))
                .volume(10000000L)
                .turnover(BigDecimal.valueOf(1010000000L))
                .transactions(20000)
                .changePrice(BigDecimal.valueOf(1))
                .build();
        when(twseApiClient.getStockMonthlyPrices(eq("2317"), eq(testDate)))
                .thenReturn(List.of(stock2Data));

        when(jobExecutionRepository.save(any(JobExecution.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        JobExecution result = stockPriceSyncService.syncStockPricesForDate(
                testDate, TriggerType.MANUAL);

        // Then
        assertThat(result.getJobStatus()).isEqualTo(JobStatus.SUCCESS.getCode());
        assertThat(result.getSuccessItems()).isEqualTo(1);
        assertThat(result.getFailedItems()).isEqualTo(1);
        assertThat(result.getTotalItems()).isEqualTo(2);
    }

    @Test
    @DisplayName("應正確計算漲跌幅")
    void shouldCalculateChangePercentCorrectly() throws InterruptedException {
        // Given
        when(tradingCalendarRepository.isTradingDay(testDate)).thenReturn(true);
        when(stockRepository.findByIsActiveTrue()).thenReturn(List.of(testStock));
        when(twseApiClient.getStockMonthlyPrices(eq("2330"), eq(testDate)))
                .thenReturn(List.of(testPriceData));
        when(jobExecutionRepository.save(any(JobExecution.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ArgumentCaptor<StockPrice> priceCaptor = ArgumentCaptor.forClass(StockPrice.class);

        // When
        stockPriceSyncService.syncStockPricesForDate(testDate, TriggerType.MANUAL);

        // Then
        verify(stockPriceRepository).save(priceCaptor.capture());

        StockPrice savedPrice = priceCaptor.getValue();
        assertThat(savedPrice.getStockId()).isEqualTo("2330");
        assertThat(savedPrice.getClosePrice()).isEqualByComparingTo(BigDecimal.valueOf(583));
        assertThat(savedPrice.getChangePrice()).isEqualByComparingTo(BigDecimal.valueOf(3));
        assertThat(savedPrice.getChangePercent()).isNotNull();
    }

    @Test
    @DisplayName("漲跌價為零時漲跌幅應為零")
    void shouldReturnZeroPercentWhenChangeIsZero() throws InterruptedException {
        // Given
        TwseStockPriceData zeroPriceData = TwseStockPriceData.builder()
                .tradeDate(testDate)
                .openPrice(BigDecimal.valueOf(580))
                .highPrice(BigDecimal.valueOf(582))
                .lowPrice(BigDecimal.valueOf(578))
                .closePrice(BigDecimal.valueOf(580))
                .volume(20000000L)
                .turnover(BigDecimal.valueOf(11600000000L))
                .transactions(40000)
                .changePrice(BigDecimal.ZERO)
                .build();

        when(tradingCalendarRepository.isTradingDay(testDate)).thenReturn(true);
        when(stockRepository.findByIsActiveTrue()).thenReturn(List.of(testStock));
        when(twseApiClient.getStockMonthlyPrices(eq("2330"), eq(testDate)))
                .thenReturn(List.of(zeroPriceData));
        when(jobExecutionRepository.save(any(JobExecution.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ArgumentCaptor<StockPrice> priceCaptor = ArgumentCaptor.forClass(StockPrice.class);

        // When
        stockPriceSyncService.syncStockPricesForDate(testDate, TriggerType.MANUAL);

        // Then
        verify(stockPriceRepository).save(priceCaptor.capture());

        StockPrice savedPrice = priceCaptor.getValue();
        assertThat(savedPrice.getChangePercent()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("應正確記錄觸發類型 - SCHEDULED")
    void shouldRecordScheduledTriggerType() {
        // Given
        when(tradingCalendarRepository.isTradingDay(testDate)).thenReturn(false);
        when(jobExecutionRepository.save(any(JobExecution.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        JobExecution result = stockPriceSyncService.syncStockPricesForDate(
                testDate, TriggerType.SCHEDULED);

        // Then
        assertThat(result.getTriggerType()).isEqualTo(TriggerType.SCHEDULED.getCode());
    }

    @Test
    @DisplayName("應正確記錄觸發類型 - MANUAL")
    void shouldRecordManualTriggerType() {
        // Given
        when(tradingCalendarRepository.isTradingDay(testDate)).thenReturn(false);
        when(jobExecutionRepository.save(any(JobExecution.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        JobExecution result = stockPriceSyncService.syncStockPricesForDate(
                testDate, TriggerType.MANUAL);

        // Then
        assertThat(result.getTriggerType()).isEqualTo(TriggerType.MANUAL.getCode());
    }
}
