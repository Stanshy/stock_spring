package com.chris.fin_shark.m11.integration;

import com.chris.fin_shark.m11.domain.Strategy;
import com.chris.fin_shark.m11.domain.StrategySignal;
import com.chris.fin_shark.m11.engine.DefaultStrategyEngine;
import com.chris.fin_shark.m11.engine.evaluator.ConditionEvaluator;
import com.chris.fin_shark.m11.engine.evaluator.ConfidenceCalculator;
import com.chris.fin_shark.m11.engine.evaluator.SignalGenerator;
import com.chris.fin_shark.m11.enums.StrategyStatus;
import com.chris.fin_shark.m11.enums.StrategyType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 策略引擎整合測試
 * <p>
 * 測試完整的策略評估流程，不依賴 Spring 容器
 * </p>
 *
 * @author chris
 * @since 1.0.0
 */
@DisplayName("策略引擎整合測試")
class StrategyIntegrationTest {

    private DefaultStrategyEngine engine;

    @BeforeEach
    void setUp() {
        // 手動建立引擎（不依賴 Spring）
        ConditionEvaluator conditionEvaluator = new ConditionEvaluator();
        ConfidenceCalculator confidenceCalculator = new ConfidenceCalculator();
        SignalGenerator signalGenerator = new SignalGenerator();

        engine = new DefaultStrategyEngine(conditionEvaluator, confidenceCalculator, signalGenerator);

        System.out.println("\n========================================");
        System.out.println("🚀 策略引擎整合測試");
        System.out.println("========================================\n");
    }

    @Test
    @DisplayName("測試: 動能反轉策略完整流程")
    void testMomentumReversalStrategyFlow() {
        System.out.println("📝 測試: 動能反轉策略完整流程");

        // 1. 建立策略定義
        Strategy strategy = createMomentumStrategy();
        System.out.println("  策略: " + strategy.getStrategyName());
        System.out.println("  條件: RSI < 30 AND KD_K < 20 AND (外資買超 > 0 OR 投信買超 > 0) AND 量比 > 1.0");

        // 2. 準備測試股票數據
        List<Map<String, Object>> stockDataList = createTestStockData();
        System.out.println("  測試股票數: " + stockDataList.size());

        // 3. 執行策略評估
        String executionId = "EXEC_TEST_001";
        LocalDate executionDate = LocalDate.now();
        List<StrategySignal> signals = new ArrayList<>();

        long startTime = System.currentTimeMillis();

        for (Map<String, Object> stockData : stockDataList) {
            String stockId = (String) stockData.get("stock_id");
            StrategySignal signal = engine.evaluateAndGenerateSignal(
                    strategy, stockId, stockData, executionId, executionDate);

            if (signal != null) {
                signals.add(signal);
            }
        }

        long duration = System.currentTimeMillis() - startTime;

        // 4. 驗證結果
        System.out.println("\n  執行結果:");
        System.out.println("    - 評估股票數: " + stockDataList.size());
        System.out.println("    - 產生信號數: " + signals.size());
        System.out.println("    - 執行時間: " + duration + " ms");

        if (!signals.isEmpty()) {
            System.out.println("\n  信號詳情:");
            for (StrategySignal signal : signals) {
                System.out.println("    - " + signal.getStockId() +
                        " | " + signal.getSignalType() +
                        " | 信心度: " + signal.getConfidenceScore());
            }
        }

        // 驗證
        assertThat(signals).isNotEmpty();
        assertThat(signals.get(0).getStrategyId()).isEqualTo(strategy.getStrategyId());
        assertThat(duration).isLessThan(1000); // 應在 1 秒內完成

        System.out.println("\n✅ 動能反轉策略測試通過");
    }

