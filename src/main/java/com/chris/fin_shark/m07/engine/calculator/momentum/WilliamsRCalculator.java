package com.chris.fin_shark.m07.engine.calculator.momentum;

import com.chris.fin_shark.m07.engine.calculator.IndicatorCalculator;
import com.chris.fin_shark.m07.engine.model.IndicatorMetadata;
import com.chris.fin_shark.m07.engine.model.PriceSeries;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Williams %R（威廉指標）計算器
 * <p>
 * 計算公式：
 * %R = ((最高價(N) - 收盤價) / (最高價(N) - 最低價(N))) * -100
 * </p>
 * <p>
 * 解讀：
 * - %R 範圍：-100 到 0
 * - %R > -20：超買區
 * - %R < -80：超賣區
 * </p>
 *
 * @author chris
 * @since 1.0.0
 */
@Component
public class WilliamsRCalculator implements IndicatorCalculator {

    @Override
    public String getName() {
        return "WILLR";
    }

    @Override
    public String getCategory() {
        return "MOMENTUM";
    }

    @Override
    public IndicatorMetadata getMetadata() {
        return IndicatorMetadata.builder()
                .name("WILLR")
                .category("MOMENTUM")
                .nameZh("威廉指標")
                .description("衡量超買超賣的動能震盪指標")
                .minDataPoints(14)
                .defaultParams(Map.of("period", 14))
                .priority("P1")
                .build();
    }

    @Override
    public Map<String, Object> calculate(PriceSeries series, Map<String, Object> params) {
        int period = (int) params.getOrDefault("period", 14);

        double[] highPrices = series.getHighArray();
        double[] lowPrices = series.getLowArray();
        double[] closePrices = series.getCloseArray();

        if (closePrices.length < period) {
            return Map.of();
        }

        // 找出最近 period 天的最高價和最低價
        double highestHigh = Double.MIN_VALUE;
        double lowestLow = Double.MAX_VALUE;

        int startIndex = highPrices.length - period;
        for (int i = startIndex; i < highPrices.length; i++) {
            highestHigh = Math.max(highestHigh, highPrices[i]);
            lowestLow = Math.min(lowestLow, lowPrices[i]);
        }

        // 計算 Williams %R
        double currentClose = closePrices[closePrices.length - 1];
        double range = highestHigh - lowestLow;

        if (range == 0) {
            return Map.of();  // 避免除以零
        }

        double williamsR = ((highestHigh - currentClose) / range) * -100;

        Map<String, Object> result = new HashMap<>();
        result.put("willr_" + period, round(williamsR));
        result.put("willr_signal", getSignal(williamsR));

        return result;
    }

    private String getSignal(double williamsR) {
        if (williamsR > -20) {
            return "OVERBOUGHT";  // 超買
        } else if (williamsR < -80) {
            return "OVERSOLD";    // 超賣
        } else {
            return "NEUTRAL";
        }
    }

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
