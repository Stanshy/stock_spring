package com.chris.fin_shark.m07.engine.calculator.trend;

import com.chris.fin_shark.m07.engine.DefaultIndicatorEngine;
import com.chris.fin_shark.m07.engine.IndicatorEngine;
import com.chris.fin_shark.m07.engine.IndicatorPlan;
import com.chris.fin_shark.m07.engine.IndicatorResult;
import com.chris.fin_shark.m07.engine.model.PriceSeries;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

/**
 * MA 計算器測試
 *
 * @author chris
 * @since 1.0.0
 */
@DisplayName("MA 計算器測試")
class MACalculatorTest {

    private MACalculator calculator;
    private IndicatorEngine engine;

    @BeforeEach
    void setUp() {
        calculator = new MACalculator();
        engine = new DefaultIndicatorEngine(List.of(calculator));

        System.out.println("\n========================================");
        System.out.println("🧪 初始化 MA 計算器測試");
        System.out.println("========================================\n");
    }

    @Test
    @DisplayName("測試: MA5 計算正確性")
    void testMA5_Calculation() {
        System.out.println("📝 測試: MA5 計算正確性");

        // Given - 5 天收盤價
        double[] prices = {100.0, 102.0, 101.0, 103.0, 105.0};
        double expected = (100.0 + 102.0 + 101.0 + 103.0 + 105.0) / 5.0;  // 102.2

        System.out.println("📥 輸入資料:");
        System.out.println("  - 收盤價: " + java.util.Arrays.toString(prices));
        System.out.println("  - 預期 MA5: " + expected);

        PriceSeries series = PriceSeries.createTest("2330", prices);
        Map<String, Object> params = Map.of("periods", List.of(5));

        // When
        System.out.println("\n🔧 執行計算...");
        Map<String, Object> result = calculator.calculate(series, params);

        // Then
        System.out.println("\n📤 計算結果:");
        System.out.println("  - MA5: " + result.get("ma5"));

        assertThat(result).containsKey("ma5");
        assertThat((Double) result.get("ma5")).isEqualTo(expected);

        System.out.println("\n✅ 測試通過: MA5 計算正確");
    }

    @Test
    @DisplayName("測試: 多週期 MA 計算")
    void testMultiplePeriods() {
        System.out.println("\n📝 測試: 多週期 MA 計算");

        // Given - 60 天資料
        double[] prices = new double[60];
        for (int i = 0; i < 60; i++) {
            prices[i] = 100.0 + i;  // 100, 101, 102, ..., 159
        }

        System.out.println("📥 輸入資料:");
        System.out.println("  - 資料天數: 60");
        System.out.println("  - 計算週期: MA5, MA20, MA60");

        PriceSeries series = PriceSeries.createTest("2330", prices);
        Map<String, Object> params = Map.of("periods", List.of(5, 20, 60));

        // When
        System.out.println("\n🔧 執行計算...");
        Map<String, Object> result = calculator.calculate(series, params);

        // Then
        System.out.println("\n📤 計算結果:");
        System.out.println("  - MA5: " + result.get("ma5"));
        System.out.println("  - MA20: " + result.get("ma20"));
        System.out.println("  - MA60: " + result.get("ma60"));

        assertThat(result).containsKeys("ma5", "ma20", "ma60");
        assertThat((Double) result.get("ma5")).isEqualTo(157.0);   // (155+156+157+158+159)/5
        assertThat((Double) result.get("ma20")).isEqualTo(149.5);  // (140+...+159)/20
        assertThat((Double) result.get("ma60")).isEqualTo(129.5);  // (100+...+159)/60

        System.out.println("\n✅ 測試通過: 多週期 MA 計算正確");
    }

    @Test
    @DisplayName("測試: 資料不足應產生警告（引擎層面）")
    void testInsufficientData_EngineLevel() {
        System.out.println("\n📝 測試: 資料不足應產生警告（引擎層面）");

        // Given - 只有 3 天資料
        double[] prices = {100.0, 102.0, 101.0};

        System.out.println("📥 輸入資料:");
        System.out.println("  - 收盤價天數: 3");
        System.out.println("  - 要求計算: MA5");
        System.out.println("  - 預期: Engine 產生警告");

        PriceSeries series = PriceSeries.createTest("2330", prices);

        IndicatorPlan plan = IndicatorPlan.builder()
                .indicators(Map.of("MA", Map.of("periods", List.of(5))))
                .build();

        // When
        System.out.println("\n🔧 執行計算...");
        IndicatorResult result = engine.compute(series, plan);

        // Then
        System.out.println("\n📤 診斷結果:");
        System.out.println("  - 有警告: " + result.hasWarnings());
        System.out.println("  - 有錯誤: " + result.hasErrors());

        if (result.hasWarnings()) {
            result.getDiagnostics().getWarnings().forEach(warning -> {
                System.out.println("  - 警告訊息: " + warning.getMessage());
            });
        }

        // 驗證：應該有警告
        assertThat(result.hasWarnings()).isTrue();
        assertThat(result.getDiagnostics().getWarnings())
                .isNotEmpty()
                .anyMatch(w -> w.getMessage().contains("資料不足"));

        // 驗證：MA5 不應該被計算出來
        assertThat(result.getValue("ma5")).isNull();

        System.out.println("\n✅ 測試通過: Engine 正確產生警告");
    }

    @Test
    @DisplayName("測試: 部分週期資料不足")
    void testPartialInsufficientData() {
        System.out.println("\n📝 測試: 部分週期資料不足");

        // Given - 10 天資料（足夠 MA5，但不足 MA20）
        double[] prices = new double[10];
        for (int i = 0; i < 10; i++) {
            prices[i] = 100.0 + i;
        }

        System.out.println("📥 輸入資料:");
        System.out.println("  - 收盤價天數: 10");
        System.out.println("  - 要求計算: MA5, MA20");
        System.out.println("  - 預期: MA5 成功，MA20 跳過");

        PriceSeries series = PriceSeries.createTest("2330", prices);
        Map<String, Object> params = Map.of("periods", List.of(5, 20));

        // When
        System.out.println("\n🔧 執行計算...");
        Map<String, Object> result = calculator.calculate(series, params);

        // Then
        System.out.println("\n📤 計算結果:");
        System.out.println("  - MA5: " + result.get("ma5"));
        System.out.println("  - MA20: " + result.get("ma20"));

        // MA5 應該計算成功
        assertThat(result).containsKey("ma5");
        assertThat((Double) result.get("ma5")).isEqualTo(107.0);  // (105+106+107+108+109)/5

        // MA20 應該被跳過（資料不足）
        assertThat(result).doesNotContainKey("ma20");

        System.out.println("\n✅ 測試通過: 部分週期正確跳過");
    }
}