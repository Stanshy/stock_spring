package com.chris.fin_shark.m07.engine;

import com.chris.fin_shark.m07.engine.calculator.trend.MACalculator;
import com.chris.fin_shark.m07.engine.model.PriceSeries;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 指標引擎整合測試
 *
 * @author chris
 * @since 1.0.0
 */
@DisplayName("指標引擎整合測試")
class DefaultIndicatorEngineTest {

    private IndicatorEngine engine;

    @BeforeEach
    void setUp() {
        // 手動建立引擎（不需要 Spring）
        engine = new DefaultIndicatorEngine(List.of(
                new MACalculator()
        ));

        System.out.println("\n========================================");
        System.out.println("🚀 指標引擎整合測試");
        System.out.println("========================================\n");
    }

    @Test
    @DisplayName("測試: 完整計算流程")
    void testFullCalculationFlow() {
        System.out.println("📝 測試: 完整計算流程");

        // Given
        double[] prices = new double[60];
        for (int i = 0; i < 60; i++) {
            prices[i] = 100.0 + i;
        }

        PriceSeries series = PriceSeries.createTest("2330", prices);

        IndicatorPlan plan = IndicatorPlan.builder()
                .indicators(Map.of(
                        "MA", Map.of("periods", List.of(5, 20, 60))
                ))
                .build();

        System.out.println("📥 輸入:");
        System.out.println("  - 股票: " + series.getStockId());
        System.out.println("  - 資料天數: " + series.size());
        System.out.println("  - 計算指標: MA (5, 20, 60)");

        // When
        System.out.println("\n🔧 執行計算...");
        IndicatorResult result = engine.compute(series, plan);

        // Then
        System.out.println("\n📤 計算結果:");
        System.out.println("  - MA5: " + result.getValue("ma5"));
        System.out.println("  - MA20: " + result.getValue("ma20"));
        System.out.println("  - MA60: " + result.getValue("ma60"));
        System.out.println("  - 是否有錯誤: " + result.hasErrors());
        System.out.println("  - 是否有警告: " + result.hasWarnings());

        assertThat(result.getValue("ma5")).isNotNull();
        assertThat(result.getValue("ma20")).isNotNull();
        assertThat(result.getValue("ma60")).isNotNull();
        assertThat(result.hasErrors()).isFalse();

        System.out.println("\n✅ 測試通過: 引擎運作正常");
    }
}