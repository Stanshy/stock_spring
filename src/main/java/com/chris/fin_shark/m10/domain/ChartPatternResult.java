package com.chris.fin_shark.m10.domain;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 圖表型態辨識結果實體
 * <p>
 * 對應資料表: chart_pattern_results
 * </p>
 *
 * @author chris
 * @since 1.0.0
 */
@Entity
@Table(name = "chart_pattern_results")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChartPatternResult {

    /**
     * 結果 ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "result_id")
    private Long resultId;

    /**
     * 股票代碼
     */
    @Column(name = "stock_id", nullable = false, length = 10)
    private String stockId;

    /**
     * 偵測日期
     */
    @Column(name = "detection_date", nullable = false)
    private LocalDate detectionDate;

    /**
     * 型態編號
     */
    @Column(name = "pattern_id", nullable = false, length = 20)
    private String patternId;

    /**
     * 型態名稱（中文）
     */
    @Column(name = "pattern_name", nullable = false, length = 50)
    private String patternName;

    /**
     * 型態名稱（英文）
     */
    @Column(name = "english_name", length = 50)
    private String englishName;

    /**
     * 型態類別: REVERSAL, CONTINUATION, GAP, BILATERAL
     */
    @Column(name = "pattern_category", nullable = false, length = 20)
    private String patternCategory;

    /**
     * 型態狀態: FORMING, CONFIRMED, COMPLETED, FAILED, INVALIDATED
     */
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private String status = "FORMING";

    /**
     * 訊號類型
     */
    @Column(name = "signal_type", nullable = false, length = 30)
    private String signalType;

    /**
     * 型態強度（0-100）
     */
    @Column(name = "strength", nullable = false)
    private Integer strength;

    /**
     * 形成開始日期
     */
    @Column(name = "formation_start", nullable = false)
    private LocalDate formationStart;

    /**
     * 形成結束日期
     */
    @Column(name = "formation_end")
    private LocalDate formationEnd;

    /**
     * 持續天數
     */
    @Column(name = "duration_days")
    private Integer durationDays;

    /**
     * 關鍵價位（JSONB）
     * 包含峰值、谷值、頸線等資訊
     */
    @Column(name = "key_levels", nullable = false, columnDefinition = "jsonb")
    private String keyLevels;

    /**
     * 目標價
     */
    @Column(name = "target_price", precision = 10, scale = 2)
    private BigDecimal targetPrice;

    /**
     * 止損價
     */
    @Column(name = "stop_loss_price", precision = 10, scale = 2)
    private BigDecimal stopLossPrice;

    /**
     * 潛在漲跌幅百分比
     */
    @Column(name = "potential_move_pct", precision = 5, scale = 2)
    private BigDecimal potentialMovePct;

    /**
     * 風險報酬比
     */
    @Column(name = "risk_reward_ratio", precision = 5, scale = 2)
    private BigDecimal riskRewardRatio;

    /**
     * 完成標準描述
     */
    @Column(name = "completion_criteria", columnDefinition = "text")
    private String completionCriteria;

    /**
     * 突破價位
     */
    @Column(name = "breakout_level", precision = 10, scale = 2)
    private BigDecimal breakoutLevel;

    /**
     * 突破方向: UP, DOWN
     */
    @Column(name = "breakout_direction", length = 10)
    private String breakoutDirection;

    /**
     * 成交量型態
     */
    @Column(name = "volume_pattern", length = 50)
    private String volumePattern;

    /**
     * 成交量是否確認
     */
    @Column(name = "volume_confirmation")
    private Boolean volumeConfirmation;

    /**
     * 可靠度評估因素（JSONB）
     */
    @Column(name = "reliability_factors", columnDefinition = "jsonb")
    private String reliabilityFactors;

    /**
     * 型態描述
     */
    @Column(name = "description", columnDefinition = "text")
    private String description;

    /**
     * 建立時間
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * 更新時間
     */
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
