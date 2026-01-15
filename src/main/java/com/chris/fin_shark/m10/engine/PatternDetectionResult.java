package com.chris.fin_shark.m10.engine;

import com.chris.fin_shark.m10.engine.model.DetectedPattern;
import com.chris.fin_shark.m10.enums.SignalType;
import com.chris.fin_shark.m10.enums.TrendDirection;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 型態偵測結果
 * <p>
 * 封裝單支股票的完整偵測結果
 * </p>
 *
 * @author chris
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PatternDetectionResult {

    /**
     * 股票代碼
     */
    private String stockId;

    /**
     * 偵測日期
     */
    private LocalDate detectionDate;

    /**
     * 當前價格
     */
    private BigDecimal currentPrice;

    // === 偵測到的型態 ===

    /**
     * K 線型態
     */
    @Builder.Default
    private List<DetectedPattern> klinePatterns = new ArrayList<>();

    /**
     * 圖表型態
     */
    @Builder.Default
    private List<DetectedPattern> chartPatterns = new ArrayList<>();

    /**
     * 趨勢分析
     */
    private TrendAnalysis trendAnalysis;

    /**
     * 支撐壓力位
     */
    @Builder.Default
    private List<SupportResistanceLevel> supportResistanceLevels = new ArrayList<>();

    /**
     * 型態訊號
     */
    @Builder.Default
    private List<PatternSignal> signals = new ArrayList<>();

    /**
     * 診斷資訊
     */
    @Builder.Default
    private Diagnostics diagnostics = new Diagnostics();

    // === 內部類別 ===

    /**
     * 趨勢分析結果
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TrendAnalysis {
        private TrendDirection primaryTrend;
        private int primaryStrength;
        private TrendDirection secondaryTrend;
        private Integer secondaryStrength;
        private int trendDurationDays;
        private LocalDate trendStartDate;
        private BigDecimal trendStartPrice;
        private BigDecimal trendGainPercent;
        private String maAlignment;
        private Integer maAlignmentStrength;
        private BigDecimal adxValue;
        private String structureType;
        private Map<String, Object> keyLevels;
        private List<String> warnings;
    }

    /**
     * 支撐壓力位
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SupportResistanceLevel {
        private BigDecimal price;
        private String levelType;  // SUPPORT, RESISTANCE
        private int strength;
        private String sourceType; // WAVE_PEAK, WAVE_TROUGH, MOVING_AVERAGE, etc.
        private String source;
        private int testCount;
        private LocalDate lastTestDate;
        private BigDecimal distancePercent;
    }

    /**
     * 型態訊號
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PatternSignal {
        private String signalId;
        private String signalName;
        private String signalType;  // BUY, SELL, WATCH
        private String sourceCategory;  // KLINE, CHART, TREND
        private String sourcePatternId;
        private String sourcePatternName;
        private LocalDate triggerDate;
        private BigDecimal triggerPrice;
        private int confidence;
        private String strength;  // CRITICAL, HIGH, MEDIUM, LOW
        private BigDecimal targetPrice;
        private BigDecimal stopLoss;
        private BigDecimal riskRewardRatio;
        private List<String> supportingFactors;
        private String description;
    }

    // === 便捷方法 ===

    /**
     * 取得所有型態
     */
    public List<DetectedPattern> getAllPatterns() {
        List<DetectedPattern> all = new ArrayList<>();
        all.addAll(klinePatterns);
        all.addAll(chartPatterns);
        return all;
    }

    /**
     * 取得看漲型態
     */
    public List<DetectedPattern> getBullishPatterns() {
        return getAllPatterns().stream()
                .filter(DetectedPattern::isBullish)
                .collect(Collectors.toList());
    }

    /**
     * 取得看跌型態
     */
    public List<DetectedPattern> getBearishPatterns() {
        return getAllPatterns().stream()
                .filter(DetectedPattern::isBearish)
                .collect(Collectors.toList());
    }

    /**
     * 取得強型態（強度 >= 70）
     */
    public List<DetectedPattern> getStrongPatterns() {
        return getAllPatterns().stream()
                .filter(DetectedPattern::isStrong)
                .collect(Collectors.toList());
    }

    /**
     * 取得買進訊號
     */
    public List<PatternSignal> getBuySignals() {
        return signals.stream()
                .filter(s -> "BUY".equals(s.getSignalType()))
                .collect(Collectors.toList());
    }

    /**
     * 取得賣出訊號
     */
    public List<PatternSignal> getSellSignals() {
        return signals.stream()
                .filter(s -> "SELL".equals(s.getSignalType()))
                .collect(Collectors.toList());
    }

    /**
     * 取得觀望訊號
     */
    public List<PatternSignal> getWatchSignals() {
        return signals.stream()
                .filter(s -> "WATCH".equals(s.getSignalType()))
                .collect(Collectors.toList());
    }

    /**
     * 是否有錯誤
     */
    public boolean hasErrors() {
        return diagnostics != null && diagnostics.hasErrors();
    }

    /**
     * 是否有警告
     */
    public boolean hasWarnings() {
        return diagnostics != null && diagnostics.hasWarnings();
    }

    /**
     * 取得型態數量統計
     */
    public Map<String, Integer> getPatternSummary() {
        Map<String, Integer> summary = new HashMap<>();
        summary.put("klineCount", klinePatterns.size());
        summary.put("chartCount", chartPatterns.size());
        summary.put("totalCount", klinePatterns.size() + chartPatterns.size());
        summary.put("bullishCount", (int) getAllPatterns().stream()
                .filter(p -> p.getSignalType() != null && p.getSignalType().isBullish()).count());
        summary.put("bearishCount", (int) getAllPatterns().stream()
                .filter(p -> p.getSignalType() != null && p.getSignalType().isBearish()).count());
        summary.put("signalCount", signals.size());
        return summary;
    }

    /**
     * 取得整體偏向
     */
    public SignalType getOverallBias() {
        long bullish = getAllPatterns().stream()
                .filter(p -> p.getSignalType() != null && p.getSignalType().isBullish()).count();
        long bearish = getAllPatterns().stream()
                .filter(p -> p.getSignalType() != null && p.getSignalType().isBearish()).count();

        if (bullish > bearish) {
            return SignalType.BULLISH_REVERSAL;
        } else if (bearish > bullish) {
            return SignalType.BEARISH_REVERSAL;
        } else {
            return SignalType.NEUTRAL;
        }
    }

    // === 添加方法 ===

    /**
     * 添加 K 線型態
     */
    public void addKLinePattern(DetectedPattern pattern) {
        if (klinePatterns == null) {
            klinePatterns = new ArrayList<>();
        }
        klinePatterns.add(pattern);
    }

    /**
     * 添加圖表型態
     */
    public void addChartPattern(DetectedPattern pattern) {
        if (chartPatterns == null) {
            chartPatterns = new ArrayList<>();
        }
        chartPatterns.add(pattern);
    }

    /**
     * 添加訊號
     */
    public void addSignal(PatternSignal signal) {
        if (signals == null) {
            signals = new ArrayList<>();
        }
        signals.add(signal);
    }

    /**
     * 添加支撐壓力位
     */
    public void addSupportResistanceLevel(SupportResistanceLevel level) {
        if (supportResistanceLevels == null) {
            supportResistanceLevels = new ArrayList<>();
        }
        supportResistanceLevels.add(level);
    }
}
