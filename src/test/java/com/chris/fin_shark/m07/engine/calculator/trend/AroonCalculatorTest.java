package com.chris.fin_shark.m07.engine.calculator.trend;

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
 * Aroon 計算器測試
 */
@DisplayName("Aroon 計算器測試")
class AroonCalculatorTest {

    private AroonCalculator calculator;

    @BeforeEach
    void setUp() {
        calculator = new AroonCalculator();
    }

    @Test
    @DisplayName("測試: 強勢上漲趨勢")
    void testStrongUptrend() {
        // Given - 持續創新高
        PriceSeries series = createTrendingSeries(30, true);
        Map<String, Object> params = Map.of("period", 25);

        // When
        Map<String, Object> result = calculator.calculate(series, params);

        // Then
        assertThat(result).containsKey("aroon_up_25");
        assertThat(result).containsKey("aroon_down_25");

        double aroonUp = (double) result.get("aroon_up_25");
        double aroonDown = (double) result.get("aroon_down_25");

        // 上漲趨勢：Aroon Up 應該高，Aroon Down 應該低
        assertThat(aroonUp).isGreaterThan(70.0);
        assertThat(aroonDown).isLessThan(30.0);
    }

    @Test
    @DisplayName("測試: 強勢下跌趨勢")
    void testStrongDowntrend() {
        // Given - 持續創新低
        PriceSeries series = createTrendingSeries(30, false);
        Map<String, Object> params = Map.of("period", 25);

        // When
        Map<String, Object> result = calculator.calculate(series, params);

        // Then
        double aroonUp = (double) result.get("aroon_up_25");
        double aroonDown = (double) result.get("aroon_down_25");

        // 下跌趨勢：Aroon Down 應該高，Aroon Up 應該低
        assertThat(aroonDown).isGreaterThan(70.0);
        assertThat(aroonUp).isLessThan(30.0);
    }

    private PriceSeries createTrendingSeries(int days, boolean uptrend) {
        List<BigDecimal> highs = new ArrayList<>();
        List<BigDecimal> lows = new ArrayList<>();
        List<BigDecimal> closes = new ArrayList<>();
        List<LocalDate> dates = new ArrayList<>();

        for (int i = 0; i < days; i++) {
            double base = uptrend ? 100 + i * 2 : 100 - i * 2;
            highs.add(BigDecimal.valueOf(base + 1));
            lows.add(BigDecimal.valueOf(base - 1));
            closes.add(BigDecimal.valueOf(base));
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
