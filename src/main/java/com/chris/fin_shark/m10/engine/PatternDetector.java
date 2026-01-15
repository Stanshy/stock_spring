package com.chris.fin_shark.m10.engine;

import com.chris.fin_shark.m07.engine.model.PriceSeries;
import com.chris.fin_shark.m10.engine.model.DetectedPattern;
import com.chris.fin_shark.m10.engine.model.PatternMetadata;
import com.chris.fin_shark.m10.enums.TrendDirection;

import java.util.List;
import java.util.Map;

/**
 * 型態偵測器介面
 * <p>
 * 所有型態偵測器必須實作此介面
 * </p>
 *
 * @author chris
 * @since 1.0.0
 */
public interface PatternDetector {

    /**
     * 偵測器名稱（唯一識別）
     */
    String getName();

    /**
     * 型態類別（KLINE_SINGLE, KLINE_DOUBLE, CHART_REVERSAL, etc.）
     */
    String getCategory();

    /**
     * 優先級（P0, P1, P2）
     */
    String getPriority();

    /**
     * 偵測器支援的型態 ID 清單
     */
    List<String> getSupportedPatternIds();

    /**
     * 取得型態元資料
     *
     * @param patternId 型態 ID
     * @return 元資料，若不支援則返回 null
     */
    PatternMetadata getMetadata(String patternId);

    /**
     * 偵測型態
     *
     * @param series  價格序列
     * @param params  偵測參數
     * @param context 趨勢背景（可為 null）
     * @return 偵測到的型態列表
     */
    List<DetectedPattern> detect(PriceSeries series, Map<String, Object> params, TrendDirection context);

    /**
     * 偵測型態（無趨勢背景）
     */
    default List<DetectedPattern> detect(PriceSeries series, Map<String, Object> params) {
        return detect(series, params, null);
    }

    /**
     * 偵測型態（使用預設參數）
     */
    default List<DetectedPattern> detect(PriceSeries series) {
        return detect(series, Map.of(), null);
    }

    /**
     * 最少需要的資料點數
     */
    default int getMinDataPoints() {
        return 1;
    }

    /**
     * 檢查資料是否足夠
     */
    default boolean hasEnoughData(PriceSeries series) {
        return series != null && series.size() >= getMinDataPoints();
    }

    /**
     * 是否啟用
     */
    default boolean isEnabled() {
        return true;
    }
}
