package com.chris.fin_shark.m07.domain;

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
 * 技術指標實體
 * <p>
 * 對應資料表: technical_indicators
 * 功能編號: F-M07-001, F-M07-002, F-M07-003, F-M07-004
 * </p>
 * <p>
 * 設計說明:
 * <ul>
 *   <li>資料庫主鍵為 (indicator_id, calculation_date)，但 JPA 僅映射 indicator_id</li>
 *   <li>使用 JSONB 儲存各類別指標，提供彈性擴充</li>
 *   <li>冗餘欄位（ma5, rsi_14 等）用於高頻查詢，避免解析 JSON</li>
 *   <li>支援 PostgreSQL 分區表</li>
 * </ul>
 * </p>
 *
 * @author chris
 * @since 1.0.0
 */
@Entity
@Table(name = "technical_indicators")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TechnicalIndicator {

    // ========== 主鍵與識別 ==========

    /**
     * 指標ID（自增主鍵）
     * <p>
     * 注意: 資料庫層面主鍵為 (indicator_id, calculation_date)，
     * 但 JPA 僅映射 indicator_id 以避免複合主鍵問題
     * </p>
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "indicator_id")
    private Long indicatorId;

    /**
     * 股票代碼
     */
    @Column(name = "stock_id", length = 10, nullable = false)
    private String stockId;

    /**
     * 計算日期
     * <p>
     * 注意: 此欄位在資料庫是複合主鍵的一部分，但在 JPA 當作普通欄位
     * </p>
     */
    @Column(name = "calculation_date", nullable = false)
    private LocalDate calculationDate;

    // ========== JSONB 欄位（指標分類儲存） ==========

    /**
     * 趨勢指標 (JSONB)
     * <p>
     * 包含: MA, EMA, MACD, ADX, Aroon, Parabolic SAR, Supertrend 等
     * </p>
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "trend_indicators", columnDefinition = "jsonb")
    private Map<String, Object> trendIndicators;

    /**
     * 動能指標 (JSONB)
     * <p>
     * 包含: RSI, Stochastic, Williams %R, CCI, MFI, ROC, Momentum 等
     * </p>
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "momentum_indicators", columnDefinition = "jsonb")
    private Map<String, Object> momentumIndicators;

    /**
     * 波動性指標 (JSONB)
     * <p>
     * 包含: Bollinger Bands, Keltner Channel, ATR, Donchian Channel 等
     * </p>
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "volatility_indicators", columnDefinition = "jsonb")
    private Map<String, Object> volatilityIndicators;

    /**
     * 成交量指標 (JSONB)
     * <p>
     * 包含: OBV, AD Line, CMF, VWAP, Volume MA 等
     * </p>
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "volume_indicators", columnDefinition = "jsonb")
    private Map<String, Object> volumeIndicators;

    /**
     * 支撐壓力指標 (JSONB)
     * <p>
     * 包含: Pivot Points, Fibonacci Retracement 等
     * </p>
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "support_resistance", columnDefinition = "jsonb")
    private Map<String, Object> supportResistance;

    /**
     * 週期指標 (JSONB)
     * <p>
     * 包含: Hurst Exponent, DPO, Schaff Trend Cycle 等
     * </p>
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "cycle_indicators", columnDefinition = "jsonb")
    private Map<String, Object> cycleIndicators;

    /**
     * 統計指標 (JSONB)
     * <p>
     * 包含: Linear Regression, R-Squared, Correlation 等
     * </p>
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "statistical_indicators", columnDefinition = "jsonb")
    private Map<String, Object> statisticalIndicators;

    /**
     * 綜合指標 (JSONB)
     * <p>
     * 包含: Elder Ray, Coppock Curve, Vortex 等
     * </p>
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "composite_indicators", columnDefinition = "jsonb")
    private Map<String, Object> compositeIndicators;

    // ========== 冗餘欄位（用於快速查詢，避免解析 JSON） ==========

    /**
     * 5日移動平均線
     */
    @Column(name = "ma5", precision = 10, scale = 2)
    private BigDecimal ma5;

    /**
     * 20日移動平均線
     */
    @Column(name = "ma20", precision = 10, scale = 2)
    private BigDecimal ma20;

    /**
     * 60日移動平均線
     */
    @Column(name = "ma60", precision = 10, scale = 2)
    private BigDecimal ma60;

    /**
     * 12日指數移動平均線
     */
    @Column(name = "ema12", precision = 10, scale = 2)
    private BigDecimal ema12;

    /**
     * 26日指數移動平均線
     */
    @Column(name = "ema26", precision = 10, scale = 2)
    private BigDecimal ema26;

    /**
     * MACD 值
     */
    @Column(name = "macd_value", precision = 10, scale = 4)
    private BigDecimal macdValue;

    /**
     * MACD 信號線
     */
    @Column(name = "macd_signal", precision = 10, scale = 4)
    private BigDecimal macdSignal;

    /**
     * MACD 柱狀圖
     */
    @Column(name = "macd_histogram", precision = 10, scale = 4)
    private BigDecimal macdHistogram;

    /**
     * 14日相對強弱指標
     */
    @Column(name = "rsi_14", precision = 5, scale = 2)
    private BigDecimal rsi14;

    /**
     * KD 隨機指標 - K 值
     */
    @Column(name = "stoch_k", precision = 5, scale = 2)
    private BigDecimal stochK;

    /**
     * KD 隨機指標 - D 值
     */
    @Column(name = "stoch_d", precision = 5, scale = 2)
    private BigDecimal stochD;

    /**
     * 布林通道上軌
     */
    @Column(name = "bbands_upper", precision = 10, scale = 2)
    private BigDecimal bbandsUpper;

    /**
     * 布林通道中軌
     */
    @Column(name = "bbands_middle", precision = 10, scale = 2)
    private BigDecimal bbandsMiddle;

    /**
     * 布林通道下軌
     */
    @Column(name = "bbands_lower", precision = 10, scale = 2)
    private BigDecimal bbandsLower;

    /**
     * 14日平均真實範圍
     */
    @Column(name = "atr_14", precision = 10, scale = 4)
    private BigDecimal atr14;

    /**
     * 能量潮指標
     */
    @Column(name = "obv")
    private Long obv;

    /**
     * 14日平均趨向指標
     */
    @Column(name = "adx_14", precision = 5, scale = 2)
    private BigDecimal adx14;

    // ========== 計算資訊 ==========

    /**
     * 計算版本
     */
    @Column(name = "calculation_version", length = 10)
    private String calculationVersion;

    /**
     * 計算引擎
     */
    @Column(name = "calculation_engine", length = 50)
    @Builder.Default
    private String calculationEngine = "pandas-ta";

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

