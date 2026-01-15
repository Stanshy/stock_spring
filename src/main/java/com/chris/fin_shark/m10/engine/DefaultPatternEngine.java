package com.chris.fin_shark.m10.engine;

import com.chris.fin_shark.m07.engine.model.PriceSeries;
import com.chris.fin_shark.m10.engine.model.DetectedPattern;
import com.chris.fin_shark.m10.enums.TrendDirection;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 預設型態偵測引擎實作
 * <p>
 * 協調多個偵測器執行型態偵測，根據 PatternDetectionPlan 決定執行範圍
 * </p>
 *
 * @author chris
 * @since 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DefaultPatternEngine implements PatternEngine {

    private final PatternDetectorRegistry registry;

    @Override
    public PatternDetectionResult detect(PriceSeries series, PatternDetectionPlan plan) {
        long startTime = System.currentTimeMillis();

        log.debug("開始型態偵測: stockId={}, plan={}",
                series.getStockId(), describePlan(plan));

        // 初始化結果
        BigDecimal lastClose = series.getClose() != null && !series.getClose().isEmpty()
                ? series.getClose().get(series.getClose().size() - 1)
                : BigDecimal.ZERO;

        PatternDetectionResult result = PatternDetectionResult.builder()
                .stockId(series.getStockId())
                .detectionDate(LocalDate.now())
                .currentPrice(lastClose)
                .diagnostics(new Diagnostics())
                .build();

        int patternsChecked = 0;
        int patternsDetected = 0;

        // 先進行趨勢分析（提供背景資訊給其他偵測器）
        TrendDirection trendContext = TrendDirection.UNKNOWN;
        if (plan.isIncludeTrendPatterns()) {
            trendContext = detectTrend(series, result, plan);
        }

        // 偵測 K 線型態
        if (plan.isIncludeKLinePatterns()) {
            int[] counts = detectKLinePatterns(series, result, plan, trendContext);
            patternsChecked += counts[0];
            patternsDetected += counts[1];
        }

        // 偵測圖表型態
        if (plan.isIncludeChartPatterns()) {
            int[] counts = detectChartPatterns(series, result, plan, trendContext);
            patternsChecked += counts[0];
            patternsDetected += counts[1];
        }

        // 過濾低強度型態
        filterByStrength(result, plan.getMinPatternStrength());

        // 產生訊號
        if (plan.isIncludeSignals()) {
            generateSignals(result);
        }

        // 記錄診斷資訊
        long elapsed = System.currentTimeMillis() - startTime;
        result.getDiagnostics().setCalculationTimeMs(elapsed);
        result.getDiagnostics().setTradingDaysScanned(series.size());
        result.getDiagnostics().setPatternsChecked(patternsChecked);
        result.getDiagnostics().setPatternsDetected(patternsDetected);

        log.debug("型態偵測完成: stockId={}, K線型態={}, 圖表型態={}, 耗時={}ms",
                series.getStockId(),
                result.getKlinePatterns().size(),
                result.getChartPatterns().size(),
                elapsed);

        return result;
    }

    @Override
    public Map<String, PatternDetectionResult> batchDetect(
            Map<String, PriceSeries> seriesMap,
            PatternDetectionPlan plan) {

        log.info("開始批次型態偵測: {} 支股票", seriesMap.size());
        long startTime = System.currentTimeMillis();

        Map<String, PatternDetectionResult> results = new HashMap<>();

        seriesMap.forEach((stockId, series) -> {
            try {
                PatternDetectionResult result = detect(series, plan);
                results.put(stockId, result);
            } catch (Exception e) {
                log.error("股票 {} 偵測失敗: {}", stockId, e.getMessage());
                // 建立空結果
                PatternDetectionResult errorResult = PatternDetectionResult.builder()
                        .stockId(stockId)
                        .detectionDate(LocalDate.now())
                        .diagnostics(new Diagnostics())
                        .build();
                errorResult.getDiagnostics().addError("ENGINE", e.getMessage());
                results.put(stockId, errorResult);
            }
        });

        long elapsed = System.currentTimeMillis() - startTime;
        log.info("批次偵測完成: {} 支股票, 耗時 {}ms", results.size(), elapsed);

        return results;
    }

    // === 私有方法 ===

    /**
     * 偵測趨勢
     */
    private TrendDirection detectTrend(PriceSeries series, PatternDetectionResult result,
                                       PatternDetectionPlan plan) {
        TrendDirection detected = TrendDirection.UNKNOWN;

        for (PatternDetector detector : registry.getTrendDetectors()) {
            try {
                if (!detector.hasEnoughData(series)) {
                    result.getDiagnostics().addWarning(detector.getName(),
                            String.format("資料不足: 需要 %d 天", detector.getMinDataPoints()));
                    continue;
                }

                List<DetectedPattern> patterns = detector.detect(series, Map.of(), null);

                // 從趨勢型態中提取趨勢方向
                for (DetectedPattern p : patterns) {
                    if ("TREND001".equals(p.getPatternId())) {
                        detected = TrendDirection.UPTREND;
                    } else if ("TREND002".equals(p.getPatternId())) {
                        detected = TrendDirection.DOWNTREND;
                    } else if ("TREND003".equals(p.getPatternId())) {
                        detected = TrendDirection.SIDEWAYS;
                    }
                }

            } catch (Exception e) {
                log.warn("趨勢偵測失敗: {}", e.getMessage());
                result.getDiagnostics().addError(detector.getName(), e.getMessage());
            }
        }

        // 設定趨勢分析結果
        if (detected != TrendDirection.UNKNOWN) {
            result.setTrendAnalysis(PatternDetectionResult.TrendAnalysis.builder()
                    .primaryTrend(detected)
                    .build());
        }

        return detected;
    }

    /**
     * 偵測 K 線型態
     *
     * @return [檢查數量, 偵測數量]
     */
    private int[] detectKLinePatterns(PriceSeries series, PatternDetectionResult result,
                                      PatternDetectionPlan plan, TrendDirection trendContext) {
        int checked = 0;
        int detected = 0;

        for (PatternDetector detector : registry.getKLineDetectors()) {
            try {
                // 檢查優先級過濾
                if (plan.getPriorityFilter() != null &&
                    !plan.getPriorityFilter().equals(detector.getPriority())) {
                    continue;
                }

                // 檢查資料是否足夠
                if (!detector.hasEnoughData(series)) {
                    result.getDiagnostics().addWarning(detector.getName(),
                            String.format("資料不足: 需要 %d 天, 實際 %d 天",
                                    detector.getMinDataPoints(), series.size()));
                    continue;
                }

                checked++;

                // 執行偵測
                Map<String, Object> params = buildDetectorParams(plan);
                List<DetectedPattern> patterns = detector.detect(series, params, trendContext);

                // 過濾並加入結果
                for (DetectedPattern pattern : patterns) {
                    if (plan.shouldDetect(pattern.getPatternId())) {
                        result.addKLinePattern(pattern);
                        detected++;
                    }
                }

            } catch (Exception e) {
                log.error("K 線偵測器 {} 執行失敗: {}", detector.getName(), e.getMessage());
                result.getDiagnostics().addError(detector.getName(), e.getMessage());
            }
        }

        return new int[]{checked, detected};
    }

    /**
     * 偵測圖表型態
     *
     * @return [檢查數量, 偵測數量]
     */
    private int[] detectChartPatterns(PriceSeries series, PatternDetectionResult result,
                                      PatternDetectionPlan plan, TrendDirection trendContext) {
        int checked = 0;
        int detected = 0;

        for (PatternDetector detector : registry.getChartPatternDetectors()) {
            try {
                // 檢查優先級過濾
                if (plan.getPriorityFilter() != null &&
                    !plan.getPriorityFilter().equals(detector.getPriority())) {
                    continue;
                }

                // 檢查資料是否足夠
                if (!detector.hasEnoughData(series)) {
                    result.getDiagnostics().addWarning(detector.getName(),
                            String.format("資料不足: 需要 %d 天, 實際 %d 天",
                                    detector.getMinDataPoints(), series.size()));
                    continue;
                }

                checked++;

                // 執行偵測
                Map<String, Object> params = buildDetectorParams(plan);
                List<DetectedPattern> patterns = detector.detect(series, params, trendContext);

                // 過濾並加入結果
                for (DetectedPattern pattern : patterns) {
                    if (plan.shouldDetect(pattern.getPatternId())) {
                        result.addChartPattern(pattern);
                        detected++;
                    }
                }

            } catch (Exception e) {
                log.error("圖表偵測器 {} 執行失敗: {}", detector.getName(), e.getMessage());
                result.getDiagnostics().addError(detector.getName(), e.getMessage());
            }
        }

        return new int[]{checked, detected};
    }

    /**
     * 過濾低強度型態
     */
    private void filterByStrength(PatternDetectionResult result, int minStrength) {
        result.getKlinePatterns().removeIf(p -> p.getStrength() < minStrength);
        result.getChartPatterns().removeIf(p -> p.getStrength() < minStrength);
    }

    /**
     * 產生訊號
     */
    private void generateSignals(PatternDetectionResult result) {
        // 從 K 線型態產生訊號
        for (DetectedPattern pattern : result.getKlinePatterns()) {
            if (pattern.getSignalType() != null && pattern.getStrength() >= 70) {
                result.addSignal(createSignalFromPattern(pattern, "KLINE"));
            }
        }

        // 從圖表型態產生訊號
        for (DetectedPattern pattern : result.getChartPatterns()) {
            if (pattern.getSignalType() != null && pattern.getStrength() >= 60) {
                result.addSignal(createSignalFromPattern(pattern, "CHART"));
            }
        }
    }

    /**
     * 從型態建立訊號
     */
    private PatternDetectionResult.PatternSignal createSignalFromPattern(
            DetectedPattern pattern, String sourceCategory) {

        String signalType = pattern.getSignalType().isBullish() ? "BUY" :
                            pattern.getSignalType().isBearish() ? "SELL" : "WATCH";

        return PatternDetectionResult.PatternSignal.builder()
                .signalId(pattern.getPatternId() + "_" + pattern.getDetectionDate())
                .signalName(pattern.getPatternName() + "訊號")
                .signalType(signalType)
                .sourceCategory(sourceCategory)
                .sourcePatternId(pattern.getPatternId())
                .sourcePatternName(pattern.getPatternName())
                .triggerDate(pattern.getDetectionDate())
                .triggerPrice(pattern.getPatternHigh())
                .confidence(pattern.getConfidence() != null ? pattern.getConfidence() : pattern.getStrength())
                .strength(pattern.getStrength() >= 80 ? "HIGH" :
                          pattern.getStrength() >= 60 ? "MEDIUM" : "LOW")
                .targetPrice(pattern.getTargetPrice())
                .stopLoss(pattern.getStopLoss())
                .riskRewardRatio(pattern.getRiskRewardRatio())
                .description(pattern.getDescription())
                .build();
    }

    /**
     * 建構偵測器參數
     */
    private Map<String, Object> buildDetectorParams(PatternDetectionPlan plan) {
        Map<String, Object> params = new HashMap<>();
        params.put("lookbackPeriod", plan.getLookbackPeriod());
        params.put("minStrength", plan.getMinPatternStrength());
        return params;
    }

    /**
     * 描述偵測計劃
     */
    private String describePlan(PatternDetectionPlan plan) {
        StringBuilder sb = new StringBuilder();
        if (plan.isIncludeKLinePatterns()) sb.append("K線 ");
        if (plan.isIncludeChartPatterns()) sb.append("圖表 ");
        if (plan.isIncludeTrendPatterns()) sb.append("趨勢 ");
        if (plan.isIncludeSignals()) sb.append("訊號 ");
        sb.append("minStrength=").append(plan.getMinPatternStrength());
        return sb.toString().trim();
    }
}
