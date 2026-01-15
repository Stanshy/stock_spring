package com.chris.fin_shark.m09.engine.calculator.institutional;

import com.chris.fin_shark.m09.engine.model.ChipSeries;
import com.chris.fin_shark.m09.enums.ChipCategory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 連續買賣超天數計算器測試
 *
 * @author chris
 * @since 1.0.0
 */
@DisplayName("連續買賣超天數計算器測試")
class ContinuousDaysCalculatorTest {

    private ContinuousDaysCalculator calculator;

    @BeforeEach
    void setUp() {
        calculator = new ContinuousDaysCalculator();
        System.out.println("\n========================================");
        System.out.println("  初始化 連續買賣超天數計算器測試");
        System.out.println("========================================\n");
    }

    @Test
    @DisplayName("測試: 基本資訊驗證")
    void testMetadata() {
        System.out.println("  測試: 基本資訊驗證");

        assertThat(calculator.getName()).isEqualTo("CONTINUOUS_DAYS");
        assertThat(calculator.getCategory()).isEqualTo(ChipCategory.INSTITUTIONAL);
        assertThat(calculator.getMetadata().getNameZh()).isEqualTo("連續買賣超天數");

        System.out.println("  測試通過: 基本資訊正確");
    }

    @Test
    @DisplayName("測試: 外資連續買超 5 天")
    void testForeignContinuousBuy() {
        System.out.println("  測試: 外資連續買超 5 天");

        // Given - 最後 5 天連續買超
        long[] foreignNetData = {
                -500000, -200000, -100000,   // 前 3 天賣超
                500000, 800000, 300000, 1000000, 2000000  // 最後 5 天連續買超
        };
        long[] trustNetData = new long[8];
        long[] dealerNetData = new long[8];

        System.out.println("  輸入資料:");
        System.out.println("    - 資料天數: " + foreignNetData.length);
        System.out.println("    - 最後 5 天: 連續買超");

        ChipSeries series = ChipSeries.createTestInstitutional("2330", foreignNetData, trustNetData, dealerNetData);

        // When
        Map<String, Object> result = calculator.calculate(series, Map.of());

        // Then
        System.out.println("\n  計算結果:");
        System.out.println("    - foreign_continuous_days: " + result.get("foreign_continuous_days"));

        assertThat(result.get("foreign_continuous_days")).isEqualTo(5);

        System.out.println("\n  測試通過: 外資連續買超天數計算正確");
    }

    @Test
    @DisplayName("測試: 外資連續賣超 3 天")
    void testForeignContinuousSell() {
        System.out.println("  測試: 外資連續賣超 3 天");

        // Given - 最後 3 天連續賣超
        long[] foreignNetData = {
                500000, 800000, 300000,   // 前 3 天買超
                -100000, -500000, -800000 // 最後 3 天連續賣超
        };
        long[] trustNetData = new long[6];
        long[] dealerNetData = new long[6];

        System.out.println("  輸入資料:");
        System.out.println("    - 資料天數: " + foreignNetData.length);
        System.out.println("    - 最後 3 天: 連續賣超");

        ChipSeries series = ChipSeries.createTestInstitutional("2330", foreignNetData, trustNetData, dealerNetData);

        // When
        Map<String, Object> result = calculator.calculate(series, Map.of());

        // Then
        System.out.println("\n  計算結果:");
        System.out.println("    - foreign_continuous_days: " + result.get("foreign_continuous_days"));

        // 連續賣超返回負數
        assertThat(result.get("foreign_continuous_days")).isEqualTo(-3);

        System.out.println("\n  測試通過: 外資連續賣超天數計算正確");
    }

