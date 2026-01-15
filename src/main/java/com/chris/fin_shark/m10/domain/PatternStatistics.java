package com.chris.fin_shark.m10.domain;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 型態統計實體
 * <p>
 * 對應資料表: pattern_statistics
 * 定期更新的快取資料
 * </p>
 *
 * @author chris
 * @since 1.0.0
 */
@Entity
@Table(name = "pattern_statistics",
        uniqueConstraints = @UniqueConstraint(
                columnNames = {"stock_id", "pattern_id", "stat_period_start", "stat_period_end"}))
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PatternStatistics {

    /**
     * 統計 ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "stat_id")
    private Long statId;

    /**
     * 股票代碼
     */
    @Column(name = "stock_id", nullable = false, length = 10)
    private String stockId;

    /**
     * 型態 ID
     */
    @Column(name = "pattern_id", nullable = false, length = 20)
    private String patternId;

    // === 統計期間 ===

    /**
     * 統計期間開始日期
     */
    @Column(name = "stat_period_start", nullable = false)
    private LocalDate statPeriodStart;

    /**
     * 統計期間結束日期
     */
    @Column(name = "stat_period_end", nullable = false)
    private LocalDate statPeriodEnd;

    /**
     * 交易天數
     */
    @Column(name = "trading_days")
    private Integer tradingDays;

    // === 基本統計 ===

    /**
     * 出現次數
     */
    @Column(name = "occurrence_count", nullable = false)
    @Builder.Default
    private Integer occurrenceCount = 0;

    /**
     * 成功次數
     */
    @Column(name = "success_count", nullable = false)
    @Builder.Default
    private Integer successCount = 0;

    /**
     * 失敗次數
     */
    @Column(name = "failure_count", nullable = false)
    @Builder.Default
    private Integer failureCount = 0;

    /**
     * 待定次數
     */
    @Column(name = "pending_count", nullable = false)
    @Builder.Default
    private Integer pendingCount = 0;

    /**
     * 成功率（百分比）
     */
    @Column(name = "success_rate", precision = 5, scale = 2)
    private BigDecimal successRate;

    // === 收益統計 ===

    /**
     * 1 日平均漲跌幅
     */
    @Column(name = "avg_gain_1d", precision = 8, scale = 4)
    private BigDecimal avgGain1d;

    /**
     * 3 日平均漲跌幅
     */
    @Column(name = "avg_gain_3d", precision = 8, scale = 4)
    private BigDecimal avgGain3d;

    /**
     * 5 日平均漲跌幅
     */
    @Column(name = "avg_gain_5d", precision = 8, scale = 4)
    private BigDecimal avgGain5d;

    /**
     * 10 日平均漲跌幅
     */
    @Column(name = "avg_gain_10d", precision = 8, scale = 4)
    private BigDecimal avgGain10d;

    /**
     * 20 日平均漲跌幅
     */
    @Column(name = "avg_gain_20d", precision = 8, scale = 4)
    private BigDecimal avgGain20d;

    /**
     * 最大漲幅
     */
    @Column(name = "max_gain", precision = 8, scale = 4)
    private BigDecimal maxGain;

    /**
     * 最大跌幅
     */
    @Column(name = "max_loss", precision = 8, scale = 4)
    private BigDecimal maxLoss;

    /**
     * 失敗時平均虧損
     */
    @Column(name = "avg_loss_when_failed", precision = 8, scale = 4)
    private BigDecimal avgLossWhenFailed;

    // === 最佳條件 ===

    /**
     * 最佳觸發條件（JSONB）
     */
    @Column(name = "optimal_conditions", columnDefinition = "jsonb")
    @Builder.Default
    private String optimalConditions = "{}";

    // === 信心度 ===

    /**
     * 信心度（0-100）
     */
    @Column(name = "confidence")
    private Integer confidence;

    // === 審計欄位 ===

    /**
     * 產生時間
     */
    @Column(name = "generated_at", nullable = false, updatable = false)
    private LocalDateTime generatedAt;

    /**
     * 過期時間
     */
    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @PrePersist
    protected void onCreate() {
        generatedAt = LocalDateTime.now();
    }
}
