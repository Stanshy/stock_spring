package com.chris.fin_shark.m07.engine.calculator.volume;

import com.chris.fin_shark.m07.engine.calculator.IndicatorCalculator;
import com.chris.fin_shark.m07.engine.model.IndicatorMetadata;
import com.chris.fin_shark.m07.engine.model.PriceSeries;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Force Index（力量指數）計算器
 * <p>
 * 計算公式：
 * Force Index = (Close - Previous Close) * Volume
 * EMA Force Index = EMA(Force Index, period)
 * </p>
 * <p>
 * 特點：結合價格變動與成交量衡量買賣力道
 * </p>
 *
 * @author chris
 * @since 1.0.0
 */
@Component
public class ForceIndexCalculator implements IndicatorCalculator {

    @Override
    public String getName() {
        return "FORCE_INDEX";
    }

    @Override
    public String getCategory() {
        return "VOLUME";
    }

    @Override
    public IndicatorMetadata getMetadata() {
        return IndicatorMetadata.builder()
                .name("FORCE_INDEX")
                .category("VOLUME")
                .nameZh("力量指數")
                .description("結合價格變動與成交量衡量買賣力道")
                .minDataPoints(15)
                .defaultParams(Map.of("period", 13))
                .priority("P2")
                .build();
    }

    @Override
    public Map<String, Object> calculate(PriceSeries series, Map<String, Object> params) {
        int period = (int) params.getOrDefault("period", 13);
        double[] closePrices = series.getCloseArray();
        long[] volumes = series.getVolumeArray();

        Map<String, Object> result = new HashMap<>();

        if (volumes.length == 0) {
            return result;
        }

        if (closePrices.length >= period + 1 && volumes.length >= period + 1) {
            double forceIndex = calculateForceIndex(closePrices, volumes, period);
            result.put("force_index", round(forceIndex));
            result.put("force_index_signal", forceIndex > 0 ? "BULLISH" : "BEARISH");
        }

        return result;
    }

    private double calculateForceIndex(double[] prices, long[] volumes, int period) {
        int length = prices.length;

        // 計算原始 Force Index 序列
        double[] forceIndex = new double[length - 1];
        for (int i = 1; i < length; i++) {
            forceIndex[i - 1] = (prices[i] - prices[i - 1]) * volumes[i];
        }

        // 計算 EMA of Force Index
        double multiplier = 2.0 / (period + 1);
        double ema = forceIndex[0];

        for (int i = 1; i < forceIndex.length; i++) {
            ema = (forceIndex[i] - ema) * multiplier + ema;
        }

        return ema;
    }

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
