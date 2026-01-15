package com.chris.fin_shark.m07.engine.calculator.volume;

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
 * CMF 計算器測試
 */
@DisplayName("CMF 計算器測試")
class CMFCalculatorTest {

    private CMFCalculator calculator;

    @BeforeEach
    void setUp() {
        calculator = new CMFCalculator();
    }

    @Test
    @DisplayName("測試: CMF 範圍")
    void testCMF_Range() {
        // Given
        PriceSeries series = createTestSeries(25);
        Map<String, Object> params = Map.of("period", 20);

        // When
        Map<String, Object> result = calculator.calculate(series, params);

        // Then
        assertThat(result).containsKey("cmf_20");
        double cmf = (double) result.get("cmf_20");

        // CMF 範圍應在 -1 到 +1 之間
        assertThat(cmf).isBetween(-1.0, 1.0);
    }

    @Test
    @DisplayName("測試: 強勁買盤信號")
    void testStrongBuying_Signal() {
        // Given - 收盤價持續接近最高價
        PriceSeries series = createBuyingSeries(25);
        Map<String, Object> params = Map.of("period", 20);

        // When
        Map<String, Object> result = calculator.calculate(series, params);

        // Then
        double cmf = (double) result.get("cmf_20");
        assertThat(cmf).isGreaterThan(0);
    }

    @Test
    @DisplayName("測試: 強勁賣盤信號")
    void testStrongSelling_Signal() {
        // Given - 收盤價持續接近最低價
        PriceSeries series = createSellingSeries(25);
        Map<String, Object> params = Map.of("period", 20);

        // When
        Map<String, Object> result = calculator.calculate(series, params);

        // Then
        double cmf = (double) result.get("cmf_20");
        assertThat(cmf).isLessThan(0);
    }

    private PriceSeries createTestSeries(int days) {
        List<BigDecimal> highs = new ArrayList<>();
        List<BigDecimal> lows = new ArrayList<>();
        List<BigDecimal> closes = new ArrayList<>();
        List<LocalDate> dates = new ArrayList<>();
        List<Long> volumes = new ArrayList<>();

        for (int i = 0; i < days; i++) {
            highs.add(BigDecimal.valueOf(110));
            lows.add(BigDecimal.valueOf(90));
            closes.add(BigDecimal.valueOf(100));  // 收盤在中間
            dates.add(LocalDate.now().minusDays(days - i));
            volumes.add(1000000L);
        }

        return PriceSeries.builder()
                .stockId("2330")
                .dates(dates)
                .high(highs)
                .low(lows)
                .close(closes)
                .open(closes)
                .volume(volumes)
                .build();
    }

    private PriceSeries createBuyingSeries(int days) {
        List<BigDecimal> highs = new ArrayList<>();
        List<BigDecimal> lows = new ArrayList<>();
        List<BigDecimal> closes = new ArrayList<>();
        List<LocalDate> dates = new ArrayList<>();
        List<Long> volumes = new ArrayList<>();

        for (int i = 0; i < days; i++) {
            highs.add(BigDecimal.valueOf(110));
            lows.add(BigDecimal.valueOf(90));
            closes.add(BigDecimal.valueOf(108));  // 收盤接近最高價
            dates.add(LocalDate.now().minusDays(days - i));
            volumes.add(1000000L);
        }

        return PriceSeries.builder()
                .stockId("2330")
                .dates(dates)
                .high(highs)
                .low(lows)
                .close(closes)
                .open(closes)
                .volume(volumes)
                .build();
    }

    private PriceSeries createSellingSeries(int days) {
        List<BigDecimal> highs = new ArrayList<>();
        List<BigDecimal> lows = new ArrayList<>();
        List<BigDecimal> closes = new ArrayList<>();
        List<LocalDate> dates = new ArrayList<>();
        List<Long> volumes = new ArrayList<>();

        for (int i = 0; i < days; i++) {
            highs.add(BigDecimal.valueOf(110));
            lows.add(BigDecimal.valueOf(90));
            closes.add(BigDecimal.valueOf(92));  // 收盤接近最低價
            dates.add(LocalDate.now().minusDays(days - i));
            volumes.add(1000000L);
        }

        return PriceSeries.builder()
                .stockId("2330")
                .dates(dates)
                .high(highs)
                .low(lows)
                .close(closes)
                .open(closes)
                .volume(volumes)
                .build();
    }
}
