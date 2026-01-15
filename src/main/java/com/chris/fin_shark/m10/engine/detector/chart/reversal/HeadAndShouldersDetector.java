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
 * 頭肩頂/頭肩底偵測器
 * <p>
 * 支援型態：
 * - CHART001: 頭肩頂 (Head and Shoulders) - 看跌反轉
 * - CHART002: 頭肩底 (Inverse Head and Shoulders) - 看漲反轉
 * </p>
 *
 * @author chris
 * @since 1.0.0
 */
@Slf4j
@Component
public class HeadAndShouldersDetector extends AbstractChartPatternDetector {

    @Override
    public String getName() {
        return "HeadAndShouldersDetector";
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
        return 40; // 頭肩型態需要較長時間形成
    }

    @Override
    protected void initializeMetadata() {
        registerMetadata(createMetadata(
                "CHART001",
                "頭肩頂",
                "Head and Shoulders",
                PatternCategory.CHART_REVERSAL,
                SignalType.BEARISH_REVERSAL,
                40,
                "三個高點形成左肩、頭部、右肩，突破頸線後確認反轉"
        ));

        registerMetadata(createMetadata(
                "CHART002",
                "頭肩底",
                "Inverse Head and Shoulders",
                PatternCategory.CHART_REVERSAL,
                SignalType.BULLISH_REVERSAL,
                40,
                "三個低點形成左肩、頭部、右肩，突破頸線後確認反轉"
        ));
    }

    @Override
    protected List<DetectedPattern> doDetect(List<CandleStick> candles,
                                             List<PeakTrough> peaksTroughs,
                                             Map<String, Object> params,
                                             TrendDirection trendContext) {
        List<DetectedPattern> patterns = new ArrayList<>();

        if (peaksTroughs.size() < 5) {
            return patterns;
        }

        // 檢查頭肩頂
        DetectedPattern headAndShoulders = detectHeadAndShoulders(candles, peaksTroughs, trendContext);
        if (headAndShoulders != null) {
            patterns.add(headAndShoulders);
        }

        // 檢查頭肩底
        DetectedPattern inverseHS = detectInverseHeadAndShoulders(candles, peaksTroughs, trendContext);
        if (inverseHS != null) {
            patterns.add(inverseHS);
        }

        return patterns;
    }

