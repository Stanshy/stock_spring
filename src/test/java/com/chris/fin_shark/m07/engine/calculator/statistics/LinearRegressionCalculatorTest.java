package com.chris.fin_shark.m07.engine.calculator.statistics;

import com.chris.fin_shark.m07.engine.model.PriceSeries;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

/**
 * Linear Regression 計算器測試
 */
@DisplayName("Linear Regression 計算器測試")
class LinearRegressionCalculatorTest {

    private LinearRegressionCalculator calculator;

    @BeforeEach
    void setUp() {
        calculator = new LinearRegressionCalculator();
    }

    @Test
    @DisplayName("測試: 完美線性上升")
    void testPerfectLinearUptrend() {
        // Given - 完美線性上升：y = x + 100
        double[] prices = new double[20];
        for (int i = 0; i < 20; i++) {
            prices[i] = 100 + i;
        }

        PriceSeries series = PriceSeries.createTest("2330", prices);
        Map<String, Object> params = Map.of("period", 14);

        // When
        Map<String, Object> result = calculator.calculate(series, params);

        // Then
        assertThat(result).containsKey("linreg_14");
        assertThat(result).containsKey("linreg_slope_14");
        assertThat(result).containsKey("linreg_r2_14");

        double slope = (double) result.get("linreg_slope_14");
        double r2 = (double) result.get("linreg_r2_14");

        // 斜率應為 1
        assertThat(slope).isCloseTo(1.0, within(0.01));

        // R² 應為 1（完美線性）
        assertThat(r2).isCloseTo(1.0, within(0.01));

        // 信號應為強勢上漲
        assertThat(result.get("linreg_signal")).isEqualTo("STRONG_UPTREND");
    }

    @Test
    @DisplayName("測試: 完美線性下降")
    void testPerfectLinearDowntrend() {
        // Given - 完美線性下降：y = -x + 100
        double[] prices = new double[20];
        for (int i = 0; i < 20; i++) {
            prices[i] = 100 - i;
        }

        PriceSeries series = PriceSeries.createTest("2330", prices);
        Map<String, Object> params = Map.of("period", 14);

        // When
        Map<String, Object> result = calculator.calculate(series, params);

        // Then
        double slope = (double) result.get("linreg_slope_14");
        double r2 = (double) result.get("linreg_r2_14");

        // 斜率應為 -1
        assertThat(slope).isCloseTo(-1.0, within(0.01));

        // R² 應為 1（完美線性）
        assertThat(r2).isCloseTo(1.0, within(0.01));

        // 信號應為強勢下跌
        assertThat(result.get("linreg_signal")).isEqualTo("STRONG_DOWNTREND");
    }

    @Test
    @DisplayName("測試: 無趨勢（水平線）")
    void testNoTrend() {
        // Given - 水平線，所有價格相同
        double[] prices = new double[20];
        for (int i = 0; i < 20; i++) {
            prices[i] = 100;
        }

        PriceSeries series = PriceSeries.createTest("2330", prices);
        Map<String, Object> params = Map.of("period", 14);

        // When
        Map<String, Object> result = calculator.calculate(series, params);

        // Then
        double slope = (double) result.get("linreg_slope_14");

        // 斜率應為 0
        assertThat(slope).isCloseTo(0.0, within(0.01));
    }

    @Test
    @DisplayName("測試: 弱趨勢（高波動）")
    void testWeakTrend() {
        // Given - 高波動，低 R²
        double[] prices = {100, 120, 95, 115, 90, 110, 85, 105, 80, 100,
                           120, 90, 110, 85, 105, 80, 100, 120, 90, 110};

        PriceSeries series = PriceSeries.createTest("2330", prices);
        Map<String, Object> params = Map.of("period", 14);

        // When
        Map<String, Object> result = calculator.calculate(series, params);

        // Then
        double r2 = (double) result.get("linreg_r2_14");

        // R² 應該較低（低於 0.5）
        assertThat(r2).isLessThan(0.5);
        assertThat(result.get("linreg_signal")).isEqualTo("WEAK_TREND");
    }
}
