package com.chris.fin_shark.m08.domain;

import com.chris.fin_shark.m08.enums.ReportType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * 基本面財務指標實體
 * <p>
 * 對應資料表: fundamental_indicators
 * 使用自增主鍵 + UNIQUE 約束
 * </p>
 *
 * @author chris
 * @since 1.0.0
 */
@Entity
@Table(name = "fundamental_indicators",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_fundamental_indicators",
                        columnNames = {"stock_id", "year", "quarter", "report_type"}
                )
        })
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FundamentalIndicator {

    // ========== 主鍵 ==========

    /** 指標 ID（自增主鍵） */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "indicator_id")
    private Long indicatorId;

    // ========== 業務主鍵（UNIQUE 約束） ==========

    /** 股票代碼 */
    @Column(name = "stock_id", length = 10, nullable = false)
    private String stockId;

    /** 年度 */
    @Column(name = "year", nullable = false)
    private Integer year;

    /** 季度（1-4） */
    @Column(name = "quarter", nullable = false)
    private Integer quarter;

    /** 報表類型（Q=季報, H=半年報, Y=年報） */

    @Enumerated(EnumType.STRING)
    @Column(name = "report_type", length = 10, nullable = false)
    private ReportType reportType;

    // ========== 基本資訊 ==========

    /** 計算日期 */
    @Column(name = "calculation_date", nullable = false)
    private LocalDate calculationDate;

    /** 股價（計算日收盤價） */
    @Column(name = "stock_price", precision = 10, scale = 2)
    private BigDecimal stockPrice;

    /** 計算版本 */
    @Column(name = "calculation_version", length = 10)
    private String calculationVersion;

    // ========== 估值指標 (JSONB) ==========

    @Column(name = "valuation_indicators", columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, BigDecimal> valuationIndicators;

    // ========== 獲利能力指標 (JSONB) ==========

    @Column(name = "profitability_indicators", columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, BigDecimal> profitabilityIndicators;

    // ========== 財務結構指標 (JSONB) ==========

    @Column(name = "financial_structure_indicators", columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, BigDecimal> financialStructureIndicators;

    // ========== 償債能力指標 (JSONB) ==========

    @Column(name = "solvency_indicators", columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, BigDecimal> solvencyIndicators;

    // ========== 經營效率指標 (JSONB) ==========

    @Column(name = "efficiency_indicators", columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, BigDecimal> efficiencyIndicators;

    // ========== 現金流量指標 (JSONB) ==========

    @Column(name = "cash_flow_indicators", columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, BigDecimal> cashFlowIndicators;

    // ========== 成長性指標 (JSONB) ==========

    @Column(name = "growth_indicators", columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, BigDecimal> growthIndicators;

    // ========== 股利政策指標 (JSONB) ==========

    @Column(name = "dividend_indicators", columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, BigDecimal> dividendIndicators;

    // ========== 常用指標（冗餘欄位，加速查詢） ==========

    /** 本益比 P/E */
    @Column(name = "pe_ratio", precision = 10, scale = 2)
    private BigDecimal peRatio;

    /** 股價淨值比 P/B */
    @Column(name = "pb_ratio", precision = 10, scale = 2)
    private BigDecimal pbRatio;

    /** 股價營收比 P/S */
    @Column(name = "ps_ratio", precision = 10, scale = 2)
    private BigDecimal psRatio;

    /** 股東權益報酬率 ROE (%) */
    @Column(name = "roe", precision = 10, scale = 2)
    private BigDecimal roe;

    /** 總資產報酬率 ROA (%) */
    @Column(name = "roa", precision = 10, scale = 2)
    private BigDecimal roa;

    /** 毛利率 (%) */
    @Column(name = "gross_margin", precision = 10, scale = 2)
    private BigDecimal grossMargin;

    /** 營業利益率 (%) */
    @Column(name = "operating_margin", precision = 10, scale = 2)
    private BigDecimal operatingMargin;

    /** 淨利率 (%) */
    @Column(name = "net_margin", precision = 10, scale = 2)
    private BigDecimal netMargin;

    /** 每股盈餘 EPS */
    @Column(name = "eps", precision = 10, scale = 2)
    private BigDecimal eps;

    /** 負債比率 (%) */
    @Column(name = "debt_ratio", precision = 10, scale = 2)
    private BigDecimal debtRatio;

    /** 流動比率 */
    @Column(name = "current_ratio", precision = 10, scale = 2)
    private BigDecimal currentRatio;

    /** 速動比率 */
    @Column(name = "quick_ratio", precision = 10, scale = 2)
    private BigDecimal quickRatio;

    /** 自由現金流 FCF (千元) */
    @Column(name = "fcf")
    private Long fcf;

    /** FCF 殖利率 (%) */
    @Column(name = "fcf_yield", precision = 10, scale = 2)
    private BigDecimal fcfYield;

    /** 現金殖利率 (%) */
    @Column(name = "dividend_yield", precision = 10, scale = 2)
    private BigDecimal dividendYield;

    /** 營收成長率 (%) */
    @Column(name = "revenue_growth", precision = 10, scale = 2)
    private BigDecimal revenueGrowth;

    /** EPS 成長率 (%) */
    @Column(name = "eps_growth", precision = 10, scale = 2)
    private BigDecimal epsGrowth;

    // ========== 時間戳 ==========

    /** 建立時間 */
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    /** 更新時間 */
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;


}