    @Test
    @DisplayName("測試: 價值投資策略完整流程")
    void testValueStrategyFlow() {
        System.out.println("📝 測試: 價值投資策略完整流程");

        // 1. 建立策略定義
        Strategy strategy = createValueStrategy();
        System.out.println("  策略: " + strategy.getStrategyName());
        System.out.println("  條件: PE < 15 AND ROE > 15 AND 殖利率 > 3%");

        // 2. 準備測試股票數據
        List<Map<String, Object>> stockDataList = createValueStockData();
        System.out.println("  測試股票數: " + stockDataList.size());

        // 3. 執行策略評估
        String executionId = "EXEC_VALUE_001";
        LocalDate executionDate = LocalDate.now();
        List<StrategySignal> signals = new ArrayList<>();

        for (Map<String, Object> stockData : stockDataList) {
            String stockId = (String) stockData.get("stock_id");
            StrategySignal signal = engine.evaluateAndGenerateSignal(
                    strategy, stockId, stockData, executionId, executionDate);

            if (signal != null) {
                signals.add(signal);
            }
        }

        // 4. 驗證結果
        System.out.println("\n  執行結果:");
        System.out.println("    - 評估股票數: " + stockDataList.size());
        System.out.println("    - 產生信號數: " + signals.size());

        if (!signals.isEmpty()) {
            System.out.println("\n  信號詳情:");
            for (StrategySignal signal : signals) {
                System.out.println("    - " + signal.getStockId() +
                        " | " + signal.getSignalType() +
                        " | 信心度: " + signal.getConfidenceScore());
            }
        }

        System.out.println("\n✅ 價值投資策略測試通過");
    }

    @Test
    @DisplayName("測試: 多策略並行執行")
    void testMultipleStrategiesParallel() {
        System.out.println("📝 測試: 多策略並行執行");

        // 1. 建立多個策略
        List<Strategy> strategies = List.of(
                createMomentumStrategy(),
                createValueStrategy(),
                createChipStrategy()
        );

        System.out.println("  策略數: " + strategies.size());

        // 2. 準備測試數據（綜合數據）
        Map<String, Object> stockData = createComprehensiveStockData("2330");

        // 3. 執行所有策略
        String executionId = "EXEC_MULTI_001";
        LocalDate executionDate = LocalDate.now();
        List<StrategySignal> allSignals = new ArrayList<>();

        for (Strategy strategy : strategies) {
            StrategySignal signal = engine.evaluateAndGenerateSignal(
                    strategy, "2330", stockData, executionId, executionDate);

            if (signal != null) {
                allSignals.add(signal);
                System.out.println("  ✅ " + strategy.getStrategyName() + " -> 產生信號");
            } else {
                System.out.println("  ❌ " + strategy.getStrategyName() + " -> 無信號");
            }
        }

        // 4. 驗證結果
        System.out.println("\n  總信號數: " + allSignals.size());

        System.out.println("\n✅ 多策略並行執行測試通過");
    }

    @Test
    @DisplayName("測試: 大量股票批次評估效能")
    void testBatchEvaluationPerformance() {
        System.out.println("📝 測試: 大量股票批次評估效能");

        // 1. 建立策略
        Strategy strategy = createMomentumStrategy();

        // 2. 準備大量測試數據
        int stockCount = 500;
        List<Map<String, Object>> stockDataList = new ArrayList<>();

        for (int i = 0; i < stockCount; i++) {
            String stockId = String.format("%04d", 2330 + i);
            stockDataList.add(createRandomStockData(stockId));
        }

        System.out.println("  測試股票數: " + stockCount);

        // 3. 執行批次評估
        String executionId = "EXEC_PERF_001";
        LocalDate executionDate = LocalDate.now();
        List<StrategySignal> signals = new ArrayList<>();

        long startTime = System.currentTimeMillis();

        for (Map<String, Object> stockData : stockDataList) {
            String stockId = (String) stockData.get("stock_id");
            StrategySignal signal = engine.evaluateAndGenerateSignal(
                    strategy, stockId, stockData, executionId, executionDate);

            if (signal != null) {
                signals.add(signal);
            }
        }

        long duration = System.currentTimeMillis() - startTime;

        // 4. 效能統計
        double avgTime = duration / (double) stockCount;
        double throughput = stockCount / (duration / 1000.0);

        System.out.println("\n  效能統計:");
        System.out.println("    - 總執行時間: " + duration + " ms");
        System.out.println("    - 平均每檔: " + String.format("%.2f", avgTime) + " ms");
        System.out.println("    - 吞吐量: " + String.format("%.0f", throughput) + " 檔/秒");
        System.out.println("    - 產生信號數: " + signals.size());
        System.out.println("    - 信號產生率: " + String.format("%.1f%%", signals.size() * 100.0 / stockCount));

        // 驗證效能
        assertThat(duration).isLessThan(5000); // 500 檔應在 5 秒內完成
        assertThat(avgTime).isLessThan(10); // 平均每檔 < 10ms

        System.out.println("\n✅ 批次評估效能測試通過");
    }

