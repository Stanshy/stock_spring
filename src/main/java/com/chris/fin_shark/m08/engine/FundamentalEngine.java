package com.chris.fin_shark.m08.engine;

import com.chris.fin_shark.m08.engine.model.CalculationPlan;
import com.chris.fin_shark.m08.engine.model.CalculationResult;
import com.chris.fin_shark.m08.engine.model.FinancialData;

import java.util.List;

/**
 * 基本面分析引擎介面
 * <p>
 * 功能編號: F-M08-001 ~ F-M08-008
 * 負責協調所有財務指標計算器的執行
 * </p>
 *
 * @author chris
 * @since 1.0.0
 */
public interface FundamentalEngine {

    /**
     * 執行完整指標計算（所有 P0 核心指標）
     *
     * @param data 財務資料
     * @return 計算結果
     */
    CalculationResult calculate(FinancialData data);

    /**
     * 執行部分指標計算（依據計算計劃）
     *
     * @param data 財務資料
     * @param plan 計算計劃（指定要計算的指標類別）
     * @return 計算結果
     */
    CalculationResult calculate(FinancialData data, CalculationPlan plan);

    /**
     * 取得引擎支援的指標清單
     *
     * @return 指標名稱列表
     */
    List<String> getSupportedIndicators();
}
