package com.chris.fin_shark.m10.engine.detector.chart.continuation;

import com.chris.fin_shark.m10.engine.detector.AbstractChartPatternDetector;
import com.chris.fin_shark.m10.engine.model.CandleStick;
import com.chris.fin_shark.m10.engine.model.DetectedPattern;
import com.chris.fin_shark.m10.engine.model.PeakTrough;
import com.chris.fin_shark.m10.enums.PatternCategory;
import com.chris.fin_shark.m10.enums.SignalType;
import com.chris.fin_shark.m10.enums.TrendDirection;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

/**
 * 三角形型態偵測器
 * <p>
 * 支援型態：
 * - CHART020: 對稱三角形 (Symmetrical Triangle) - 雙向突破
 * - CHART021: 上升三角形 (Ascending Triangle) - 看漲延續
 * - CHART022: 下降三角形 (Descending Triangle) - 看跌延續
 * </p>
 *
 * @author chris
 * @since 1.0.0
 */
@Slf4j
@Component
public class TriangleDetector extends AbstractChartPatternDetector {

    @Override
    public String getName() {
        return "TriangleDetector";
    }

    @Override
    public String getCategory() {
        return "CHART_CONTINUATION";
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
    protected void initializeMetadata() {
        registerMetadata(createMetadata(
                "CHART020",
                "對稱三角形",
                "Symmetrical Triangle",
                PatternCategory.CHART_CONTINUATION,
                SignalType.NEUTRAL,
                20,
                "上方趨勢線下降，下方趨勢線上升，形成收斂結構，可能向任一方向突破"
        ));

        registerMetadata(createMetadata(
                "CHART021",
                "上升三角形",
                "Ascending Triangle",
                PatternCategory.CHART_CONTINUATION,
                SignalType.BULLISH_CONTINUATION,
                20,
                "上方為水平壓力線，下方趨勢線上升，通常向上突破"
        ));

        registerMetadata(createMetadata(
                "CHART022",
                "下降三角形",
                "Descending Triangle",
                PatternCategory.CHART_CONTINUATION,
                SignalType.BEARISH_CONTINUATION,
                20,
                "下方為水平支撐線，上方趨勢線下降，通常向下突破"
        ));
    }

    @Override
    protected List<DetectedPattern> doDetect(List<CandleStick> candles,
                                             List<PeakTrough> peaksTroughs,
                                             Map<String, Object> params,
                                             TrendDirection trendContext) {
        List<DetectedPattern> patterns = new ArrayList<>();

        if (peaksTroughs.size() < 4) {
            return patterns;
        }

        // 分離波峰和波谷
        List<PeakTrough> peaks = peaksTroughs.stream()
                .filter(pt -> pt.getType() == PeakTrough.Type.PEAK)
                .toList();

        List<PeakTrough> troughs = peaksTroughs.stream()
                .filter(pt -> pt.getType() == PeakTrough.Type.TROUGH)
                .toList();

        if (peaks.size() < 2 || troughs.size() < 2) {
            return patterns;
        }

        // 分析趨勢線
        TrendLineAnalysis upperTrend = analyzeTrendLine(peaks, true);
        TrendLineAnalysis lowerTrend = analyzeTrendLine(troughs, false);

        if (upperTrend == null || lowerTrend == null) {
            return patterns;
        }

        // 根據趨勢線特徵判斷三角形類型
        String patternId = determineTriangleType(upperTrend, lowerTrend);

        if (patternId == null) {
            return patterns;
        }

        // 計算強度
        int strength = calculateTriangleStrength(upperTrend, lowerTrend, peaks, troughs);

        // 計算頸線和目標價
        BigDecimal apexPrice = calculateApexPrice(upperTrend, lowerTrend, peaks, troughs);
        BigDecimal patternHeight = calculatePatternHeight(peaks, troughs);
        BigDecimal targetPrice = calculateTargetPrice(patternId, apexPrice, patternHeight);

        // 取得涉及的 K 線
        int startIdx = Math.min(peaks.get(0).getIndex(), troughs.get(0).getIndex());
        int endIdx = candles.size() - 1;
        List<CandleStick> involvedCandles = candles.subList(
                Math.max(0, startIdx),
                Math.min(candles.size(), endIdx + 1)
        );

        Map<String, Object> keyLevels = new HashMap<>();
        keyLevels.put("upperTrendSlope", upperTrend.slope);
        keyLevels.put("lowerTrendSlope", lowerTrend.slope);
        keyLevels.put("isUpperFlat", upperTrend.isFlat);
        keyLevels.put("isLowerFlat", lowerTrend.isFlat);
        keyLevels.put("apexPrice", apexPrice != null ? apexPrice.doubleValue() : null);
        keyLevels.put("patternHeight", patternHeight.doubleValue());

        DetectedPattern pattern = buildChartPattern(
                patternId,
                involvedCandles,
                strength,
                trendContext,
                apexPrice,
                targetPrice,
                keyLevels
        );

        if (pattern != null) {
            patterns.add(pattern);
            log.debug("偵測到型態: {} ({}), 強度={}", patternId, pattern.getPatternName(), strength);
        }

        return patterns;
    }

    /**
     * 趨勢線分析結果
     */
    private static class TrendLineAnalysis {
        double slope;       // 斜率（正上升，負下降）
        boolean isFlat;     // 是否接近水平
        double r2;          // R² 擬合度
    }

    /**
     * 分析趨勢線
     */
    private TrendLineAnalysis analyzeTrendLine(List<PeakTrough> points, boolean isUpper) {
        if (points.size() < 2) {
            return null;
        }

        // 取最後幾個點
        int n = Math.min(points.size(), 4);
        List<PeakTrough> recentPoints = points.subList(points.size() - n, points.size());

        // 簡單線性回歸計算斜率
        double sumX = 0, sumY = 0, sumXY = 0, sumX2 = 0, sumY2 = 0;
        for (int i = 0; i < recentPoints.size(); i++) {
            double x = recentPoints.get(i).getIndex();
            double y = recentPoints.get(i).getPrice().doubleValue();
            sumX += x;
            sumY += y;
            sumXY += x * y;
            sumX2 += x * x;
            sumY2 += y * y;
        }

        int count = recentPoints.size();
        double slope = (count * sumXY - sumX * sumY) / (count * sumX2 - sumX * sumX);

        // 計算 R²
        double meanY = sumY / count;
        double ssTotal = 0, ssResidual = 0;
        double intercept = (sumY - slope * sumX) / count;

        for (PeakTrough pt : recentPoints) {
            double x = pt.getIndex();
            double y = pt.getPrice().doubleValue();
            double predicted = slope * x + intercept;
            ssTotal += Math.pow(y - meanY, 2);
            ssResidual += Math.pow(y - predicted, 2);
        }

        double r2 = ssTotal > 0 ? 1 - (ssResidual / ssTotal) : 0;

        TrendLineAnalysis analysis = new TrendLineAnalysis();
        analysis.slope = slope;
        analysis.r2 = r2;

        // 判斷是否接近水平（斜率絕對值 < 價格的 0.1%）
        double avgPrice = sumY / count;
        analysis.isFlat = Math.abs(slope) < avgPrice * 0.001;

        return analysis;
    }

    /**
     * 判斷三角形類型
     */
    private String determineTriangleType(TrendLineAnalysis upper, TrendLineAnalysis lower) {
        // 上升三角形：上方水平，下方上升
        if (upper.isFlat && lower.slope > 0) {
            return "CHART021";
        }

        // 下降三角形：下方水平，上方下降
        if (lower.isFlat && upper.slope < 0) {
            return "CHART022";
        }

        // 對稱三角形：上方下降，下方上升（收斂）
        if (upper.slope < 0 && lower.slope > 0) {
            return "CHART020";
        }

        return null;
    }

    /**
     * 計算三角形強度
     */
    private int calculateTriangleStrength(TrendLineAnalysis upper, TrendLineAnalysis lower,
                                          List<PeakTrough> peaks, List<PeakTrough> troughs) {
        int strength = 60;

        // 趨勢線擬合度
        if (upper.r2 > 0.8) strength += 10;
        if (lower.r2 > 0.8) strength += 10;

        // 至少有 3 個接觸點
        if (peaks.size() >= 3) strength += 5;
        if (troughs.size() >= 3) strength += 5;

        // 價格在收斂（最後的高低點接近）
        if (peaks.size() >= 2 && troughs.size() >= 2) {
            BigDecimal lastPeak = peaks.get(peaks.size() - 1).getPrice();
            BigDecimal lastTrough = troughs.get(troughs.size() - 1).getPrice();
            BigDecimal firstPeak = peaks.get(0).getPrice();
            BigDecimal firstTrough = troughs.get(0).getPrice();

            BigDecimal initialRange = firstPeak.subtract(firstTrough);
            BigDecimal currentRange = lastPeak.subtract(lastTrough);

            if (initialRange.compareTo(BigDecimal.ZERO) > 0 &&
                currentRange.compareTo(initialRange.multiply(BigDecimal.valueOf(0.5))) < 0) {
                strength += 10; // 明顯收斂
            }
        }

        return Math.min(100, strength);
    }

    /**
     * 計算頂點價位
     */
    private BigDecimal calculateApexPrice(TrendLineAnalysis upper, TrendLineAnalysis lower,
                                          List<PeakTrough> peaks, List<PeakTrough> troughs) {
        // 簡化計算：取最後波峰和波谷的中點
        if (peaks.isEmpty() || troughs.isEmpty()) {
            return null;
        }

        BigDecimal lastPeak = peaks.get(peaks.size() - 1).getPrice();
        BigDecimal lastTrough = troughs.get(troughs.size() - 1).getPrice();

        return lastPeak.add(lastTrough).divide(BigDecimal.valueOf(2), 4, RoundingMode.HALF_UP);
    }

    /**
     * 計算型態高度
     */
    private BigDecimal calculatePatternHeight(List<PeakTrough> peaks, List<PeakTrough> troughs) {
        if (peaks.isEmpty() || troughs.isEmpty()) {
            return BigDecimal.ZERO;
        }

        // 取型態開始時的高度
        BigDecimal firstPeak = peaks.get(0).getPrice();
        BigDecimal firstTrough = troughs.get(0).getPrice();

        return firstPeak.subtract(firstTrough).abs();
    }

    /**
     * 計算目標價
     */
    private BigDecimal calculateTargetPrice(String patternId, BigDecimal apexPrice, BigDecimal patternHeight) {
        if (apexPrice == null) {
            return null;
        }

        switch (patternId) {
            case "CHART021": // 上升三角形 - 向上突破
                return apexPrice.add(patternHeight);
            case "CHART022": // 下降三角形 - 向下突破
                return apexPrice.subtract(patternHeight);
            default: // 對稱三角形 - 兩個方向都可能
                return apexPrice.add(patternHeight); // 預設向上
        }
    }
}
