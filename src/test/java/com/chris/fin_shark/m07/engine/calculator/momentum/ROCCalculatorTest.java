package com.chris.fin_shark.m07.engine.calculator.momentum;

import com.chris.fin_shark.m07.engine.model.PriceSeries;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * ROC 計算器測試
 */
@DisplayName("ROC 計算器測試")
class ROCCalculatorTest {

    private ROCCalculator calculator;

    @BeforeEach
    void setUp() {
        calculator = new ROCCalculator();
    }

    @Test
    @DisplayName("測試: ROC 計算正確性")
    void testROC_Calculation() {
        // Given - 價格從 100 漲到 110（10% 漲幅）
        double[] prices = new double[15];
        prices[0] = 100;
        for (int i = 1; i < 15; i++) {
            prices[i] = 100 + i * 0.714;  // 最終約 110
        }
        prices[14] = 110;  // 確保最後價格是 110

        PriceSeries series = PriceSeries.createTest("2330", prices);
        Map<String, Object> params = Map.of("period", 12);

        // When
        Map<String, Object> result = calculator.calculate(series, params);

        // Then
        assertThat(result).containsKey("roc_12");
        double roc = (double) result.get("roc_12");

        // ROC = (110 - 100.714) / 100.714 * 100 ≈ 正值
        assertThat(roc).isGreaterThan(0);
    }

    @Test
    @DisplayName("測試: 上漲信號")
    void testBullish_Signal() {
        // Given - 持續上漲
        double[] prices = new double[15];
        for (int i = 0; i < 15; i++) {
            prices[i] = 100 + i * 2;
        }

        PriceSeries series = PriceSeries.createTest("2330", prices);
        Map<String, Object> params = Map.of("period", 12);

        // When
        Map<String, Object> result = calculator.calculate(series, params);

        // Then
        String signal = (String) result.get("roc_signal");
        assertThat(signal).isIn("BULLISH", "STRONG_BULLISH");
    }

    @Test
    @DisplayName("測試: 下跌信號")
    void testBearish_Signal() {
        // Given - 持續下跌
        double[] prices = new double[15];
        for (int i = 0; i < 15; i++) {
            prices[i] = 100 - i * 2;
        }

        PriceSeries series = PriceSeries.createTest("2330", prices);
        Map<String, Object> params = Map.of("period", 12);

        // When
        Map<String, Object> result = calculator.calculate(series, params);

        // Then
        String signal = (String) result.get("roc_signal");
        assertThat(signal).isIn("BEARISH", "STRONG_BEARISH");
    }
}
