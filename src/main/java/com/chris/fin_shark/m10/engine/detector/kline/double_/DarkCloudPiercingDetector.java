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
 * 烏雲蓋頂/曙光初現偵測器
 * <p>
 * 支援型態：
 * - KLINE022: 烏雲蓋頂 (Dark Cloud Cover) - 上漲趨勢中的看跌反轉
 * - KLINE023: 曙光初現 (Piercing Line) - 下跌趨勢中的看漲反轉
 * </p>
 * <p>
 * 特徵：
 * - 烏雲蓋頂：陽線後出現陰線，陰線開盤高於前日高點，收盤穿入前日實體 50% 以上
 * - 曙光初現：陰線後出現陽線，陽線開盤低於前日低點，收盤穿入前日實體 50% 以上
 * </p>
 *
 * @author chris
 * @since 1.0.0
 */
@Slf4j
@Component
public class DarkCloudPiercingDetector extends AbstractKLineDetector {

    @Override
    public String getName() {
        return "DarkCloudPiercingDetector";
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
                "KLINE022",
                "烏雲蓋頂",
                "Dark Cloud Cover",
                PatternCategory.KLINE_DOUBLE,
                SignalType.BEARISH_REVERSAL,
                2,
                "上漲趨勢中，陰線開盤高於前日高點，收盤穿入前日陽線實體超過50%，為看跌反轉信號"
        ));

        registerMetadata(createMetadata(
                "KLINE023",
                "曙光初現",
                "Piercing Line",
                PatternCategory.KLINE_DOUBLE,
                SignalType.BULLISH_REVERSAL,
                2,
                "下跌趨勢中，陽線開盤低於前日低點，收盤穿入前日陰線實體超過50%，為看漲反轉信號"
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

        // 檢查烏雲蓋頂
        if (isDarkCloudCover(prev, current, trend)) {
            DetectedPattern pattern = createDarkCloudPattern(prev, current, candles, trend);
            if (pattern != null) {
                patterns.add(pattern);
            }
        }

        // 檢查曙光初現
        if (isPiercingLine(prev, current, trend)) {
            DetectedPattern pattern = createPiercingPattern(prev, current, candles, trend);
            if (pattern != null) {
                patterns.add(pattern);
            }
        }

        return patterns;
    }

    /**
     * 檢查是否為烏雲蓋頂
     */
    private boolean isDarkCloudCover(CandleStick prev, CandleStick current, TrendDirection trend) {
        // 1. 前一根是陽線
        if (!prev.isBullish()) return false;

        // 2. 當前是陰線
        if (!current.isBearish()) return false;

        // 3. 當前開盤高於前日高點（向上跳空開）
        if (current.getOpen().compareTo(prev.getHigh()) <= 0) return false;

        // 4. 當前收盤穿入前日實體 50% 以上
        BigDecimal prevMid = prev.getOpen().add(prev.getClose())
                .divide(BigDecimal.valueOf(2), 4, RoundingMode.HALF_UP);
        if (current.getClose().compareTo(prevMid) >= 0) return false;

        // 5. 當前收盤不能低於前日開盤（否則是看跌吞噬）
        if (current.getClose().compareTo(prev.getOpen()) < 0) return false;

        // 6. 兩根 K 線都要有一定的實體
        return prev.isLargeBody(0.40) && current.isLargeBody(0.40);
    }

    /**
     * 檢查是否為曙光初現
     */
    private boolean isPiercingLine(CandleStick prev, CandleStick current, TrendDirection trend) {
        // 1. 前一根是陰線
        if (!prev.isBearish()) return false;

        // 2. 當前是陽線
        if (!current.isBullish()) return false;

        // 3. 當前開盤低於前日低點（向下跳空開）
        if (current.getOpen().compareTo(prev.getLow()) >= 0) return false;

        // 4. 當前收盤穿入前日實體 50% 以上
        BigDecimal prevMid = prev.getOpen().add(prev.getClose())
                .divide(BigDecimal.valueOf(2), 4, RoundingMode.HALF_UP);
        if (current.getClose().compareTo(prevMid) <= 0) return false;

        // 5. 當前收盤不能高於前日開盤（否則是看漲吞噬）
        if (current.getClose().compareTo(prev.getOpen()) > 0) return false;

        // 6. 兩根 K 線都要有一定的實體
        return prev.isLargeBody(0.40) && current.isLargeBody(0.40);
    }

    /**
     * 建立烏雲蓋頂型態
     */
    private DetectedPattern createDarkCloudPattern(CandleStick prev, CandleStick current,
                                                   List<CandleStick> candles, TrendDirection trend) {
        // 基礎強度：在上漲趨勢中更強
        int baseStrength = trend == TrendDirection.UPTREND ? 75 : 55;

        // 計算穿入深度
        BigDecimal prevBody = prev.getBody();
        BigDecimal penetration = prev.getClose().subtract(current.getClose());
        double penetrationRatio = prevBody.compareTo(BigDecimal.ZERO) > 0 ?
                penetration.divide(prevBody, 4, RoundingMode.HALF_UP).doubleValue() : 0;

        // 額外強度因子
        long avgVolume = calculateAverageVolume(candles, 20);
        boolean volumeConfirm = isVolumeIncreased(current, avgVolume, 1.3);
        boolean deepPenetration = penetrationRatio >= 0.65;

        int strength = calculateStrength(baseStrength, volumeConfirm, deepPenetration);

        Map<String, Object> additionalData = Map.of(
                "volumeConfirmation", volumeConfirm,
                "keyLevels", Map.of(
                        "penetrationRatio", penetrationRatio,
                        "gapUp", current.getOpen().subtract(prev.getHigh()).doubleValue()
                )
        );

        DetectedPattern pattern = buildPattern(
                "KLINE022",
                List.of(prev, current),
                strength,
                trend,
                additionalData
        );

        if (pattern != null) {
            log.debug("偵測到型態: KLINE022 (烏雲蓋頂), 強度={}", strength);
        }

        return pattern;
    }

    /**
     * 建立曙光初現型態
     */
    private DetectedPattern createPiercingPattern(CandleStick prev, CandleStick current,
                                                  List<CandleStick> candles, TrendDirection trend) {
        // 基礎強度：在下跌趨勢中更強
        int baseStrength = trend == TrendDirection.DOWNTREND ? 75 : 55;

        // 計算穿入深度
        BigDecimal prevBody = prev.getBody();
        BigDecimal penetration = current.getClose().subtract(prev.getClose());
        double penetrationRatio = prevBody.compareTo(BigDecimal.ZERO) > 0 ?
                penetration.divide(prevBody, 4, RoundingMode.HALF_UP).doubleValue() : 0;

        // 額外強度因子
        long avgVolume = calculateAverageVolume(candles, 20);
        boolean volumeConfirm = isVolumeIncreased(current, avgVolume, 1.3);
        boolean deepPenetration = penetrationRatio >= 0.65;

        int strength = calculateStrength(baseStrength, volumeConfirm, deepPenetration);

        Map<String, Object> additionalData = Map.of(
                "volumeConfirmation", volumeConfirm,
                "keyLevels", Map.of(
                        "penetrationRatio", penetrationRatio,
                        "gapDown", prev.getLow().subtract(current.getOpen()).doubleValue()
                )
        );

        DetectedPattern pattern = buildPattern(
                "KLINE023",
                List.of(prev, current),
                strength,
                trend,
                additionalData
        );

        if (pattern != null) {
            log.debug("偵測到型態: KLINE023 (曙光初現), 強度={}", strength);
        }

        return pattern;
    }
}
