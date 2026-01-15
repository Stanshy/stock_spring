package com.chris.fin_shark.m10.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * K 線型態辨識結果實體
 * <p>
 * 對應資料表: kline_pattern_results (按年份分區)
 * </p>
 *
 * @author chris
 * @since 1.0.0
 */
@Entity
@Table(name = "kline_pattern_results")
@IdClass(KLinePatternResultId.class)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KLinePatternResult {

    /**
     * 結果 ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "result_id")
    private Long resultId;

    /**
     * 交易日期（分區鍵）
     */
    @Id
    @Column(name = "trade_date", nullable = false)
    private LocalDate tradeDate;

    /**
     * 股票代碼
     */
    @Column(name = "stock_id", nullable = false, length = 10)
    private String stockId;

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
     * 型態類別: SINGLE_KLINE, DOUBLE_KLINE, TRIPLE_KLINE, MULTI_KLINE
     */
    @Column(name = "pattern_category", nullable = false, length = 20)
    private String patternCategory;

    /**
     * 訊號類型: BULLISH_REVERSAL, BEARISH_REVERSAL, BULLISH_CONTINUATION,
     *          BEARISH_CONTINUATION, NEUTRAL_REVERSAL, NEUTRAL
     */
    @Column(name = "signal_type", nullable = false, length = 30)
    private String signalType;

    /**
     * 型態強度（0-100）
     */
    @Column(name = "strength", nullable = false)
    private Integer strength;

    /**
     * 信心度（0-100）
     */
    @Column(name = "confidence")
    private Integer confidence;

    /**
     * 涉及的交易日期（PostgreSQL DATE[] 陣列，以逗號分隔字串儲存）
     * MyBatis 使用 DateArrayTypeHandler 處理
     */
    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(
            name = "involved_dates",
            nullable = false,
            columnDefinition = "date[]"
    )
    private LocalDate[] involvedDates;

    /**
     * 型態最低價
     */
    @Column(name = "pattern_low", precision = 10, scale = 2)
    private BigDecimal patternLow;

    /**
     * 型態最高價
     */
    @Column(name = "pattern_high", precision = 10, scale = 2)
    private BigDecimal patternHigh;

    /**
     * K 線詳細資料（JSONB）
     */
    @Column(name = "candle_data", columnDefinition = "jsonb")
    private String candleData;

    /**
     * 成交量是否確認
     */
    @Column(name = "volume_confirmation")
    @Builder.Default
    private Boolean volumeConfirmation = false;

    /**
     * 成交量比率
     */
    @Column(name = "volume_ratio", precision = 5, scale = 2)
    private BigDecimal volumeRatio;

    /**
     * 趨勢背景
     */
    @Column(name = "trend_context", length = 20)
    private String trendContext;

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

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
