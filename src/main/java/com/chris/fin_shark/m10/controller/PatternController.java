package com.chris.fin_shark.m10.controller;

import com.chris.fin_shark.common.dto.ApiResponse;
import com.chris.fin_shark.m10.engine.PatternDetectionPlan;
import com.chris.fin_shark.m10.engine.PatternDetectionResult;
import com.chris.fin_shark.m10.engine.model.DetectedPattern;
import com.chris.fin_shark.m10.service.PatternAnalysisService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 型態分析 API 控制器
 *
 * @author chris
 * @since 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/pattern")
@RequiredArgsConstructor
@Tag(name = "M10 型態分析", description = "技術型態辨識 API")
public class PatternController {

    private final PatternAnalysisService patternAnalysisService;

    /**
     * 取得完整型態分析
     */
    @GetMapping("/{stockId}/analysis")
    @Operation(summary = "完整型態分析", description = "執行完整的型態分析，包含 K 線、圖表、趨勢")
    public ApiResponse<PatternDetectionResult> getFullAnalysis(
            @Parameter(description = "股票代碼", example = "2330")
            @PathVariable String stockId,
            @Parameter(description = "回溯天數", example = "120")
            @RequestParam(defaultValue = "120") int lookbackDays,
            @Parameter(description = "最低強度", example = "50")
            @RequestParam(defaultValue = "50") int minStrength) {

        PatternDetectionPlan plan = PatternDetectionPlan.builder()
                .includeKLinePatterns(true)
                .includeChartPatterns(true)
                .includeTrendPatterns(true)
                .includeSupportResistance(true)
                .includeSignals(true)
                .lookbackPeriod(lookbackDays)
                .minPatternStrength(minStrength)
                .build();

        PatternDetectionResult result = patternAnalysisService.analyzePatterns(stockId, plan);
        return ApiResponse.success(result);
    }

    /**
     * 取得 K 線型態
     */
    @GetMapping("/{stockId}/kline")
    @Operation(summary = "K 線型態分析", description = "只分析 K 線型態（單根、雙根、三根）")
    public ApiResponse<List<DetectedPattern>> getKLinePatterns(
            @Parameter(description = "股票代碼", example = "2330")
            @PathVariable String stockId,
            @Parameter(description = "回溯天數", example = "60")
            @RequestParam(defaultValue = "60") int lookbackDays,
            @Parameter(description = "最低強度", example = "50")
            @RequestParam(defaultValue = "50") int minStrength) {

        PatternDetectionPlan plan = PatternDetectionPlan.klineOnly()
                .toBuilder()
                .lookbackPeriod(lookbackDays)
                .minPatternStrength(minStrength)
                .build();

        PatternDetectionResult result = patternAnalysisService.analyzePatterns(stockId, plan);
        return ApiResponse.success(result.getKlinePatterns());
    }

    /**
     * 取得圖表型態
     */
    @GetMapping("/{stockId}/chart")
    @Operation(summary = "圖表型態分析", description = "只分析圖表型態（頭肩、雙重頂底、三角形）")
    public ApiResponse<List<DetectedPattern>> getChartPatterns(
            @Parameter(description = "股票代碼", example = "2330")
            @PathVariable String stockId,
            @Parameter(description = "回溯天數", example = "120")
            @RequestParam(defaultValue = "120") int lookbackDays,
            @Parameter(description = "最低強度", example = "50")
            @RequestParam(defaultValue = "50") int minStrength) {

        PatternDetectionPlan plan = PatternDetectionPlan.chartOnly()
                .toBuilder()
                .lookbackPeriod(lookbackDays)
                .minPatternStrength(minStrength)
                .build();

        PatternDetectionResult result = patternAnalysisService.analyzePatterns(stockId, plan);
        return ApiResponse.success(result.getChartPatterns());
    }

    /**
     * 取得趨勢分析
     */
    @GetMapping("/{stockId}/trend")
    @Operation(summary = "趨勢分析", description = "分析當前趨勢方向與強度")
    public ApiResponse<PatternDetectionResult.TrendAnalysis> getTrendAnalysis(
            @Parameter(description = "股票代碼", example = "2330")
            @PathVariable String stockId,
            @Parameter(description = "回溯天數", example = "60")
            @RequestParam(defaultValue = "60") int lookbackDays) {

        PatternDetectionPlan plan = PatternDetectionPlan.trendOnly()
                .toBuilder()
                .lookbackPeriod(lookbackDays)
                .build();

        PatternDetectionResult result = patternAnalysisService.analyzePatterns(stockId, plan);
        return ApiResponse.success(result.getTrendAnalysis());
    }

