package com.chris.fin_shark.m07.engine.calculator.trend;

import com.chris.fin_shark.m07.engine.calculator.IndicatorCalculator;
import com.chris.fin_shark.m07.engine.model.IndicatorMetadata;
import com.chris.fin_shark.m07.engine.model.PriceSeries;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * TEMA（三重指數移動平均）計算器
 * <p>
 * 計算公式：
 * TEMA = 3 * EMA(n) - 3 * EMA(EMA(n)) + EMA(EMA(EMA(n)))
 * </p>
 * <p>
 * 特點：比 DEMA 更快速反應，進一步減少滯後性
 * </p>
 *
 * @author chris
 * @since 1.0.0
 */
@Component
public class TEMACalculator implements IndicatorCalculator {

    @Override
    public String getName() {
        return "TEMA";
    }

    @Override
    public String getCategory() {
        return "TREND";
    }

    @Override
    public IndicatorMetadata getMetadata() {
        return IndicatorMetadata.builder()
                .name("TEMA")
                .category("TREND")
                .nameZh("三重指數移動平均")
                .description("減少滯後性的三重 EMA")
                .minDataPoints(30)
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
            if (closePrices.length >= period * 3) {
                double tema = calculateTEMA(closePrices, period);
                result.put("tema_" + period, round(tema));
            }
        }

        return result;
    }

    private double calculateTEMA(double[] prices, int period) {
        // 計算 EMA1
        double[] ema1 = calculateEMASeries(prices, period);

        // 計算 EMA2 = EMA(EMA1)
        double[] ema2 = calculateEMASeries(ema1, period);

        // 計算 EMA3 = EMA(EMA2)
        double[] ema3 = calculateEMASeries(ema2, period);

        // TEMA = 3 * EMA1 - 3 * EMA2 + EMA3
        int lastIndex = ema1.length - 1;
        return 3 * ema1[lastIndex] - 3 * ema2[lastIndex] + ema3[lastIndex];
    }

    private double[] calculateEMASeries(double[] prices, int period) {
        double[] ema = new double[prices.length];
        double multiplier = 2.0 / (period + 1);

        // 初始 EMA = SMA
        double sum = 0;
        for (int i = 0; i < period && i < prices.length; i++) {
            sum += prices[i];
        }
        int startIndex = Math.min(period - 1, prices.length - 1);
        ema[startIndex] = sum / Math.min(period, prices.length);

        // 計算後續 EMA
        for (int i = startIndex + 1; i < prices.length; i++) {
            ema[i] = (prices[i] - ema[i - 1]) * multiplier + ema[i - 1];
        }

        return ema;
    }

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
