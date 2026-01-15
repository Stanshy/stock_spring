package com.chris.fin_shark.m11.engine.evaluator;

import com.chris.fin_shark.common.enums.SignalType;
import com.chris.fin_shark.m11.domain.Strategy;
import com.chris.fin_shark.m11.domain.StrategySignal;
import com.chris.fin_shark.m11.engine.StockEvaluationResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * 信號生成器
 * <p>
 * 根據評估結果生成策略信號
 * </p>
 *
 * @author chris
 * @since 1.0.0
 */
@Slf4j
@Component
public class SignalGenerator {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");
    private final AtomicInteger signalCounter = new AtomicInteger(0);

    /**
     * 生成策略信號
     *
     * @param strategy        策略定義
     * @param evaluationResult 評估結果
     * @param executionId     執行 ID
     * @param tradeDate       交易日期
     * @return 策略信號（如不符合條件則返回 null）
     */
    public StrategySignal generate(
            Strategy strategy,
            StockEvaluationResult evaluationResult,
            String executionId,
            LocalDate tradeDate) {

        if (!evaluationResult.isMatched()) {
            return null;
        }

        // 從策略輸出配置取得信號類型
        SignalType signalType = getSignalType(strategy);

        // 生成信號 ID
        String signalId = generateSignalId(tradeDate);

        // 轉換匹配條件為 Map
        Map<String, Object> matchedConditionsMap = convertMatchedConditions(
                evaluationResult.getMatchedConditions());

        return StrategySignal.builder()
                .signalId(signalId)
                .executionId(executionId)
                .strategyId(strategy.getStrategyId())
                .strategyVersion(strategy.getCurrentVersion())
                .stockId(evaluationResult.getStockId())
                .tradeDate(tradeDate)
                .signalType(signalType)
                .confidenceScore(evaluationResult.getConfidenceScore())
                .matchedConditions(matchedConditionsMap)
                .factorValues(evaluationResult.getFactorValues())
                .isConsumed(false)
                .createdAt(LocalDateTime.now())
                .build();
    }

    /**
     * 從策略配置取得信號類型
     */
    private SignalType getSignalType(Strategy strategy) {
        Map<String, Object> outputConfig = strategy.getOutputConfig();
        if (outputConfig != null) {
            Object signalTypeObj = outputConfig.get("signal_type");
            if (signalTypeObj != null) {
                try {
                    return SignalType.valueOf(signalTypeObj.toString().toUpperCase());
                } catch (IllegalArgumentException e) {
                    log.warn("無效的信號類型: {}", signalTypeObj);
                }
            }
        }
        return SignalType.BUY; // 預設為買進信號
    }

    /**
     * 生成信號 ID
     */
    private String generateSignalId(LocalDate tradeDate) {
        int counter = signalCounter.incrementAndGet();
        if (counter > 999999) {
            signalCounter.set(0);
        }
        return String.format("STG_SIG_%s_%06d",
                tradeDate.format(DATE_FORMATTER),
                counter);
    }

    /**
     * 轉換匹配條件為 Map（用於 JSONB 儲存）
     */
    private Map<String, Object> convertMatchedConditions(
            List<StockEvaluationResult.MatchedCondition> conditions) {

        if (conditions == null || conditions.isEmpty()) {
            return new HashMap<>();
        }

        List<Map<String, Object>> conditionsList = conditions.stream()
                .map(c -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("factor_id", c.getFactorId());
                    map.put("factor_value", c.getFactorValue());
                    map.put("operator", c.getOperator());
                    map.put("threshold", c.getThreshold());
                    map.put("matched", c.isMatched());
                    if (c.getDescription() != null) {
                        map.put("description", c.getDescription());
                    }
                    return map;
                })
                .collect(Collectors.toList());

        Map<String, Object> result = new HashMap<>();
        result.put("conditions", conditionsList);
        return result;
    }

    /**
     * 重置計數器（用於測試）
     */
    public void resetCounter() {
        signalCounter.set(0);
    }
}
