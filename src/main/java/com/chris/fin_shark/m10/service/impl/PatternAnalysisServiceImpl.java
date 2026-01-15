package com.chris.fin_shark.m10.service.impl;

import com.chris.fin_shark.m06.domain.StockPrice;
import com.chris.fin_shark.m06.repository.StockPriceRepository;
import com.chris.fin_shark.m07.engine.model.PriceSeries;
import com.chris.fin_shark.m10.domain.ChartPatternResult;
import com.chris.fin_shark.m10.domain.KLinePatternResult;
import com.chris.fin_shark.m10.engine.Diagnostics;
import com.chris.fin_shark.m10.engine.PatternDetectionPlan;
import com.chris.fin_shark.m10.engine.PatternDetectionResult;
import com.chris.fin_shark.m10.engine.PatternEngine;
import com.chris.fin_shark.m10.engine.model.DetectedPattern;
import com.chris.fin_shark.m10.exception.PatternDetectionException;
import com.chris.fin_shark.m10.repository.ChartPatternResultRepository;
import com.chris.fin_shark.m10.repository.KLinePatternResultRepository;
import com.chris.fin_shark.m10.service.PatternAnalysisService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * 型態分析服務實作
 *
 * @author chris
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PatternAnalysisServiceImpl implements PatternAnalysisService {

    private final PatternEngine patternEngine;
    private final StockPriceRepository stockPriceRepository;
    private final KLinePatternResultRepository klinePatternResultRepository;
    private final ChartPatternResultRepository chartPatternResultRepository;
    private final ObjectMapper objectMapper;

    @Override
    public PatternDetectionResult analyzePatterns(String stockId, PatternDetectionPlan plan) {
        log.info("開始型態分析: stockId={}, plan={}", stockId, describePlan(plan));

        // 取得價格資料
        PriceSeries series = loadPriceSeries(stockId, plan.getLookbackPeriod());

        if (series == null || series.size() == 0) {
            throw PatternDetectionException.insufficientData(stockId, plan.getLookbackPeriod(), 0);
        }

        // 執行偵測
        PatternDetectionResult result = patternEngine.detect(series, plan);

        log.info("型態分析完成: stockId={}, K線型態={}, 圖表型態={}, 訊號={}, 耗時={}ms",
                stockId,
                result.getKlinePatterns().size(),
                result.getChartPatterns().size(),
                result.getSignals().size(),
                result.getDiagnostics().getCalculationTimeMs());

        return result;
    }

    @Override
    public PatternDetectionResult analyzePatternsFull(String stockId) {
        return analyzePatterns(stockId, PatternDetectionPlan.full());
    }

    @Override
    public PatternDetectionResult analyzePatternsQuick(String stockId) {
        return analyzePatterns(stockId, PatternDetectionPlan.quick());
    }

    @Override
    public Map<String, PatternDetectionResult> batchAnalyze(Iterable<String> stockIds, PatternDetectionPlan plan) {
        Map<String, PatternDetectionResult> results = new HashMap<>();

        long count = StreamSupport.stream(stockIds.spliterator(), false).count();
        log.info("開始批次型態分析: {} 支股票", count);

        for (String stockId : stockIds) {
            try {
                PatternDetectionResult result = analyzePatterns(stockId, plan);
                results.put(stockId, result);
            } catch (Exception e) {
                log.error("股票 {} 分析失敗: {}", stockId, e.getMessage());
                // 建立空結果
                PatternDetectionResult errorResult = PatternDetectionResult.builder()
                        .stockId(stockId)
                        .detectionDate(LocalDate.now())
                        .diagnostics(new Diagnostics())
                        .build();
                errorResult.getDiagnostics().addError("SERVICE", e.getMessage());
                results.put(stockId, errorResult);
            }
        }

        log.info("批次分析完成: 成功 {} / {} 支", results.size(), count);

        return results;
    }

    @Override
    @Transactional
    public void saveAnalysisResult(PatternDetectionResult result) {
        if (result == null || result.getStockId() == null) {
            return;
        }

        String stockId = result.getStockId();
        LocalDate tradeDate = result.getDetectionDate();

        // 刪除舊記錄
        klinePatternResultRepository.deleteByStockIdAndTradeDate(stockId, tradeDate);
        chartPatternResultRepository.deleteByStockIdAndDetectionDate(stockId, tradeDate);

        // 儲存 K 線型態
        for (DetectedPattern pattern : result.getKlinePatterns()) {
            KLinePatternResult entity = convertToKLineEntity(stockId, tradeDate, pattern);
            klinePatternResultRepository.save(entity);
        }

        // 儲存圖表型態
        for (DetectedPattern pattern : result.getChartPatterns()) {
            ChartPatternResult entity = convertToChartEntity(stockId, pattern);
            chartPatternResultRepository.save(entity);
        }

        log.debug("儲存分析結果: stockId={}, K線型態={}, 圖表型態={}",
                stockId, result.getKlinePatterns().size(), result.getChartPatterns().size());
    }

    @Override
    @Transactional
    public PatternDetectionResult analyzeAndSave(String stockId, PatternDetectionPlan plan) {
        PatternDetectionResult result = analyzePatterns(stockId, plan);
        saveAnalysisResult(result);
        return result;
    }

    // === 私有方法 ===

    /**
     * 載入價格序列
     */
    private PriceSeries loadPriceSeries(String stockId, int days) {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(days + 30); // 多取一些資料

        List<StockPrice> prices = stockPriceRepository
                .findByStockIdAndTradeDateBetweenOrderByTradeDateAsc(stockId, startDate, endDate);

        if (prices.isEmpty()) {
            return null;
        }

        // 轉換為 PriceSeries
        List<LocalDate> dates = new ArrayList<>();
        List<BigDecimal> opens = new ArrayList<>();
        List<BigDecimal> highs = new ArrayList<>();
        List<BigDecimal> lows = new ArrayList<>();
        List<BigDecimal> closes = new ArrayList<>();
        List<Long> volumes = new ArrayList<>();

        for (StockPrice p : prices) {
            dates.add(p.getTradeDate());
            opens.add(p.getOpenPrice() != null ? p.getOpenPrice() : BigDecimal.ZERO);
            highs.add(p.getHighPrice() != null ? p.getHighPrice() : BigDecimal.ZERO);
            lows.add(p.getLowPrice() != null ? p.getLowPrice() : BigDecimal.ZERO);
            closes.add(p.getClosePrice() != null ? p.getClosePrice() : BigDecimal.ZERO);
            volumes.add(p.getVolume() != null ? p.getVolume() : 0L);
        }

        return PriceSeries.builder()
                .stockId(stockId)
                .dates(dates)
                .open(opens)
                .high(highs)
                .low(lows)
                .close(closes)
                .volume(volumes)
                .build();
    }

    /**
     * 轉換為 K 線型態實體
     */
    private KLinePatternResult convertToKLineEntity(String stockId, LocalDate tradeDate, DetectedPattern pattern) {

        LocalDate[] involvedDatesArr = pattern.getInvolvedDates() == null
                ? new LocalDate[0]
                : pattern.getInvolvedDates().toArray(new LocalDate[0]);

        // 計算 pattern_category 字串
        String patternCategory = pattern.getCategory() != null ? pattern.getCategory().name() : "SINGLE_KLINE";

        // 計算 signal_type 字串
        String signalType = pattern.getSignalType() != null ? pattern.getSignalType().name() : "NEUTRAL";

        return KLinePatternResult.builder()
                .stockId(stockId)
                .tradeDate(tradeDate)
                .patternId(pattern.getPatternId())
                .patternName(pattern.getPatternName())
                .englishName(pattern.getEnglishName())
                .patternCategory(patternCategory)
                .signalType(signalType)
                .strength(pattern.getStrength())
                .confidence(pattern.getConfidence())
                .involvedDates(involvedDatesArr)
                .patternLow(pattern.getPatternLow())
                .patternHigh(pattern.getPatternHigh())
                .candleData(toJson(pattern.getInvolvedCandles()))
                .volumeConfirmation(pattern.isVolumeConfirmation())
                .volumeRatio(pattern.getVolumeRatio())
                .trendContext(pattern.getTrendContext())
                .description(pattern.getDescription())
                .build();
    }

    /**
     * 轉換為圖表型態實體
     */
    private ChartPatternResult convertToChartEntity(String stockId, DetectedPattern pattern) {
        List<LocalDate> dates = pattern.getInvolvedDates();
        LocalDate formationStart = dates != null && !dates.isEmpty() ? dates.get(0) : pattern.getDetectionDate();
        LocalDate formationEnd = dates != null && !dates.isEmpty() ? dates.get(dates.size() - 1)
                : pattern.getDetectionDate();
        Integer durationDays = dates != null ? dates.size() : 0;

        // 計算 pattern_category 字串
        String patternCategory = pattern.getCategory() != null ? pattern.getCategory().name() : "REVERSAL";

        // 計算 signal_type 字串
        String signalType = pattern.getSignalType() != null ? pattern.getSignalType().name() : "NEUTRAL";

        // 計算 status 字串
        String status = pattern.getStatus() != null ? pattern.getStatus().name() : "CONFIRMED";

        // 建構 key_levels JSONB
        Map<String, Object> keyLevels = new HashMap<>();
        if (pattern.getKeyLevels() != null) {
            keyLevels.putAll(pattern.getKeyLevels());
        }
        keyLevels.put("pattern_low", pattern.getPatternLow());
        keyLevels.put("pattern_high", pattern.getPatternHigh());
        if (pattern.getNeckline() != null) {
            keyLevels.put("neckline", pattern.getNeckline());
        }

        // 計算潛在漲跌幅
        BigDecimal potentialMovePct = null;
        if (pattern.getTargetPrice() != null && pattern.getPatternHigh() != null &&
                pattern.getPatternHigh().compareTo(BigDecimal.ZERO) > 0) {
            potentialMovePct = pattern.getTargetPrice()
                    .subtract(pattern.getPatternHigh())
                    .divide(pattern.getPatternHigh(), 4, BigDecimal.ROUND_HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
        }

        // 計算風險報酬比
        BigDecimal riskRewardRatio = pattern.getRiskRewardRatio();

        return ChartPatternResult.builder()
                .stockId(stockId)
                .detectionDate(pattern.getDetectionDate())
                .patternId(pattern.getPatternId())
                .patternName(pattern.getPatternName())
                .englishName(pattern.getEnglishName())
                .patternCategory(patternCategory)
                .status(status)
                .signalType(signalType)
                .strength(pattern.getStrength())
                .formationStart(formationStart)
                .formationEnd(formationEnd)
                .durationDays(durationDays)
                .keyLevels(toJson(keyLevels))
                .targetPrice(pattern.getTargetPrice())
                .stopLossPrice(pattern.getStopLoss())
                .potentialMovePct(potentialMovePct)
                .riskRewardRatio(riskRewardRatio)
                .breakoutLevel(pattern.getBreakoutLevel())
                .breakoutDirection(pattern.getBreakoutLevel() != null
                        ? (pattern.getSignalType() != null && pattern.getSignalType().isBullish() ? "UP" : "DOWN")
                        : null)
                .volumeConfirmation(pattern.isVolumeConfirmation())
                .description(pattern.getDescription())
                .build();
    }

    /**
     * 將日期列表轉換為 PostgreSQL DATE[] 格式
     */
    private String convertDatesToPostgresArray(List<LocalDate> dates) {
        if (dates == null || dates.isEmpty()) {
            return "{}";
        }
        return dates.stream()
                .map(LocalDate::toString)
                .collect(Collectors.joining(",", "{", "}"));
    }

    /**
     * 轉換為 JSON
     */
    private String toJson(Object obj) {
        if (obj == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            log.warn("JSON 序列化失敗: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 描述偵測計劃
     */
    private String describePlan(PatternDetectionPlan plan) {
        StringBuilder sb = new StringBuilder();
        if (plan.isIncludeKLinePatterns())
            sb.append("K線 ");
        if (plan.isIncludeChartPatterns())
            sb.append("圖表 ");
        if (plan.isIncludeTrendPatterns())
            sb.append("趨勢 ");
        sb.append("minStrength=").append(plan.getMinPatternStrength());
        return sb.toString().trim();
    }
}
