package com.chris.fin_shark.m10.engine.model;

import com.chris.fin_shark.m10.enums.PatternCategory;
import com.chris.fin_shark.m10.enums.PatternStatus;
import com.chris.fin_shark.m10.enums.SignalType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 偵測到的型態
 * <p>
 * 表示單個被偵測到的型態實例
 * </p>
 *
 * @author chris
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DetectedPattern {

    /**
     * 型態 ID（如 KLINE001, CHART003）
     */
    private String patternId;

    /**
     * 型態名稱（中文）
     */
    private String patternName;

    /**
     * 型態名稱（英文）
     */
    private String englishName;

    /**
     * 型態類別
     */
    private PatternCategory category;

    /**
     * 偵測日期（型態出現的最後一天）
     */
    private LocalDate detectionDate;

    /**
     * 訊號類型
     */
    private SignalType signalType;

    /**
     * 型態狀態
     */
    @Builder.Default
    private PatternStatus status = PatternStatus.CONFIRMED;

    /**
     * 型態強度（0-100）
     */
    private int strength;

    /**
     * 信心度（0-100）
     */
    private Integer confidence;

    /**
     * 涉及的交易日期
     */
    private List<LocalDate> involvedDates;

    /**
     * 涉及的 K 線資料
     */
    private List<CandleStick> involvedCandles;

    // === 價格相關 ===

    /**
     * 型態最低價
     */
    private BigDecimal patternLow;

    /**
     * 型態最高價
     */
    private BigDecimal patternHigh;

    /**
     * 目標價
     */
    private BigDecimal targetPrice;

    /**
     * 止損價
     */
    private BigDecimal stopLoss;

    /**
     * 頸線價位（用於頭肩、雙重頂底等）
     */
    private BigDecimal neckline;

    /**
     * 突破價位
     */
    private BigDecimal breakoutLevel;

    // === 成交量 ===

    /**
     * 成交量是否確認
     */
    @Builder.Default
    private boolean volumeConfirmation = false;

    /**
     * 成交量比率
     */
    private BigDecimal volumeRatio;

    // === 趨勢背景 ===

    /**
     * 趨勢背景（出現在什麼趨勢中）
     */
    private String trendContext;

    // === 其他 ===

    /**
     * 型態描述
     */
    private String description;

    /**
     * 關鍵價位（JSONB 儲存彈性結構）
     */
    private Map<String, Object> keyLevels;

    /**
     * 可靠度因素
     */
    private Map<String, Object> reliabilityFactors;

    // === 便捷方法 ===

    /**
     * 是否為看漲型態
     */
    public boolean isBullish() {
        return signalType != null && signalType.isBullish();
    }

    /**
     * 是否為看跌型態
     */
    public boolean isBearish() {
        return signalType != null && signalType.isBearish();
    }

    /**
     * 是否為 K 線型態
     */
    public boolean isKLinePattern() {
        return category != null && category.isKLinePattern();
    }

    /**
     * 是否為圖表型態
     */
    public boolean isChartPattern() {
        return category != null && category.isChartPattern();
    }

    /**
     * 是否為強型態（強度 >= 70）
     */
    public boolean isStrong() {
        return strength >= 70;
    }

    /**
     * 計算風險報酬比
     */
    public BigDecimal getRiskRewardRatio() {
        if (targetPrice == null || stopLoss == null || patternHigh == null) {
            return null;
        }
        BigDecimal currentPrice = patternHigh; // 假設以型態高點為進場價
        BigDecimal potentialGain = targetPrice.subtract(currentPrice).abs();
        BigDecimal potentialLoss = currentPrice.subtract(stopLoss).abs();

        if (potentialLoss.compareTo(BigDecimal.ZERO) == 0) {
            return null;
        }
        return potentialGain.divide(potentialLoss, 2, java.math.RoundingMode.HALF_UP);
    }
}