    @Test
    @DisplayName("測試: 三大法人同買 (BULLISH)")
    void testInstitutionalAgreement_Bullish() {
        System.out.println("  測試: 三大法人同買 (BULLISH)");

        // Given - 最後一天三大法人都買超
        long[] foreignNetData = {500000, 800000, 1000000};   // 外資買超
        long[] trustNetData = {100000, 200000, 300000};      // 投信買超
        long[] dealerNetData = {50000, 80000, 100000};       // 自營買超

        System.out.println("  輸入資料:");
        System.out.println("    - 外資最後一天: " + foreignNetData[2] + " (買超)");
        System.out.println("    - 投信最後一天: " + trustNetData[2] + " (買超)");
        System.out.println("    - 自營最後一天: " + dealerNetData[2] + " (買超)");

        ChipSeries series = ChipSeries.createTestInstitutional("2330", foreignNetData, trustNetData, dealerNetData);

        // When
        Map<String, Object> result = calculator.calculate(series, Map.of());

        // Then
        System.out.println("\n  計算結果:");
        System.out.println("    - institutional_agreement: " + result.get("institutional_agreement"));

        assertThat(result.get("institutional_agreement")).isEqualTo("BULLISH");

        System.out.println("\n  測試通過: 三大法人同買識別正確");
    }

    @Test
    @DisplayName("測試: 三大法人同賣 (BEARISH)")
    void testInstitutionalAgreement_Bearish() {
        System.out.println("  測試: 三大法人同賣 (BEARISH)");

        // Given - 最後一天三大法人都賣超
        long[] foreignNetData = {-500000, -800000, -1000000};   // 外資賣超
        long[] trustNetData = {-100000, -200000, -300000};      // 投信賣超
        long[] dealerNetData = {-50000, -80000, -100000};       // 自營賣超

        System.out.println("  輸入資料:");
        System.out.println("    - 外資最後一天: " + foreignNetData[2] + " (賣超)");
        System.out.println("    - 投信最後一天: " + trustNetData[2] + " (賣超)");
        System.out.println("    - 自營最後一天: " + dealerNetData[2] + " (賣超)");

        ChipSeries series = ChipSeries.createTestInstitutional("2330", foreignNetData, trustNetData, dealerNetData);

        // When
        Map<String, Object> result = calculator.calculate(series, Map.of());

        // Then
        System.out.println("\n  計算結果:");
        System.out.println("    - institutional_agreement: " + result.get("institutional_agreement"));

        assertThat(result.get("institutional_agreement")).isEqualTo("BEARISH");

        System.out.println("\n  測試通過: 三大法人同賣識別正確");
    }

    @Test
    @DisplayName("測試: 法人方向分歧 (MIXED)")
    void testInstitutionalAgreement_Mixed() {
        System.out.println("  測試: 法人方向分歧 (MIXED)");

        // Given - 最後一天法人方向不一致
        long[] foreignNetData = {500000, 800000, 1000000};    // 外資買超
        long[] trustNetData = {100000, 200000, 300000};       // 投信買超
        long[] dealerNetData = {50000, -80000, -100000};      // 自營賣超

        System.out.println("  輸入資料:");
        System.out.println("    - 外資最後一天: " + foreignNetData[2] + " (買超)");
        System.out.println("    - 投信最後一天: " + trustNetData[2] + " (買超)");
        System.out.println("    - 自營最後一天: " + dealerNetData[2] + " (賣超)");

        ChipSeries series = ChipSeries.createTestInstitutional("2330", foreignNetData, trustNetData, dealerNetData);

        // When
        Map<String, Object> result = calculator.calculate(series, Map.of());

        // Then
        System.out.println("\n  計算結果:");
        System.out.println("    - institutional_agreement: " + result.get("institutional_agreement"));

        assertThat(result.get("institutional_agreement")).isEqualTo("MIXED");

        System.out.println("\n  測試通過: 法人方向分歧識別正確");
    }

    @Test
    @DisplayName("測試: 最後一天為零的處理")
    void testZeroLastDay() {
        System.out.println("  測試: 最後一天為零的處理");

        // Given - 最後一天外資買賣超為 0
        long[] foreignNetData = {500000, 800000, 0};
        long[] trustNetData = new long[3];
        long[] dealerNetData = new long[3];

        ChipSeries series = ChipSeries.createTestInstitutional("2330", foreignNetData, trustNetData, dealerNetData);

        // When
        Map<String, Object> result = calculator.calculate(series, Map.of());

        // Then
        System.out.println("  計算結果:");
        System.out.println("    - foreign_continuous_days: " + result.get("foreign_continuous_days"));

        // 最後一天為 0，連續天數應為 0
        assertThat(result.get("foreign_continuous_days")).isEqualTo(0);

        System.out.println("\n  測試通過: 零值處理正確");
    }
}
