package com.chris.fin_shark.m11.engine;

import com.chris.fin_shark.common.enums.SignalType;
import com.chris.fin_shark.m11.domain.Strategy;
import com.chris.fin_shark.m11.domain.StrategySignal;
import com.chris.fin_shark.m11.engine.evaluator.ConditionEvaluator;
import com.chris.fin_shark.m11.engine.evaluator.ConfidenceCalculator;
import com.chris.fin_shark.m11.engine.evaluator.SignalGenerator;
import com.chris.fin_shark.m11.enums.ExecutionStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 預設策略執行引擎實現
 * <p>
 * 協調條件評估、信心度計算、信號生成等元件
 * </p>
 *
 * @author chris
 * @since 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DefaultStrategyEngine implements StrategyEngine {

    private final ConditionEvaluator conditionEvaluator;
    private final ConfidenceCalculator confidenceCalculator;
    private final SignalGenerator signalGenerator;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");
    private final AtomicInteger executionCounter = new AtomicInteger(0);

    @Override
    public StrategyExecutionResult execute(Strategy strategy, StrategyExecutionPlan plan) {
        log.info("開始執行策略: {} ({})", strategy.getStrategyName(), strategy.getStrategyId());
        long startTime = System.currentTimeMillis();

        // 生成執行 ID
        String executionId = generateExecutionId(plan.getExecutionDate());

        StrategyExecutionResult result = StrategyExecutionResult.builder()
                .executionId(executionId)
                .strategyId(strategy.getStrategyId())
                .strategyName(strategy.getStrategyName())
                .executionDate(plan.getExecutionDate())
                .status(ExecutionStatus.RUNNING)
                .diagnostics(new Diagnostics())
                .executedAt(LocalDateTime.now())
                .build();

        try {
            // 注意：此方法不負責載入因子數據，因子數據由 Service 層提供
            // 這裡只負責純計算邏輯

            log.info("策略執行完成: {}, 耗時 {} ms",
                    strategy.getStrategyId(),
                    System.currentTimeMillis() - startTime);

            result.setStatus(ExecutionStatus.SUCCESS);

        } catch (Exception e) {
            log.error("策略執行失敗: {}", strategy.getStrategyId(), e);
            result.markFailed(e.getMessage());
        }

        result.setExecutionTimeMs(System.currentTimeMillis() - startTime);
        return result;
    }

    @Override
    public StockEvaluationResult evaluateStock(
            Strategy strategy,
            String stockId,
            Map<String, Object> factorData) {

        log.debug("評估股票: {} 使用策略: {}", stockId, strategy.getStrategyId());

        try {
            // 1. 評估條件
            Map<String, Object> conditions = strategy.getConditions();
            ConditionEvaluator.EvaluationResult evalResult =
                    conditionEvaluator.evaluate(conditions, factorData);

            // 2. 如不符合條件，返回
            if (!evalResult.isMatched()) {
                return StockEvaluationResult.notMatched(stockId);
            }

            // 3. 計算信心度
            String confidenceFormula = getConfidenceFormula(strategy);
            BigDecimal confidenceScore = confidenceCalculator.calculate(confidenceFormula, factorData);

            // 4. 取得信號類型
            SignalType signalType = getSignalType(strategy);

            // 5. 建立評估結果
            StockEvaluationResult result = StockEvaluationResult.builder()
                    .stockId(stockId)
                    .matched(true)
                    .signalType(signalType)
                    .confidenceScore(confidenceScore)
                    .matchedConditions(evalResult.getMatchedConditions())
                    .factorValues(new HashMap<>(factorData))
                    .build();

            log.debug("股票 {} 符合策略條件，信心度: {}", stockId, confidenceScore);
            return result;

        } catch (Exception e) {
            log.error("股票評估失敗: {} 策略: {}", stockId, strategy.getStrategyId(), e);
            return StockEvaluationResult.error(stockId, e.getMessage());
        }
    }

    /**
     * 評估股票並生成信號
     *
     * @param strategy    策略
     * @param stockId     股票代碼
     * @param factorData  因子數據
     * @param executionId 執行 ID
     * @param tradeDate   交易日期
     * @return 策略信號（如不符合條件則返回 null）
     */
    public StrategySignal evaluateAndGenerateSignal(
            Strategy strategy,
            String stockId,
            Map<String, Object> factorData,
            String executionId,
            LocalDate tradeDate) {

        StockEvaluationResult evalResult = evaluateStock(strategy, stockId, factorData);

        if (!evalResult.isMatched()) {
            return null;
        }

        return signalGenerator.generate(strategy, evalResult, executionId, tradeDate);
    }

    /**
     * 從策略配置取得信心度公式
     */
    private String getConfidenceFormula(Strategy strategy) {
        Map<String, Object> outputConfig = strategy.getOutputConfig();
        if (outputConfig != null) {
            Object formula = outputConfig.get("confidence_formula");
            if (formula != null) {
                return formula.toString();
            }
        }
        return null; // 使用預設計算
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
        return SignalType.BUY;
    }

    /**
     * 生成執行 ID
     */
    private String generateExecutionId(LocalDate date) {
        int counter = executionCounter.incrementAndGet();
        if (counter > 999) {
            executionCounter.set(0);
        }
        return String.format("EXEC_%s_%03d", date.format(DATE_FORMATTER), counter);
    }
}
