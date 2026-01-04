package com.chris.fin_shark.m07.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * 技術指標資料傳輸物件
 * <p>
 * 用於 API 請求和回應
 * </p>
 *
 * @author chris
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TechnicalIndicatorDTO {

    /** 指標ID */
    @JsonProperty("indicator_id")
    private Long indicatorId;

    /** 股票代碼 */
    @JsonProperty("stock_id")
    private String stockId;

    /** 計算日期 */
    @JsonProperty("calculation_date")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate calculationDate;

    // ========== JSONB 欄位 ==========

    /** 趨勢指標 */
    @JsonProperty("trend")
    private Map<String, Object> trendIndicators;

    /** 動能指標 */
    @JsonProperty("momentum")
    private Map<String, Object> momentumIndicators;

    /** 波動性指標 */
    @JsonProperty("volatility")
    private Map<String, Object> volatilityIndicators;

    /** 成交量指標 */
    @JsonProperty("volume")
    private Map<String, Object> volumeIndicators;

    /** 支撐壓力指標 */
    @JsonProperty("support_resistance")
    private Map<String, Object> supportResistance;

    /** 週期指標 */
    @JsonProperty("cycle")
    private Map<String, Object> cycleIndicators;

    /** 統計指標 */
    @JsonProperty("statistical")
    private Map<String, Object> statisticalIndicators;

    /** 綜合指標 */
    @JsonProperty("composite")
    private Map<String, Object> compositeIndicators;

    // ========== 冗餘欄位（常用指標） ==========

    /** 5日移動平均線 */
    @JsonProperty("ma5")
    private BigDecimal ma5;

    /** 20日移動平均線 */
    @JsonProperty("ma20")
    private BigDecimal ma20;

    /** 60日移動平均線 */
    @JsonProperty("ma60")
    private BigDecimal ma60;

    /** 12日指數移動平均線 */
    @JsonProperty("ema12")
    private BigDecimal ema12;

    /** 26日指數移動平均線 */
    @JsonProperty("ema26")
    private BigDecimal ema26;

    /** MACD 值 */
    @JsonProperty("macd_value")
    private BigDecimal macdValue;

    /** MACD 信號線 */
    @JsonProperty("macd_signal")
    private BigDecimal macdSignal;

    /** MACD 柱狀圖 */
    @JsonProperty("macd_histogram")
    private BigDecimal macdHistogram;

    /** 14日相對強弱指標 */
    @JsonProperty("rsi_14")
    private BigDecimal rsi14;

    /** KD 隨機指標 - K 值 */
    @JsonProperty("stoch_k")
    private BigDecimal stochK;

    /** KD 隨機指標 - D 值 */
    @JsonProperty("stoch_d")
    private BigDecimal stochD;

    /** 布林通道上軌 */
    @JsonProperty("bbands_upper")
    private BigDecimal bbandsUpper;

    /** 布林通道中軌 */
    @JsonProperty("bbands_middle")
    private BigDecimal bbandsMiddle;

    /** 布林通道下軌 */
    @JsonProperty("bbands_lower")
    private BigDecimal bbandsLower;

    /** 14日平均真實範圍 */
    @JsonProperty("atr_14")
    private BigDecimal atr14;

    /** 能量潮指標 */
    @JsonProperty("obv")
    private Long obv;

    /** 14日平均趨向指標 */
    @JsonProperty("adx_14")
    private BigDecimal adx14;

    // ========== 計算資訊 ==========

    /** 計算版本 */
    @JsonProperty("calculation_version")
    private String calculationVersion;

    /** 計算引擎 */
    @JsonProperty("calculation_engine")
    private String calculationEngine;

    // ========== 審計欄位 ==========

    /** 建立時間 */
    @JsonProperty("created_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    /** 更新時間 */
    @JsonProperty("updated_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
}
