package com.chris.fin_shark.m06.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

/**
 * 財務報表資料傳輸物件
 *
 * @author chris
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FinancialStatementDTO {

    /** 財報 ID */
    @JsonProperty("statement_id")
    private Long statementId;

    /** 股票代碼 */
    @JsonProperty("stock_id")
    private String stockId;

    /** 年份 */
    private Integer year;

    /** 季度 */
    private Short quarter;

    /** 報表類型（Q=季報, A=年報） */
    @JsonProperty("report_type")
    private String reportType;

    // ========== 損益表 ==========

    /** 營業收入 */
    private BigDecimal revenue;

    /** 營業利益 */
    @JsonProperty("operating_income")
    private BigDecimal operatingIncome;

    /** 稅後淨利 */
    @JsonProperty("net_income")
    private BigDecimal netIncome;

    /** 毛利 */
    @JsonProperty("gross_profit")
    private BigDecimal grossProfit;

    /** 營業費用 */
    @JsonProperty("operating_expense")
    private BigDecimal operatingExpense;

    // ========== 資產負債表 ==========

    /** 總資產 */
    @JsonProperty("total_assets")
    private BigDecimal totalAssets;

    /** 總負債 */
    @JsonProperty("total_liabilities")
    private BigDecimal totalLiabilities;

    /** 股東權益 */
    private BigDecimal equity;

    /** 流動資產 */
    @JsonProperty("current_assets")
    private BigDecimal currentAssets;

    /** 流動負債 */
    @JsonProperty("current_liabilities")
    private BigDecimal currentLiabilities;

    // ========== 現金流量表 ==========

    /** 營業活動現金流量 */
    @JsonProperty("operating_cash_flow")
    private BigDecimal operatingCashFlow;

    /** 投資活動現金流量 */
    @JsonProperty("investing_cash_flow")
    private BigDecimal investingCashFlow;

    /** 融資活動現金流量 */
    @JsonProperty("financing_cash_flow")
    private BigDecimal financingCashFlow;

    /** 自由現金流量 */
    @JsonProperty("free_cash_flow")
    private BigDecimal freeCashFlow;

    // ========== 每股指標 ==========

    /** 每股盈餘 */
    private BigDecimal eps;

    /** 每股淨值 */
    private BigDecimal bps;

    // ========== JSONB 完整資料 ==========

    /** 完整損益表 */
    @JsonProperty("income_statement")
    private Map<String, Object> incomeStatement;

    /** 完整資產負債表 */
    @JsonProperty("balance_sheet")
    private Map<String, Object> balanceSheet;

    /** 完整現金流量表 */
    @JsonProperty("cash_flow_statement")
    private Map<String, Object> cashFlowStatement;

    /** 財務比率 */
    @JsonProperty("financial_ratios")
    private Map<String, Object> financialRatios;

    // ========== 元資料 ==========

    /** 公告日期 */
    @JsonProperty("publish_date")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate publishDate;

    /** 資料來源 */
    private String source;
}
