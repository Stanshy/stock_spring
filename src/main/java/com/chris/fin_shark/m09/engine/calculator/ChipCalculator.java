package com.chris.fin_shark.m09.engine.calculator;

import com.chris.fin_shark.m09.engine.model.ChipMetadata;
import com.chris.fin_shark.m09.engine.model.ChipSeries;
import com.chris.fin_shark.m09.enums.ChipCategory;

import java.util.Map;

/**
 * 籌碼計算器介面
 * <p>
 * 所有籌碼指標計算器必須實作此介面。
 * 與 M07 IndicatorCalculator 結構對齊。
 * </p>
 *
 * @author chris
 * @since 1.0.0
 */
public interface ChipCalculator {

    /**
     * 計算器名稱（唯一識別）
     */
    String getName();

    /**
     * 計算器類別
     */
    ChipCategory getCategory();

    /**
     * 取得元資料
     */
    ChipMetadata getMetadata();

    /**
     * 計算籌碼指標
     *
     * @param series 籌碼資料序列
     * @param params 計算參數
     * @return 計算結果（key-value 形式）
     */
    Map<String, Object> calculate(ChipSeries series, Map<String, Object> params);

    /**
     * 驗證資料是否足夠
     *
     * @param series 籌碼資料序列
     * @param params 計算參數
     * @return 是否有足夠資料
     */
    default boolean hasEnoughData(ChipSeries series, Map<String, Object> params) {
        int required = getMetadata().getMinDataDays();
        return series.size() >= required;
    }
}
