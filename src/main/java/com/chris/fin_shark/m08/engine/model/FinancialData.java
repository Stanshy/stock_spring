package com.chris.fin_shark.m08.engine.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 財務資料輸入模型
 * <p>
 * 封裝計算財務指標所需的所有輸入資料
 * </p>
 *
 * @author chris
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FinancialData {

    // ========== 基本資訊 ==========

    /** 股票代碼 */
    private String stockId;

    /** 年度 */
    private Integer year;

    /** 季度 */
    private Integer quarter;

    /** 計算日期 */
    private LocalDate calculationDate;

    // ========== 來自 M06 財報資料 ==========
    // 需要: com.chris.fin_shark.m06.domain.FinancialStatement

    /** 營收（千元） */
    private BigDecimal revenue;

    /** 營業成本（千元） */
    private BigDecimal operatingCost;

    /** 營業利益（千元） */
    private BigDecimal operatingIncome;

    /** 稅後淨利（千元） */
    private BigDecimal netIncome;

    /** 總資產（千元） */
    private BigDecimal totalAssets;

    /** 流動資產（千元） */
    private BigDecimal currentAssets;

    /** 總負債（千元） */
    private BigDecimal totalLiabilities;

    /** 流動負債（千元） */
    private BigDecimal currentLiabilities;

    /** 股東權益（千元） */
    private BigDecimal totalEquity;

    /** 營運現金流（千元） */
    private BigDecimal operatingCashFlow;

    /** 投資現金流（千元） */
    private BigDecimal investingCashFlow;

    /** 融資現金流（千元） */
    private BigDecimal financingCashFlow;

    /** 資本支出（千元） */
    private BigDecimal capitalExpenditure;

    /** 每股盈餘 EPS */
    private BigDecimal eps;

    /** 每股淨值 */
    private BigDecimal bookValuePerShare;

    /** 流通股數（股） */
    private Long outstandingShares;

    // ========== 來自 M06 股價資料 ==========
    // 需要: com.chris.fin_shark.m06.domain.StockPrice

    /** 股價（計算日收盤價） */
    private BigDecimal stockPrice;

    /** 市值（千元） */
    private BigDecimal marketCap;

    // ========== 歷史資料（用於成長率計算） ==========
    // 需要: 去年同季財報資料

    /** 去年同季營收 */
    private BigDecimal lastYearRevenue;

    /** 去年同季淨利 */
    private BigDecimal lastYearNetIncome;

    /** 去年同季 EPS */
    private BigDecimal lastYearEps;

    /** 去年同季 ROE */
    private BigDecimal lastYearRoe;

    /** 上一季淨利 */
    private BigDecimal lastQuarterNetIncome;

    // ========== 股利資料 ==========

    /** 現金股利（元/股） */
    private BigDecimal cashDividend;

    /** 股票股利（元/股） */
    private BigDecimal stockDividend;

    // ========== 歷史趨勢資料（用於進階計算） ==========

    /** 歷史財報資料列表（用於計算 CAGR、趨勢等） */
    private List<HistoricalFinancialData> historicalData;

    /**
     * 驗證資料完整性
     *
     * @return 驗證是否通過
     */
    public boolean validate() {
        // 核心欄位不可為 null
        return stockId != null && year != null && quarter != null
                && revenue != null && netIncome != null
                && totalAssets != null && totalEquity != null;
    }

    /**
     * 計算自由現金流
     *
     * @return 自由現金流
     */
    public BigDecimal calculateFreeCashFlow() {
        if (operatingCashFlow == null || capitalExpenditure == null) {
            return null;
        }
        return operatingCashFlow.subtract(capitalExpenditure);
    }
}
