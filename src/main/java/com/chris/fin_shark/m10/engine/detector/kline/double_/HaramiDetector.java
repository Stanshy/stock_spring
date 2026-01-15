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
 * 孕線型態偵測器
 * <p>
 * 支援型態：
 * - KLINE026: 看漲孕線 (Bullish Harami) - 下跌趨勢中的看漲反轉
 * - KLINE027: 看跌孕線 (Bearish Harami) - 上漲趨勢中的看跌反轉
 * </p>
 * <p>
 * 特徵：第二根 K 線的實體完全被第一根的實體包覆
 * </p>
 *
 * @author chris
 * @since 1.0.0
 */
@Slf4j
@Component
public class HaramiDetector extends AbstractKLineDetector {

    @Override
    public String getName() {
        return "HaramiDetector";
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
                "KLINE026",
                "看漲孕線",
                "Bullish Harami",
                PatternCategory.KLINE_DOUBLE,
                SignalType.BULLISH_REVERSAL,
                2,
                "下跌趨勢中，一根小陽線被前一根大陰線的實體包覆，表示賣壓減弱"
        ));

        registerMetadata(createMetadata(
                "KLINE027",
                "看跌孕線",
                "Bearish Harami",
                PatternCategory.KLINE_DOUBLE,
                SignalType.BEARISH_REVERSAL,
                2,
                "上漲趨勢中，一根小陰線被前一根大陽線的實體包覆，表示買氣減弱"
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

        // 檢查孕線型態：第二根被第一根包覆
        if (!current.isInsideOf(prev)) {
            return patterns;
        }

        // 第一根要是大實體
        if (!prev.isLargeBody(0.50)) {
            return patterns;
        }

        // 第二根要是小實體
        if (!current.isSmallBody(0.50)) {
            return patterns;
        }

        // 判斷趨勢背景
        TrendDirection trend = trendContext;
        if (trend == null || trend == TrendDirection.UNKNOWN) {
            if (isInDowntrend(candles, 5)) {
                trend = TrendDirection.DOWNTREND;
            } else if (isInUptrend(candles, 5)) {
                trend = TrendDirection.UPTREND;
            }
        }

        // 確認是反向的 K 線（孕線通常要反向）
        boolean isBullishHarami = prev.isBearish() && (current.isBullish() || current.isNearDoji(0.15));
        boolean isBearishHarami = prev.isBullish() && (current.isBearish() || current.isNearDoji(0.15));

        if (!isBullishHarami && !isBearishHarami) {
            return patterns;
        }

        String patternId;
        int baseStrength;

        if (isBullishHarami) {
            patternId = "KLINE026";
            // 在下跌趨勢中信號更強
            baseStrength = trend == TrendDirection.DOWNTREND ? 65 : 50;
        } else {
            patternId = "KLINE027";
            // 在上漲趨勢中信號更強
            baseStrength = trend == TrendDirection.UPTREND ? 65 : 50;
        }

        // 計算額外強度因子
        long avgVolume = calculateAverageVolume(candles, 20);

        // 孕線的成交量通常會萎縮
        boolean volumeDecreased = current.getVolume() != null && prev.getVolume() != null &&
                current.getVolume() < prev.getVolume() * 0.7;

        // 第二根實體越小（越接近十字星），信號越強
        boolean smallSecondCandle = current.isSmallBody(0.25);

        // 第二根 K 線在第一根的中心區域
        BigDecimal prevMid = prev.getBodyMidpoint();
        BigDecimal currentMid = current.getBodyMidpoint();
        BigDecimal prevBody = prev.getBody();
        boolean centeredHarami = prevBody.compareTo(BigDecimal.ZERO) > 0 &&
                currentMid.subtract(prevMid).abs()
                        .divide(prevBody, 2, RoundingMode.HALF_UP).doubleValue() <= 0.25;

        int strength = calculateStrength(baseStrength,
                volumeDecreased,
                smallSecondCandle,
                centeredHarami);

        // 建構結果
        List<CandleStick> involvedCandles = List.of(prev, current);

        Map<String, Object> additionalData = Map.of(
                "volumeConfirmation", volumeDecreased,
                "keyLevels", Map.of(
                        "firstBodyRatio", prev.getRange().compareTo(BigDecimal.ZERO) > 0 ?
                                prevBody.divide(prev.getRange(), 4, RoundingMode.HALF_UP).doubleValue() : 0,
                        "secondBodyRatio", current.getRange().compareTo(BigDecimal.ZERO) > 0 ?
                                current.getBody().divide(current.getRange(), 4, RoundingMode.HALF_UP).doubleValue() : 0,
                        "isCentered", centeredHarami,
                        "isVolumeDecreased", volumeDecreased
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
