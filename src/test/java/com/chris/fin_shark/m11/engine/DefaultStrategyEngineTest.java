package com.chris.fin_shark.m11.engine;

import com.chris.fin_shark.m11.domain.Strategy;
import com.chris.fin_shark.m11.domain.StrategySignal;
import com.chris.fin_shark.m11.engine.evaluator.ConditionEvaluator;
import com.chris.fin_shark.m11.engine.evaluator.ConfidenceCalculator;
import com.chris.fin_shark.m11.engine.evaluator.SignalGenerator;
import com.chris.fin_shark.m11.enums.StrategyStatus;
import com.chris.fin_shark.m11.enums.StrategyType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 策略引擎單元測試
 *
 * @author chris
 * @since 1.0.0
 */
@DisplayName("策略引擎測試")
class DefaultStrategyEngineTest {

    private DefaultStrategyEngine engine;
    private Strategy testStrategy;
    private Map<String, Object> factorValues;

    @BeforeEach
    void setUp() {
        // 手動建立引擎（不需要 Spring）
        ConditionEvaluator conditionEvaluator = new ConditionEvaluator();
        ConfidenceCalculator confidenceCalculator = new ConfidenceCalculator();
        SignalGenerator signalGenerator = new SignalGenerator();

        engine = new DefaultStrategyEngine(conditionEvaluator, confidenceCalculator, signalGenerator);

        // 建立測試策略
        testStrategy = createTestStrategy();

        // 建立測試因子數據
        factorValues = createTestFactorValues();

        System.out.println("\n========================================");
        System.out.println("🚀 策略引擎測試");
        System.out.println("========================================\n");
    }

    private Strategy createTestStrategy() {
        // 動能反轉策略: RSI < 30 AND KD_K < 20 AND (外資買超 > 0 OR 投信買超 > 0) AND 量比 > 1.0
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

        Map<String, Object> outputConfig = new HashMap<>();
        outputConfig.put("signal_type", "BUY");

        return Strategy.builder()
                .strategyId("STG_TEST_001")
                .strategyName("測試動能反轉策略")
                .strategyType(StrategyType.MOMENTUM)
                .description("用於測試的動能反轉策略")
                .currentVersion(1)
                .status(StrategyStatus.ACTIVE)
                .conditions(conditions)
                .outputConfig(outputConfig)
                .build();
    }

    private Map<String, Object> createTestFactorValues() {
        Map<String, Object> values = new HashMap<>();

        // 滿足條件的因子數據
        values.put("rsi_14", BigDecimal.valueOf(25.5));
        values.put("kd_k", BigDecimal.valueOf(18.2));
        values.put("kd_d", BigDecimal.valueOf(22.1));
        values.put("macd_histogram", BigDecimal.valueOf(0.5));
        values.put("foreign_net", BigDecimal.valueOf(5000000));
        values.put("trust_net", BigDecimal.valueOf(-100000));
        values.put("volume_ratio", BigDecimal.valueOf(1.35));
        values.put("close_price", BigDecimal.valueOf(580.00));

        return values;
    }

    @Nested
    @DisplayName("股票評估測試")
    class EvaluateStockTests {

        @Test
        @DisplayName("測試: 符合條件的股票產生信號")
        void testEvaluateStockMatchingConditions() {
            System.out.println("📝 測試: 符合條件的股票產生信號");

            // Given
            String stockId = "2330";
            String executionId = "EXEC_TEST_001";
            LocalDate executionDate = LocalDate.now();

            System.out.println("  策略: " + testStrategy.getStrategyName());
            System.out.println("  股票: " + stockId);
            System.out.println("  因子值:");
            System.out.println("    - RSI: " + factorValues.get("rsi_14"));
            System.out.println("    - KD_K: " + factorValues.get("kd_k"));
            System.out.println("    - 外資買超: " + factorValues.get("foreign_net"));
            System.out.println("    - 量比: " + factorValues.get("volume_ratio"));

            // When
            StrategySignal signal = engine.evaluateAndGenerateSignal(
                    testStrategy, stockId, factorValues, executionId, executionDate);

            // Then
            System.out.println("\n  評估結果:");
            if (signal != null) {
                System.out.println("    ✅ 產生信號");
                System.out.println("    - 信號類型: " + signal.getSignalType());
                System.out.println("    - 信心度: " + signal.getConfidenceScore());
            } else {
                System.out.println("    ❌ 未產生信號");
            }

            assertThat(signal).isNotNull();
            assertThat(signal.getStockId()).isEqualTo(stockId);
            assertThat(signal.getStrategyId()).isEqualTo(testStrategy.getStrategyId());
        }

