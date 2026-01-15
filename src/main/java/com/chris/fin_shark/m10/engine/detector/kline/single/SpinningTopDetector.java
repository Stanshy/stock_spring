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
 * 紡錘線偵測器
 * <p>
 * 支援型態：
 * - KLINE011: 紡錘線 (Spinning Top) - 市場猶豫
 * </p>
 * <p>
 * 特徵：小實體，上下影線長度相近
 * </p>
 *
 * @author chris
 * @since 1.0.0
 */
@Slf4j
@Component
public class SpinningTopDetector extends AbstractKLineDetector {

    @Override
    public String getName() {
        return "SpinningTopDetector";
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
                "KLINE011",
                "紡錘線",
                "Spinning Top",
                PatternCategory.KLINE_SINGLE,
                SignalType.NEUTRAL,
                1,
                "小實體配上下影線，表示市場多空拉鋸，可能出現反轉或盤整"
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

        // 檢查紡錘線形態特徵
        // 1. 小實體（實體 <= 全距的 30%）
        if (!current.isSmallBody(SMALL_BODY_RATIO)) {
            return patterns;
        }

        // 2. 不是十字星（實體要有一點大小）
        if (current.isNearDoji(DOJI_BODY_RATIO)) {
            return patterns; // 這是十字星，由 DojiDetector 處理
        }

        // 3. 上下影線都要有一定長度（各 >= 實體的 50%）
        BigDecimal body = current.getBody();
        BigDecimal upperShadow = current.getUpperShadow();
        BigDecimal lowerShadow = current.getLowerShadow();

        if (body.compareTo(BigDecimal.ZERO) == 0) {
            return patterns;
        }

        double upperRatio = upperShadow.divide(body, 4, RoundingMode.HALF_UP).doubleValue();
        double lowerRatio = lowerShadow.divide(body, 4, RoundingMode.HALF_UP).doubleValue();

        if (upperRatio < 0.5 || lowerRatio < 0.5) {
            return patterns;
        }

        // 4. 上下影線長度相近（比例差異 <= 2 倍）
        double shadowRatio = Math.max(upperRatio, lowerRatio) /
                Math.max(Math.min(upperRatio, lowerRatio), 0.001);
        if (shadowRatio > 2.5) {
            return patterns; // 影線差異太大，可能是錘子線或射擊之星
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

        // 基礎強度
        int baseStrength = 55;

        // 在明確趨勢末端出現的紡錘線更有意義
        if (trend == TrendDirection.UPTREND || trend == TrendDirection.DOWNTREND) {
            baseStrength = 65;
        }

        // 計算額外強度因子
        long avgVolume = calculateAverageVolume(candles, 20);
        boolean volumeConfirm = isVolumeIncreased(current, avgVolume, 1.2);

        // 上下影線越平衡，信號越強
        boolean balancedShadows = shadowRatio <= 1.5;

        int strength = calculateStrength(baseStrength, volumeConfirm, balancedShadows);

        // 建構結果
        Map<String, Object> additionalData = Map.of(
                "volumeConfirmation", volumeConfirm,
                "volumeRatio", avgVolume > 0 ?
                        BigDecimal.valueOf((double) current.getVolume() / avgVolume)
                                .setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO,
                "keyLevels", Map.of(
                        "bodyToRangeRatio", current.getRange().compareTo(BigDecimal.ZERO) > 0 ?
                                body.divide(current.getRange(), 4, RoundingMode.HALF_UP).doubleValue() : 0,
                        "upperShadowToBodyRatio", upperRatio,
                        "lowerShadowToBodyRatio", lowerRatio,
                        "shadowBalanceRatio", shadowRatio
                )
        );

        DetectedPattern pattern = buildPattern(
                "KLINE011",
                List.of(current),
                strength,
                trend,
                additionalData
        );

        if (pattern != null) {
            patterns.add(pattern);
            log.debug("偵測到型態: KLINE011 (紡錘線), 強度={}", strength);
        }

        return patterns;
    }
}
