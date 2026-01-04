package com.chris.fin_shark.m06.domain;

import io.hypersistence.utils.hibernate.type.json.JsonBinaryType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * 財務報表實體
 * <p>
 * 對應資料表: financial_statements
 * 包含 4 個 JSONB 欄位儲存完整財報資料
 * 支援 JPA 和 MyBatis 混搭使用
 * </p>
 *
 * @author chris
 * @since 1.0.0
 */
@Entity
@Table(name = "financial_statements")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FinancialStatement {

    /** 財報 ID（自動遞增主鍵） */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "statement_id")
    private Long statementId;

    /** 股票代碼 */
    @Column(name = "stock_id", length = 10, nullable = false)
    private String stockId;

    /** 年份 */
    @Column(name = "year", nullable = false)
    private Integer year;

    /** 季度（1-4） */
    @Column(name = "quarter", nullable = false)
    private Short quarter;

    /** 報表類型（Q=季報, A=年報） */
    @Column(name = "report_type", length = 2, nullable = false)
    private String reportType;

    // ========== 損益表欄位 ==========

    /** 營業收入 */
    @Column(name = "revenue", precision = 20, scale = 2)
    private BigDecimal revenue;

    /** 營業利益 */
    @Column(name = "operating_income", precision = 20, scale = 2)
    private BigDecimal operatingIncome;

    /** 稅後淨利 */
    @Column(name = "net_income", precision = 20, scale = 2)
    private BigDecimal netIncome;

    /** 毛利 */
    @Column(name = "gross_profit", precision = 20, scale = 2)
    private BigDecimal grossProfit;

    /** 營業費用 */
    @Column(name = "operating_expense", precision = 20, scale = 2)
    private BigDecimal operatingExpense;

    // ========== 資產負債表欄位 ==========

    /** 總資產 */
    @Column(name = "total_assets", precision = 20, scale = 2)
    private BigDecimal totalAssets;

    /** 總負債 */
    @Column(name = "total_liabilities", precision = 20, scale = 2)
    private BigDecimal totalLiabilities;

    /** 股東權益 */
    @Column(name = "equity", precision = 20, scale = 2)
    private BigDecimal equity;

    /** 流動資產 */
    @Column(name = "current_assets", precision = 20, scale = 2)
    private BigDecimal currentAssets;

    /** 流動負債 */
    @Column(name = "current_liabilities", precision = 20, scale = 2)
    private BigDecimal currentLiabilities;

    // ========== 現金流量表欄位 ==========

    /** 營業活動現金流量 */
    @Column(name = "operating_cash_flow", precision = 20, scale = 2)
    private BigDecimal operatingCashFlow;

    /** 投資活動現金流量 */
    @Column(name = "investing_cash_flow", precision = 20, scale = 2)
    private BigDecimal investingCashFlow;

    /** 融資活動現金流量 */
    @Column(name = "financing_cash_flow", precision = 20, scale = 2)
    private BigDecimal financingCashFlow;

    /** 自由現金流量 */
    @Column(name = "free_cash_flow", precision = 20, scale = 2)
    private BigDecimal freeCashFlow;

    // ========== 每股指標 ==========

    /** 每股盈餘（EPS） */
    @Column(name = "eps", precision = 10, scale = 2)
    private BigDecimal eps;

    /** 每股淨值（BPS） */
    @Column(name = "bps", precision = 10, scale = 2)
    private BigDecimal bps;

    // ========== JSONB 完整資料 ==========

    /** 完整損益表（JSONB） */
    @Type(JsonBinaryType.class)
    @Column(name = "income_statement", columnDefinition = "jsonb")
    private Map<String, Object> incomeStatement;

    /** 完整資產負債表（JSONB） */
    @Type(JsonBinaryType.class)
    @Column(name = "balance_sheet", columnDefinition = "jsonb")
    private Map<String, Object> balanceSheet;

    /** 完整現金流量表（JSONB） */
    @Type(JsonBinaryType.class)
    @Column(name = "cash_flow_statement", columnDefinition = "jsonb")
    private Map<String, Object> cashFlowStatement;

    /** 財務比率（ROE/ROA/負債比等，JSONB） */
    @Type(JsonBinaryType.class)
    @Column(name = "financial_ratios", columnDefinition = "jsonb")
    private Map<String, Object> financialRatios;

    // ========== 元資料 ==========

    /** 公告日期 */
    @Column(name = "publish_date")
    private LocalDate publishDate;

    /** 資料來源（例如: MOPS） */
    @Column(name = "source", length = 50)
    private String source;

    /** 建立時間 */
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    /** 更新時間 */
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * 新增前自動設定時間戳
     */
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    /**
     * 更新前自動設定更新時間
     */
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