        @Test
        @DisplayName("測試: 不符合條件的股票不產生信號")
        void testEvaluateStockNotMatchingConditions() {
            System.out.println("📝 測試: 不符合條件的股票不產生信號");

            // Given: 修改因子值使條件不匹配
            Map<String, Object> notMatchingValues = new HashMap<>(factorValues);
            notMatchingValues.put("rsi_14", BigDecimal.valueOf(55.0)); // RSI > 30，不符合

            String stockId = "2317";
            String executionId = "EXEC_TEST_001";
            LocalDate executionDate = LocalDate.now();

            System.out.println("  策略: " + testStrategy.getStrategyName());
            System.out.println("  股票: " + stockId);
            System.out.println("  因子值:");
            System.out.println("    - RSI: " + notMatchingValues.get("rsi_14") + " (不符合 < 30)");

            // When
            StrategySignal signal = engine.evaluateAndGenerateSignal(
                    testStrategy, stockId, notMatchingValues, executionId, executionDate);

            // Then
            System.out.println("\n  評估結果:");
            if (signal != null) {
                System.out.println("    ✅ 產生信號（非預期）");
            } else {
                System.out.println("    ❌ 未產生信號（預期行為）");
            }

            assertThat(signal).isNull();
        }

        @Test
        @DisplayName("測試: 部分因子缺失")
        void testEvaluateStockWithMissingFactors() {
            System.out.println("📝 測試: 部分因子缺失");

            // Given: 移除部分因子
            Map<String, Object> incompleteValues = new HashMap<>();
            incompleteValues.put("rsi_14", BigDecimal.valueOf(25.5));
            // 缺少 kd_k, foreign_net, volume_ratio

            String stockId = "2454";
            String executionId = "EXEC_TEST_001";
            LocalDate executionDate = LocalDate.now();

            System.out.println("  股票: " + stockId);
            System.out.println("  缺失因子: kd_k, foreign_net, volume_ratio");

            // When
            StrategySignal signal = engine.evaluateAndGenerateSignal(
                    testStrategy, stockId, incompleteValues, executionId, executionDate);

            // Then
            System.out.println("\n  評估結果:");
            if (signal != null) {
                System.out.println("    ✅ 產生信號");
            } else {
                System.out.println("    ❌ 未產生信號（因子缺失導致條件不匹配）");
            }

            // 因子缺失應導致條件不匹配
            assertThat(signal).isNull();
        }
    }

    @Nested
    @DisplayName("信號生成測試")
    class SignalGenerationTests {

        @Test
        @DisplayName("測試: 信號包含正確的元數據")
        void testSignalMetadata() {
            System.out.println("📝 測試: 信號包含正確的元數據");

            // Given
            String stockId = "2330";
            String executionId = "EXEC_TEST_001";
            LocalDate executionDate = LocalDate.of(2024, 12, 24);

            // When
            StrategySignal signal = engine.evaluateAndGenerateSignal(
                    testStrategy, stockId, factorValues, executionId, executionDate);

            // Then
            assertThat(signal).isNotNull();

            System.out.println("  信號元數據:");
            System.out.println("    - 信號 ID: " + signal.getSignalId());
            System.out.println("    - 執行 ID: " + signal.getExecutionId());
            System.out.println("    - 策略 ID: " + signal.getStrategyId());
            System.out.println("    - 策略版本: " + signal.getStrategyVersion());
            System.out.println("    - 股票代碼: " + signal.getStockId());
            System.out.println("    - 交易日期: " + signal.getTradeDate());
            System.out.println("    - 信號類型: " + signal.getSignalType());
            System.out.println("    - 信心度: " + signal.getConfidenceScore());

            assertThat(signal.getSignalId()).isNotNull();
            assertThat(signal.getExecutionId()).isEqualTo(executionId);
            assertThat(signal.getStrategyId()).isEqualTo(testStrategy.getStrategyId());
            assertThat(signal.getStrategyVersion()).isEqualTo(testStrategy.getCurrentVersion());
            assertThat(signal.getStockId()).isEqualTo(stockId);
            assertThat(signal.getTradeDate()).isEqualTo(executionDate);
        }

        @Test
        @DisplayName("測試: 信號包含匹配條件詳情")
        void testSignalMatchedConditions() {
            System.out.println("📝 測試: 信號包含匹配條件詳情");

            // Given
            String stockId = "2330";

            // When
            StrategySignal signal = engine.evaluateAndGenerateSignal(
                    testStrategy, stockId, factorValues, "EXEC_TEST_001", LocalDate.now());

            // Then
            assertThat(signal).isNotNull();
            assertThat(signal.getMatchedConditions()).isNotNull();

            System.out.println("  匹配條件:");
            if (signal.getMatchedConditions() != null) {
                System.out.println("    共 " + signal.getMatchedConditions().size() + " 個條件匹配");
            }
        }

