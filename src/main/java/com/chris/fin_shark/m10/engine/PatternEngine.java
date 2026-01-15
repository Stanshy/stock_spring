package com.chris.fin_shark.m10.engine;

import com.chris.fin_shark.m07.engine.model.PriceSeries;

import java.util.Map;

/**
 * 型態偵測引擎介面
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
public interface PatternEngine {

    /**
     * 偵測型態
     *
     * @param series 價格序列
     * @param plan   偵測計劃
     * @return 偵測結果
     */
    PatternDetectionResult detect(PriceSeries series, PatternDetectionPlan plan);

    /**
     * 批次偵測
     *
     * @param seriesMap 股票代碼 → 價格序列
     * @param plan      偵測計劃
     * @return 股票代碼 → 偵測結果
     */
    Map<String, PatternDetectionResult> batchDetect(
            Map<String, PriceSeries> seriesMap,
            PatternDetectionPlan plan
    );

    /**
     * 使用完整計劃偵測
     */
    default PatternDetectionResult detectFull(PriceSeries series) {
        return detect(series, PatternDetectionPlan.full());
    }

    /**
     * 使用快速計劃偵測（只偵測 K 線型態）
     */
    default PatternDetectionResult detectQuick(PriceSeries series) {
        return detect(series, PatternDetectionPlan.quick());
    }
}
