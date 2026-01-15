package com.chris.fin_shark.m09.domain;

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
 * 籌碼分析結果實體
 * <p>
 * 對應資料表: chip_analysis_results
 * 功能編號: F-M09-001, F-M09-002, F-M09-003, F-M09-004
 * </p>
 * <p>
 * 設計說明:
 * <ul>
 *   <li>資料庫主鍵為 (result_id, trade_date)，但 JPA 僅映射 result_id</li>
 *   <li>使用 JSONB 儲存詳細指標，提供彈性擴充</li>
 *   <li>冗餘欄位（foreign_net, margin_balance 等）用於高頻查詢與排行榜</li>
 *   <li>支援 PostgreSQL 分區表</li>
 * </ul>
 * </p>
 *
 * @author chris
 * @since 1.0.0
 */
@Entity
@Table(name = "chip_analysis_results")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChipAnalysisResult {

    // ========== 主鍵與識別 ==========

    /**
     * 結果ID（自增主鍵）
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "result_id")
    private Long resultId;

    /**
     * 股票代碼
     */
    @Column(name = "stock_id", length = 10, nullable = false)
    private String stockId;

    /**
     * 交易日期
     */
    @Column(name = "trade_date", nullable = false)
    private LocalDate tradeDate;

    // ========== 三大法人核心指標（冗餘欄位） ==========

    /**
     * 外資買賣超（股）
     */
    @Column(name = "foreign_net")
    private Long foreignNet;

    /**
     * 外資買賣超 5 日均
     */
    @Column(name = "foreign_net_ma5", precision = 15, scale = 2)
    private BigDecimal foreignNetMa5;

    /**
     * 外資買賣超 20 日均
     */
    @Column(name = "foreign_net_ma20", precision = 15, scale = 2)
    private BigDecimal foreignNetMa20;

    /**
     * 外資連續買超天數（負數表示連續賣超）
     */
    @Column(name = "foreign_continuous_days")
    private Integer foreignContinuousDays;

    /**
     * 外資 20 日累計買賣超
     */
    @Column(name = "foreign_accumulated_20d")
    private Long foreignAccumulated20d;

    /**
     * 投信買賣超（股）
     */
    @Column(name = "trust_net")
    private Long trustNet;

    /**
     * 投信買賣超 5 日均
     */
    @Column(name = "trust_net_ma5", precision = 15, scale = 2)
    private BigDecimal trustNetMa5;

    /**
     * 投信連續買超天數
     */
    @Column(name = "trust_continuous_days")
    private Integer trustContinuousDays;

    /**
     * 自營商買賣超（股）
     */
    @Column(name = "dealer_net")
    private Long dealerNet;

    /**
     * 三大法人合計買賣超（股）
     */
    @Column(name = "total_net")
    private Long totalNet;

    // ========== 融資融券核心指標（冗餘欄位） ==========

    /**
     * 融資餘額（股）
     */
    @Column(name = "margin_balance")
    private Long marginBalance;

    /**
     * 融資增減（股）
     */
    @Column(name = "margin_change")
    private Long marginChange;

    /**
     * 融資使用率（%）
     */
    @Column(name = "margin_usage_rate", precision = 5, scale = 2)
    private BigDecimal marginUsageRate;

    /**
     * 融資連續增加天數
     */
    @Column(name = "margin_continuous_days")
    private Integer marginContinuousDays;

    /**
     * 融券餘額（股）
     */
    @Column(name = "short_balance")
    private Long shortBalance;

    /**
     * 融券增減（股）
     */
    @Column(name = "short_change")
    private Long shortChange;

    /**
     * 券資比（%）
     */
    @Column(name = "margin_short_ratio", precision = 5, scale = 2)
    private BigDecimal marginShortRatio;

    // ========== 籌碼集中度 ==========

    /**
     * 法人持股比例估算（%）
     */
    @Column(name = "institutional_ratio", precision = 5, scale = 2)
    private BigDecimal institutionalRatio;

    /**
     * 籌碼集中趨勢
     */
    @Column(name = "concentration_trend", length = 20)
    private String concentrationTrend;

    // ========== JSONB 欄位（詳細指標） ==========

    /**
     * 三大法人詳細指標 (JSONB)
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "institutional_indicators", columnDefinition = "jsonb")
    private Map<String, Object> institutionalIndicators;

    /**
     * 融資融券詳細指標 (JSONB)
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "margin_indicators", columnDefinition = "jsonb")
    private Map<String, Object> marginIndicators;

    /**
     * 籌碼集中度詳細指標 (JSONB)
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "concentration_indicators", columnDefinition = "jsonb")
    private Map<String, Object> concentrationIndicators;

    // ========== 籌碼評分 ==========

    /**
     * 籌碼評分（0-100）
     */
    @Column(name = "chip_score")
    private Integer chipScore;

    /**
     * 籌碼評級（A/B/C/D/F）
     */
    @Column(name = "chip_grade", length = 2)
    private String chipGrade;

    // ========== 計算資訊 ==========

    /**
     * 計算耗時（毫秒）
     */
    @Column(name = "calculation_time_ms")
    private Integer calculationTimeMs;

    // ========== 審計欄位 ==========

    /**
     * 建立時間
     */
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    /**
     * 更新時間
     */
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
