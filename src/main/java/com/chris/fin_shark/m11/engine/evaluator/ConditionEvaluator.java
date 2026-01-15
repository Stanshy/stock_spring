package com.chris.fin_shark.m11.engine.evaluator;

import com.chris.fin_shark.m11.engine.StockEvaluationResult.MatchedCondition;
import com.chris.fin_shark.m11.enums.ConditionOperator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.*;

/**
 * 條件評估器
 * <p>
 * 負責評估策略條件是否成立，支援 AND/OR 巢狀邏輯
 * </p>
 *
 * @author chris
 * @since 1.0.0
 */
@Slf4j
@Component
public class ConditionEvaluator {

    /**
     * 評估條件樹
     *
     * @param conditionNode 條件節點（可能是單一條件或巢狀邏輯）
     * @param factorData    因子數據 Map
     * @return 評估結果
     */
    public EvaluationResult evaluate(Map<String, Object> conditionNode, Map<String, Object> factorData) {
        if (conditionNode == null) {
            return EvaluationResult.success(true);
        }

        // 判斷是否為巢狀邏輯節點
        if (conditionNode.containsKey("logic")) {
            return evaluateLogicNode(conditionNode, factorData);
        } else {
            return evaluateLeafNode(conditionNode, factorData);
        }
    }

    /**
     * 評估邏輯節點（AND/OR）
     */
    @SuppressWarnings("unchecked")
    private EvaluationResult evaluateLogicNode(Map<String, Object> node, Map<String, Object> factorData) {
        String logic = (String) node.get("logic");
        List<Map<String, Object>> conditions = (List<Map<String, Object>>) node.get("conditions");

        if (conditions == null || conditions.isEmpty()) {
            return EvaluationResult.success(true);
        }

        List<MatchedCondition> allMatchedConditions = new ArrayList<>();
        List<Boolean> results = new ArrayList<>();

        for (Map<String, Object> childCondition : conditions) {
            EvaluationResult childResult = evaluate(childCondition, factorData);
            results.add(childResult.isMatched());
            allMatchedConditions.addAll(childResult.getMatchedConditions());
        }

        boolean finalResult;
        if ("AND".equalsIgnoreCase(logic)) {
            finalResult = results.stream().allMatch(Boolean::booleanValue);
        } else { // OR
            finalResult = results.stream().anyMatch(Boolean::booleanValue);
        }

        return EvaluationResult.builder()
                .matched(finalResult)
                .matchedConditions(allMatchedConditions)
                .build();
    }

    /**
     * 評估葉節點（單一條件）
     */
    private EvaluationResult evaluateLeafNode(Map<String, Object> node, Map<String, Object> factorData) {
        String factorId = (String) node.get("factor_id");
        String operatorStr = (String) node.get("operator");
        Object threshold = node.get("value");
        String description = (String) node.get("description");

        // 取得因子值
        Object factorValue = factorData.get(factorId);

        // 因子值缺失
        if (factorValue == null) {
            MatchedCondition condition = MatchedCondition.builder()
                    .factorId(factorId)
                    .factorValue(null)
                    .operator(operatorStr)
                    .threshold(threshold)
                    .matched(false)
                    .description(description)
                    .build();

            return EvaluationResult.builder()
                    .matched(false)
                    .matchedConditions(List.of(condition))
                    .build();
        }

        // 執行運算
        ConditionOperator operator = ConditionOperator.fromString(operatorStr);
        boolean matched = applyOperator(factorValue, operator, threshold);

        MatchedCondition condition = MatchedCondition.builder()
                .factorId(factorId)
                .factorValue(factorValue)
                .operator(operatorStr)
                .threshold(threshold)
                .matched(matched)
                .description(description)
                .build();

        return EvaluationResult.builder()
                .matched(matched)
                .matchedConditions(List.of(condition))
                .build();
    }

    /**
     * 執行運算子比較
     */
    @SuppressWarnings("unchecked")
    private boolean applyOperator(Object value, ConditionOperator operator, Object threshold) {
        try {
            BigDecimal numValue = toBigDecimal(value);
            BigDecimal numThreshold = toBigDecimal(threshold);

            switch (operator) {
                case EQUAL:
                    return numValue.compareTo(numThreshold) == 0;
                case NOT_EQUAL:
                    return numValue.compareTo(numThreshold) != 0;
                case GREATER_THAN:
                    return numValue.compareTo(numThreshold) > 0;
                case GREATER_THAN_EQUAL:
                    return numValue.compareTo(numThreshold) >= 0;
                case LESS_THAN:
                    return numValue.compareTo(numThreshold) < 0;
                case LESS_THAN_EQUAL:
                    return numValue.compareTo(numThreshold) <= 0;
                case BETWEEN:
                    if (threshold instanceof Map) {
                        Map<String, Object> range = (Map<String, Object>) threshold;
                        BigDecimal min = toBigDecimal(range.get("min"));
                        BigDecimal max = toBigDecimal(range.get("max"));
                        return numValue.compareTo(min) >= 0 && numValue.compareTo(max) <= 0;
                    }
                    return false;
                case IN:
                    if (threshold instanceof List) {
                        List<?> values = (List<?>) threshold;
                        return values.stream()
                                .map(this::toBigDecimal)
                                .anyMatch(t -> numValue.compareTo(t) == 0);
                    }
                    return false;
                case CROSS_ABOVE:
                case CROSS_BELOW:
                    // 交叉運算需要歷史資料，此處簡化處理
                    log.warn("交叉運算子需要歷史資料支援: {}", operator);
                    return false;
                default:
                    log.warn("不支援的運算子: {}", operator);
                    return false;
            }
        } catch (Exception e) {
            log.error("運算失敗: value={}, operator={}, threshold={}", value, operator, threshold, e);
            return false;
        }
    }

    /**
     * 轉換為 BigDecimal
     */
    private BigDecimal toBigDecimal(Object value) {
        if (value == null) {
            return BigDecimal.ZERO;
        }
        if (value instanceof BigDecimal) {
            return (BigDecimal) value;
        }
        if (value instanceof Number) {
            return BigDecimal.valueOf(((Number) value).doubleValue());
        }
        if (value instanceof String) {
            return new BigDecimal((String) value);
        }
        return BigDecimal.ZERO;
    }

    /**
     * 評估結果
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class EvaluationResult {
        private boolean matched;
        @lombok.Builder.Default
        private List<MatchedCondition> matchedConditions = new ArrayList<>();

        public static EvaluationResult success(boolean matched) {
            return EvaluationResult.builder()
                    .matched(matched)
                    .matchedConditions(new ArrayList<>())
                    .build();
        }
    }
}
