package com.chris.fin_shark.m10.engine.detector.chart.reversal;

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
 * 雙重頂/雙重底偵測器
 * <p>
 * 支援型態：
 * - CHART003: 雙重頂 (Double Top / M 頭) - 看跌反轉
 * - CHART004: 雙重底 (Double Bottom / W 底) - 看漲反轉
 * </p>
 *
 * @author chris
 * @since 1.0.0
 */
@Slf4j
@Component
public class DoubleTopBottomDetector extends AbstractChartPatternDetector {

    @Override
    public String getName() {
        return "DoubleTopBottomDetector";
    }

    @Override
    public String getCategory() {
        return "CHART_REVERSAL";
    }

    @Override
    public String getPriority() {
        return "P0";
    }

    @Override
    public int getMinDataPoints() {
        return 30; // 雙重頂底需要較長的時間形成
    }

    @Override
    protected void initializeMetadata() {
        registerMetadata(createMetadata(
                "CHART003",
                "雙重頂",
                "Double Top",
                PatternCategory.CHART_REVERSAL,
                SignalType.BEARISH_REVERSAL,
                30,
                "M 頭形態，兩個相近高點之間有一個明顯低點，突破頸線後確認"
        ));

        registerMetadata(createMetadata(
                "CHART004",
                "雙重底",
                "Double Bottom",
                PatternCategory.CHART_REVERSAL,
                SignalType.BULLISH_REVERSAL,
                30,
                "W 底形態，兩個相近低點之間有一個明顯高點，突破頸線後確認"
        ));
    }

    @Override
    protected List<DetectedPattern> doDetect(List<CandleStick> candles,
                                             List<PeakTrough> peaksTroughs,
                                             Map<String, Object> params,
                                             TrendDirection trendContext) {
        List<DetectedPattern> patterns = new ArrayList<>();

        if (peaksTroughs.size() < 3) {
            return patterns;
        }

        // 檢查雙重頂
        DetectedPattern doubleTop = detectDoubleTop(candles, peaksTroughs, trendContext);
        if (doubleTop != null) {
            patterns.add(doubleTop);
        }

        // 檢查雙重底
        DetectedPattern doubleBottom = detectDoubleBottom(candles, peaksTroughs, trendContext);
        if (doubleBottom != null) {
            patterns.add(doubleBottom);
        }

        return patterns;
    }

    /**
     * 偵測雙重頂
     */
    private DetectedPattern detectDoubleTop(List<CandleStick> candles,
                                            List<PeakTrough> peaksTroughs,
                                            TrendDirection trendContext) {
        // 找出最近的波峰
        List<PeakTrough> peaks = peaksTroughs.stream()
                .filter(pt -> pt.getType() == PeakTrough.Type.PEAK)
                .toList();

        if (peaks.size() < 2) {
            return null;
        }

        // 取最後兩個波峰
        PeakTrough peak1 = peaks.get(peaks.size() - 2);
        PeakTrough peak2 = peaks.get(peaks.size() - 1);

        // 檢查兩個高點是否在同一水平（容忍度內）
        if (!isSameLevel(peak1.getPrice(), peak2.getPrice(), LEVEL_TOLERANCE)) {
            return null;
        }

        // 找出兩個高點之間的低點（頸線）
        Optional<PeakTrough> necklinePoint = peaksTroughs.stream()
                .filter(pt -> pt.getType() == PeakTrough.Type.TROUGH)
                .filter(pt -> pt.getIndex() > peak1.getIndex() && pt.getIndex() < peak2.getIndex())
                .min(Comparator.comparing(PeakTrough::getPrice));

        if (necklinePoint.isEmpty()) {
            return null;
        }

        BigDecimal neckline = necklinePoint.get().getPrice();

        // 計算型態高度
        BigDecimal patternHeight = peak1.getPrice().add(peak2.getPrice())
                .divide(BigDecimal.valueOf(2), 4, RoundingMode.HALF_UP)
                .subtract(neckline);

        // 型態高度要有意義（至少 3%）
        if (patternHeight.divide(neckline, 4, RoundingMode.HALF_UP).doubleValue() < 0.03) {
            return null;
        }

        // 檢查是否突破頸線
        CandleStick lastCandle = candles.get(candles.size() - 1);
        boolean isBreaking = lastCandle.getClose().compareTo(neckline) < 0;

        // 計算強度
        int baseStrength = trendContext == TrendDirection.UPTREND ? 75 : 60;
        int strength = calculateStrength(baseStrength, peak1, peak2, necklinePoint.get(), isBreaking);

        // 計算目標價（頸線 - 型態高度）
        BigDecimal targetPrice = neckline.subtract(patternHeight);

        // 取得涉及的 K 線
        int startIdx = peak1.getIndex();
        int endIdx = Math.min(candles.size() - 1, peak2.getIndex() + 5);
        List<CandleStick> involvedCandles = candles.subList(startIdx, endIdx + 1);

        Map<String, Object> keyLevels = new HashMap<>();
        keyLevels.put("peak1", peak1.getPrice().doubleValue());
        keyLevels.put("peak2", peak2.getPrice().doubleValue());
        keyLevels.put("neckline", neckline.doubleValue());
        keyLevels.put("patternHeight", patternHeight.doubleValue());
        keyLevels.put("isBreaking", isBreaking);

        return buildChartPattern(
                "CHART003",
                involvedCandles,
                strength,
                trendContext,
                neckline,
                targetPrice,
                keyLevels
        );
    }

