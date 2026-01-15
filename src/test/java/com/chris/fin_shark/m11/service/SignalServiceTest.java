package com.chris.fin_shark.m11.service;

import com.chris.fin_shark.common.dto.PageResponse;
import com.chris.fin_shark.common.enums.SignalType;
import com.chris.fin_shark.m11.converter.StrategyConverter;
import com.chris.fin_shark.m11.domain.Strategy;
import com.chris.fin_shark.m11.domain.StrategySignal;
import com.chris.fin_shark.m11.dto.StrategySignalDTO;
import com.chris.fin_shark.m11.dto.request.SignalQueryRequest;
import com.chris.fin_shark.m11.dto.response.SignalScanResponse;
import com.chris.fin_shark.m11.enums.StrategyStatus;
import com.chris.fin_shark.m11.enums.StrategyType;
import com.chris.fin_shark.m11.exception.StrategyNotFoundException;
import com.chris.fin_shark.m11.mapper.StrategyMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 信號服務單元測試
 *
 * @author chris
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("信號服務測試")
class SignalServiceTest {

    @Mock
    private StrategyMapper strategyMapper;

    @Mock
    private StrategyConverter strategyConverter;

    @InjectMocks
    private SignalService signalService;

    private Strategy testStrategy;
    private StrategySignal testSignal;
    private StrategySignalDTO testSignalDTO;

    @BeforeEach
    void setUp() {
        testStrategy = Strategy.builder()
                .strategyId("STG_TEST_001")
                .strategyName("測試策略")
                .strategyType(StrategyType.MOMENTUM)
                .currentVersion(1)
                .status(StrategyStatus.ACTIVE)
                .build();

        testSignal = StrategySignal.builder()
                .signalId("SIG_TEST_001")
                .strategyId("STG_TEST_001")
                .stockId("2330")
                .tradeDate(LocalDate.now())
                .signalType(SignalType.BUY)
                .confidenceScore(BigDecimal.valueOf(75.5))
                .isConsumed(false)
                .build();

        testSignalDTO = StrategySignalDTO.builder()
                .signalId("SIG_TEST_001")
                .strategyId("STG_TEST_001")
                .stockId("2330")
                .tradeDate(LocalDate.now())
                .signalType(SignalType.BUY)
                .confidenceScore(BigDecimal.valueOf(75.5))
                .build();

        System.out.println("\n========================================");
        System.out.println("🧪 信號服務測試");
        System.out.println("========================================\n");
    }

    @Nested
    @DisplayName("查詢策略信號測試")
    class GetSignalsTests {

        @Test
        @DisplayName("測試: 查詢策略信號")
        void testGetSignals() {
            System.out.println("📝 測試: 查詢策略信號");

            // Given
            String strategyId = "STG_TEST_001";
            SignalQueryRequest request = SignalQueryRequest.builder()
                    .page(0)
                    .size(50)
                    .build();

            when(strategyMapper.selectById(strategyId)).thenReturn(testStrategy);
            when(strategyMapper.selectSignals(eq(strategyId), any(), any(), any(), any(), any(), anyInt(), anyInt()))
                    .thenReturn(List.of(testSignal));
            when(strategyMapper.countSignals(eq(strategyId), any(), any(), any(), any(), any()))
                    .thenReturn(1);
            when(strategyConverter.toSignalDTOList(anyList())).thenReturn(List.of(testSignalDTO));

            // When
            PageResponse<StrategySignalDTO> result = signalService.getSignals(strategyId, request);

            // Then
            System.out.println("  結果: 查詢到 " + result.getItems().size() + " 個信號");
            assertThat(result.getItems()).hasSize(1);
            assertThat(result.getPagination().getTotalItems()).isEqualTo(1);

            verify(strategyMapper).selectSignals(eq(strategyId), any(), any(), any(), any(), any(), anyInt(), anyInt());
        }

        @Test
        @DisplayName("測試: 策略不存在時拋出異常")
        void testGetSignalsStrategyNotFound() {
            System.out.println("📝 測試: 策略不存在時拋出異常");

            // Given
            String strategyId = "NON_EXISTENT";
            SignalQueryRequest request = SignalQueryRequest.builder().build();

            when(strategyMapper.selectById(strategyId)).thenReturn(null);

            // When/Then
            assertThatThrownBy(() -> signalService.getSignals(strategyId, request))
                    .isInstanceOf(StrategyNotFoundException.class);

            System.out.println("  結果: ✅ 正確拋出 StrategyNotFoundException");
        }

