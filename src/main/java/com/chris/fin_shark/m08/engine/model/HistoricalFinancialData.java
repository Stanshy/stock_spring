package com.chris.fin_shark.m08.engine.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 歷史財務資料
 * <p>
 * 用於計算成長率、趨勢等需要歷史資料的指標
 * </p>
 *
 * @author chris
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HistoricalFinancialData {

    /** 年度 */
    private Integer year;

    /** 季度 */
    private Integer quarter;

    /** 營收 */
    private BigDecimal revenue;

    /** 淨利 */
    private BigDecimal netIncome;

    /** EPS */
    private BigDecimal eps;

    /** ROE */
    private BigDecimal roe;

    /** 總資產 */
    private BigDecimal totalAssets;

    /** 股東權益 */
    private BigDecimal totalEquity;
}
