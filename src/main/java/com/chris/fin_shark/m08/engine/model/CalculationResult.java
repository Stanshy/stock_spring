package com.chris.fin_shark.m08.engine.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * 計算結果容器
 * <p>
 * 儲存所有計算出的財務指標與診斷資訊
 * </p>
 *
 * @author chris
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CalculationResult {

    /** 股票代碼 */
    private String stockId;

    /** 年度 */
    private Integer year;

    /** 季度 */
    private Integer quarter;

    /** 估值指標 */
    @Builder.Default
    private Map<String, BigDecimal> valuationIndicators = new HashMap<>();

    /** 獲利能力指標 */
    @Builder.Default
    private Map<String, BigDecimal> profitabilityIndicators = new HashMap<>();

    /** 財務結構指標 */
    @Builder.Default
    private Map<String, BigDecimal> financialStructureIndicators = new HashMap<>();

    /** 償債能力指標 */
    @Builder.Default
    private Map<String, BigDecimal> solvencyIndicators = new HashMap<>();

    /** 現金流量指標 */
    @Builder.Default
    private Map<String, BigDecimal> cashFlowIndicators = new HashMap<>();

    /** 成長性指標 */
    @Builder.Default
    private Map<String, BigDecimal> growthIndicators = new HashMap<>();

    /** 股利政策指標 */
    @Builder.Default
    private Map<String, BigDecimal> dividendIndicators = new HashMap<>();

    /** 綜合評分 */
    @Builder.Default
    private Map<String, BigDecimal> scores = new HashMap<>();

    /** 診斷資訊 */
    private Diagnostics diagnostics;

    /**
     * 新增估值指標
     */
    public void addValuationIndicator(String name, BigDecimal value) {
        valuationIndicators.put(name, value);
    }

    /**
     * 新增獲利能力指標
     */
    public void addProfitabilityIndicator(String name, BigDecimal value) {
        profitabilityIndicators.put(name, value);
    }

    /**
     * 新增財務結構指標
     */
    public void addFinancialStructureIndicator(String name, BigDecimal value) {
        financialStructureIndicators.put(name, value);
    }

    /**
     * 新增償債能力指標
     */
    public void addSolvencyIndicator(String name, BigDecimal value) {
        solvencyIndicators.put(name, value);
    }

    /**
     * 新增現金流量指標
     */
    public void addCashFlowIndicator(String name, BigDecimal value) {
        cashFlowIndicators.put(name, value);
    }

    /**
     * 新增成長性指標
     */
    public void addGrowthIndicator(String name, BigDecimal value) {
        growthIndicators.put(name, value);
    }

    /**
     * 新增股利政策指標
     */
    public void addDividendIndicator(String name, BigDecimal value) {
        dividendIndicators.put(name, value);
    }

    /**
     * 新增綜合評分
     */
    public void addScore(String name, BigDecimal value) {
        scores.put(name, value);
    }

    /**
     * 檢查是否有錯誤
     */
    public boolean hasErrors() {
        return diagnostics != null &&
                diagnostics.getErrors() != null &&
                !diagnostics.getErrors().isEmpty();
    }

    /**
     * 取得所有指標數量
     */
    public int getTotalIndicatorCount() {
        return valuationIndicators.size()
                + profitabilityIndicators.size()
                + financialStructureIndicators.size()
                + solvencyIndicators.size()
                + cashFlowIndicators.size()
                + growthIndicators.size()
                + dividendIndicators.size()
                + scores.size();
    }
}
