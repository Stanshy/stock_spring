package com.chris.fin_shark.m08.engine;

import com.chris.fin_shark.m08.engine.model.CalculationResult;
import com.chris.fin_shark.m08.engine.model.FinancialData;
import com.chris.fin_shark.m08.engine.model.IndicatorMetadata;

/**
 * 基本面指標計算器介面
 * <p>
 * 所有財務指標計算器必須實作此介面
 * 參考 M07 技術指標引擎設計模式
 * </p>
 *
 * @author chris
 * @since 1.0.0
 */
public interface FundamentalCalculator {

    /**
     * 執行指標計算
     *
     * @param data   財務資料輸入
     * @param result 計算結果容器
     */
    void calculate(FinancialData data, CalculationResult result);

    /**
     * 取得指標元數據
     *
     * @return 指標元數據（名稱、描述、分類等）
     */
    IndicatorMetadata getMetadata();

    /**
     * 取得計算器類別
     *
     * @return 計算器類別（VALUATION, PROFITABILITY 等）
     */
    default String getCategory() {
        return getMetadata().getCategory();
    }
}
