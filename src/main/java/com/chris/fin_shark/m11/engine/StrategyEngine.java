package com.chris.fin_shark.m11.engine;

import com.chris.fin_shark.m11.domain.Strategy;

import java.util.Map;

/**
 * 策略執行引擎介面
 * <p>
 * 核心設計原則：
 * 1. 純計算邏輯，不依賴外部資源
 * 2. 可獨立單元測試
 * 3. 可替換實現
 * </p>
 *
 * @author chris
 * @since 1.0.0
 */
public interface StrategyEngine {

    /**
     * 執行策略
     *
     * @param strategy 策略定義
     * @param plan     執行計劃
     * @return 執行結果
     */
    StrategyExecutionResult execute(Strategy strategy, StrategyExecutionPlan plan);

    /**
     * 評估單一股票
     *
     * @param strategy   策略定義
     * @param stockId    股票代碼
     * @param factorData 因子數據
     * @return 評估結果
     */
    StockEvaluationResult evaluateStock(
            Strategy strategy,
            String stockId,
            Map<String, Object> factorData
    );
}
