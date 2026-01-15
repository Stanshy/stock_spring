package com.chris.fin_shark.m07.engine.calculator.trend;

import com.chris.fin_shark.m07.engine.calculator.IndicatorCalculator;
import com.chris.fin_shark.m07.engine.model.IndicatorMetadata;
import com.chris.fin_shark.m07.engine.model.PriceSeries;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ZLEMA（零延遲指數移動平均）計算器
 * <p>
 * 計算公式：
 * lag = (period - 1) / 2
 * adjustedPrice = price + (price - price[lag])
 * ZLEMA = EMA(adjustedPrice, period)
 * </p>
 * <p>
 * 特點：透過調整價格來消除滯後性
 * </p>
 *
 * @author chris
 * @since 1.0.0
 */
@Component
public class ZLEMACalculator implements IndicatorCalculator {

    @Override
    public String getName() {
        return "ZLEMA";
    }

    @Override
    public String getCategory() {
        return "TREND";
    }

    @Override
    public IndicatorMetadata getMetadata() {
        return IndicatorMetadata.builder()
                .name("ZLEMA")
                .category("TREND")
                .nameZh("零延遲指數移動平均")
                .description("消除滯後性的 EMA 變體")
                .minDataPoints(20)
                .defaultParams(Map.of("periods", List.of(10, 20)))
                .priority("P2")
                .build();
    }

    @Override
    public Map<String, Object> calculate(PriceSeries series, Map<String, Object> params) {
        @SuppressWarnings("unchecked")
        List<Integer> periods = (List<Integer>) params.getOrDefault("periods", List.of(10, 20));

        double[] closePrices = series.getCloseArray();
        Map<String, Object> result = new HashMap<>();

        for (Integer period : periods) {
            int lag = (period - 1) / 2;
            if (closePrices.length >= period + lag) {
                double zlema = calculateZLEMA(closePrices, period);
                result.put("zlema_" + period, round(zlema));
            }
        }

        return result;
    }

    private double calculateZLEMA(double[] prices, int period) {
        int lag = (period - 1) / 2;
        int length = prices.length;

        // 計算調整後的價格序列
        double[] adjustedPrices = new double[length];
        for (int i = lag; i < length; i++) {
            adjustedPrices[i] = prices[i] + (prices[i] - prices[i - lag]);
        }
        // 填充前面的值
        for (int i = 0; i < lag; i++) {
            adjustedPrices[i] = prices[i];
        }

        // 計算 EMA
        double multiplier = 2.0 / (period + 1);
        double ema = adjustedPrices[0];

        for (int i = 1; i < length; i++) {
            ema = (adjustedPrices[i] - ema) * multiplier + ema;
        }

        return ema;
    }

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
