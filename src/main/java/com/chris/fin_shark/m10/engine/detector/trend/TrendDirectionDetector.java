package com.chris.fin_shark.m10.engine.detector.trend;

import com.chris.fin_shark.m07.engine.model.PriceSeries;
import com.chris.fin_shark.m10.engine.PatternDetector;
import com.chris.fin_shark.m10.engine.model.CandleStick;
import com.chris.fin_shark.m10.engine.model.DetectedPattern;
import com.chris.fin_shark.m10.engine.model.PatternMetadata;
import com.chris.fin_shark.m10.enums.PatternCategory;
import com.chris.fin_shark.m10.enums.PatternStatus;
import com.chris.fin_shark.m10.enums.SignalType;
import com.chris.fin_shark.m10.enums.TrendDirection;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;

/**
 * 趨勢方向偵測器
 * <p>
 * 支援型態：
 * - TREND001: 上升趨勢 (Uptrend)
 * - TREND002: 下降趨勢 (Downtrend)
 * - TREND003: 盤整趨勢 (Sideways)
 * </p>
 *
 * @author chris
 * @since 1.0.0
 */
@Slf4j
@Component
public class TrendDirectionDetector implements PatternDetector {

    private final Map<String, PatternMetadata> metadataCache = new HashMap<>();

    public TrendDirectionDetector() {
        initializeMetadata();
    }

    @Override
    public String getName() {
        return "TrendDirectionDetector";
    }

    @Override
    public String getCategory() {
        return "TREND";
    }

    @Override
    public String getPriority() {
        return "P0";
    }

    @Override
    public int getMinDataPoints() {
        return 20;
    }

    @Override
    public List<String> getSupportedPatternIds() {
        return new ArrayList<>(metadataCache.keySet());
    }

    @Override
    public PatternMetadata getMetadata(String patternId) {
        return metadataCache.get(patternId);
    }

    private void initializeMetadata() {
        metadataCache.put("TREND001", PatternMetadata.builder()
                .patternId("TREND001")
                .nameZh("上升趨勢")
                .nameEn("Uptrend")
                .category(PatternCategory.TREND)
                .signalType(SignalType.BULLISH_CONTINUATION)
                .priority("P0")
                .minDataPoints(20)
                .description("價格呈現較高的高點和較高的低點，均線多頭排列")
                .build());

        metadataCache.put("TREND002", PatternMetadata.builder()
                .patternId("TREND002")
                .nameZh("下降趨勢")
                .nameEn("Downtrend")
                .category(PatternCategory.TREND)
                .signalType(SignalType.BEARISH_CONTINUATION)
                .priority("P0")
                .minDataPoints(20)
                .description("價格呈現較低的高點和較低的低點，均線空頭排列")
                .build());

        metadataCache.put("TREND003", PatternMetadata.builder()
                .patternId("TREND003")
                .nameZh("盤整趨勢")
                .nameEn("Sideways")
                .category(PatternCategory.TREND)
                .signalType(SignalType.NEUTRAL)
                .priority("P0")
                .minDataPoints(20)
                .description("價格在區間內波動，無明顯方向")
                .build());
    }

    @Override
    public List<DetectedPattern> detect(PriceSeries series, Map<String, Object> params, TrendDirection context) {
        List<DetectedPattern> patterns = new ArrayList<>();

        if (series.size() < getMinDataPoints()) {
            return patterns;
        }

        // 分析趨勢
        TrendAnalysisResult analysis = analyzeTrend(series, params);

        // 建立偵測結果
        PatternMetadata meta = getMetadata(analysis.patternId);
        if (meta == null) {
            return patterns;
        }

        // 取得涉及的 K 線
        List<CandleStick> involvedCandles = convertToCandleSticks(series, analysis.trendStartIndex);

        DetectedPattern pattern = DetectedPattern.builder()
                .patternId(analysis.patternId)
                .patternName(meta.getNameZh())
                .englishName(meta.getNameEn())
                .category(meta.getCategory())
                .signalType(meta.getSignalType())
                .status(PatternStatus.CONFIRMED)
                .strength(analysis.strength)
                .confidence(analysis.strength)
                .detectionDate(series.getDates().get(series.getDates().size() - 1))
                .involvedDates(involvedCandles.stream().map(CandleStick::getDate).toList())
                .involvedCandles(involvedCandles)
                .patternLow(analysis.trendLow)
                .patternHigh(analysis.trendHigh)
                .trendContext(analysis.direction.name())
                .description(analysis.description)
                .keyLevels(analysis.keyLevels)
                .build();

        patterns.add(pattern);

        log.debug("偵測到趨勢: {} ({}), 強度={}", analysis.patternId, meta.getNameZh(), analysis.strength);

        return patterns;
    }

