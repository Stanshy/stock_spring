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
 * 晨星/夜星型態偵測器
 * <p>
 * 支援型態：
 * - KLINE040: 晨星 (Morning Star) - 下跌趨勢中的看漲反轉
 * - KLINE041: 夜星 (Evening Star) - 上漲趨勢中的看跌反轉
 * </p>
 * <p>
 * 特徵：
 * - 晨星：大陰線 + 小實體（跳空向下）+ 大陽線（收盤穿入第一根實體）
 * - 夜星：大陽線 + 小實體（跳空向上）+ 大陰線（收盤穿入第一根實體）
 * </p>
 *
 * @author chris
 * @since 1.0.0
 */
@Slf4j
@Component
public class MorningEveningStarDetector extends AbstractKLineDetector {

    @Override
    public String getName() {
        return "MorningEveningStarDetector";
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
        return 8; // 3 根 K 線 + 趨勢判斷
    }

    @Override
    protected void initializeMetadata() {
        registerMetadata(createMetadata(
                "KLINE040",
                "晨星",
                "Morning Star",
                PatternCategory.KLINE_TRIPLE,
                SignalType.BULLISH_REVERSAL,
                3,
                "下跌趨勢末端出現：大陰線 + 跳空向下的小實體 + 大陽線，為強烈的看漲反轉信號"
        ));

        registerMetadata(createMetadata(
                "KLINE041",
                "夜星",
                "Evening Star",
                PatternCategory.KLINE_TRIPLE,
                SignalType.BEARISH_REVERSAL,
                3,
                "上漲趨勢末端出現：大陽線 + 跳空向上的小實體 + 大陰線，為強烈的看跌反轉信號"
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
            // 用更長的回溯期來判斷趨勢
            if (isInDowntrend(candles.subList(0, candles.size() - 3), 5)) {
                trend = TrendDirection.DOWNTREND;
            } else if (isInUptrend(candles.subList(0, candles.size() - 3), 5)) {
                trend = TrendDirection.UPTREND;
            }
        }

        // 檢查晨星
        if (isMorningStar(first, second, third)) {
            DetectedPattern pattern = createMorningStarPattern(first, second, third, candles, trend);
            if (pattern != null) {
                patterns.add(pattern);
            }
        }

        // 檢查夜星
        if (isEveningStar(first, second, third)) {
            DetectedPattern pattern = createEveningStarPattern(first, second, third, candles, trend);
            if (pattern != null) {
                patterns.add(pattern);
            }
        }

        return patterns;
    }

    /**
     * 檢查是否為晨星
     */
    private boolean isMorningStar(CandleStick first, CandleStick second, CandleStick third) {
        // 1. 第一根是大陰線
        if (!first.isBearish() || !first.isLargeBody(0.50)) {
            return false;
        }

        // 2. 第二根是小實體（星體）
        if (!second.isSmallBody(0.35)) {
            return false;
        }

        // 3. 第二根跳空向下（開盤低於第一根收盤）
        // 注意：有些市場可能不一定有明顯跳空
        BigDecimal gap = first.getClose().subtract(second.getOpen().max(second.getClose()));
        if (gap.compareTo(BigDecimal.ZERO) < 0) {
            return false;
        }

        // 4. 第三根是大陽線
        if (!third.isBullish() || !third.isLargeBody(0.50)) {
            return false;
        }

        // 5. 第三根收盤穿入第一根實體的 50% 以上
        BigDecimal firstMid = first.getBodyMidpoint();
        return third.getClose().compareTo(firstMid) > 0;
    }

    /**
     * 檢查是否為夜星
     */
    private boolean isEveningStar(CandleStick first, CandleStick second, CandleStick third) {
        // 1. 第一根是大陽線
        if (!first.isBullish() || !first.isLargeBody(0.50)) {
            return false;
        }

        // 2. 第二根是小實體（星體）
        if (!second.isSmallBody(0.35)) {
            return false;
        }

        // 3. 第二根跳空向上（開盤高於第一根收盤）
        BigDecimal gap = second.getOpen().min(second.getClose()).subtract(first.getClose());
        if (gap.compareTo(BigDecimal.ZERO) < 0) {
            return false;
        }

        // 4. 第三根是大陰線
        if (!third.isBearish() || !third.isLargeBody(0.50)) {
            return false;
        }

        // 5. 第三根收盤穿入第一根實體的 50% 以上
        BigDecimal firstMid = first.getBodyMidpoint();
        return third.getClose().compareTo(firstMid) < 0;
    }

    /**
     * 建立晨星型態
     */
    private DetectedPattern createMorningStarPattern(CandleStick first, CandleStick second,
                                                     CandleStick third, List<CandleStick> candles,
                                                     TrendDirection trend) {
        // 基礎強度：在下跌趨勢中更強
        int baseStrength = trend == TrendDirection.DOWNTREND ? 80 : 60;

        // 額外強度因子
        long avgVolume = calculateAverageVolume(candles, 20);
        boolean thirdVolumeHigh = isVolumeIncreased(third, avgVolume, 1.3);

        // 第二根是十字星更強
        boolean starIsDoji = second.isNearDoji(0.15);

        // 第三根收盤越高越強
        BigDecimal penetration = third.getClose().subtract(first.getOpen());
        boolean deepPenetration = penetration.compareTo(BigDecimal.ZERO) >= 0;

        int strength = calculateStrength(baseStrength, thirdVolumeHigh, starIsDoji, deepPenetration);

        Map<String, Object> additionalData = Map.of(
                "volumeConfirmation", thirdVolumeHigh,
                "keyLevels", Map.of(
                        "firstBodyRatio", first.getRange().compareTo(BigDecimal.ZERO) > 0 ?
                                first.getBody().divide(first.getRange(), 4, RoundingMode.HALF_UP).doubleValue() : 0,
                        "starBodyRatio", second.getRange().compareTo(BigDecimal.ZERO) > 0 ?
                                second.getBody().divide(second.getRange(), 4, RoundingMode.HALF_UP).doubleValue() : 0,
                        "thirdBodyRatio", third.getRange().compareTo(BigDecimal.ZERO) > 0 ?
                                third.getBody().divide(third.getRange(), 4, RoundingMode.HALF_UP).doubleValue() : 0,
                        "isStarDoji", starIsDoji
                )
        );

        DetectedPattern pattern = buildPattern(
                "KLINE040",
                List.of(first, second, third),
                strength,
                trend,
                additionalData
        );

        if (pattern != null) {
            log.debug("偵測到型態: KLINE040 (晨星), 強度={}", strength);
        }

        return pattern;
    }

    /**
     * 建立夜星型態
     */
    private DetectedPattern createEveningStarPattern(CandleStick first, CandleStick second,
                                                     CandleStick third, List<CandleStick> candles,
                                                     TrendDirection trend) {
        // 基礎強度：在上漲趨勢中更強
        int baseStrength = trend == TrendDirection.UPTREND ? 80 : 60;

        // 額外強度因子
        long avgVolume = calculateAverageVolume(candles, 20);
        boolean thirdVolumeHigh = isVolumeIncreased(third, avgVolume, 1.3);

        // 第二根是十字星更強
        boolean starIsDoji = second.isNearDoji(0.15);

        // 第三根收盤越低越強
        BigDecimal penetration = first.getOpen().subtract(third.getClose());
        boolean deepPenetration = penetration.compareTo(BigDecimal.ZERO) >= 0;

        int strength = calculateStrength(baseStrength, thirdVolumeHigh, starIsDoji, deepPenetration);

        Map<String, Object> additionalData = Map.of(
                "volumeConfirmation", thirdVolumeHigh,
                "keyLevels", Map.of(
                        "firstBodyRatio", first.getRange().compareTo(BigDecimal.ZERO) > 0 ?
                                first.getBody().divide(first.getRange(), 4, RoundingMode.HALF_UP).doubleValue() : 0,
                        "starBodyRatio", second.getRange().compareTo(BigDecimal.ZERO) > 0 ?
                                second.getBody().divide(second.getRange(), 4, RoundingMode.HALF_UP).doubleValue() : 0,
                        "thirdBodyRatio", third.getRange().compareTo(BigDecimal.ZERO) > 0 ?
                                third.getBody().divide(third.getRange(), 4, RoundingMode.HALF_UP).doubleValue() : 0,
                        "isStarDoji", starIsDoji
                )
        );

        DetectedPattern pattern = buildPattern(
                "KLINE041",
                List.of(first, second, third),
                strength,
                trend,
                additionalData
        );

        if (pattern != null) {
            log.debug("偵測到型態: KLINE041 (夜星), 強度={}", strength);
        }

        return pattern;
    }
}
