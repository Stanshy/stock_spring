package com.chris.fin_shark.m07.engine.calculator.momentum;

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
 * Williams %R 計算器測試
 */
@DisplayName("Williams %R 計算器測試")
class WilliamsRCalculatorTest {

    private WilliamsRCalculator calculator;

    @BeforeEach
    void setUp() {
        calculator = new WilliamsRCalculator();
    }

    @Test
    @DisplayName("測試: Williams %R 範圍")
    void testWilliamsR_Range() {
        // Given
        PriceSeries series = createTestSeries(20);
        Map<String, Object> params = Map.of("period", 14);

        // When
        Map<String, Object> result = calculator.calculate(series, params);

        // Then
        assertThat(result).containsKey("willr_14");
        double willR = (double) result.get("willr_14");

        // Williams %R 範圍應為 -100 到 0
        assertThat(willR).isBetween(-100.0, 0.0);
    }

    @Test
    @DisplayName("測試: 超買信號（收盤價接近最高價）")
    void testOverbought_Signal() {
        // Given - 收盤價接近區間最高價
        PriceSeries series = createOverboughtSeries(20);
        Map<String, Object> params = Map.of("period", 14);

        // When
        Map<String, Object> result = calculator.calculate(series, params);

        // Then
        double willR = (double) result.get("willr_14");
        String signal = (String) result.get("willr_signal");

        // 收盤價接近最高價時，%R 接近 0（超買）
        assertThat(willR).isGreaterThan(-20.0);
        assertThat(signal).isEqualTo("OVERBOUGHT");
    }

    @Test
    @DisplayName("測試: 超賣信號（收盤價接近最低價）")
    void testOversold_Signal() {
        // Given - 收盤價接近區間最低價
        PriceSeries series = createOversoldSeries(20);
        Map<String, Object> params = Map.of("period", 14);

        // When
        Map<String, Object> result = calculator.calculate(series, params);

        // Then
        double willR = (double) result.get("willr_14");
        String signal = (String) result.get("willr_signal");

        // 收盤價接近最低價時，%R 接近 -100（超賣）
        assertThat(willR).isLessThan(-80.0);
        assertThat(signal).isEqualTo("OVERSOLD");
    }

    private PriceSeries createTestSeries(int days) {
        List<BigDecimal> highs = new ArrayList<>();
        List<BigDecimal> lows = new ArrayList<>();
        List<BigDecimal> closes = new ArrayList<>();
        List<LocalDate> dates = new ArrayList<>();

        for (int i = 0; i < days; i++) {
            highs.add(BigDecimal.valueOf(105 + Math.random() * 5));
            lows.add(BigDecimal.valueOf(95 + Math.random() * 5));
            closes.add(BigDecimal.valueOf(100 + Math.random() * 5));
            dates.add(LocalDate.now().minusDays(days - i));
        }

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

    private PriceSeries createOverboughtSeries(int days) {
        List<BigDecimal> highs = new ArrayList<>();
        List<BigDecimal> lows = new ArrayList<>();
        List<BigDecimal> closes = new ArrayList<>();
        List<LocalDate> dates = new ArrayList<>();

        for (int i = 0; i < days; i++) {
            highs.add(BigDecimal.valueOf(110));
            lows.add(BigDecimal.valueOf(90));
            // 收盤價接近最高價
            closes.add(BigDecimal.valueOf(109));
            dates.add(LocalDate.now().minusDays(days - i));
        }

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

    private PriceSeries createOversoldSeries(int days) {
        List<BigDecimal> highs = new ArrayList<>();
        List<BigDecimal> lows = new ArrayList<>();
        List<BigDecimal> closes = new ArrayList<>();
        List<LocalDate> dates = new ArrayList<>();

        for (int i = 0; i < days; i++) {
            highs.add(BigDecimal.valueOf(110));
            lows.add(BigDecimal.valueOf(90));
            // 收盤價接近最低價
            closes.add(BigDecimal.valueOf(91));
            dates.add(LocalDate.now().minusDays(days - i));
        }

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
