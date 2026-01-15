package com.chris.fin_shark.m10.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 趨勢分析結果實體
 * <p>
 * 對應資料表: trend_analysis_results
 * </p>
 *
 * @author chris
 * @since 1.0.0
 */
@Entity
@Table(name = "trend_analysis_results",
        uniqueConstraints = @UniqueConstraint(columnNames = {"stock_id", "analysis_date"}))
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrendAnalysisResult {

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
     * 分析日期
     */
    @Column(name = "analysis_date", nullable = false)
    private LocalDate analysisDate;

    // === 主要趨勢 ===

    /**
     * 主要趨勢 ID
     */
    @Column(name = "primary_trend_id", nullable = false, length = 20)
    private String primaryTrendId;

    /**
     * 主要趨勢名稱
     */
    @Column(name = "primary_trend_name", nullable = false, length = 30)
    private String primaryTrendName;

    /**
     * 主要趨勢強度（0-100）
     */
    @Column(name = "primary_strength", nullable = false)
    private Integer primaryStrength;

    /**
     * 趨勢持續天數
     */
    @Column(name = "trend_duration_days")
    private Integer trendDurationDays;

    /**
     * 趨勢開始日期
     */
    @Column(name = "trend_start_date")
    private LocalDate trendStartDate;

    /**
     * 趨勢開始價格
     */
    @Column(name = "trend_start_price", precision = 10, scale = 2)
    private BigDecimal trendStartPrice;

    /**
     * 趨勢漲跌幅百分比
     */
    @Column(name = "trend_gain_pct", precision = 8, scale = 2)
    private BigDecimal trendGainPct;

    // === 次要趨勢 ===

    /**
     * 次要趨勢 ID
     */
    @Column(name = "secondary_trend_id", length = 20)
    private String secondaryTrendId;

    /**
     * 次要趨勢名稱
     */
    @Column(name = "secondary_trend_name", length = 30)
    private String secondaryTrendName;

    /**
     * 次要趨勢強度
     */
    @Column(name = "secondary_strength")
    private Integer secondaryStrength;

    // === 均線分析 ===

    /**
     * 5 日均線
     */
    @Column(name = "ma5", precision = 10, scale = 2)
    private BigDecimal ma5;

    /**
     * 10 日均線
     */
    @Column(name = "ma10", precision = 10, scale = 2)
    private BigDecimal ma10;

    /**
     * 20 日均線
     */
    @Column(name = "ma20", precision = 10, scale = 2)
    private BigDecimal ma20;

    /**
     * 60 日均線
     */
    @Column(name = "ma60", precision = 10, scale = 2)
    private BigDecimal ma60;

    /**
     * 120 日均線
     */
    @Column(name = "ma120", precision = 10, scale = 2)
    private BigDecimal ma120;

    /**
     * 均線排列: BULLISH, BEARISH, MIXED, NEUTRAL
     */
    @Column(name = "ma_alignment", length = 20)
    private String maAlignment;

    /**
     * 均線排列強度
     */
    @Column(name = "ma_alignment_strength")
    private Integer maAlignmentStrength;

    // === 趨勢指標 ===

    /**
     * ADX 值
     */
    @Column(name = "adx_value", precision = 5, scale = 2)
    private BigDecimal adxValue;

    /**
     * DI+
     */
    @Column(name = "di_plus", precision = 5, scale = 2)
    private BigDecimal diPlus;

    /**
     * DI-
     */
    @Column(name = "di_minus", precision = 5, scale = 2)
    private BigDecimal diMinus;

    /**
     * 趨勢強度等級
     */
    @Column(name = "trend_strength_level", length = 20)
    private String trendStrengthLevel;

    // === 結構分析 ===

    /**
     * 創高次數
     */
    @Column(name = "higher_highs_count")
    @Builder.Default
    private Integer higherHighsCount = 0;

    /**
     * 墊高次數
     */
    @Column(name = "higher_lows_count")
    @Builder.Default
    private Integer higherLowsCount = 0;

    /**
     * 壓低次數
     */
    @Column(name = "lower_highs_count")
    @Builder.Default
    private Integer lowerHighsCount = 0;

    /**
     * 創低次數
     */
    @Column(name = "lower_lows_count")
    @Builder.Default
    private Integer lowerLowsCount = 0;

    /**
     * 結構類型: BULLISH, BEARISH, NEUTRAL, TRANSITIONING
     */
    @Column(name = "structure_type", length = 20)
    private String structureType;

    // === 趨勢品質 ===

    /**
     * 一致性
     */
    @Column(name = "consistency", precision = 5, scale = 2)
    private BigDecimal consistency;

    /**
     * 波動率
     */
    @Column(name = "volatility", precision = 5, scale = 2)
    private BigDecimal volatility;

    /**
     * 動能: POSITIVE, NEGATIVE, NEUTRAL, ACCELERATING, DECELERATING
     */
    @Column(name = "momentum", length = 20)
    private String momentum;

    // === 詳細分析 ===

    /**
     * 詳細分析（JSONB）
     */
    @Column(name = "detailed_analysis", columnDefinition = "jsonb")
    @Builder.Default
    private String detailedAnalysis = "{}";

    /**
     * 警告訊息（PostgreSQL TEXT[]）
     */
    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "warnings", nullable = false, columnDefinition = "text[]")
    private String[] warnings;

    // === 預測 ===

    /**
     * 短期預測
     */
    @Column(name = "short_term_forecast", length = 20)
    private String shortTermForecast;

    /**
     * 中期預測
     */
    @Column(name = "medium_term_forecast", length = 20)
    private String mediumTermForecast;

    /**
     * 預測信心度
     */
    @Column(name = "forecast_confidence")
    private Integer forecastConfidence;

    // === 審計欄位 ===

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
