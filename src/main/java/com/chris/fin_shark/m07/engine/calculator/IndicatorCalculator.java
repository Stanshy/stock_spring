package com.chris.fin_shark.m07.engine.calculator;

import com.chris.fin_shark.m07.engine.model.IndicatorMetadata;
import com.chris.fin_shark.m07.engine.model.PriceSeries;

import java.util.Map;

/**
 * 指標計算器介面
 * <p>
 * 所有指標計算器必須實作此介面
 * </p>
 *
 * @author chris
 * @since 1.0.0
 */
public interface IndicatorCalculator {

    /**
     * 指標名稱（唯一識別）
     */
    String getName();

    /**
     * 指標類別
     */
    String getCategory();

    /**
     * 元資料
     */
    IndicatorMetadata getMetadata();

    /**
     * 計算指標
     *
     * @param series 價格序列
     * @param params 參數
     * @return 計算結果
     */
    Map<String, Object> calculate(PriceSeries series, Map<String, Object> params);

    /**
     * 驗證資料是否足夠
     */
    default boolean hasEnoughData(PriceSeries series, Map<String, Object> params) {
        int required = getMetadata().getMinDataPoints();
        return series.size() >= required;
    }
}