    /**
     * 趨勢分析結果
     */
    private static class TrendAnalysisResult {
        String patternId;
        TrendDirection direction;
        int strength;
        int trendStartIndex;
        BigDecimal trendHigh;
        BigDecimal trendLow;
        String description;
        Map<String, Object> keyLevels;
    }

    /**
     * 分析趨勢
     */
    private TrendAnalysisResult analyzeTrend(PriceSeries series, Map<String, Object> params) {
        TrendAnalysisResult result = new TrendAnalysisResult();

        int period = (int) params.getOrDefault("trendPeriod", 20);
        double[] closes = series.getCloseArray();
        double[] highs = series.getHighArray();
        double[] lows = series.getLowArray();

        int n = closes.length;
        int startIdx = Math.max(0, n - period);

        // 計算簡單移動平均線
        double ma5 = calculateSMA(closes, 5);
        double ma10 = calculateSMA(closes, 10);
        double ma20 = calculateSMA(closes, 20);

        // 計算趨勢斜率（線性回歸）
        double slope = calculateSlope(closes, startIdx, n);

        // 計算價格波動範圍
        double high = Double.MIN_VALUE;
        double low = Double.MAX_VALUE;
        for (int i = startIdx; i < n; i++) {
            if (highs[i] > high) high = highs[i];
            if (lows[i] < low) low = lows[i];
        }

        // 計算波動幅度比例
        double rangePercent = low > 0 ? (high - low) / low * 100 : 0;

        // 分析高低點結構
        HighLowStructure structure = analyzeHighLowStructure(highs, lows, startIdx, n);

        // 判斷趨勢方向和強度
        int uptrendScore = 0;
        int downtrendScore = 0;

        // 因子 1: 斜率方向
        if (slope > 0.001) uptrendScore += 20;
        else if (slope < -0.001) downtrendScore += 20;

        // 因子 2: 均線排列
        if (ma5 > ma10 && ma10 > ma20) uptrendScore += 25;
        else if (ma5 < ma10 && ma10 < ma20) downtrendScore += 25;

        // 因子 3: 收盤價相對於均線位置
        double lastClose = closes[n - 1];
        if (lastClose > ma5 && lastClose > ma10 && lastClose > ma20) uptrendScore += 15;
        else if (lastClose < ma5 && lastClose < ma10 && lastClose < ma20) downtrendScore += 15;

        // 因子 4: 高低點結構
        uptrendScore += structure.higherHighsCount * 8;
        uptrendScore += structure.higherLowsCount * 7;
        downtrendScore += structure.lowerHighsCount * 8;
        downtrendScore += structure.lowerLowsCount * 7;

        // 決定趨勢方向
        int totalScore = uptrendScore + downtrendScore;
        int strength;

        if (uptrendScore > downtrendScore + 15) {
            result.patternId = "TREND001";
            result.direction = TrendDirection.UPTREND;
            strength = Math.min(100, 50 + uptrendScore);
            result.description = String.format("上升趨勢：MA5>MA10>MA20，高點%d次創新高，低點%d次墊高",
                    structure.higherHighsCount, structure.higherLowsCount);
        } else if (downtrendScore > uptrendScore + 15) {
            result.patternId = "TREND002";
            result.direction = TrendDirection.DOWNTREND;
            strength = Math.min(100, 50 + downtrendScore);
            result.description = String.format("下降趨勢：MA5<MA10<MA20，高點%d次降低，低點%d次創新低",
                    structure.lowerHighsCount, structure.lowerLowsCount);
        } else {
            result.patternId = "TREND003";
            result.direction = TrendDirection.SIDEWAYS;
            strength = Math.min(100, 50 + Math.abs(uptrendScore - downtrendScore));
            result.description = String.format("盤整趨勢：價格在 %.2f%% 範圍內波動", rangePercent);
        }

        result.strength = strength;
        result.trendStartIndex = startIdx;
        result.trendHigh = BigDecimal.valueOf(high);
        result.trendLow = BigDecimal.valueOf(low);

        // 關鍵價位
        result.keyLevels = new HashMap<>();
        result.keyLevels.put("ma5", ma5);
        result.keyLevels.put("ma10", ma10);
        result.keyLevels.put("ma20", ma20);
        result.keyLevels.put("slope", slope);
        result.keyLevels.put("rangePercent", rangePercent);
        result.keyLevels.put("uptrendScore", uptrendScore);
        result.keyLevels.put("downtrendScore", downtrendScore);

        return result;
    }

