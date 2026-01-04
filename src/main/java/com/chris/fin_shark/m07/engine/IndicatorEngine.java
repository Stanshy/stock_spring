package com.chris.fin_shark.m07.engine;

import com.chris.fin_shark.m07.engine.model.PriceSeries;

import java.util.Map;

/**
 * 技術指標計算引擎
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
public interface IndicatorEngine {

    /**
     * 計算技術指標
     *
     * @param series 價格序列
     * @param plan   計算計劃
     * @return 計算結果
     */
    IndicatorResult compute(PriceSeries series, IndicatorPlan plan);

    /**
     * 批次計算
     *
     * @param seriesMap 股票代碼 → 價格序列
     * @param plan      計算計劃
     * @return 股票代碼 → 計算結果
     */
    Map<String, IndicatorResult> batchCompute(
            Map<String, PriceSeries> seriesMap,
            IndicatorPlan plan
    );
}
