package com.chris.fin_shark.m11.engine;

import com.chris.fin_shark.common.enums.SignalType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 單一股票評估結果
 *
 * @author chris
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockEvaluationResult {

    /**
     * 股票代碼
     */
    private String stockId;

    /**
     * 是否符合條件
     */
    private boolean matched;

    /**
     * 信號類型（如符合條件）
     */
    private SignalType signalType;

    /**
     * 信心度分數
     */
    private BigDecimal confidenceScore;

    /**
     * 匹配的條件詳情
     */
    @Builder.Default
    private List<MatchedCondition> matchedConditions = new ArrayList<>();

    /**
     * 因子數值快照
     */
    @Builder.Default
    private Map<String, Object> factorValues = new HashMap<>();

    /**
     * 評估錯誤訊息
     */
    private String errorMessage;

    /**
     * 匹配條件詳情
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MatchedCondition {
        private String factorId;
        private Object factorValue;
        private String operator;
        private Object threshold;
        private boolean matched;
        private String description;
    }

    /**
     * 新增匹配條件
     */
    public void addMatchedCondition(MatchedCondition condition) {
        if (matchedConditions == null) {
            matchedConditions = new ArrayList<>();
        }
        matchedConditions.add(condition);
    }

    /**
     * 建立成功結果
     */
    public static StockEvaluationResult matched(
            String stockId,
            SignalType signalType,
            BigDecimal confidenceScore) {
        return StockEvaluationResult.builder()
                .stockId(stockId)
                .matched(true)
                .signalType(signalType)
                .confidenceScore(confidenceScore)
                .build();
    }

    /**
     * 建立不符合結果
     */
    public static StockEvaluationResult notMatched(String stockId) {
        return StockEvaluationResult.builder()
                .stockId(stockId)
                .matched(false)
                .build();
    }

    /**
     * 建立錯誤結果
     */
    public static StockEvaluationResult error(String stockId, String errorMessage) {
        return StockEvaluationResult.builder()
                .stockId(stockId)
                .matched(false)
                .errorMessage(errorMessage)
                .build();
    }
}
