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
 * 吞噬型態偵測器
 * <p>
 * 支援型態：
 * - KLINE020: 看漲吞噬 (Bullish Engulfing) - 下跌趨勢中的看漲反轉
 * - KLINE021: 看跌吞噬 (Bearish Engulfing) - 上漲趨勢中的看跌反轉
 * </p>
 * <p>
 * 特徵：第二根 K 線的實體完全包覆第一根的實體
 * </p>
 *
 * @author chris
 * @since 1.0.0
 */
@Slf4j
@Component
public class EngulfingDetector extends AbstractKLineDetector {

    @Override
    public String getName() {
        return "EngulfingDetector";
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
        return 7; // 2 根 K 線 + 趨勢判斷
    }

    @Override
    protected void initializeMetadata() {
        registerMetadata(createMetadata(
                "KLINE020",
                "看漲吞噬",
                "Bullish Engulfing",
                PatternCategory.KLINE_DOUBLE,
                SignalType.BULLISH_REVERSAL,
                2,
                "下跌趨勢中，一根大陽線完全包覆前一根陰線的實體，為強烈的看漲反轉信號"
        ));

        registerMetadata(createMetadata(
                "KLINE021",
                "看跌吞噬",
                "Bearish Engulfing",
                PatternCategory.KLINE_DOUBLE,
                SignalType.BEARISH_REVERSAL,
                2,
                "上漲趨勢中，一根大陰線完全包覆前一根陽線的實體，為強烈的看跌反轉信號"
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

        // 檢查吞噬型態
        if (!current.engulfs(prev)) {
            return patterns;
        }

        // 確認是反向的 K 線
        boolean isBullishEngulfing = prev.isBearish() && current.isBullish();
        boolean isBearishEngulfing = prev.isBullish() && current.isBearish();

        if (!isBullishEngulfing && !isBearishEngulfing) {
            return patterns;
        }

        String patternId;
        int baseStrength;

        if (isBullishEngulfing) {
            patternId = "KLINE020";
            // 在下跌趨勢中信號更強
            baseStrength = trend == TrendDirection.DOWNTREND ? 80 : 60;
        } else {
            patternId = "KLINE021";
            // 在上漲趨勢中信號更強
            baseStrength = trend == TrendDirection.UPTREND ? 80 : 60;
        }

        // 計算額外強度因子
        long avgVolume = calculateAverageVolume(candles, 20);
        boolean volumeConfirm = isVolumeIncreased(current, avgVolume, 1.5);

        // 第二根 K 線實體越大，信號越強
        BigDecimal currentBody = current.getBody();
        BigDecimal prevBody = prev.getBody();
        boolean strongEngulf = prevBody.compareTo(BigDecimal.ZERO) > 0 &&
                currentBody.divide(prevBody, 2, RoundingMode.HALF_UP).doubleValue() >= 2.0;

        // 第二根 K 線的收盤價超出第一根的最高/最低價
        boolean fullEngulf;
        if (isBullishEngulfing) {
            fullEngulf = current.getClose().compareTo(prev.getHigh()) > 0;
        } else {
            fullEngulf = current.getClose().compareTo(prev.getLow()) < 0;
        }

        int strength = calculateStrength(baseStrength,
                volumeConfirm,
                strongEngulf,
                fullEngulf);

        // 建構結果
        List<CandleStick> involvedCandles = List.of(prev, current);

        Map<String, Object> additionalData = Map.of(
                "volumeConfirmation", volumeConfirm,
                "volumeRatio", avgVolume > 0 ?
                        BigDecimal.valueOf((double) current.getVolume() / avgVolume)
                                .setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO,
                "keyLevels", Map.of(
                        "engulfRatio", prevBody.compareTo(BigDecimal.ZERO) > 0 ?
                                currentBody.divide(prevBody, 4, RoundingMode.HALF_UP).doubleValue() : 0,
                        "isStrongEngulf", strongEngulf,
                        "isFullEngulf", fullEngulf
                )
        );

        DetectedPattern pattern = buildPattern(
                patternId,
                involvedCandles,
                strength,
                trend,
                additionalData
        );

        if (pattern != null) {
            patterns.add(pattern);
            log.debug("偵測到型態: {} ({}), 強度={}", patternId, pattern.getPatternName(), strength);
        }

        return patterns;
    }
}
