package com.chris.fin_shark.m10.engine.detector.kline.double_;

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
 * 平頭型態偵測器
 * <p>
 * 支援型態：
 * - KLINE024: 平頭頂部 (Tweezer Top) - 上漲趨勢中的看跌反轉
 * - KLINE025: 平頭底部 (Tweezer Bottom) - 下跌趨勢中的看漲反轉
 * </p>
 * <p>
 * 特徵：兩根連續的 K 線具有相同的最高點（頂部）或最低點（底部）
 * </p>
 *
 * @author chris
 * @since 1.0.0
 */
@Slf4j
@Component
public class TweezerDetector extends AbstractKLineDetector {

    /**
     * 價格容忍度（視為相同價位的最大差異比例）
     */
    private static final double PRICE_TOLERANCE = 0.005; // 0.5%

    @Override
    public String getName() {
        return "TweezerDetector";
    }

    @Override
    public String getCategory() {
        return "KLINE_DOUBLE";
    }

    @Override
    public String getPriority() {
        return "P0";
    }

    @Override
    public int getMinDataPoints() {
        return 7;
    }

    @Override
    protected void initializeMetadata() {
        registerMetadata(createMetadata(
                "KLINE024",
                "平頭頂部",
                "Tweezer Top",
                PatternCategory.KLINE_DOUBLE,
                SignalType.BEARISH_REVERSAL,
                2,
                "上漲趨勢末端，兩根K線有相同的最高價，表示在此價位遇到強大賣壓"
        ));

        registerMetadata(createMetadata(
                "KLINE025",
                "平頭底部",
                "Tweezer Bottom",
                PatternCategory.KLINE_DOUBLE,
                SignalType.BULLISH_REVERSAL,
                2,
                "下跌趨勢末端，兩根K線有相同的最低價，表示在此價位有強力買盤支撐"
        ));
    }

    @Override
    protected List<DetectedPattern> doDetect(List<CandleStick> candles,
                                             Map<String, Object> params,
                                             TrendDirection trendContext) {
        List<DetectedPattern> patterns = new ArrayList<>();

        if (candles.size() < 2) {
            return patterns;
        }

        CandleStick prev = candles.get(candles.size() - 2);
        CandleStick current = candles.get(candles.size() - 1);

        // 判斷趨勢背景
        TrendDirection trend = trendContext;
        if (trend == null || trend == TrendDirection.UNKNOWN) {
            if (isInDowntrend(candles, 5)) {
                trend = TrendDirection.DOWNTREND;
            } else if (isInUptrend(candles, 5)) {
                trend = TrendDirection.UPTREND;
            }
        }

        // 檢查平頭頂部
        if (trend == TrendDirection.UPTREND || trend == TrendDirection.UNKNOWN) {
            if (isTweezerTop(prev, current)) {
                DetectedPattern pattern = createTweezerTopPattern(prev, current, candles, trend);
                if (pattern != null) {
                    patterns.add(pattern);
                }
            }
        }

        // 檢查平頭底部
        if (trend == TrendDirection.DOWNTREND || trend == TrendDirection.UNKNOWN) {
            if (isTweezerBottom(prev, current)) {
                DetectedPattern pattern = createTweezerBottomPattern(prev, current, candles, trend);
                if (pattern != null) {
                    patterns.add(pattern);
                }
            }
        }

        return patterns;
    }

    /**
     * 檢查是否為平頭頂部
     */
    private boolean isTweezerTop(CandleStick prev, CandleStick current) {
        // 兩根 K 線的最高價相近
        if (!isPriceEqual(prev.getHigh(), current.getHigh())) {
            return false;
        }

        // 第一根通常是陽線，第二根是陰線（但不強制）
        // 主要特徵是在相同高點受阻

        // 至少有一根有明顯的上影線
        boolean hasUpperShadow = prev.getUpperShadow().compareTo(prev.getBody().multiply(BigDecimal.valueOf(0.3))) > 0 ||
                current.getUpperShadow().compareTo(current.getBody().multiply(BigDecimal.valueOf(0.3))) > 0;

        return hasUpperShadow;
    }

