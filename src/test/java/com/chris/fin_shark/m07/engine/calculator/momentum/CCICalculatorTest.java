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
 * CCI 計算器測試
 */
@DisplayName("CCI 計算器測試")
class CCICalculatorTest {

    private CCICalculator calculator;

    @BeforeEach
    void setUp() {
        calculator = new CCICalculator();
    }

    @Test
    @DisplayName("測試: CCI 計算輸出")
    void testCCI_Output() {
        // Given
        PriceSeries series = createTestSeries(25);
        Map<String, Object> params = Map.of("period", 20);

        // When
        Map<String, Object> result = calculator.calculate(series, params);

        // Then
        assertThat(result).containsKey("cci_20");
        assertThat(result).containsKey("cci_signal");
    }

    @Test
    @DisplayName("測試: 超買信號")
    void testOverbought_Signal() {
        // Given - 價格大幅高於平均
        PriceSeries series = createOverboughtSeries(25);
        Map<String, Object> params = Map.of("period", 20);

        // When
        Map<String, Object> result = calculator.calculate(series, params);

        // Then
        double cci = (double) result.get("cci_20");
        assertThat(cci).isGreaterThan(100.0);
        assertThat(result.get("cci_signal")).isEqualTo("OVERBOUGHT");
    }

    @Test
    @DisplayName("測試: 超賣信號")
    void testOversold_Signal() {
        // Given - 價格大幅低於平均
        PriceSeries series = createOversoldSeries(25);
        Map<String, Object> params = Map.of("period", 20);

        // When
        Map<String, Object> result = calculator.calculate(series, params);

        // Then
        double cci = (double) result.get("cci_20");
        assertThat(cci).isLessThan(-100.0);
        assertThat(result.get("cci_signal")).isEqualTo("OVERSOLD");
    }

    private PriceSeries createTestSeries(int days) {
        List<BigDecimal> highs = new ArrayList<>();
        List<BigDecimal> lows = new ArrayList<>();
        List<BigDecimal> closes = new ArrayList<>();
        List<LocalDate> dates = new ArrayList<>();

        for (int i = 0; i < days; i++) {
            highs.add(BigDecimal.valueOf(102));
            lows.add(BigDecimal.valueOf(98));
            closes.add(BigDecimal.valueOf(100));
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

        // 前 20 天正常，最後幾天暴漲
        for (int i = 0; i < days - 3; i++) {
            highs.add(BigDecimal.valueOf(102));
            lows.add(BigDecimal.valueOf(98));
            closes.add(BigDecimal.valueOf(100));
            dates.add(LocalDate.now().minusDays(days - i));
        }
        // 最後 3 天大漲
        for (int i = days - 3; i < days; i++) {
            highs.add(BigDecimal.valueOf(120));
            lows.add(BigDecimal.valueOf(115));
            closes.add(BigDecimal.valueOf(118));
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

        // 前 20 天正常，最後幾天暴跌
        for (int i = 0; i < days - 3; i++) {
            highs.add(BigDecimal.valueOf(102));
            lows.add(BigDecimal.valueOf(98));
            closes.add(BigDecimal.valueOf(100));
            dates.add(LocalDate.now().minusDays(days - i));
        }
        // 最後 3 天大跌
        for (int i = days - 3; i < days; i++) {
            highs.add(BigDecimal.valueOf(85));
            lows.add(BigDecimal.valueOf(80));
            closes.add(BigDecimal.valueOf(82));
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
