package com.chris.fin_shark.m07.engine.calculator.support;

import com.chris.fin_shark.m07.engine.model.PriceSeries;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Pivot Points 計算器測試
 */
@DisplayName("Pivot Points 計算器測試")
class PivotPointsCalculatorTest {

    private PivotPointsCalculator calculator;

    @BeforeEach
    void setUp() {
        calculator = new PivotPointsCalculator();
    }

    @Test
    @DisplayName("測試: 標準 Pivot Points 計算")
    void testStandardPivot_Calculation() {
        // Given - 前一天 High=110, Low=90, Close=100
        PriceSeries series = createTestSeries(110, 90, 100);
        Map<String, Object> params = Map.of("type", "standard");

        // When
        Map<String, Object> result = calculator.calculate(series, params);

        // Then
        // PP = (110 + 90 + 100) / 3 = 100
        assertThat(result.get("pivot_pp")).isEqualTo(100.0);

        // R1 = 2 * 100 - 90 = 110
        assertThat(result.get("pivot_r1")).isEqualTo(110.0);

        // S1 = 2 * 100 - 110 = 90
        assertThat(result.get("pivot_s1")).isEqualTo(90.0);

        // R2 = 100 + (110 - 90) = 120
        assertThat(result.get("pivot_r2")).isEqualTo(120.0);

        // S2 = 100 - (110 - 90) = 80
        assertThat(result.get("pivot_s2")).isEqualTo(80.0);
    }

    @Test
    @DisplayName("測試: Fibonacci Pivot Points")
    void testFibonacciPivot_Calculation() {
        // Given
        PriceSeries series = createTestSeries(110, 90, 100);
        Map<String, Object> params = Map.of("type", "fibonacci");

        // When
        Map<String, Object> result = calculator.calculate(series, params);

        // Then
        double pp = (double) result.get("pivot_pp");
        assertThat(pp).isEqualTo(100.0);

        // Fib R1 = PP + 0.382 * range = 100 + 0.382 * 20 = 107.64
        double r1 = (double) result.get("pivot_r1");
        assertThat(r1).isCloseTo(107.64, org.assertj.core.api.Assertions.within(0.01));
    }

    @Test
    @DisplayName("測試: 價格高於 PP 信號")
    void testAbovePP_Signal() {
        // Given - 今日收盤價 105，高於 PP (100)
        PriceSeries series = createTestSeriesWithCurrentClose(110, 90, 100, 105);
        Map<String, Object> params = Map.of("type", "standard");

        // When
        Map<String, Object> result = calculator.calculate(series, params);

        // Then
        assertThat(result.get("pivot_signal")).isEqualTo("ABOVE_PP");
    }

    private PriceSeries createTestSeries(double prevHigh, double prevLow, double prevClose) {
        return createTestSeriesWithCurrentClose(prevHigh, prevLow, prevClose, prevClose);
    }

    private PriceSeries createTestSeriesWithCurrentClose(double prevHigh, double prevLow,
                                                          double prevClose, double currentClose) {
        List<BigDecimal> highs = new ArrayList<>();
        List<BigDecimal> lows = new ArrayList<>();
        List<BigDecimal> closes = new ArrayList<>();
        List<LocalDate> dates = new ArrayList<>();

        // 前一天
        highs.add(BigDecimal.valueOf(prevHigh));
        lows.add(BigDecimal.valueOf(prevLow));
        closes.add(BigDecimal.valueOf(prevClose));
        dates.add(LocalDate.now().minusDays(1));

        // 今天
        highs.add(BigDecimal.valueOf(prevHigh));
        lows.add(BigDecimal.valueOf(prevLow));
        closes.add(BigDecimal.valueOf(currentClose));
        dates.add(LocalDate.now());

        return PriceSeries.builder()
                .stockId("2330")
                .dates(dates)
                .high(highs)
                .low(lows)
                .close(closes)
                .open(closes)
                .volume(new ArrayList<>())
                .build();
    }
}
