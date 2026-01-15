package com.chris.fin_shark.m10.engine;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

/**
 * 型態偵測計劃
 * <p>
 * 定義要執行哪些型態偵測
 * </p>
 *
 * @author chris
 * @since 1.0.0
 */
@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class PatternDetectionPlan {

    /**
     * 是否偵測 K 線型態
     */
    @Builder.Default
    private boolean includeKLinePatterns = true;

    /**
     * 是否偵測圖表型態
     */
    @Builder.Default
    private boolean includeChartPatterns = true;

    /**
     * 是否偵測趨勢型態
     */
    @Builder.Default
    private boolean includeTrendPatterns = true;

    /**
     * 是否識別支撐壓力
     */
    @Builder.Default
    private boolean includeSupportResistance = false;

    /**
     * 是否產生訊號
     */
    @Builder.Default
    private boolean includeSignals = true;

    /**
     * 最低型態強度（0-100）
     */
    @Builder.Default
    private int minPatternStrength = 50;

    /**
     * 回溯天數
     */
    @Builder.Default
    private int lookbackPeriod = 120;

    /**
     * 優先級過濾（null 表示所有）
     */
    private String priorityFilter;

    /**
     * 指定要偵測的型態 ID（null 或空表示所有）
     */
    @Builder.Default
    private Set<String> patternIds = new HashSet<>();

    /**
     * 指定要排除的型態 ID
     */
    @Builder.Default
    private Set<String> excludePatternIds = new HashSet<>();

    /**
     * 是否強制重新計算（忽略快取）
     */
    @Builder.Default
    private boolean forceRecalculate = false;

    // === 預設計劃 ===

    /**
     * 完整偵測計劃
     */
    public static PatternDetectionPlan full() {
        return PatternDetectionPlan.builder()
                .includeKLinePatterns(true)
                .includeChartPatterns(true)
                .includeTrendPatterns(true)
                .includeSupportResistance(true)
                .includeSignals(true)
                .minPatternStrength(50)
                .lookbackPeriod(120)
                .build();
    }

    /**
     * 快速計劃：只偵測 K 線型態
     */
    public static PatternDetectionPlan quick() {
        return PatternDetectionPlan.builder()
                .includeKLinePatterns(true)
                .includeChartPatterns(false)
                .includeTrendPatterns(false)
                .includeSupportResistance(false)
                .includeSignals(false)
                .minPatternStrength(60)
                .lookbackPeriod(30)
                .build();
    }

    /**
     * 掃描計劃：全市場掃描用
     */
    public static PatternDetectionPlan scan() {
        return PatternDetectionPlan.builder()
                .includeKLinePatterns(true)
                .includeChartPatterns(false)
                .includeTrendPatterns(false)
                .includeSupportResistance(false)
                .includeSignals(true)
                .minPatternStrength(70)
                .lookbackPeriod(30)
                .build();
    }

    /**
     * 只偵測 K 線型態
     */
    public static PatternDetectionPlan klineOnly() {
        return PatternDetectionPlan.builder()
                .includeKLinePatterns(true)
                .includeChartPatterns(false)
                .includeTrendPatterns(false)
                .includeSupportResistance(false)
                .includeSignals(true)
                .minPatternStrength(50)
                .lookbackPeriod(60)
                .build();
    }

    /**
     * 只偵測圖表型態
     */
    public static PatternDetectionPlan chartOnly() {
        return PatternDetectionPlan.builder()
                .includeKLinePatterns(false)
                .includeChartPatterns(true)
                .includeTrendPatterns(false)
                .includeSupportResistance(false)
                .includeSignals(true)
                .minPatternStrength(50)
                .lookbackPeriod(120)
                .build();
    }

    /**
     * 只偵測趨勢
     */
    public static PatternDetectionPlan trendOnly() {
        return PatternDetectionPlan.builder()
                .includeKLinePatterns(false)
                .includeChartPatterns(false)
                .includeTrendPatterns(true)
                .includeSupportResistance(true)
                .includeSignals(false)
                .minPatternStrength(50)
                .lookbackPeriod(120)
                .build();
    }

    // === 便捷方法 ===

    /**
     * 是否應該偵測指定的型態 ID
     */
    public boolean shouldDetect(String patternId) {
        // 如果有排除清單且包含此型態，則不偵測
        if (excludePatternIds != null && excludePatternIds.contains(patternId)) {
            return false;
        }
        // 如果有指定清單但不包含此型態，則不偵測
        if (patternIds != null && !patternIds.isEmpty() && !patternIds.contains(patternId)) {
            return false;
        }
        return true;
    }

    /**
     * 添加要偵測的型態 ID
     */
    public PatternDetectionPlan withPatternId(String patternId) {
        if (this.patternIds == null) {
            this.patternIds = new HashSet<>();
        }
        this.patternIds.add(patternId);
        return this;
    }

    /**
     * 添加要排除的型態 ID
     */
    public PatternDetectionPlan withoutPatternId(String patternId) {
        if (this.excludePatternIds == null) {
            this.excludePatternIds = new HashSet<>();
        }
        this.excludePatternIds.add(patternId);
        return this;
    }
}