        @Test
        @DisplayName("測試: 信號包含因子值快照")
        void testSignalFactorValues() {
            System.out.println("📝 測試: 信號包含因子值快照");

            // Given
            String stockId = "2330";

            // When
            StrategySignal signal = engine.evaluateAndGenerateSignal(
                    testStrategy, stockId, factorValues, "EXEC_TEST_001", LocalDate.now());

            // Then
            assertThat(signal).isNotNull();
            assertThat(signal.getFactorValues()).isNotNull();

            System.out.println("  因子值快照:");
            if (signal.getFactorValues() != null) {
                signal.getFactorValues().forEach((k, v) ->
                        System.out.println("    - " + k + ": " + v));
            }
        }
    }

    @Nested
    @DisplayName("多種策略類型測試")
    class StrategyTypeTests {

        @Test
        @DisplayName("測試: 價值策略")
        void testValueStrategy() {
            System.out.println("📝 測試: 價值策略");

            // Given: 價值策略 - PE < 15 AND ROE > 15
            Map<String, Object> valueConditions = Map.of(
                    "logic", "AND",
                    "conditions", List.of(
                            Map.of("factor_id", "pe_ratio", "operator", "LESS_THAN", "value", 15),
                            Map.of("factor_id", "roe", "operator", "GREATER_THAN", "value", 15)
                    )
            );

            Strategy valueStrategy = Strategy.builder()
                    .strategyId("STG_VALUE_001")
                    .strategyName("價值低估策略")
                    .strategyType(StrategyType.VALUE)
                    .currentVersion(1)
                    .status(StrategyStatus.ACTIVE)
                    .conditions(valueConditions)
                    .outputConfig(Map.of("signal_type", "BUY"))
                    .build();

            Map<String, Object> values = Map.of(
                    "pe_ratio", BigDecimal.valueOf(10.5),
                    "roe", BigDecimal.valueOf(18.0)
            );

            System.out.println("  策略: " + valueStrategy.getStrategyName());
            System.out.println("  條件: PE < 15 AND ROE > 15");
            System.out.println("  因子值: PE=" + values.get("pe_ratio") + ", ROE=" + values.get("roe"));

            // When
            StrategySignal signal = engine.evaluateAndGenerateSignal(
                    valueStrategy, "2330", values, "EXEC_TEST_001", LocalDate.now());

            // Then
            System.out.println("  結果: " + (signal != null ? "✅ 產生信號" : "❌ 未產生信號"));
            assertThat(signal).isNotNull();
        }

        @Test
        @DisplayName("測試: 混合策略")
        void testHybridStrategy() {
            System.out.println("📝 測試: 混合策略");

            // Given: 混合策略 - 技術面 + 籌碼面
            Map<String, Object> hybridConditions = Map.of(
                    "logic", "AND",
                    "conditions", List.of(
                            Map.of("factor_id", "rsi_14", "operator", "LESS_THAN", "value", 40),
                            Map.of("factor_id", "foreign_net", "operator", "GREATER_THAN", "value", 0)
                    )
            );

            Strategy hybridStrategy = Strategy.builder()
                    .strategyId("STG_HYBRID_001")
                    .strategyName("法人認養策略")
                    .strategyType(StrategyType.HYBRID)
                    .currentVersion(1)
                    .status(StrategyStatus.ACTIVE)
                    .conditions(hybridConditions)
                    .outputConfig(Map.of("signal_type", "BUY"))
                    .build();

            System.out.println("  策略: " + hybridStrategy.getStrategyName());
            System.out.println("  條件: RSI < 40 AND 外資買超 > 0");

            // When
            StrategySignal signal = engine.evaluateAndGenerateSignal(
                    hybridStrategy, "2330", factorValues, "EXEC_TEST_001", LocalDate.now());

            // Then
            System.out.println("  結果: " + (signal != null ? "✅ 產生信號" : "❌ 未產生信號"));
            assertThat(signal).isNotNull();
        }
    }

    @Nested
    @DisplayName("效能測試")
    class PerformanceTests {

        @Test
        @DisplayName("測試: 批量評估效能")
        void testBatchEvaluationPerformance() {
            System.out.println("📝 測試: 批量評估效能");

            // Given
            int stockCount = 100;
            String executionId = "EXEC_PERF_001";
            LocalDate executionDate = LocalDate.now();

            System.out.println("  評估股票數: " + stockCount);

            // When
            long startTime = System.currentTimeMillis();
            int signalCount = 0;

            for (int i = 0; i < stockCount; i++) {
                String stockId = String.format("%04d", 2330 + i);
                StrategySignal signal = engine.evaluateAndGenerateSignal(
                        testStrategy, stockId, factorValues, executionId, executionDate);
                if (signal != null) {
                    signalCount++;
                }
            }

            long duration = System.currentTimeMillis() - startTime;

            // Then
            System.out.println("  執行時間: " + duration + " ms");
            System.out.println("  產生信號數: " + signalCount);
            System.out.println("  平均每檔: " + (duration / (double) stockCount) + " ms");

            // 100 檔股票應在 1 秒內完成
            assertThat(duration).isLessThan(1000);
        }
    }
}
