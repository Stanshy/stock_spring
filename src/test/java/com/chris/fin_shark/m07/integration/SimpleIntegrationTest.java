package com.chris.fin_shark.m07.integration;

import com.chris.fin_shark.m07.engine.*;
import com.chris.fin_shark.m07.engine.model.PriceSeries;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 簡單整合測試
 */
@SpringBootTest
public class SimpleIntegrationTest {

    @Autowired
    private IndicatorEngine engine;

    @Autowired
    private IndicatorRegistry registry;

    @Test
    public void testEngineAndRegistryAreLoaded() {
        System.out.println("\n========================================");
        System.out.println("🧪 測試: Engine 和 Registry 是否正確載入");
        System.out.println("========================================\n");

        // 驗證 Engine 存在
        assertThat(engine).isNotNull();
        System.out.println("✅ IndicatorEngine 已載入");

        // 驗證 Registry 存在
        assertThat(registry).isNotNull();
        System.out.println("✅ IndicatorRegistry 已載入");

        // 驗證計算器已註冊
        assertThat(registry.getAllIndicatorNames()).containsExactlyInAnyOrder(
                "MA", "EMA", "MACD", "RSI", "BBANDS"
        );
        System.out.println("✅ 5 個計算器已註冊: " + registry.getAllIndicatorNames());

        // 驗證 P0 指標
        List<String> p0Indicators = registry.getIndicatorsByPriority("P0");
        assertThat(p0Indicators).hasSize(5);
        System.out.println("✅ P0 指標數量: " + p0Indicators.size());

        System.out.println("\n========================================");
        System.out.println("✅ 整合測試通過");
        System.out.println("========================================\n");
    }

    @Test
    public void testCalculateWithEngine() {
        System.out.println("\n========================================");
        System.out.println("🧪 測試: Engine 計算功能");
        System.out.println("========================================\n");

        // 準備測試資料
        double[] prices = new double[60];
        for (int i = 0; i < 60; i++) {
            prices[i] = 100.0 + i * 0.5;
        }
        PriceSeries series = PriceSeries.createTest("2330", prices);

        // 建立計算計劃（只計算 MA）
        IndicatorPlan plan = IndicatorPlan.builder()
                .indicators(Map.of("MA", Map.of("periods", List.of(5, 20, 60))))
                .build();

        // 執行計算
        IndicatorResult result = engine.compute(series, plan);

        // 驗證結果
        assertThat(result).isNotNull();
        assertThat(result.getStockId()).isEqualTo("2330");
        assertThat(result.hasErrors()).isFalse();
        assertThat(result.getValue("ma5")).isNotNull();
        assertThat(result.getValue("ma20")).isNotNull();
        assertThat(result.getValue("ma60")).isNotNull();

        System.out.println("✅ MA5: " + result.getValue("ma5"));
        System.out.println("✅ MA20: " + result.getValue("ma20"));
        System.out.println("✅ MA60: " + result.getValue("ma60"));

        System.out.println("\n========================================");
        System.out.println("✅ Engine 計算測試通過");
        System.out.println("========================================\n");
    }
}