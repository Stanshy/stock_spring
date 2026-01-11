package com.chris.fin_shark.common.dto.external;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * FinMind 財務報表資料傳輸物件
 * <p>
 * 用於封裝從 FinMind API 取得的財務報表資料
 * API 端點: https://api.finmindtrade.com/api/v4/data
 * Dataset: TaiwanStockFinancialStatements
 * </p>
 *
 * @author chris
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FinMindFinancialData {

    /**
     * 股票代碼
     */
    private String stockId;

    /**
     * 資料日期
     */
    private LocalDate date;

    /**
     * 年度
     */
    private Integer year;

    /**
     * 季度 (1-4)
     */
    private Short quarter;

    /**
     * 報表類型 (Q=季報, A=年報)
     */
    private String reportType;

    // ========== 損益表欄位 ==========

    /**
     * 營業收入
     */
    private BigDecimal revenue;

    /**
     * 營業成本
     */
    private BigDecimal operatingCost;

    /**
     * 毛利
     */
    private BigDecimal grossProfit;

    /**
     * 營業費用
     */
    private BigDecimal operatingExpense;

    /**
     * 營業利益
     */
    private BigDecimal operatingIncome;

    /**
     * 稅前淨利
     */
    private BigDecimal preTaxIncome;

    /**
     * 稅後淨利
     */
    private BigDecimal netIncome;

    // ========== 資產負債表欄位 ==========

    /**
     * 總資產
     */
    private BigDecimal totalAssets;

    /**
     * 總負債
     */
    private BigDecimal totalLiabilities;

    /**
     * 股東權益
     */
    private BigDecimal equity;

    /**
     * 流動資產
     */
    private BigDecimal currentAssets;

    /**
     * 流動負債
     */
    private BigDecimal currentLiabilities;

    // ========== 現金流量表欄位 ==========

    /**
     * 營業活動現金流量
     */
    private BigDecimal operatingCashFlow;

    /**
     * 投資活動現金流量
     */
    private BigDecimal investingCashFlow;

    /**
     * 融資活動現金流量
     */
    private BigDecimal financingCashFlow;

    // ========== 每股指標 ==========

    /**
     * 每股盈餘 (EPS)
     */
    private BigDecimal eps;

    /**
     * 每股淨值 (BPS)
     */
    private BigDecimal bps;
}
