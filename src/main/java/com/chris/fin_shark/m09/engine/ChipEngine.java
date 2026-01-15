package com.chris.fin_shark.m09.engine;

import com.chris.fin_shark.m09.engine.model.ChipSeries;

import java.util.Map;

/**
 * 籌碼計算引擎介面
 * <p>
 * 核心設計原則：
 * 1. 純計算邏輯，不依賴外部資源（DB、HTTP）
 * 2. 可獨立單元測試
 * 3. 可替換實現
 * </p>
 * <p>
 * 與 M07 IndicatorEngine 結構對齊。
 * </p>
 *
 * @author chris
 * @since 1.0.0
 */
public interface ChipEngine {

    /**
     * 計算籌碼指標
     *
     * @param series 籌碼資料序列
     * @param plan   計算計劃
     * @return 計算結果
     */
    ChipResult compute(ChipSeries series, ChipPlan plan);

    /**
     * 批次計算
     *
     * @param seriesMap 股票代碼 → 籌碼資料序列
     * @param plan      計算計劃
     * @return 股票代碼 → 計算結果
     */
    Map<String, ChipResult> batchCompute(
            Map<String, ChipSeries> seriesMap,
            ChipPlan plan
    );
}
