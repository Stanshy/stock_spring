package com.chris.fin_shark.m10.engine.detector.kline.triple;

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
 * 三白兵/三烏鴉型態偵測器
 * <p>
 * 支援型態：
 * - KLINE042: 三白兵 (Three White Soldiers) - 看漲延續/反轉
 * - KLINE043: 三烏鴉 (Three Black Crows) - 看跌延續/反轉
 * </p>
 * <p>
 * 特徵：
 * - 三白兵：連續三根大陽線，每根開盤在前一根實體內，收盤創新高
 * - 三烏鴉：連續三根大陰線，每根開盤在前一根實體內，收盤創新低
 * </p>
 *
 * @author chris
 * @since 1.0.0
 */
@Slf4j
@Component
public class ThreeSoldiersDetector extends AbstractKLineDetector {

    @Override
    public String getName() {
        return "ThreeSoldiersDetector";
    }

    @Override
    public String getCategory() {
        return "KLINE_TRIPLE";
    }

    @Override
    public String getPriority() {
        return "P0";
    }

    @Override
    public int getMinDataPoints() {
        return 8;
    }

    @Override
    protected void initializeMetadata() {
        registerMetadata(createMetadata(
                "KLINE042",
                "三白兵",
                "Three White Soldiers",
                PatternCategory.KLINE_TRIPLE,
                SignalType.BULLISH_CONTINUATION,
                3,
                "連續三根大陽線，每根收盤創新高，表示買方強勢主導，看漲趨勢確認"
        ));

        registerMetadata(createMetadata(
                "KLINE043",
                "三烏鴉",
                "Three Black Crows",
                PatternCategory.KLINE_TRIPLE,
                SignalType.BEARISH_CONTINUATION,
                3,
                "連續三根大陰線，每根收盤創新低，表示賣方強勢主導，看跌趨勢確認"
        ));
    }

    @Override
    protected List<DetectedPattern> doDetect(List<CandleStick> candles,
                                             Map<String, Object> params,
                                             TrendDirection trendContext) {
        List<DetectedPattern> patterns = new ArrayList<>();

        if (candles.size() < 3) {
            return patterns;
        }

        CandleStick first = candles.get(candles.size() - 3);
        CandleStick second = candles.get(candles.size() - 2);
        CandleStick third = candles.get(candles.size() - 1);

        // 判斷趨勢背景
        TrendDirection trend = trendContext;
        if (trend == null || trend == TrendDirection.UNKNOWN) {
            if (isInDowntrend(candles.subList(0, candles.size() - 3), 5)) {
                trend = TrendDirection.DOWNTREND;
            } else if (isInUptrend(candles.subList(0, candles.size() - 3), 5)) {
                trend = TrendDirection.UPTREND;
            }
        }

        // 檢查三白兵
        if (isThreeWhiteSoldiers(first, second, third)) {
            DetectedPattern pattern = createThreeWhiteSoldiersPattern(first, second, third, candles, trend);
            if (pattern != null) {
                patterns.add(pattern);
            }
        }

        // 檢查三烏鴉
        if (isThreeBlackCrows(first, second, third)) {
            DetectedPattern pattern = createThreeBlackCrowsPattern(first, second, third, candles, trend);
            if (pattern != null) {
                patterns.add(pattern);
            }
        }

        return patterns;
    }

    /**
     * 檢查是否為三白兵
     */
    private boolean isThreeWhiteSoldiers(CandleStick first, CandleStick second, CandleStick third) {
        // 1. 三根都是陽線
        if (!first.isBullish() || !second.isBullish() || !third.isBullish()) {
            return false;
        }

        // 2. 三根都有一定的實體大小
        if (!first.isLargeBody(0.40) || !second.isLargeBody(0.40) || !third.isLargeBody(0.40)) {
            return false;
        }

        // 3. 每根的收盤價都高於前一根
        if (second.getClose().compareTo(first.getClose()) <= 0 ||
            third.getClose().compareTo(second.getClose()) <= 0) {
            return false;
        }

        // 4. 每根的開盤在前一根實體內（或略高）
        if (second.getOpen().compareTo(first.getOpen()) < 0 ||
            second.getOpen().compareTo(first.getClose()) > 0) {
            return false;
        }
        if (third.getOpen().compareTo(second.getOpen()) < 0 ||
            third.getOpen().compareTo(second.getClose()) > 0) {
            return false;
        }

        // 5. 上影線不能太長（避免「前進受阻」變體）
        boolean shortUpperShadows = first.hasShortUpperShadow(0.5) &&
                second.hasShortUpperShadow(0.5) &&
                third.hasShortUpperShadow(0.5);

        return shortUpperShadows;
    }

    /**
     * 檢查是否為三烏鴉
     */
    private boolean isThreeBlackCrows(CandleStick first, CandleStick second, CandleStick third) {
        // 1. 三根都是陰線
        if (!first.isBearish() || !second.isBearish() || !third.isBearish()) {
            return false;
        }

        // 2. 三根都有一定的實體大小
        if (!first.isLargeBody(0.40) || !second.isLargeBody(0.40) || !third.isLargeBody(0.40)) {
            return false;
        }

        // 3. 每根的收盤價都低於前一根
        if (second.getClose().compareTo(first.getClose()) >= 0 ||
            third.getClose().compareTo(second.getClose()) >= 0) {
            return false;
        }

        // 4. 每根的開盤在前一根實體內（或略低）
        if (second.getOpen().compareTo(first.getClose()) < 0 ||
            second.getOpen().compareTo(first.getOpen()) > 0) {
            return false;
        }
        if (third.getOpen().compareTo(second.getClose()) < 0 ||
            third.getOpen().compareTo(second.getOpen()) > 0) {
            return false;
        }

        // 5. 下影線不能太長
        boolean shortLowerShadows = first.hasShortLowerShadow(0.5) &&
                second.hasShortLowerShadow(0.5) &&
                third.hasShortLowerShadow(0.5);

        return shortLowerShadows;
    }