    /**
     * 偵測頭肩頂
     */
    private DetectedPattern detectHeadAndShoulders(List<CandleStick> candles,
                                                   List<PeakTrough> peaksTroughs,
                                                   TrendDirection trendContext) {
        // 找出波峰
        List<PeakTrough> peaks = peaksTroughs.stream()
                .filter(pt -> pt.getType() == PeakTrough.Type.PEAK)
                .toList();

        if (peaks.size() < 3) {
            return null;
        }

        // 取最後三個波峰
        PeakTrough leftShoulder = peaks.get(peaks.size() - 3);
        PeakTrough head = peaks.get(peaks.size() - 2);
        PeakTrough rightShoulder = peaks.get(peaks.size() - 1);

        // 驗證頭肩頂結構
        // 1. 頭部要高於兩肩
        if (head.getPrice().compareTo(leftShoulder.getPrice()) <= 0 ||
            head.getPrice().compareTo(rightShoulder.getPrice()) <= 0) {
            return null;
        }

        // 2. 兩肩高度相近（容忍度內）
        if (!isSameLevel(leftShoulder.getPrice(), rightShoulder.getPrice(), LEVEL_TOLERANCE * 1.5)) {
            return null;
        }

        // 3. 頭部要明顯高於兩肩（至少高 2%）
        BigDecimal shoulderAvg = leftShoulder.getPrice().add(rightShoulder.getPrice())
                .divide(BigDecimal.valueOf(2), 4, RoundingMode.HALF_UP);
        BigDecimal headExcess = head.getPrice().subtract(shoulderAvg);
        if (headExcess.divide(shoulderAvg, 4, RoundingMode.HALF_UP).doubleValue() < 0.02) {
            return null;
        }

        // 找出頸線（兩肩之間的低點）
        List<PeakTrough> troughs = peaksTroughs.stream()
                .filter(pt -> pt.getType() == PeakTrough.Type.TROUGH)
                .filter(pt -> pt.getIndex() > leftShoulder.getIndex() && pt.getIndex() < rightShoulder.getIndex())
                .toList();

        if (troughs.size() < 2) {
            return null;
        }

        // 頸線由左肩後的低點和頭部後的低點連接
        PeakTrough neckLeft = troughs.stream()
                .filter(pt -> pt.getIndex() < head.getIndex())
                .min(Comparator.comparing(PeakTrough::getPrice))
                .orElse(null);

        PeakTrough neckRight = troughs.stream()
                .filter(pt -> pt.getIndex() > head.getIndex())
                .min(Comparator.comparing(PeakTrough::getPrice))
                .orElse(null);

        if (neckLeft == null || neckRight == null) {
            return null;
        }

        // 計算頸線價位（取平均或較高者）
        BigDecimal neckline = neckLeft.getPrice().max(neckRight.getPrice());

        // 計算型態高度
        BigDecimal patternHeight = head.getPrice().subtract(neckline);

        // 檢查是否突破頸線
        CandleStick lastCandle = candles.get(candles.size() - 1);
        boolean isBreaking = lastCandle.getClose().compareTo(neckline) < 0;

        // 計算強度
        int baseStrength = trendContext == TrendDirection.UPTREND ? 80 : 65;
        int strength = calculateHSStrength(baseStrength, leftShoulder, head, rightShoulder, isBreaking);

        // 計算目標價
        BigDecimal targetPrice = neckline.subtract(patternHeight);

        // 取得涉及的 K 線
        int startIdx = leftShoulder.getIndex();
        int endIdx = Math.min(candles.size() - 1, rightShoulder.getIndex() + 5);
        List<CandleStick> involvedCandles = candles.subList(startIdx, endIdx + 1);

        Map<String, Object> keyLevels = new HashMap<>();
        keyLevels.put("leftShoulder", leftShoulder.getPrice().doubleValue());
        keyLevels.put("head", head.getPrice().doubleValue());
        keyLevels.put("rightShoulder", rightShoulder.getPrice().doubleValue());
        keyLevels.put("necklineLeft", neckLeft.getPrice().doubleValue());
        keyLevels.put("necklineRight", neckRight.getPrice().doubleValue());
        keyLevels.put("neckline", neckline.doubleValue());
        keyLevels.put("patternHeight", patternHeight.doubleValue());
        keyLevels.put("isBreaking", isBreaking);

        return buildChartPattern(
                "CHART001",
                involvedCandles,
                strength,
                trendContext,
                neckline,
                targetPrice,
                keyLevels
        );
    }