    /**
     * 取得型態訊號
     */
    @GetMapping("/{stockId}/signals")
    @Operation(summary = "型態訊號", description = "取得基於型態產生的交易訊號")
    public ApiResponse<List<PatternDetectionResult.PatternSignal>> getPatternSignals(
            @Parameter(description = "股票代碼", example = "2330")
            @PathVariable String stockId,
            @Parameter(description = "訊號類型", example = "BUY")
            @RequestParam(required = false) String signalType,
            @Parameter(description = "回溯天數", example = "60")
            @RequestParam(defaultValue = "60") int lookbackDays) {

        PatternDetectionPlan plan = PatternDetectionPlan.builder()
                .includeKLinePatterns(true)
                .includeChartPatterns(true)
                .includeTrendPatterns(false)
                .includeSignals(true)
                .lookbackPeriod(lookbackDays)
                .minPatternStrength(60)
                .build();

        PatternDetectionResult result = patternAnalysisService.analyzePatterns(stockId, plan);

        List<PatternDetectionResult.PatternSignal> signals;
        if ("BUY".equalsIgnoreCase(signalType)) {
            signals = result.getBuySignals();
        } else if ("SELL".equalsIgnoreCase(signalType)) {
            signals = result.getSellSignals();
        } else if ("WATCH".equalsIgnoreCase(signalType)) {
            signals = result.getWatchSignals();
        } else {
            signals = result.getSignals();
        }

        return ApiResponse.success(signals);
    }

    /**
     * 快速掃描
     */
    @GetMapping("/{stockId}/quick")
    @Operation(summary = "快速掃描", description = "快速掃描最近的 K 線型態")
    public ApiResponse<Map<String, Object>> quickScan(
            @Parameter(description = "股票代碼", example = "2330")
            @PathVariable String stockId) {

        PatternDetectionResult result = patternAnalysisService.analyzePatternsQuick(stockId);

        Map<String, Object> response = new HashMap<>();
        response.put("stockId", stockId);
        response.put("detectionDate", result.getDetectionDate());
        response.put("patterns", result.getKlinePatterns());
        response.put("summary", result.getPatternSummary());
        response.put("overallBias", result.getOverallBias());
        response.put("diagnostics", Map.of(
                "calculationTimeMs", result.getDiagnostics().getCalculationTimeMs(),
                "patternsDetected", result.getDiagnostics().getPatternsDetected()
        ));

        return ApiResponse.success(response);
    }

    /**
     * 分析並儲存結果
     */
    @PostMapping("/{stockId}/analyze")
    @Operation(summary = "分析並儲存", description = "執行型態分析並將結果儲存到資料庫")
    public ApiResponse<PatternDetectionResult> analyzeAndSave(
            @Parameter(description = "股票代碼", example = "2330")
            @PathVariable String stockId,
            @Parameter(description = "回溯天數", example = "120")
            @RequestParam(defaultValue = "120") int lookbackDays,
            @Parameter(description = "最低強度", example = "50")
            @RequestParam(defaultValue = "50") int minStrength) {

        PatternDetectionPlan plan = PatternDetectionPlan.builder()
                .includeKLinePatterns(true)
                .includeChartPatterns(true)
                .includeTrendPatterns(true)
                .includeSignals(true)
                .lookbackPeriod(lookbackDays)
                .minPatternStrength(minStrength)
                .build();

        PatternDetectionResult result = patternAnalysisService.analyzeAndSave(stockId, plan);
        return ApiResponse.success(result);
    }

    /**
     * 取得型態統計
     */
    @GetMapping("/{stockId}/summary")
    @Operation(summary = "型態統計", description = "取得型態數量統計與整體偏向")
    public ApiResponse<Map<String, Object>> getPatternSummary(
            @Parameter(description = "股票代碼", example = "2330")
            @PathVariable String stockId) {

        PatternDetectionResult result = patternAnalysisService.analyzePatternsFull(stockId);

        Map<String, Object> summary = new HashMap<>();
        summary.put("stockId", stockId);
        summary.put("detectionDate", result.getDetectionDate());
        summary.put("currentPrice", result.getCurrentPrice());
        summary.put("patternSummary", result.getPatternSummary());
        summary.put("overallBias", result.getOverallBias());
        summary.put("bullishPatterns", result.getBullishPatterns().size());
        summary.put("bearishPatterns", result.getBearishPatterns().size());
        summary.put("strongPatterns", result.getStrongPatterns().size());
        summary.put("trendAnalysis", result.getTrendAnalysis());
        summary.put("hasErrors", result.hasErrors());
        summary.put("hasWarnings", result.hasWarnings());

        return ApiResponse.success(summary);
    }
}