    /**
     * 檢查是否為平頭底部
     */
    private boolean isTweezerBottom(CandleStick prev, CandleStick current) {
        // 兩根 K 線的最低價相近
        if (!isPriceEqual(prev.getLow(), current.getLow())) {
            return false;
        }

        // 至少有一根有明顯的下影線
        boolean hasLowerShadow = prev.getLowerShadow().compareTo(prev.getBody().multiply(BigDecimal.valueOf(0.3))) > 0 ||
                current.getLowerShadow().compareTo(current.getBody().multiply(BigDecimal.valueOf(0.3))) > 0;

        return hasLowerShadow;
    }

    /**
     * 檢查兩個價格是否相近
     */
    private boolean isPriceEqual(BigDecimal price1, BigDecimal price2) {
        if (price1.compareTo(BigDecimal.ZERO) == 0) {
            return false;
        }
        BigDecimal diff = price1.subtract(price2).abs();
        BigDecimal ratio = diff.divide(price1, 6, RoundingMode.HALF_UP);
        return ratio.doubleValue() <= PRICE_TOLERANCE;
    }

    /**
     * 建立平頭頂部型態
     */
    private DetectedPattern createTweezerTopPattern(CandleStick prev, CandleStick current,
                                                    List<CandleStick> candles, TrendDirection trend) {
        // 基礎強度
        int baseStrength = trend == TrendDirection.UPTREND ? 65 : 50;

        // 額外強度因子
        long avgVolume = calculateAverageVolume(candles, 20);
        boolean volumeConfirm = isVolumeIncreased(current, avgVolume, 1.2);

        // 第二根是陰線更強
        boolean secondBearish = current.isBearish();

        // 價格完全相同更強
        boolean exactMatch = prev.getHigh().compareTo(current.getHigh()) == 0;

        int strength = calculateStrength(baseStrength, volumeConfirm, secondBearish, exactMatch);

        Map<String, Object> additionalData = Map.of(
                "volumeConfirmation", volumeConfirm,
                "keyLevels", Map.of(
                        "resistanceLevel", prev.getHigh().doubleValue(),
                        "priceDiff", prev.getHigh().subtract(current.getHigh()).abs().doubleValue(),
                        "isExactMatch", exactMatch
                )
        );

        DetectedPattern pattern = buildPattern(
                "KLINE024",
                List.of(prev, current),
                strength,
                trend,
                additionalData
        );

        if (pattern != null) {
            log.debug("偵測到型態: KLINE024 (平頭頂部), 強度={}", strength);
        }

        return pattern;
    }

    /**
     * 建立平頭底部型態
     */
    private DetectedPattern createTweezerBottomPattern(CandleStick prev, CandleStick current,
                                                       List<CandleStick> candles, TrendDirection trend) {
        // 基礎強度
        int baseStrength = trend == TrendDirection.DOWNTREND ? 65 : 50;

        // 額外強度因子
        long avgVolume = calculateAverageVolume(candles, 20);
        boolean volumeConfirm = isVolumeIncreased(current, avgVolume, 1.2);

        // 第二根是陽線更強
        boolean secondBullish = current.isBullish();

        // 價格完全相同更強
        boolean exactMatch = prev.getLow().compareTo(current.getLow()) == 0;

        int strength = calculateStrength(baseStrength, volumeConfirm, secondBullish, exactMatch);

        Map<String, Object> additionalData = Map.of(
                "volumeConfirmation", volumeConfirm,
                "keyLevels", Map.of(
                        "supportLevel", prev.getLow().doubleValue(),
                        "priceDiff", prev.getLow().subtract(current.getLow()).abs().doubleValue(),
                        "isExactMatch", exactMatch
                )
        );

        DetectedPattern pattern = buildPattern(
                "KLINE025",
                List.of(prev, current),
                strength,
                trend,
                additionalData
        );

        if (pattern != null) {
            log.debug("偵測到型態: KLINE025 (平頭底部), 強度={}", strength);
        }

        return pattern;
    }
}
