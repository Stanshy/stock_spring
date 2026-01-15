package com.chris.fin_shark.m07.engine.calculator.oscillator;

import com.chris.fin_shark.m07.engine.calculator.IndicatorCalculator;
import com.chris.fin_shark.m07.engine.model.IndicatorMetadata;
import com.chris.fin_shark.m07.engine.model.PriceSeries;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Elder Ray Index（乖離射線指標）計算器
 * <p>
 * 計算公式：
 * Bull Power = High - EMA(Close, period)
 * Bear Power = Low - EMA(Close, period)
 * </p>
 * <p>
 * 信號解讀：
 * - Bull Power > 0: 多頭力量
 * - Bear Power < 0: 空頭力量
 * - 結合趨勢使用效果更佳
 * </p>
 * <p>
 * TODO: 原版 Elder Ray 通常搭配 13 期 EMA 和趨勢判斷（如 200 EMA）使用
 * </p>
 *
 * @author chris
 * @since 1.0.0
 */
@Component
public class ElderRayCalculator implements IndicatorCalculator {

    @Override
    public String getName() {
        return "ELDER_RAY";
    }

    @Override
    public String getCategory() {
        return "OSCILLATOR";
    }

    @Override
    public IndicatorMetadata getMetadata() {
        return IndicatorMetadata.builder()
                .name("ELDER_RAY")
                .category("OSCILLATOR")
                .nameZh("乖離射線指標")
                .description("衡量多空力量的指標")
                .minDataPoints(20)
                .defaultParams(Map.of("period", 13))
                .priority("P2")
                .build();
    }

    @Override
    public Map<String, Object> calculate(PriceSeries series, Map<String, Object> params) {
        int period = (int) params.getOrDefault("period", 13);

        double[] highPrices = series.getHighArray();
        double[] lowPrices = series.getLowArray();
        double[] closePrices = series.getCloseArray();

        Map<String, Object> result = new HashMap<>();

        if (closePrices.length >= period) {
            double[] elderRay = calculateElderRay(highPrices, lowPrices, closePrices, period);

            result.put("bull_power", round(elderRay[0]));
            result.put("bear_power", round(elderRay[1]));
            result.put("elder_ray_signal", getSignal(elderRay[0], elderRay[1]));
        }

        return result;
    }

    private double[] calculateElderRay(double[] highs, double[] lows, double[] closes, int period) {
        int length = closes.length;

        // 計算 EMA
        double ema = calculateEMA(closes, period);

        // Bull Power = High - EMA
        double bullPower = highs[length - 1] - ema;

        // Bear Power = Low - EMA
        double bearPower = lows[length - 1] - ema;

        return new double[]{bullPower, bearPower};
    }

    private double calculateEMA(double[] prices, int period) {
        double multiplier = 2.0 / (period + 1);
        double ema = prices[0];

        for (int i = 1; i < prices.length; i++) {
            ema = (prices[i] - ema) * multiplier + ema;
        }

        return ema;
    }

    /**
     * TODO: 信號判斷應結合趨勢方向，此處僅為簡化版本
     */
    private String getSignal(double bullPower, double bearPower) {
        if (bullPower > 0 && bearPower > 0) {
            return "STRONG_BULLISH";  // 強多頭
        } else if (bullPower > 0 && bearPower < 0) {
            // 正常情況：多頭有力量但空頭也存在
            if (bullPower > Math.abs(bearPower)) {
                return "BULLISH";
            } else {
                return "BEARISH";
            }
        } else if (bullPower < 0 && bearPower < 0) {
            return "STRONG_BEARISH";  // 強空頭
        }
        return "NEUTRAL";
    }

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
