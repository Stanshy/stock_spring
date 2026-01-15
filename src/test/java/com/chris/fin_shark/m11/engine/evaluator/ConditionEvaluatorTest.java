package com.chris.fin_shark.m11.engine.evaluator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 條件評估器單元測試
 *
 * @author chris
 * @since 1.0.0
 */
@DisplayName("條件評估器測試")
class ConditionEvaluatorTest {

    private ConditionEvaluator evaluator;
    private Map<String, Object> factorValues;

    @BeforeEach
    void setUp() {
        evaluator = new ConditionEvaluator();
        factorValues = new HashMap<>();

        // 模擬因子數據
        factorValues.put("rsi_14", BigDecimal.valueOf(25.5));
        factorValues.put("kd_k", BigDecimal.valueOf(18.2));
        factorValues.put("kd_d", BigDecimal.valueOf(22.1));
        factorValues.put("macd_histogram", BigDecimal.valueOf(0.5));
        factorValues.put("pe_ratio", BigDecimal.valueOf(12.5));
        factorValues.put("roe", BigDecimal.valueOf(18.0));
        factorValues.put("foreign_net", BigDecimal.valueOf(5000000));
        factorValues.put("volume_ratio", BigDecimal.valueOf(1.35));

        System.out.println("\n========================================");
        System.out.println("🧪 條件評估器測試");
        System.out.println("========================================\n");
    }

    @Nested
    @DisplayName("單一條件評估")
    class SingleConditionTests {

        @Test
        @DisplayName("測試: GREATER_THAN 運算符")
        void testGreaterThan() {
            System.out.println("📝 測試: GREATER_THAN 運算符");

            // Given
            Map<String, Object> condition = Map.of(
                    "factor_id", "rsi_14",
                    "operator", "GREATER_THAN",
                    "value", 20
            );

            System.out.println("  條件: RSI(14) > 20");
            System.out.println("  實際值: " + factorValues.get("rsi_14"));

            // When
            ConditionEvaluator.EvaluationResult result = evaluator.evaluate(condition, factorValues);

            // Then
            System.out.println("  結果: " + (result.isMatched() ? "✅ 匹配" : "❌ 不匹配"));
            assertThat(result.isMatched()).isTrue();
        }

        @Test
        @DisplayName("測試: LESS_THAN 運算符")
        void testLessThan() {
            System.out.println("📝 測試: LESS_THAN 運算符");

            // Given
            Map<String, Object> condition = Map.of(
                    "factor_id", "rsi_14",
                    "operator", "LESS_THAN",
                    "value", 30
            );

            System.out.println("  條件: RSI(14) < 30");
            System.out.println("  實際值: " + factorValues.get("rsi_14"));

            // When
            ConditionEvaluator.EvaluationResult result = evaluator.evaluate(condition, factorValues);

            // Then
            System.out.println("  結果: " + (result.isMatched() ? "✅ 匹配" : "❌ 不匹配"));
            assertThat(result.isMatched()).isTrue();
        }

        @Test
        @DisplayName("測試: EQUAL 運算符")
        void testEqual() {
            System.out.println("📝 測試: EQUAL 運算符");

            // Given
            factorValues.put("test_value", BigDecimal.valueOf(100));
            Map<String, Object> condition = Map.of(
                    "factor_id", "test_value",
                    "operator", "EQUAL",
                    "value", 100
            );

            System.out.println("  條件: test_value == 100");
            System.out.println("  實際值: " + factorValues.get("test_value"));

            // When
            ConditionEvaluator.EvaluationResult result = evaluator.evaluate(condition, factorValues);

            // Then
            System.out.println("  結果: " + (result.isMatched() ? "✅ 匹配" : "❌ 不匹配"));
            assertThat(result.isMatched()).isTrue();
        }

        @Test
        @DisplayName("測試: BETWEEN 運算符")
        void testBetween() {
            System.out.println("測試: BETWEEN 運算符");

            // Given - BETWEEN 需要使用 Map 格式指定 min 和 max
            Map<String, Object> condition = Map.of(
                    "factor_id", "rsi_14",
                    "operator", "BETWEEN",
                    "value", Map.of("min", 20, "max", 30)
            );

            System.out.println("  條件: 20 <= RSI(14) <= 30");
            System.out.println("  實際值: " + factorValues.get("rsi_14"));

            // When
            ConditionEvaluator.EvaluationResult result = evaluator.evaluate(condition, factorValues);

            // Then
            System.out.println("  結果: " + (result.isMatched() ? "匹配" : "不匹配"));
            assertThat(result.isMatched()).isTrue();
        }

