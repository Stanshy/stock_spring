package com.chris.fin_shark.m10.engine.detector.kline.single;

import com.chris.fin_shark.m10.engine.detector.AbstractKLineDetector;
import com.chris.fin_shark.m10.engine.model.CandleStick;
import com.chris.fin_shark.m10.engine.model.DetectedPattern;
import com.chris.fin_shark.m10.enums.PatternCategory;
import com.chris.fin_shark.m10.enums.SignalType;
import com.chris.fin_shark.m10.enums.TrendDirection;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 大陽線/大陰線偵測器
 * <p>
 * 支援型態：
 * - KLINE009: 大陽線 (Bullish Marubozu) - 強烈看漲
 * - KLINE010: 大陰線 (Bearish Marubozu) - 強烈看跌
 * </p>
 * <p>
 * 特徵：實體很長，幾乎沒有上下影線
 * </p>
 *
 * @author chris
 * @since 1.0.0
 */
@Slf4j
@Component
public class MarubozuDetector extends AbstractKLineDetector {

    @Override
    public String getName() {
        return "MarubozuDetector";
    }

    @Override
    public String getCategory() {
        return "KLINE_SINGLE";
    }

    @Override
    public String getPriority() {
        return "P0";
    }

    @Override
    public int getMinDataPoints() {
        return 5;
    }

    @Override
    protected void initializeMetadata() {
        registerMetadata(createMetadata(
                "KLINE009",
                "大陽線",
                "Bullish Marubozu",
                PatternCategory.KLINE_SINGLE,
                SignalType.BULLISH_CONTINUATION,
                1,
                "實體很長的陽線，上下影線極短或無，表示買方強勢控盤，看漲延續"
        ));

        registerMetadata(createMetadata(
                "KLINE010",
                "大陰線",
                "Bearish Marubozu",
                PatternCategory.KLINE_SINGLE,
                SignalType.BEARISH_CONTINUATION,
                1,
                "實體很長的陰線，上下影線極短或無，表示賣方強勢控盤，看跌延續"
        ));
    }

    @Override
    protected List<DetectedPattern> doDetect(List<CandleStick> candles,
                                             Map<String, Object> params,
                                             TrendDirection trendContext) {
        List<DetectedPattern> patterns = new ArrayList<>();

        if (candles.isEmpty()) {
            return patterns;
        }

        CandleStick current = candles.get(candles.size() - 1);

        // 檢查大實體（實體 >= 全距的 85%）
        // Marubozu 的特徵是幾乎沒有影線
        if (!current.isLargeBody(0.85)) {
            return patterns;
        }

        // 確認影線都很短
        BigDecimal range = current.getRange();
        if (range.compareTo(BigDecimal.ZERO) == 0) {
            return patterns;
        }

        BigDecimal upperShadow = current.getUpperShadow();
        BigDecimal lowerShadow = current.getLowerShadow();

        double upperRatio = upperShadow.divide(range, 4, RoundingMode.HALF_UP).doubleValue();
        double lowerRatio = lowerShadow.divide(range, 4, RoundingMode.HALF_UP).doubleValue();

        // 上下影線都要 <= 10%
        if (upperRatio > 0.10 || lowerRatio > 0.10) {
            return patterns;
        }

        // 根據陰陽線決定型態
        String patternId;
        int baseStrength;

        if (current.isBullish()) {
            patternId = "KLINE009"; // 大陽線
            baseStrength = 75;
        } else if (current.isBearish()) {
            patternId = "KLINE010"; // 大陰線
            baseStrength = 75;
        } else {
            return patterns; // 平盤不是 Marubozu
        }

        // 計算額外強度因子
        long avgVolume = calculateAverageVolume(candles, 20);
        boolean volumeConfirm = isVolumeIncreased(current, avgVolume, 1.5);

        // 計算實體相對於近期平均實體的大小
        BigDecimal avgBody = calculateAverageBody(candles, 10);
        boolean extraLargeBody = avgBody.compareTo(BigDecimal.ZERO) > 0 &&
                current.getBody().divide(avgBody, 2, RoundingMode.HALF_UP).doubleValue() >= 2.0;

        // 完美的 Marubozu（完全無影線）
        boolean perfectMarubozu = upperRatio <= 0.02 && lowerRatio <= 0.02;

        int strength = calculateStrength(baseStrength,
                volumeConfirm,
                extraLargeBody,
                perfectMarubozu);

        // 建構結果
        Map<String, Object> additionalData = Map.of(
                "volumeConfirmation", volumeConfirm,
                "volumeRatio", avgVolume > 0 ?
                        BigDecimal.valueOf((double) current.getVolume() / avgVolume)
                                .setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO,
                "keyLevels", Map.of(
                        "bodyToRangeRatio", current.getBody().divide(range, 4, RoundingMode.HALF_UP).doubleValue(),
                        "upperShadowRatio", upperRatio,
                        "lowerShadowRatio", lowerRatio,
                        "isPerfectMarubozu", perfectMarubozu
                )
        );

        DetectedPattern pattern = buildPattern(
                patternId,
                List.of(current),
                strength,
                trendContext,
                additionalData
        );

        if (pattern != null) {
            patterns.add(pattern);
            log.debug("偵測到型態: {} ({}), 強度={}", patternId, pattern.getPatternName(), strength);
        }

        return patterns;
    }

    /**
     * 計算近期平均實體大小
     */
    private BigDecimal calculateAverageBody(List<CandleStick> candles, int period) {
        int count = Math.min(period, candles.size() - 1);
        if (count <= 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal sum = BigDecimal.ZERO;
        for (int i = candles.size() - 1 - count; i < candles.size() - 1; i++) {
            sum = sum.add(candles.get(i).getBody());
        }

        return sum.divide(BigDecimal.valueOf(count), 4, RoundingMode.HALF_UP);
    }
}