        @Test
        @DisplayName("測試: 篩選特定信號類型")
        void testGetSignalsWithFilter() {
            System.out.println("📝 測試: 篩選特定信號類型");

            // Given
            String strategyId = "STG_TEST_001";
            SignalQueryRequest request = SignalQueryRequest.builder()
                    .signalType("BUY")
                    .minConfidence(BigDecimal.valueOf(70))
                    .page(0)
                    .size(50)
                    .build();

            when(strategyMapper.selectById(strategyId)).thenReturn(testStrategy);
            when(strategyMapper.selectSignals(eq(strategyId), any(), any(), eq("BUY"), any(), eq(BigDecimal.valueOf(70)), anyInt(), anyInt()))
                    .thenReturn(List.of(testSignal));
            when(strategyMapper.countSignals(eq(strategyId), any(), any(), eq("BUY"), any(), eq(BigDecimal.valueOf(70))))
                    .thenReturn(1);
            when(strategyConverter.toSignalDTOList(anyList())).thenReturn(List.of(testSignalDTO));

            // When
            PageResponse<StrategySignalDTO> result = signalService.getSignals(strategyId, request);

            // Then
            System.out.println("  結果: 篩選到 " + result.getItems().size() + " 個 BUY 信號");
            assertThat(result.getItems()).hasSize(1);
        }
    }

    @Nested
    @DisplayName("全市場信號掃描測試")
    class ScanSignalsTests {

        @Test
        @DisplayName("測試: 全市場信號掃描")
        void testScanSignals() {
            System.out.println("📝 測試: 全市場信號掃描");

            // Given
            LocalDate tradeDate = LocalDate.now();
            when(strategyMapper.selectActiveStrategies()).thenReturn(List.of(testStrategy));
            when(strategyMapper.selectUnconsumedSignals(eq(tradeDate), any(), any(), any(), anyInt()))
                    .thenReturn(List.of(testSignal));
            when(strategyConverter.toSignalDTOList(anyList())).thenReturn(List.of(testSignalDTO));

            // When
            SignalScanResponse result = signalService.scanSignals(
                    tradeDate,
                    null,
                    BigDecimal.valueOf(60),
                    null,
                    100);

            // Then
            System.out.println("  結果:");
            System.out.println("    - 掃描策略數: " + result.getStrategiesScanned());
            System.out.println("    - 總信號數: " + result.getTotalSignals());
            System.out.println("    - 掃描時間: " + result.getScanTimeMs() + " ms");

            assertThat(result).isNotNull();
            assertThat(result.getStrategiesScanned()).isEqualTo(1);
            assertThat(result.getTotalSignals()).isGreaterThanOrEqualTo(0);
        }

        @Test
        @DisplayName("測試: 篩選特定策略類型的信號")
        void testScanSignalsWithStrategyType() {
            System.out.println("📝 測試: 篩選特定策略類型的信號");

            // Given
            LocalDate tradeDate = LocalDate.now();
            when(strategyMapper.selectActiveStrategies()).thenReturn(List.of(testStrategy));
            when(strategyMapper.selectUnconsumedSignals(eq(tradeDate), any(), any(), any(), anyInt()))
                    .thenReturn(List.of(testSignal));
            when(strategyConverter.toSignalDTOList(anyList())).thenReturn(List.of(testSignalDTO));

            // When
            SignalScanResponse result = signalService.scanSignals(
                    tradeDate,
                    null,
                    BigDecimal.valueOf(60),
                    "MOMENTUM",
                    100);

            // Then
            System.out.println("  結果: 掃描 MOMENTUM 類型策略的信號");
            assertThat(result).isNotNull();
        }