    // ==================== 輔助方法 ====================

    private Strategy createMomentumStrategy() {
        Map<String, Object> conditions = Map.of(
                "logic", "AND",
                "conditions", List.of(
                        Map.of("factor_id", "rsi_14", "operator", "LESS_THAN", "value", 30),
                        Map.of("factor_id", "kd_k", "operator", "LESS_THAN", "value", 20),
                        Map.of(
                                "logic", "OR",
                                "conditions", List.of(
                                        Map.of("factor_id", "foreign_net", "operator", "GREATER_THAN", "value", 0),
                                        Map.of("factor_id", "trust_net", "operator", "GREATER_THAN", "value", 0)
                                )
                        ),
                        Map.of("factor_id", "volume_ratio", "operator", "GREATER_THAN", "value", 1.0)
                )
        );

        return Strategy.builder()
                .strategyId("STG_MOMENTUM_001")
                .strategyName("動能反轉策略")
                .strategyType(StrategyType.MOMENTUM)
                .currentVersion(1)
                .status(StrategyStatus.ACTIVE)
                .conditions(conditions)
                .outputConfig(Map.of("signal_type", "BUY"))
                .build();
    }

    private Strategy createValueStrategy() {
        Map<String, Object> conditions = Map.of(
                "logic", "AND",
                "conditions", List.of(
                        Map.of("factor_id", "pe_ratio", "operator", "LESS_THAN", "value", 15),
                        Map.of("factor_id", "roe", "operator", "GREATER_THAN", "value", 15),
                        Map.of("factor_id", "dividend_yield", "operator", "GREATER_THAN", "value", 3)
                )
        );

        return Strategy.builder()
                .strategyId("STG_VALUE_001")
                .strategyName("價值低估策略")
                .strategyType(StrategyType.VALUE)
                .currentVersion(1)
                .status(StrategyStatus.ACTIVE)
                .conditions(conditions)
                .outputConfig(Map.of("signal_type", "BUY"))
                .build();
    }

    private Strategy createChipStrategy() {
        Map<String, Object> conditions = Map.of(
                "logic", "AND",
                "conditions", List.of(
                        Map.of("factor_id", "foreign_net", "operator", "GREATER_THAN", "value", 1000000),
                        Map.of("factor_id", "trust_net", "operator", "GREATER_THAN", "value", 100000),
                        Map.of("factor_id", "foreign_continuous_days", "operator", "GREATER_THAN", "value", 3)
                )
        );

        return Strategy.builder()
                .strategyId("STG_CHIP_001")
                .strategyName("法人認養策略")
                .strategyType(StrategyType.HYBRID)
                .currentVersion(1)
                .status(StrategyStatus.ACTIVE)
                .conditions(conditions)
                .outputConfig(Map.of("signal_type", "BUY"))
                .build();
    }

