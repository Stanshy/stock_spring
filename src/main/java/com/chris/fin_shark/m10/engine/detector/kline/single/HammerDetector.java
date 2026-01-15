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
 * 錘子線/上吊線偵測器
 * <p>
 * 支援型態：
 * - KLINE001: 錘子線 (Hammer) - 下跌趨勢中的看漲反轉
 * - KLINE003: 上吊線 (Hanging Man) - 上漲趨勢中的看跌反轉
 * </p>
 * <p>
 * 特徵：小實體在上方，長下影線，短或無上影線
 * </p>
 *
 * @author chris
 * @since 1.0.0
 */
@Slf4j
@Component
public class HammerDetector extends AbstractKLineDetector {

    @Override
    public String getName() {
        return "HammerDetector";
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
                "KLINE001",
                "錘子線",
                "Hammer",
                PatternCategory.KLINE_SINGLE,
                SignalType.BULLISH_REVERSAL,
                1,
                "下跌趨勢末端出現，小實體在上方，長下影線（>=實體2倍），表示買方開始反擊"
        ));

        registerMetadata(createMetadata(
                "KLINE003",
                "上吊線",
                "Hanging Man",
                PatternCategory.KLINE_SINGLE,
                SignalType.BEARISH_REVERSAL,
                1,
                "上漲趨勢末端出現，形態與錘子線相同，但出現在上漲趨勢中，警示可能反轉"
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

        // 檢查錘子線形態特徵
        // 1. 小實體（實體 <= 全距的 35%）
        if (!current.isSmallBody(0.35)) {
            return patterns;
        }

        // 2. 長下影線（>= 實體的 2 倍）
        if (!current.hasLongLowerShadow(LONG_SHADOW_MULTIPLIER)) {
            return patterns;
        }

        // 3. 短上影線或無上影線（<= 實體的 30%）
        if (!current.hasShortUpperShadow(SHORT_SHADOW_RATIO)) {
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

        // 根據趨勢決定是錘子線還是上吊線
        String patternId;
        int baseStrength;

        if (trend == TrendDirection.DOWNTREND) {
            // 下跌趨勢 → 錘子線（看漲反轉）
            patternId = "KLINE001";
            baseStrength = 70;
        } else if (trend == TrendDirection.UPTREND) {
            // 上漲趨勢 → 上吊線（看跌反轉）
            patternId = "KLINE003";
            baseStrength = 65;
        } else {
            // 無明確趨勢，信號較弱
            patternId = "KLINE001";
            baseStrength = 50;
        }

        // 計算額外強度因子
        long avgVolume = calculateAverageVolume(candles, 20);
        boolean volumeConfirm = isVolumeIncreased(current, avgVolume, 1.3);

        // 下影線越長，信號越強
        BigDecimal body = current.getBody();
        BigDecimal lowerShadow = current.getLowerShadow();
        boolean extraLongShadow = body.compareTo(BigDecimal.ZERO) > 0 &&
                lowerShadow.divide(body, 2, RoundingMode.HALF_UP).doubleValue() >= 3.0;

        // 實體是陽線（對於錘子線更強）
        boolean isBullishBody = current.isBullish();

        int strength = calculateStrength(baseStrength,
                volumeConfirm,
                extraLongShadow,
                isBullishBody && "KLINE001".equals(patternId));

        // 建構結果
        Map<String, Object> additionalData = Map.of(
                "volumeConfirmation", volumeConfirm,
                "volumeRatio", avgVolume > 0 ?
                        BigDecimal.valueOf((double) current.getVolume() / avgVolume)
                                .setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO,
                "keyLevels", Map.of(
                        "bodyToRangeRatio", current.getRange().compareTo(BigDecimal.ZERO) > 0 ?
                                body.divide(current.getRange(), 4, RoundingMode.HALF_UP).doubleValue() : 0,
                        "lowerShadowToBodyRatio", body.compareTo(BigDecimal.ZERO) > 0 ?
                                lowerShadow.divide(body, 4, RoundingMode.HALF_UP).doubleValue() : 0,
                        "isBullishBody", isBullishBody
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
            log.debug("偵測到型態: {} ({}), 強度={}", patternId, pattern.getPatternName(), strength);
        }

        return patterns;
    }
}