    /**
     * 建立三白兵型態
     */
    private DetectedPattern createThreeWhiteSoldiersPattern(CandleStick first, CandleStick second,
                                                            CandleStick third, List<CandleStick> candles,
                                                            TrendDirection trend) {
        // 基礎強度：在下跌趨勢後出現更有意義（反轉）
        int baseStrength = trend == TrendDirection.DOWNTREND ? 85 : 75;

        // 額外強度因子
        long avgVolume = calculateAverageVolume(candles, 20);

        // 成交量逐步放大
        boolean volumeIncreasing = first.getVolume() != null && second.getVolume() != null && third.getVolume() != null &&
                second.getVolume() > first.getVolume() &&
                third.getVolume() > second.getVolume();

        // 實體大小相近（健康的三白兵）
        BigDecimal avgBody = first.getBody().add(second.getBody()).add(third.getBody())
                .divide(BigDecimal.valueOf(3), 4, RoundingMode.HALF_UP);
        boolean consistentBodies = first.getBody().divide(avgBody, 2, RoundingMode.HALF_UP).doubleValue() >= 0.7 &&
                second.getBody().divide(avgBody, 2, RoundingMode.HALF_UP).doubleValue() >= 0.7 &&
                third.getBody().divide(avgBody, 2, RoundingMode.HALF_UP).doubleValue() >= 0.7;

        int strength = calculateStrength(baseStrength, volumeIncreasing, consistentBodies);

        // 計算總漲幅
        BigDecimal totalGain = third.getClose().subtract(first.getOpen());
        double gainPercent = first.getOpen().compareTo(BigDecimal.ZERO) > 0 ?
                totalGain.divide(first.getOpen(), 4, RoundingMode.HALF_UP).doubleValue() * 100 : 0;

        Map<String, Object> additionalData = Map.of(
                "volumeConfirmation", volumeIncreasing,
                "keyLevels", Map.of(
                        "totalGainPercent", gainPercent,
                        "avgBodySize", avgBody.doubleValue(),
                        "isConsistentBodies", consistentBodies,
                        "isVolumeIncreasing", volumeIncreasing
                )
        );

        DetectedPattern pattern = buildPattern(
                "KLINE042",
                List.of(first, second, third),
                strength,
                trend,
                additionalData
        );

        if (pattern != null) {
            log.debug("偵測到型態: KLINE042 (三白兵), 強度={}", strength);
        }

        return pattern;
    }

    /**
     * 建立三烏鴉型態
     */
    private DetectedPattern createThreeBlackCrowsPattern(CandleStick first, CandleStick second,
                                                         CandleStick third, List<CandleStick> candles,
                                                         TrendDirection trend) {
        // 基礎強度：在上漲趨勢後出現更有意義（反轉）
        int baseStrength = trend == TrendDirection.UPTREND ? 85 : 75;

        // 額外強度因子
        long avgVolume = calculateAverageVolume(candles, 20);

        // 成交量逐步放大
        boolean volumeIncreasing = first.getVolume() != null && second.getVolume() != null && third.getVolume() != null &&
                second.getVolume() > first.getVolume() &&
                third.getVolume() > second.getVolume();

        // 實體大小相近
        BigDecimal avgBody = first.getBody().add(second.getBody()).add(third.getBody())
                .divide(BigDecimal.valueOf(3), 4, RoundingMode.HALF_UP);
        boolean consistentBodies = avgBody.compareTo(BigDecimal.ZERO) > 0 &&
                first.getBody().divide(avgBody, 2, RoundingMode.HALF_UP).doubleValue() >= 0.7 &&
                second.getBody().divide(avgBody, 2, RoundingMode.HALF_UP).doubleValue() >= 0.7 &&
                third.getBody().divide(avgBody, 2, RoundingMode.HALF_UP).doubleValue() >= 0.7;

        int strength = calculateStrength(baseStrength, volumeIncreasing, consistentBodies);

        // 計算總跌幅
        BigDecimal totalLoss = first.getOpen().subtract(third.getClose());
        double lossPercent = first.getOpen().compareTo(BigDecimal.ZERO) > 0 ?
                totalLoss.divide(first.getOpen(), 4, RoundingMode.HALF_UP).doubleValue() * 100 : 0;

        Map<String, Object> additionalData = Map.of(
                "volumeConfirmation", volumeIncreasing,
                "keyLevels", Map.of(
                        "totalLossPercent", lossPercent,
                        "avgBodySize", avgBody.doubleValue(),
                        "isConsistentBodies", consistentBodies,
                        "isVolumeIncreasing", volumeIncreasing
                )
        );

        DetectedPattern pattern = buildPattern(
                "KLINE043",
                List.of(first, second, third),
                strength,
                trend,
                additionalData
        );

        if (pattern != null) {
            log.debug("偵測到型態: KLINE043 (三烏鴉), 強度={}", strength);
        }

        return pattern;
    }
}