    /**
     * 高低點結構分析結果
     */
    private static class HighLowStructure {
        int higherHighsCount = 0;
        int higherLowsCount = 0;
        int lowerHighsCount = 0;
        int lowerLowsCount = 0;
    }

    /**
     * 分析高低點結構
     */
    private HighLowStructure analyzeHighLowStructure(double[] highs, double[] lows, int startIdx, int endIdx) {
        HighLowStructure structure = new HighLowStructure();

        int pivotLookback = 3;
        double prevHigh = 0, prevLow = Double.MAX_VALUE;

        for (int i = startIdx + pivotLookback; i < endIdx - pivotLookback; i++) {
            // 檢查局部高點
            boolean isHigh = true;
            for (int j = i - pivotLookback; j <= i + pivotLookback; j++) {
                if (j != i && highs[j] >= highs[i]) {
                    isHigh = false;
                    break;
                }
            }
            if (isHigh) {
                if (prevHigh > 0) {
                    if (highs[i] > prevHigh) structure.higherHighsCount++;
                    else if (highs[i] < prevHigh) structure.lowerHighsCount++;
                }
                prevHigh = highs[i];
            }

            // 檢查局部低點
            boolean isLow = true;
            for (int j = i - pivotLookback; j <= i + pivotLookback; j++) {
                if (j != i && lows[j] <= lows[i]) {
                    isLow = false;
                    break;
                }
            }
            if (isLow) {
                if (prevLow < Double.MAX_VALUE) {
                    if (lows[i] > prevLow) structure.higherLowsCount++;
                    else if (lows[i] < prevLow) structure.lowerLowsCount++;
                }
                prevLow = lows[i];
            }
        }

        return structure;
    }

    /**
     * 計算簡單移動平均
     */
    private double calculateSMA(double[] data, int period) {
        if (data.length < period) {
            return data[data.length - 1];
        }
        double sum = 0;
        for (int i = data.length - period; i < data.length; i++) {
            sum += data[i];
        }
        return sum / period;
    }

    /**
     * 計算斜率（簡單線性回歸）
     */
    private double calculateSlope(double[] data, int startIdx, int endIdx) {
        int n = endIdx - startIdx;
        if (n < 2) return 0;

        double sumX = 0, sumY = 0, sumXY = 0, sumX2 = 0;
        for (int i = startIdx; i < endIdx; i++) {
            double x = i - startIdx;
            double y = data[i];
            sumX += x;
            sumY += y;
            sumXY += x * y;
            sumX2 += x * x;
        }

        double denominator = n * sumX2 - sumX * sumX;
        if (denominator == 0) return 0;

        return (n * sumXY - sumX * sumY) / denominator;
    }

    /**
     * 轉換為 CandleStick
     */
    private List<CandleStick> convertToCandleSticks(PriceSeries series, int startIdx) {
        List<CandleStick> candles = new ArrayList<>();

        List<LocalDate> dates = series.getDates();
        double[] opens = series.getOpenArray();
        double[] highs = series.getHighArray();
        double[] lows = series.getLowArray();
        double[] closes = series.getCloseArray();
        long[] volumes = series.getVolumeArray();

        for (int i = startIdx; i < series.size(); i++) {
            candles.add(CandleStick.builder()
                    .date(dates.get(i))
                    .open(BigDecimal.valueOf(opens[i]))
                    .high(BigDecimal.valueOf(highs[i]))
                    .low(BigDecimal.valueOf(lows[i]))
                    .close(BigDecimal.valueOf(closes[i]))
                    .volume(volumes != null && volumes.length > i ? volumes[i] : 0L)
                    .build());
        }

        return candles;
    }
}
