package com.chris.fin_shark.m07.engine.calculator.volatility;

import com.chris.fin_shark.m07.engine.model.PriceSeries;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * BBands 計算器測試
 *
 * @author chris
 * @since 1.0.0
 */
@DisplayName("BBands 計算器測試")
class BBandsCalculatorTest {

    private BBandsCalculator calculator;

    @BeforeEach
    void setUp() {
        calculator = new BBandsCalculator();
        System.out.println("\n========================================");
        System.out.println("🧪 初始化 BBands 計算器測試");
        System.out.println("========================================\n");
    }

    @Test
    @DisplayName("測試: BBands 計算正確性")
    void testBBands_Calculation() {
        System.out.println("📝 測試: BBands 計算正確性");

        // Given - 30 天資料
        double[] prices = new double[30];
        for (int i = 0; i < 30; i++) {
            prices[i] = 100.0 + Math.sin(i * 0.3) * 5;  // 波動價格
        }

        System.out.println("📥 輸入資料:");
        System.out.println("  - 資料天數: 30");
        System.out.println("  - 計算參數: period=20, std_dev=2");

        PriceSeries series = PriceSeries.createTest("2330", prices);
        Map<String, Object> params = Map.of("period", 20, "std_dev", 2.0);

        // When
        System.out.println("\n🔧 執行計算...");
        Map<String, Object> result = calculator.calculate(series, params);

        // Then
        System.out.println("\n📤 計算結果:");
        @SuppressWarnings("unchecked")
        Map<String, Object> bbands = (Map<String, Object>) result.get("bbands");

        System.out.println("  - Upper: " + bbands.get("upper"));
        System.out.println("  - Middle: " + bbands.get("middle"));
        System.out.println("  - Lower: " + bbands.get("lower"));
        System.out.println("  - %B: " + bbands.get("percent_b"));
        System.out.println("  - Bandwidth: " + bbands.get("bandwidth"));
        System.out.println("  - 信號: " + bbands.get("signal"));

        assertThat(result).containsKey("bbands");
        assertThat(bbands).containsKeys("upper", "middle", "lower", "percent_b", "bandwidth", "signal");

        // 驗證：上軌 > 中軌 > 下軌
        double upper = (double) bbands.get("upper");
        double middle = (double) bbands.get("middle");
        double lower = (double) bbands.get("lower");

        assertThat(upper).isGreaterThan(middle);
        assertThat(middle).isGreaterThan(lower);

        System.out.println("\n✅ 測試通過: BBands 計算正確");
    }

    @Test
    @DisplayName("測試: 突破上軌信號")
    void testAboveUpper_Signal() {
        System.out.println("\n📝 測試: 突破上軌信號");

        // Given - 穩定後突然上漲
        double[] prices = new double[30];
        for (int i = 0; i < 25; i++) {
            prices[i] = 100.0;  // 前 25 天穩定
        }
        for (int i = 25; i < 30; i++) {
            prices[i] = 115.0;  // 後 5 天大漲
        }

        System.out.println("📥 輸入資料:");
        System.out.println("  - 前 25 天: 穩定 100");
        System.out.println("  - 後 5 天: 大漲到 115");
        System.out.println("  - 預期: 突破上軌");

        PriceSeries series = PriceSeries.createTest("2330", prices);
        Map<String, Object> params = Map.of("period", 20, "std_dev", 2.0);

        // When
        System.out.println("\n🔧 執行計算...");
        Map<String, Object> result = calculator.calculate(series, params);

        // Then
        @SuppressWarnings("unchecked")
        Map<String, Object> bbands = (Map<String, Object>) result.get("bbands");
        String signal = (String) bbands.get("signal");

        System.out.println("\n📤 計算結果:");
        System.out.println("  - 信號: " + signal);
        System.out.println("  - %B: " + bbands.get("percent_b"));

        assertThat(signal).isIn("ABOVE_UPPER", "NEAR_UPPER");
        assertThat((double) bbands.get("percent_b")).isGreaterThan(0.8);

        System.out.println("\n✅ 測試通過: 正確識別突破上軌");
    }
}