    /**
     * 偵測雙重底
     */
    private DetectedPattern detectDoubleBottom(List<CandleStick> candles,
                                               List<PeakTrough> peaksTroughs,
                                               TrendDirection trendContext) {
        // 找出最近的波谷
        List<PeakTrough> troughs = peaksTroughs.stream()
                .filter(pt -> pt.getType() == PeakTrough.Type.TROUGH)
                .toList();

        if (troughs.size() < 2) {
            return null;
        }

        // 取最後兩個波谷
        PeakTrough trough1 = troughs.get(troughs.size() - 2);
        PeakTrough trough2 = troughs.get(troughs.size() - 1);

        // 檢查兩個低點是否在同一水平
        if (!isSameLevel(trough1.getPrice(), trough2.getPrice(), LEVEL_TOLERANCE)) {
            return null;
        }

        // 找出兩個低點之間的高點（頸線）
        Optional<PeakTrough> necklinePoint = peaksTroughs.stream()
                .filter(pt -> pt.getType() == PeakTrough.Type.PEAK)
                .filter(pt -> pt.getIndex() > trough1.getIndex() && pt.getIndex() < trough2.getIndex())
                .max(Comparator.comparing(PeakTrough::getPrice));

        if (necklinePoint.isEmpty()) {
            return null;
        }

        BigDecimal neckline = necklinePoint.get().getPrice();

        // 計算型態高度
        BigDecimal patternHeight = neckline.subtract(
                trough1.getPrice().add(trough2.getPrice())
                        .divide(BigDecimal.valueOf(2), 4, RoundingMode.HALF_UP)
        );

        // 型態高度要有意義
        if (patternHeight.divide(neckline, 4, RoundingMode.HALF_UP).doubleValue() < 0.03) {
            return null;
        }

        // 檢查是否突破頸線
        CandleStick lastCandle = candles.get(candles.size() - 1);
        boolean isBreaking = lastCandle.getClose().compareTo(neckline) > 0;

        // 計算強度
        int baseStrength = trendContext == TrendDirection.DOWNTREND ? 75 : 60;
        int strength = calculateStrength(baseStrength, trough1, trough2, necklinePoint.get(), isBreaking);

        // 計算目標價（頸線 + 型態高度）
        BigDecimal targetPrice = neckline.add(patternHeight);

        // 取得涉及的 K 線
        int startIdx = trough1.getIndex();
        int endIdx = Math.min(candles.size() - 1, trough2.getIndex() + 5);
        List<CandleStick> involvedCandles = candles.subList(startIdx, endIdx + 1);

        Map<String, Object> keyLevels = new HashMap<>();
        keyLevels.put("trough1", trough1.getPrice().doubleValue());
        keyLevels.put("trough2", trough2.getPrice().doubleValue());
        keyLevels.put("neckline", neckline.doubleValue());
        keyLevels.put("patternHeight", patternHeight.doubleValue());
        keyLevels.put("isBreaking", isBreaking);

        return buildChartPattern(
                "CHART004",
                involvedCandles,
                strength,
                trendContext,
                neckline,
                targetPrice,
                keyLevels
        );
    }

    /**
     * 計算型態強度
     */
    private int calculateStrength(int baseStrength, PeakTrough pt1, PeakTrough pt2,
                                  PeakTrough necklinePoint, boolean isBreaking) {
        int strength = baseStrength;

        // 兩個頂/底的高度越接近越強
        BigDecimal priceDiff = pt1.getPrice().subtract(pt2.getPrice()).abs();
        BigDecimal avgPrice = pt1.getPrice().add(pt2.getPrice()).divide(BigDecimal.valueOf(2), 4, RoundingMode.HALF_UP);
        double diffRatio = avgPrice.compareTo(BigDecimal.ZERO) > 0 ?
                priceDiff.divide(avgPrice, 4, RoundingMode.HALF_UP).doubleValue() : 0;
        if (diffRatio < 0.01) {
            strength += 10;
        }

        // 形成時間適中（不能太短也不能太長）
        int formationDays = pt2.getIndex() - pt1.getIndex();
        if (formationDays >= 10 && formationDays <= 60) {
            strength += 5;
        }

        // 已突破頸線
        if (isBreaking) {
            strength += 15;
        }

        return Math.min(100, strength);
    }
}
