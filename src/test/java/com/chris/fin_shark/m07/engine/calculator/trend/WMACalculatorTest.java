package com.chris.fin_shark.m07.engine.calculator.trend;

import com.chris.fin_shark.m07.engine.model.PriceSeries;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * WMA 計算器測試
 */
@DisplayName("WMA 計算器測試")
class WMACalculatorTest {

    private WMACalculator calculator;

    @BeforeEach
    void setUp() {
        calculator = new WMACalculator();
    }

    @Test
    @DisplayName("測試: WMA 計算正確性")
    void testWMA_Calculation() {
        // Given - 10 天價格
        double[] prices = {100, 102, 101, 103, 105, 104, 106, 108, 107, 109};
        // WMA(5) = (105*1 + 104*2 + 106*3 + 108*4 + 107*5 + 109*6) / (1+2+3+4+5) = 手動驗算

        PriceSeries series = PriceSeries.createTest("2330", prices);
        Map<String, Object> params = Map.of("periods", List.of(5));

        // When
        Map<String, Object> result = calculator.calculate(series, params);

        // Then
        assertThat(result).containsKey("wma_5");
        double wma5 = (double) result.get("wma_5");

        // WMA 應該在價格範圍內且接近近期價格
        assertThat(wma5).isBetween(100.0, 115.0);
    }

    @Test
    @DisplayName("測試: WMA 多週期")
    void testWMA_MultiplePeriods() {
        // Given
        double[] prices = new double[25];
        for (int i = 0; i < 25; i++) {
            prices[i] = 100 + i;
        }

        PriceSeries series = PriceSeries.createTest("2330", prices);
        Map<String, Object> params = Map.of("periods", List.of(10, 20));

        // When
        Map<String, Object> result = calculator.calculate(series, params);

        // Then
        assertThat(result).containsKey("wma_10");
        assertThat(result).containsKey("wma_20");

        double wma10 = (double) result.get("wma_10");
        double wma20 = (double) result.get("wma_20");

        // 短週期 WMA 應該更接近最新價格
        assertThat(wma10).isGreaterThan(wma20);
    }
}
