package com.chris.fin_shark.m07.engine.calculator.momentum;

import com.chris.fin_shark.m07.engine.model.PriceSeries;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * RSI 計算器測試
 *
 * @author chris
 * @since 1.0.0
 */
@DisplayName("RSI 計算器測試")
class RSICalculatorTest {

    private RSICalculator calculator;

    @BeforeEach
    void setUp() {
        calculator = new RSICalculator();
        System.out.println("\n========================================");
        System.out.println("🧪 初始化 RSI 計算器測試");
        System.out.println("========================================\n");
    }

    @Test
    @DisplayName("測試: RSI 計算正確性")
    void testRSI_Calculation() {
        System.out.println("📝 測試: RSI 計算正確性");

        // Given - 20 天價格（模擬上漲趨勢）
        double[] prices = {
                100, 102, 104, 103, 105,  // 漲多跌少
                107, 108, 106, 109, 111,  // 持續上漲
                110, 112, 115, 114, 116,  // 漲勢放緩
                118, 120, 119, 121, 123   // 繼續上漲
        };

        System.out.println("📥 輸入資料:");
        System.out.println("  - 資料天數: " + prices.length);
        System.out.println("  - 計算週期: 14");

        PriceSeries series = PriceSeries.createTest("2330", prices);
        Map<String, Object> params = Map.of("period", 14);

        // When
        System.out.println("\n🔧 執行計算...");
        Map<String, Object> result = calculator.calculate(series, params);

        // Then
        System.out.println("\n📤 計算結果:");
        System.out.println("  - RSI_14: " + result.get("rsi_14"));
        System.out.println("  - 信號: " + result.get("rsi_signal"));

        assertThat(result).containsKey("rsi_14");
        assertThat(result).containsKey("rsi_signal");

        double rsi = (double) result.get("rsi_14");
        assertThat(rsi).isBetween(0.0, 100.0);

        // 上漲趨勢，RSI 應該 > 50
        assertThat(rsi).isGreaterThan(50.0);

        System.out.println("\n✅ 測試通過: RSI 計算正確");
    }

    @Test
    @DisplayName("測試: 超買信號")
    void testOverbought_Signal() {
        System.out.println("\n📝 測試: 超買信號");

        // Given - 強烈上漲趨勢（RSI 應該 > 70）
        double[] prices = new double[20];
        for (int i = 0; i < 20; i++) {
            prices[i] = 100 + (i * 2);  // 每天漲 2 元
        }

        System.out.println("📥 輸入資料:");
        System.out.println("  - 趨勢: 持續上漲");
        System.out.println("  - 預期: 超買信號");

        PriceSeries series = PriceSeries.createTest("2330", prices);
        Map<String, Object> params = Map.of("period", 14);

        // When
        System.out.println("\n🔧 執行計算...");
        Map<String, Object> result = calculator.calculate(series, params);

        // Then
        System.out.println("\n📤 計算結果:");
        System.out.println("  - RSI_14: " + result.get("rsi_14"));
        System.out.println("  - 信號: " + result.get("rsi_signal"));

        double rsi = (double) result.get("rsi_14");
        String signal = (String) result.get("rsi_signal");

        assertThat(rsi).isGreaterThan(70.0);
        assertThat(signal).isEqualTo("OVERBOUGHT");

        System.out.println("\n✅ 測試通過: 正確識別超買");
    }

    @Test
    @DisplayName("測試: 超賣信號")
    void testOversold_Signal() {
        System.out.println("\n📝 測試: 超賣信號");

        // Given - 強烈下跌趨勢（RSI 應該 < 30）
        double[] prices = new double[20];
        for (int i = 0; i < 20; i++) {
            prices[i] = 100 - (i * 2);  // 每天跌 2 元
        }

        System.out.println("📥 輸入資料:");
        System.out.println("  - 趨勢: 持續下跌");
        System.out.println("  - 預期: 超賣信號");

        PriceSeries series = PriceSeries.createTest("2330", prices);
        Map<String, Object> params = Map.of("period", 14);

        // When
        System.out.println("\n🔧 執行計算...");
        Map<String, Object> result = calculator.calculate(series, params);

        // Then
        System.out.println("\n📤 計算結果:");
        System.out.println("  - RSI_14: " + result.get("rsi_14"));
        System.out.println("  - 信號: " + result.get("rsi_signal"));

        double rsi = (double) result.get("rsi_14");
        String signal = (String) result.get("rsi_signal");

        assertThat(rsi).isLessThan(30.0);
        assertThat(signal).isEqualTo("OVERSOLD");

        System.out.println("\n✅ 測試通過: 正確識別超賣");
    }
}
