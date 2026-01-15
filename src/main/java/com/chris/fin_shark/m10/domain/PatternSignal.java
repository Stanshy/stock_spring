package com.chris.fin_shark.m10.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 型態訊號實體
 * <p>
 * 對應資料表: pattern_signals
 * </p>
 *
 * @author chris
 * @since 1.0.0
 */
@Entity
@Table(name = "pattern_signals")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PatternSignal {

    /**
     * 訊號 ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "signal_id")
    private Long signalId;

    /**
     * 股票代碼
     */
    @Column(name = "stock_id", nullable = false, length = 10)
    private String stockId;

    /**
     * 交易日期
     */
    @Column(name = "trade_date", nullable = false)
    private LocalDate tradeDate;

    // === 訊號資訊 ===

    /**
     * 訊號代碼（如 PTN_SIG_001）
     */
    @Column(name = "signal_code", nullable = false, length = 20)
    private String signalCode;

    /**
     * 訊號名稱
     */
    @Column(name = "signal_name", nullable = false, length = 50)
    private String signalName;

    /**
     * 訊號類型: BUY, SELL, WATCH
     */
    @Column(name = "signal_type", nullable = false, length = 10)
    private String signalType;

    /**
     * 來源類別: KLINE, CHART, TREND, SUPPORT_RESISTANCE
     */
    @Column(name = "source_category", nullable = false, length = 15)
    private String sourceCategory;

    // === 來源型態 ===

    /**
     * 來源型態 ID
     */
    @Column(name = "source_pattern_id", length = 20)
    private String sourcePatternId;

    /**
     * 來源型態名稱
     */
    @Column(name = "source_pattern_name", length = 50)
    private String sourcePatternName;

    // === 觸發資訊 ===

    /**
     * 觸發價格
     */
    @Column(name = "trigger_price", precision = 10, scale = 2)
    private BigDecimal triggerPrice;

    /**
     * 當前價格
     */
    @Column(name = "current_price", precision = 10, scale = 2)
    private BigDecimal currentPrice;

    /**
     * 信心度（0-100）
     */
    @Column(name = "confidence", nullable = false)
    private Integer confidence;

    /**
     * 強度: CRITICAL, HIGH, MEDIUM, LOW
     */
    @Column(name = "strength", nullable = false, length = 15)
    private String strength;

    // === 目標與風險 ===

    /**
     * 目標價
     */
    @Column(name = "target_price", precision = 10, scale = 2)
    private BigDecimal targetPrice;

    /**
     * 止損價
     */
    @Column(name = "stop_loss", precision = 10, scale = 2)
    private BigDecimal stopLoss;

    /**
     * 目標漲幅百分比
     */
    @Column(name = "target_gain_pct", precision = 5, scale = 2)
    private BigDecimal targetGainPct;

    /**
     * 止損幅度百分比
     */
    @Column(name = "stop_loss_pct", precision = 5, scale = 2)
    private BigDecimal stopLossPct;

    /**
     * 風險報酬比
     */
    @Column(name = "risk_reward_ratio", precision = 5, scale = 2)
    private BigDecimal riskRewardRatio;

    // === 支持因素 ===

    /**
     * 支持因素（PostgreSQL TEXT[]）
     */
    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "supporting_factors", nullable = false, columnDefinition = "text[]")
    private String[] supportingFactors;

    // === 描述 ===

    /**
     * 訊號描述
     */
    @Column(name = "description", columnDefinition = "text")
    private String description;

    // === 狀態追蹤 ===

    /**
     * 狀態: ACTIVE, TRIGGERED, EXPIRED, CANCELLED
     */
    @Column(name = "status", length = 20)
    @Builder.Default
    private String status = "ACTIVE";

    /**
     * 結果: SUCCESS, FAILURE, PARTIAL
     */
    @Column(name = "outcome", length = 20)
    private String outcome;

    /**
     * 結果日期
     */
    @Column(name = "outcome_date")
    private LocalDate outcomeDate;

    /**
     * 實際漲跌幅百分比
     */
    @Column(name = "actual_gain_pct", precision = 8, scale = 2)
    private BigDecimal actualGainPct;

    // === 審計欄位 ===

    /**
     * 建立時間
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