        @Test
        @DisplayName("測試: 無活躍策略時返回空結果")
        void testScanSignalsNoActiveStrategies() {
            System.out.println("📝 測試: 無活躍策略時返回空結果");

            // Given
            when(strategyMapper.selectActiveStrategies()).thenReturn(List.of());

            // When
            SignalScanResponse result = signalService.scanSignals(
                    LocalDate.now(),
                    null,
                    BigDecimal.valueOf(60),
                    null,
                    100);

            // Then
            System.out.println("  結果: 無活躍策略，總信號數 = " + result.getTotalSignals());
            assertThat(result.getStrategiesScanned()).isEqualTo(0);
            assertThat(result.getTotalSignals()).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("信號消費測試 (供 M13 使用)")
    class ConsumeSignalsTests {

        @Test
        @DisplayName("測試: 查詢未消費的信號")
        void testGetUnconsumedSignals() {
            System.out.println("📝 測試: 查詢未消費的信號");

            // Given
            LocalDate tradeDate = LocalDate.now();
            when(strategyMapper.selectUnconsumedSignals(eq(tradeDate), any(), any(), any(), anyInt()))
                    .thenReturn(List.of(testSignal));

            // When
            List<StrategySignal> result = signalService.getUnconsumedSignals(
                    tradeDate,
                    null,
                    null,
                    BigDecimal.valueOf(60),
                    100);

            // Then
            System.out.println("  結果: 查詢到 " + result.size() + " 個未消費信號");
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getIsConsumed()).isFalse();
        }

        @Test
        @DisplayName("測試: 標記信號已消費")
        void testMarkSignalsConsumed() {
            System.out.println("📝 測試: 標記信號已消費");

            // Given
            List<String> signalIds = List.of("SIG_TEST_001", "SIG_TEST_002");
            LocalDate tradeDate = LocalDate.now();
            String consumedBy = "M13";

            when(strategyMapper.markSignalsConsumed(signalIds, tradeDate, consumedBy)).thenReturn(2);

            // When
            signalService.markSignalsConsumed(signalIds, tradeDate, consumedBy);

            // Then
            System.out.println("  結果: 已標記 " + signalIds.size() + " 個信號為已消費");
            verify(strategyMapper).markSignalsConsumed(signalIds, tradeDate, consumedBy);
        }

        @Test
        @DisplayName("測試: 空列表不執行標記")
        void testMarkSignalsConsumedEmptyList() {
            System.out.println("📝 測試: 空列表不執行標記");

            // Given
            List<String> signalIds = List.of();

            // When
            signalService.markSignalsConsumed(signalIds, LocalDate.now(), "M13");

            // Then
            System.out.println("  結果: 空列表，跳過標記操作");
            verify(strategyMapper, never()).markSignalsConsumed(any(), any(), any());
        }

        @Test
        @DisplayName("測試: null 列表不執行標記")
        void testMarkSignalsConsumedNullList() {
            System.out.println("📝 測試: null 列表不執行標記");

            // When
            signalService.markSignalsConsumed(null, LocalDate.now(), "M13");

            // Then
            System.out.println("  結果: null 列表，跳過標記操作");
            verify(strategyMapper, never()).markSignalsConsumed(any(), any(), any());
        }
    }

    @Nested
    @DisplayName("信號統計測試")
    class SignalStatisticsTests {

        @Test
        @DisplayName("測試: 信號摘要統計正確")
        void testSignalSummary() {
            System.out.println("📝 測試: 信號摘要統計正確");

            // Given
            StrategySignal buySignal1 = createSignal("SIG_001", SignalType.BUY, 80);
            StrategySignal buySignal2 = createSignal("SIG_002", SignalType.BUY, 75);
            StrategySignal sellSignal = createSignal("SIG_003", SignalType.SELL, 70);
            StrategySignal holdSignal = createSignal("SIG_004", SignalType.HOLD, 65);

            List<StrategySignal> signals = List.of(buySignal1, buySignal2, sellSignal, holdSignal);

            when(strategyMapper.selectActiveStrategies()).thenReturn(List.of(testStrategy));
            when(strategyMapper.selectUnconsumedSignals(any(), any(), any(), any(), anyInt()))
                    .thenReturn(signals);
            when(strategyConverter.toSignalDTOList(anyList())).thenReturn(List.of());

            // When
            SignalScanResponse result = signalService.scanSignals(
                    LocalDate.now(), null, null, null, 100);

            // Then
            System.out.println("  信號摘要:");
            System.out.println("    - BUY: " + result.getSignalSummary().getBuy());
            System.out.println("    - SELL: " + result.getSignalSummary().getSell());
            System.out.println("    - HOLD: " + result.getSignalSummary().getHold());

            assertThat(result.getSignalSummary().getBuy()).isEqualTo(2);
            assertThat(result.getSignalSummary().getSell()).isEqualTo(1);
            assertThat(result.getSignalSummary().getHold()).isEqualTo(1);
        }

        private StrategySignal createSignal(String signalId, SignalType type, double confidence) {
            return StrategySignal.builder()
                    .signalId(signalId)
                    .strategyId("STG_TEST_001")
                    .stockId("2330")
                    .tradeDate(LocalDate.now())
                    .signalType(type)
                    .confidenceScore(BigDecimal.valueOf(confidence))
                    .isConsumed(false)
                    .build();
        }
    }
}