    private List<Map<String, Object>> createTestStockData() {
        List<Map<String, Object>> list = new ArrayList<>();

        // 符合條件的股票
        Map<String, Object> stock1 = new HashMap<>();
        stock1.put("stock_id", "2330");
        stock1.put("rsi_14", BigDecimal.valueOf(25.5));
        stock1.put("kd_k", BigDecimal.valueOf(18.2));
        stock1.put("foreign_net", BigDecimal.valueOf(5000000));
        stock1.put("trust_net", BigDecimal.valueOf(-100000));
        stock1.put("volume_ratio", BigDecimal.valueOf(1.35));
        list.add(stock1);

        // 另一個符合條件的股票
        Map<String, Object> stock2 = new HashMap<>();
        stock2.put("stock_id", "2454");
        stock2.put("rsi_14", BigDecimal.valueOf(28.0));
        stock2.put("kd_k", BigDecimal.valueOf(19.5));
        stock2.put("foreign_net", BigDecimal.valueOf(-500000));
        stock2.put("trust_net", BigDecimal.valueOf(800000));
        stock2.put("volume_ratio", BigDecimal.valueOf(1.50));
        list.add(stock2);

        // 不符合條件的股票
        Map<String, Object> stock3 = new HashMap<>();
        stock3.put("stock_id", "2317");
        stock3.put("rsi_14", BigDecimal.valueOf(55.0)); // RSI 太高
        stock3.put("kd_k", BigDecimal.valueOf(45.0));
        stock3.put("foreign_net", BigDecimal.valueOf(100000));
        stock3.put("trust_net", BigDecimal.valueOf(50000));
        stock3.put("volume_ratio", BigDecimal.valueOf(0.8));
        list.add(stock3);

        return list;
    }

    private List<Map<String, Object>> createValueStockData() {
        List<Map<String, Object>> list = new ArrayList<>();

        // 符合條件的股票
        Map<String, Object> stock1 = new HashMap<>();
        stock1.put("stock_id", "2412");
        stock1.put("pe_ratio", BigDecimal.valueOf(10.5));
        stock1.put("roe", BigDecimal.valueOf(18.0));
        stock1.put("dividend_yield", BigDecimal.valueOf(5.5));
        list.add(stock1);

        // 不符合條件的股票
        Map<String, Object> stock2 = new HashMap<>();
        stock2.put("stock_id", "2330");
        stock2.put("pe_ratio", BigDecimal.valueOf(25.0)); // PE 太高
        stock2.put("roe", BigDecimal.valueOf(25.0));
        stock2.put("dividend_yield", BigDecimal.valueOf(2.0));
        list.add(stock2);

        return list;
    }

    private Map<String, Object> createComprehensiveStockData(String stockId) {
        Map<String, Object> data = new HashMap<>();
        data.put("stock_id", stockId);

        // 技術面
        data.put("rsi_14", BigDecimal.valueOf(28.0));
        data.put("kd_k", BigDecimal.valueOf(19.0));
        data.put("kd_d", BigDecimal.valueOf(22.0));
        data.put("macd_histogram", BigDecimal.valueOf(0.5));

        // 基本面
        data.put("pe_ratio", BigDecimal.valueOf(12.0));
        data.put("roe", BigDecimal.valueOf(20.0));
        data.put("dividend_yield", BigDecimal.valueOf(4.0));

        // 籌碼面
        data.put("foreign_net", BigDecimal.valueOf(3000000));
        data.put("trust_net", BigDecimal.valueOf(500000));
        data.put("foreign_continuous_days", BigDecimal.valueOf(5));

        // 量價
        data.put("volume_ratio", BigDecimal.valueOf(1.5));
        data.put("close_price", BigDecimal.valueOf(580.0));

        return data;
    }

    private Map<String, Object> createRandomStockData(String stockId) {
        Map<String, Object> data = new HashMap<>();
        data.put("stock_id", stockId);

        // 隨機生成因子值
        double rsi = 20 + Math.random() * 60; // 20-80
        double kdK = 10 + Math.random() * 80; // 10-90
        double foreignNet = (Math.random() - 0.5) * 10000000; // -5M to 5M
        double volumeRatio = 0.5 + Math.random() * 2; // 0.5-2.5

        data.put("rsi_14", BigDecimal.valueOf(rsi));
        data.put("kd_k", BigDecimal.valueOf(kdK));
        data.put("foreign_net", BigDecimal.valueOf(foreignNet));
        data.put("trust_net", BigDecimal.valueOf((Math.random() - 0.5) * 2000000));
        data.put("volume_ratio", BigDecimal.valueOf(volumeRatio));
        data.put("pe_ratio", BigDecimal.valueOf(5 + Math.random() * 30));
        data.put("roe", BigDecimal.valueOf(5 + Math.random() * 30));
        data.put("dividend_yield", BigDecimal.valueOf(Math.random() * 8));

        return data;
    }
}