    /**
     * 偵測頭肩底
     */
    private DetectedPattern detectInverseHeadAndShoulders(List<CandleStick> candles,
                                                          List<PeakTrough> peaksTroughs,
                                                          TrendDirection trendContext) {
        // 找出波谷
        List<PeakTrough> troughs = peaksTroughs.stream()
                .filter(pt -> pt.getType() == PeakTrough.Type.TROUGH)
                .toList();

        if (troughs.size() < 3) {
            return null;
        }

        // 取最後三個波谷
        PeakTrough leftShoulder = troughs.get(troughs.size() - 3);
        PeakTrough head = troughs.get(troughs.size() - 2);
        PeakTrough rightShoulder = troughs.get(troughs.size() - 1);

        // 驗證頭肩底結構
        // 1. 頭部要低於兩肩
        if (head.getPrice().compareTo(leftShoulder.getPrice()) >= 0 ||
            head.getPrice().compareTo(rightShoulder.getPrice()) >= 0) {
            return null;
        }

        // 2. 兩肩高度相近
        if (!isSameLevel(leftShoulder.getPrice(), rightShoulder.getPrice(), LEVEL_TOLERANCE * 1.5)) {
            return null;
        }

        // 3. 頭部要明顯低於兩肩
        BigDecimal shoulderAvg = leftShoulder.getPrice().add(rightShoulder.getPrice())
                .divide(BigDecimal.valueOf(2), 4, RoundingMode.HALF_UP);
        BigDecimal headExcess = shoulderAvg.subtract(head.getPrice());
        if (headExcess.divide(shoulderAvg, 4, RoundingMode.HALF_UP).doubleValue() < 0.02) {
            return null;
        }

        // 找出頸線
        List<PeakTrough> peaks = peaksTroughs.stream()
                .filter(pt -> pt.getType() == PeakTrough.Type.PEAK)
                .filter(pt -> pt.getIndex() > leftShoulder.getIndex() && pt.getIndex() < rightShoulder.getIndex())
                .toList();

        if (peaks.size() < 2) {
            return null;
        }

        PeakTrough neckLeft = peaks.stream()
                .filter(pt -> pt.getIndex() < head.getIndex())
                .max(Comparator.comparing(PeakTrough::getPrice))
                .orElse(null);

        PeakTrough neckRight = peaks.stream()
                .filter(pt -> pt.getIndex() > head.getIndex())
                .max(Comparator.comparing(PeakTrough::getPrice))
                .orElse(null);

        if (neckLeft == null || neckRight == null) {
            return null;
        }

        // 計算頸線價位
        BigDecimal neckline = neckLeft.getPrice().min(neckRight.getPrice());

        // 計算型態高度
        BigDecimal patternHeight = neckline.subtract(head.getPrice());

        // 檢查是否突破頸線
        CandleStick lastCandle = candles.get(candles.size() - 1);
        boolean isBreaking = lastCandle.getClose().compareTo(neckline) > 0;

        // 計算強度
        int baseStrength = trendContext == TrendDirection.DOWNTREND ? 80 : 65;
        int strength = calculateHSStrength(baseStrength, leftShoulder, head, rightShoulder, isBreaking);

        // 計算目標價
        BigDecimal targetPrice = neckline.add(patternHeight);

        // 取得涉及的 K 線
        int startIdx = leftShoulder.getIndex();
        int endIdx = Math.min(candles.size() - 1, rightShoulder.getIndex() + 5);
        List<CandleStick> involvedCandles = candles.subList(startIdx, endIdx + 1);

        Map<String, Object> keyLevels = new HashMap<>();
        keyLevels.put("leftShoulder", leftShoulder.getPrice().doubleValue());
        keyLevels.put("head", head.getPrice().doubleValue());
        keyLevels.put("rightShoulder", rightShoulder.getPrice().doubleValue());
        keyLevels.put("necklineLeft", neckLeft.getPrice().doubleValue());
        keyLevels.put("necklineRight", neckRight.getPrice().doubleValue());
        keyLevels.put("neckline", neckline.doubleValue());
        keyLevels.put("patternHeight", patternHeight.doubleValue());
        keyLevels.put("isBreaking", isBreaking);

        return buildChartPattern(
                "CHART002",
                involvedCandles,
                strength,
                trendContext,
                neckline,
                targetPrice,
                keyLevels
        );
    }

    /**
     * 計算頭肩型態強度
     */
    private int calculateHSStrength(int baseStrength, PeakTrough leftShoulder,
                                    PeakTrough head, PeakTrough rightShoulder, boolean isBreaking) {
        int strength = baseStrength;

        // 兩肩對稱性
        BigDecimal shoulderDiff = leftShoulder.getPrice().subtract(rightShoulder.getPrice()).abs();
        BigDecimal shoulderAvg = leftShoulder.getPrice().add(rightShoulder.getPrice())
                .divide(BigDecimal.valueOf(2), 4, RoundingMode.HALF_UP);
        double diffRatio = shoulderAvg.compareTo(BigDecimal.ZERO) > 0 ?
                shoulderDiff.divide(shoulderAvg, 4, RoundingMode.HALF_UP).doubleValue() : 0;
        if (diffRatio < 0.02) {
            strength += 10;
        }

        // 形成時間的對稱性（左肩到頭 vs 頭到右肩）
        int leftDuration = head.getIndex() - leftShoulder.getIndex();
        int rightDuration = rightShoulder.getIndex() - head.getIndex();
        double durationRatio = leftDuration > 0 ? (double) rightDuration / leftDuration : 0;
        if (durationRatio >= 0.7 && durationRatio <= 1.4) {
            strength += 5;
        }

        // 已突破頸線
        if (isBreaking) {
            strength += 15;
        }

        return Math.min(100, strength);
    }
}