        @Test
        @DisplayName("測試: 條件不匹配")
        void testConditionNotMatched() {
            System.out.println("📝 測試: 條件不匹配");

            // Given
            Map<String, Object> condition = Map.of(
                    "factor_id", "rsi_14",
                    "operator", "GREATER_THAN",
                    "value", 50
            );

            System.out.println("  條件: RSI(14) > 50");
            System.out.println("  實際值: " + factorValues.get("rsi_14"));

            // When
            ConditionEvaluator.EvaluationResult result = evaluator.evaluate(condition, factorValues);

            // Then
            System.out.println("  結果: " + (result.isMatched() ? "✅ 匹配" : "❌ 不匹配"));
            assertThat(result.isMatched()).isFalse();
        }

        @Test
        @DisplayName("測試: 因子值缺失")
        void testMissingFactorValue() {
            System.out.println("📝 測試: 因子值缺失");

            // Given
            Map<String, Object> condition = Map.of(
                    "factor_id", "non_existent_factor",
                    "operator", "GREATER_THAN",
                    "value", 50
            );

            System.out.println("  條件: non_existent_factor > 50");
            System.out.println("  實際值: (不存在)");

            // When
            ConditionEvaluator.EvaluationResult result = evaluator.evaluate(condition, factorValues);

            // Then
            System.out.println("  結果: " + (result.isMatched() ? "✅ 匹配" : "❌ 不匹配"));
            System.out.println("  未匹配條件數: " + result.getMatchedConditions().stream().filter(c -> !c.isMatched()).count());
            assertThat(result.isMatched()).isFalse();
        }
    }

    @Nested
    @DisplayName("複合條件評估 (AND/OR)")
    class CompositeConditionTests {

        @Test
        @DisplayName("測試: AND 條件 - 全部匹配")
        void testAndConditionAllMatch() {
            System.out.println("📝 測試: AND 條件 - 全部匹配");

            // Given: RSI < 30 AND KD_K < 20
            Map<String, Object> conditions = Map.of(
                    "logic", "AND",
                    "conditions", List.of(
                            Map.of("factor_id", "rsi_14", "operator", "LESS_THAN", "value", 30),
                            Map.of("factor_id", "kd_k", "operator", "LESS_THAN", "value", 20)
                    )
            );

            System.out.println("  條件: RSI(14) < 30 AND KD_K < 20");
            System.out.println("  實際值: RSI=" + factorValues.get("rsi_14") + ", KD_K=" + factorValues.get("kd_k"));

            // When
            ConditionEvaluator.EvaluationResult result = evaluator.evaluate(conditions, factorValues);

            // Then
            System.out.println("  結果: " + (result.isMatched() ? "✅ 匹配" : "❌ 不匹配"));
            assertThat(result.isMatched()).isTrue();
        }

        @Test
        @DisplayName("測試: AND 條件 - 部分不匹配")
        void testAndConditionPartialMatch() {
            System.out.println("📝 測試: AND 條件 - 部分不匹配");

            // Given: RSI < 30 AND KD_K < 10 (KD_K 不匹配)
            Map<String, Object> conditions = Map.of(
                    "logic", "AND",
                    "conditions", List.of(
                            Map.of("factor_id", "rsi_14", "operator", "LESS_THAN", "value", 30),
                            Map.of("factor_id", "kd_k", "operator", "LESS_THAN", "value", 10)
                    )
            );

            System.out.println("  條件: RSI(14) < 30 AND KD_K < 10");
            System.out.println("  實際值: RSI=" + factorValues.get("rsi_14") + ", KD_K=" + factorValues.get("kd_k"));

            // When
            ConditionEvaluator.EvaluationResult result = evaluator.evaluate(conditions, factorValues);

            // Then
            System.out.println("  結果: " + (result.isMatched() ? "✅ 匹配" : "❌ 不匹配"));
            assertThat(result.isMatched()).isFalse();
        }

        @Test
        @DisplayName("測試: OR 條件 - 其中一個匹配")
        void testOrConditionOneMatch() {
            System.out.println("📝 測試: OR 條件 - 其中一個匹配");

            // Given: RSI > 50 OR KD_K < 20 (KD_K 匹配)
            Map<String, Object> conditions = Map.of(
                    "logic", "OR",
                    "conditions", List.of(
                            Map.of("factor_id", "rsi_14", "operator", "GREATER_THAN", "value", 50),
                            Map.of("factor_id", "kd_k", "operator", "LESS_THAN", "value", 20)
                    )
            );

            System.out.println("  條件: RSI(14) > 50 OR KD_K < 20");
            System.out.println("  實際值: RSI=" + factorValues.get("rsi_14") + ", KD_K=" + factorValues.get("kd_k"));

            // When
            ConditionEvaluator.EvaluationResult result = evaluator.evaluate(conditions, factorValues);

            // Then
            System.out.println("  結果: " + (result.isMatched() ? "✅ 匹配" : "❌ 不匹配"));
            assertThat(result.isMatched()).isTrue();
        }

