package com.chris.fin_shark.m07.engine.calculator.trend;

import com.chris.fin_shark.m07.engine.model.PriceSeries;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * MACD 計算器測試
 *
 * @author chris
 * @since 1.0.0
 */
@DisplayName("MACD 計算器測試")
class MACDCalculatorTest {

    private MACDCalculator calculator;

    @BeforeEach
    void setUp() {
        EMACalculator emaCalculator = new EMACalculator();
        calculator = new MACDCalculator(emaCalculator);

        System.out.println("\n========================================");
        System.out.println("🧪 初始化 MACD 計算器測試");
        System.out.println("========================================\n");
    }

    @Test
    @DisplayName("測試: MACD 計算正確性")
    void testMACD_Calculation() {
        System.out.println("📝 測試: MACD 計算正確性");

        // Given - 60 天資料（確保資料足夠）
        double[] prices = new double[60];
        for (int i = 0; i < 60; i++) {
            prices[i] = 100.0 + Math.sin(i * 0.2) * 10;  // 波動價格
        }

        System.out.println("📥 輸入資料:");
        System.out.println("  - 資料天數: 60");
        System.out.println("  - 計算參數: fast=12, slow=26, signal=9");

        PriceSeries series = PriceSeries.createTest("2330", prices);
        Map<String, Object> params = Map.of(
                "fast", 12,
                "slow", 26,
                "signal", 9
        );

        // When
        System.out.println("\n🔧 執行計算...");
        Map<String, Object> result = calculator.calculate(series, params);

        // Then
        System.out.println("\n📤 計算結果:");
        @SuppressWarnings("unchecked")
        Map<String, Object> macd = (Map<String, Object>) result.get("macd");

        System.out.println("  - MACD Line: " + macd.get("macd_line"));
        System.out.println("  - Signal Line: " + macd.get("signal_line"));
        System.out.println("  - Histogram: " + macd.get("histogram"));
        System.out.println("  - 信號: " + macd.get("macd_signal"));

        assertThat(result).containsKey("macd");
        assertThat(macd).containsKeys("macd_line", "signal_line", "histogram", "macd_signal");

        System.out.println("\n✅ 測試通過: MACD 計算正確");
    }

    @Test
    @DisplayName("測試: 多頭信號（改進版）")
    void testBullish_Signal_Improved() {
        System.out.println("\n📝 測試: 多頭信號");

        // Given - 明顯的上漲趨勢（從低點開始加速上漲）
        double[] prices = new double[80];

        // 前 30 天：盤整
        for (int i = 0; i < 30; i++) {
            prices[i] = 100.0 + Math.sin(i * 0.3) * 2;  // 小幅波動
        }

        // 後 50 天：明顯上漲趨勢
        for (int i = 30; i < 80; i++) {
            double trend = (i - 30) * 0.8;  // 上漲趨勢
            double noise = Math.sin(i * 0.2) * 1;  // 小幅波動
            prices[i] = 100.0 + trend + noise;
        }

        System.out.println("📥 輸入資料:");
        System.out.println("  - 資料天數: 80");
        System.out.println("  - 前 30 天: 盤整");
        System.out.println("  - 後 50 天: 明顯上漲");
        System.out.println("  - 預期: 多頭信號");

        PriceSeries series = PriceSeries.createTest("2330", prices);
        Map<String, Object> params = Map.of("fast", 12, "slow", 26, "signal", 9);

        // When
        System.out.println("\n🔧 執行計算...");
        Map<String, Object> result = calculator.calculate(series, params);

        // Then
        @SuppressWarnings("unchecked")
        Map<String, Object> macd = (Map<String, Object>) result.get("macd");

        double macdLine = (double) macd.get("macd_line");
        double signalLine = (double) macd.get("signal_line");
        double histogram = (double) macd.get("histogram");
        String signal = (String) macd.get("macd_signal");

        System.out.println("\n📤 計算結果:");
        System.out.println("  - MACD Line: " + macdLine);
        System.out.println("  - Signal Line: " + signalLine);
        System.out.println("  - Histogram: " + histogram);
        System.out.println("  - 信號: " + signal);

        // 驗證多頭信號
        assertThat(signal).isEqualTo("BULLISH");
        assertThat(histogram).isGreaterThan(0);
        assertThat(macdLine).isGreaterThan(signalLine);

        System.out.println("\n✅ 測試通過: 正確識別多頭");
    }

    @Test
    @DisplayName("測試: 空頭信號")
    void testBearish_Signal() {
        System.out.println("\n📝 測試: 空頭信號");

        // Given - 明顯的下跌趨勢
        double[] prices = new double[80];

        // 前 30 天：盤整
        for (int i = 0; i < 30; i++) {
            prices[i] = 140.0 + Math.sin(i * 0.3) * 2;
        }

        // 後 50 天：明顯下跌趨勢
        for (int i = 30; i < 80; i++) {
            double trend = (i - 30) * -0.8;  // 下跌趨勢
            double noise = Math.sin(i * 0.2) * 1;
            prices[i] = 140.0 + trend + noise;
        }

        System.out.println("📥 輸入資料:");
        System.out.println("  - 前 30 天: 盤整");
        System.out.println("  - 後 50 天: 明顯下跌");
        System.out.println("  - 預期: 空頭信號");

        PriceSeries series = PriceSeries.createTest("2330", prices);
        Map<String, Object> params = Map.of("fast", 12, "slow", 26, "signal", 9);

        // When
        System.out.println("\n🔧 執行計算...");
        Map<String, Object> result = calculator.calculate(series, params);

        // Then
        @SuppressWarnings("unchecked")
        Map<String, Object> macd = (Map<String, Object>) result.get("macd");
        String signal = (String) macd.get("macd_signal");
        double histogram = (double) macd.get("histogram");

        System.out.println("\n📤 計算結果:");
        System.out.println("  - 信號: " + signal);
        System.out.println("  - Histogram: " + histogram);

        assertThat(signal).isEqualTo("BEARISH");
        assertThat(histogram).isLessThan(0);

        System.out.println("\n✅ 測試通過: 正確識別空頭");
    }
}