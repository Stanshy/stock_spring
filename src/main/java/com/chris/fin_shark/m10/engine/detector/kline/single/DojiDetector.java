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
 * 十字星型態偵測器
 * <p>
 * 支援型態：
 * - KLINE005: 十字星 (Doji)
 * - KLINE006: 蜻蜓十字 (Dragonfly Doji)
 * - KLINE007: 墓碑十字 (Gravestone Doji)
 * - KLINE008: 長腿十字 (Long-Legged Doji)
 * </p>
 *
 * @author chris
 * @since 1.0.0
 */
@Slf4j
@Component
public class DojiDetector extends AbstractKLineDetector {

    @Override
    public String getName() {
        return "DojiDetector";
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
        return 5; // 需要前幾天資料判斷趨勢背景
    }

    @Override
    protected void initializeMetadata() {
        registerMetadata(createMetadata(
                "KLINE005",
                "十字星",
                "Doji",
                PatternCategory.KLINE_SINGLE,
                SignalType.NEUTRAL,
                1,
                "開盤價與收盤價幾乎相同，表示市場猶豫不決，可能出現反轉"
        ));

        registerMetadata(createMetadata(
                "KLINE006",
                "蜻蜓十字",
                "Dragonfly Doji",
                PatternCategory.KLINE_SINGLE,
                SignalType.BULLISH_REVERSAL,
                1,
                "下影線很長，上影線幾乎沒有，出現在下跌趨勢末端為看漲信號"
        ));

        registerMetadata(createMetadata(
                "KLINE007",
                "墓碑十字",
                "Gravestone Doji",
                PatternCategory.KLINE_SINGLE,
                SignalType.BEARISH_REVERSAL,
                1,
                "上影線很長，下影線幾乎沒有，出現在上漲趨勢末端為看跌信號"
        ));

        registerMetadata(createMetadata(
                "KLINE008",
                "長腿十字",
                "Long-Legged Doji",
                PatternCategory.KLINE_SINGLE,
                SignalType.NEUTRAL,
                1,
                "上下影線都很長，表示多空激烈爭奪，市場極度不確定"
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

        // 取得最後一根 K 線
        CandleStick current = candles.get(candles.size() - 1);

        // 檢查是否為十字星（實體很小）
        if (!current.isNearDoji(DOJI_BODY_RATIO)) {
            return patterns;
        }

        BigDecimal range = current.getRange();
        if (range.compareTo(BigDecimal.ZERO) == 0) {
            return patterns;
        }

        BigDecimal upperShadow = current.getUpperShadow();
        BigDecimal lowerShadow = current.getLowerShadow();

        // 計算影線比例
        double upperRatio = upperShadow.divide(range, 4, RoundingMode.HALF_UP).doubleValue();
        double lowerRatio = lowerShadow.divide(range, 4, RoundingMode.HALF_UP).doubleValue();

        // 計算平均成交量
        long avgVolume = calculateAverageVolume(candles, 20);
        boolean volumeConfirm = isVolumeIncreased(current, avgVolume, 1.2);

        // 判斷趨勢背景
        TrendDirection trend = trendContext;
        if (trend == null || trend == TrendDirection.UNKNOWN) {
            if (isInDowntrend(candles, 5)) {
                trend = TrendDirection.DOWNTREND;
            } else if (isInUptrend(candles, 5)) {
                trend = TrendDirection.UPTREND;
            }
        }

        // 分類十字星類型
        String patternId;
        int baseStrength;

        if (lowerRatio >= 0.6 && upperRatio <= 0.15) {
            // 蜻蜓十字：長下影線，無上影線
            patternId = "KLINE006";
            baseStrength = trend == TrendDirection.DOWNTREND ? 75 : 55;
        } else if (upperRatio >= 0.6 && lowerRatio <= 0.15) {
            // 墓碑十字：長上影線，無下影線
            patternId = "KLINE007";
            baseStrength = trend == TrendDirection.UPTREND ? 75 : 55;
        } else if (upperRatio >= 0.3 && lowerRatio >= 0.3) {
            // 長腿十字：上下影線都長
            patternId = "KLINE008";
            baseStrength = 60;
        } else {
            // 普通十字星
            patternId = "KLINE005";
            baseStrength = 55;
        }

        // 計算強度
        int strength = calculateStrength(baseStrength, volumeConfirm);

        // 建構結果
        Map<String, Object> additionalData = Map.of(
                "volumeConfirmation", volumeConfirm,
                "volumeRatio", avgVolume > 0 ?
                        BigDecimal.valueOf((double) current.getVolume() / avgVolume)
                                .setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO,
                "keyLevels", Map.of(
                        "upperShadowRatio", upperRatio,
                        "lowerShadowRatio", lowerRatio
                )
        );

        DetectedPattern pattern = buildPattern(
                patternId,
                List.of(current),
                strength,
                trend,
                additionalData
        );

        if (pattern != null) {
            patterns.add(pattern);
            log.debug("偵測到十字星型態: {} ({}), 強度={}", patternId, pattern.getPatternName(), strength);
        }

        return patterns;
    }
}
