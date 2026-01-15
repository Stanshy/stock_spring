package com.chris.fin_shark.m07.engine.calculator.volatility;

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
 * Donchian Channel 計算器測試
 */
@DisplayName("Donchian Channel 計算器測試")
class DonchianChannelCalculatorTest {

    private DonchianChannelCalculator calculator;

    @BeforeEach
    void setUp() {
        calculator = new DonchianChannelCalculator();
    }

    @Test
    @DisplayName("測試: 通道計算正確性")
    void testChannel_Calculation() {
        // Given - 已知最高價 120，最低價 80
        PriceSeries series = createTestSeries(25, 80, 120);
        Map<String, Object> params = Map.of("period", 20);

        // When
        Map<String, Object> result = calculator.calculate(series, params);

        // Then
        assertThat(result).containsKey("donchian_upper_20");
        assertThat(result).containsKey("donchian_lower_20");
        assertThat(result).containsKey("donchian_middle_20");

        double upper = (double) result.get("donchian_upper_20");
        double lower = (double) result.get("donchian_lower_20");
        double middle = (double) result.get("donchian_middle_20");

        assertThat(upper).isEqualTo(120.0);
        assertThat(lower).isEqualTo(80.0);
        assertThat(middle).isEqualTo(100.0);  // (120 + 80) / 2
    }

    @Test
    @DisplayName("測試: 突破上軌信號")
    void testBreakoutUp_Signal() {
        // Given - 收盤價等於最高價
        PriceSeries series = createBreakoutUpSeries(25);
        Map<String, Object> params = Map.of("period", 20);

        // When
        Map<String, Object> result = calculator.calculate(series, params);

        // Then
        assertThat(result.get("donchian_signal")).isEqualTo("BREAKOUT_UP");
    }

    @Test
    @DisplayName("測試: 跌破下軌信號")
    void testBreakoutDown_Signal() {
        // Given - 收盤價等於最低價
        PriceSeries series = createBreakoutDownSeries(25);
        Map<String, Object> params = Map.of("period", 20);

        // When
        Map<String, Object> result = calculator.calculate(series, params);

        // Then
        assertThat(result.get("donchian_signal")).isEqualTo("BREAKOUT_DOWN");
    }

    private PriceSeries createTestSeries(int days, double low, double high) {
        List<BigDecimal> highs = new ArrayList<>();
        List<BigDecimal> lows = new ArrayList<>();
        List<BigDecimal> closes = new ArrayList<>();
        List<LocalDate> dates = new ArrayList<>();

        for (int i = 0; i < days; i++) {
            highs.add(BigDecimal.valueOf(high));
            lows.add(BigDecimal.valueOf(low));
            closes.add(BigDecimal.valueOf((high + low) / 2));  // 收盤價在中間
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

    private PriceSeries createBreakoutUpSeries(int days) {
        List<BigDecimal> highs = new ArrayList<>();
        List<BigDecimal> lows = new ArrayList<>();
        List<BigDecimal> closes = new ArrayList<>();
        List<LocalDate> dates = new ArrayList<>();

        for (int i = 0; i < days; i++) {
            highs.add(BigDecimal.valueOf(120));
            lows.add(BigDecimal.valueOf(80));
            // 最後一天收盤價等於最高價
            closes.add(BigDecimal.valueOf(i == days - 1 ? 120 : 100));
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

    private PriceSeries createBreakoutDownSeries(int days) {
        List<BigDecimal> highs = new ArrayList<>();
        List<BigDecimal> lows = new ArrayList<>();
        List<BigDecimal> closes = new ArrayList<>();
        List<LocalDate> dates = new ArrayList<>();

        for (int i = 0; i < days; i++) {
            highs.add(BigDecimal.valueOf(120));
            lows.add(BigDecimal.valueOf(80));
            // 最後一天收盤價等於最低價
            closes.add(BigDecimal.valueOf(i == days - 1 ? 80 : 100));
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