        @Test
        @DisplayName("測試: OR 條件 - 全部不匹配")
        void testOrConditionNoneMatch() {
            System.out.println("📝 測試: OR 條件 - 全部不匹配");

            // Given: RSI > 50 OR KD_K > 50
            Map<String, Object> conditions = Map.of(
                    "logic", "OR",
                    "conditions", List.of(
                            Map.of("factor_id", "rsi_14", "operator", "GREATER_THAN", "value", 50),
                            Map.of("factor_id", "kd_k", "operator", "GREATER_THAN", "value", 50)
                    )
            );

            System.out.println("  條件: RSI(14) > 50 OR KD_K > 50");
            System.out.println("  實際值: RSI=" + factorValues.get("rsi_14") + ", KD_K=" + factorValues.get("kd_k"));

            // When
            ConditionEvaluator.EvaluationResult result = evaluator.evaluate(conditions, factorValues);

            // Then
            System.out.println("  結果: " + (result.isMatched() ? "✅ 匹配" : "❌ 不匹配"));
            assertThat(result.isMatched()).isFalse();
        }
    }

    @Nested
    @DisplayName("巢狀條件評估")
    class NestedConditionTests {

        @Test
        @DisplayName("測試: 巢狀 AND/OR 條件")
        void testNestedConditions() {
            System.out.println("📝 測試: 巢狀 AND/OR 條件");

            // Given: (RSI < 30 AND KD_K < 20) AND (外資買超 > 0 OR 投信買超 > 0)
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

            System.out.println("  條件: RSI < 30 AND KD_K < 20 AND (外資買超 > 0 OR 投信買超 > 0) AND 量比 > 1.0");
            System.out.println("  實際值:");
            System.out.println("    - RSI: " + factorValues.get("rsi_14"));
            System.out.println("    - KD_K: " + factorValues.get("kd_k"));
            System.out.println("    - 外資買超: " + factorValues.get("foreign_net"));
            System.out.println("    - 量比: " + factorValues.get("volume_ratio"));

            // When
            ConditionEvaluator.EvaluationResult result = evaluator.evaluate(conditions, factorValues);

            // Then
            System.out.println("  結果: " + (result.isMatched() ? "✅ 匹配" : "❌ 不匹配"));
            System.out.println("  匹配的條件數: " + result.getMatchedConditions().size());
            assertThat(result.isMatched()).isTrue();
        }

        @Test
        @DisplayName("測試: 三層巢狀條件")
        void testDeepNestedConditions() {
            System.out.println("📝 測試: 三層巢狀條件");

            // Given: 複雜的三層巢狀結構
            Map<String, Object> conditions = Map.of(
                    "logic", "AND",
                    "conditions", List.of(
                            Map.of(
                                    "logic", "OR",
                                    "conditions", List.of(
                                            Map.of("factor_id", "rsi_14", "operator", "LESS_THAN", "value", 30),
                                            Map.of(
                                                    "logic", "AND",
                                                    "conditions", List.of(
                                                            Map.of("factor_id", "kd_k", "operator", "LESS_THAN", "value", 20),
                                                            Map.of("factor_id", "kd_d", "operator", "LESS_THAN", "value", 25)
                                                    )
                                            )
                                    )
                            ),
                            Map.of("factor_id", "volume_ratio", "operator", "GREATER_THAN", "value", 1.0)
                    )
            );

            System.out.println("  條件: ((RSI < 30) OR (KD_K < 20 AND KD_D < 25)) AND 量比 > 1.0");

            // When
            ConditionEvaluator.EvaluationResult result = evaluator.evaluate(conditions, factorValues);

            // Then
            System.out.println("  結果: " + (result.isMatched() ? "✅ 匹配" : "❌ 不匹配"));
            assertThat(result.isMatched()).isTrue();
        }
    }

    @Nested
    @DisplayName("邊界條件測試")
    class EdgeCaseTests {

        @Test
        @DisplayName("測試: 空條件")
        void testEmptyConditions() {
            System.out.println("📝 測試: 空條件");

            // Given
            Map<String, Object> conditions = Map.of(
                    "logic", "AND",
                    "conditions", List.of()
            );

            // When
            ConditionEvaluator.EvaluationResult result = evaluator.evaluate(conditions, factorValues);

            // Then
            System.out.println("  結果: " + (result.isMatched() ? "✅ 匹配" : "❌ 不匹配"));
            // 空的 AND 條件應該回傳 true（無條件限制）
            assertThat(result.isMatched()).isTrue();
        }

        @Test
        @DisplayName("測試: null 因子值")
        void testNullFactorValue() {
            System.out.println("📝 測試: null 因子值");

            // Given
            factorValues.put("null_factor", null);
            Map<String, Object> condition = Map.of(
                    "factor_id", "null_factor",
                    "operator", "GREATER_THAN",
                    "value", 50
            );

            // When
            ConditionEvaluator.EvaluationResult result = evaluator.evaluate(condition, factorValues);

            // Then
            System.out.println("  結果: " + (result.isMatched() ? "✅ 匹配" : "❌ 不匹配"));
            assertThat(result.isMatched()).isFalse();
        }
    }
}
