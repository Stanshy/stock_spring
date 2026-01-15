package com.chris.fin_shark.m07.engine.calculator.trend;

import com.chris.fin_shark.m07.engine.calculator.IndicatorCalculator;
import com.chris.fin_shark.m07.engine.model.IndicatorMetadata;
import com.chris.fin_shark.m07.engine.model.PriceSeries;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * DEMA（雙重指數移動平均）計算器
 * <p>
 * 計算公式：
 * DEMA = 2 * EMA(n) - EMA(EMA(n))
 * </p>
 * <p>
 * 特點：比 EMA 更快速反應價格變化，減少滯後性
 * </p>
 *
 * @author chris
 * @since 1.0.0
 */
@Component
public class DEMACalculator implements IndicatorCalculator {

    @Override
    public String getName() {
        return "DEMA";
    }

    @Override
    public String getCategory() {
        return "TREND";
    }

    @Override
    public IndicatorMetadata getMetadata() {
        return IndicatorMetadata.builder()
                .name("DEMA")
                .category("TREND")
                .nameZh("雙重指數移動平均")
                .description("減少滯後性的雙重 EMA")
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
            if (closePrices.length >= period * 2) {
                double dema = calculateDEMA(closePrices, period);
                result.put("dema_" + period, round(dema));
            }
        }

        return result;
    }

    private double calculateDEMA(double[] prices, int period) {
        // 計算 EMA
        double[] ema = calculateEMASeries(prices, period);

        // 計算 EMA of EMA
        double[] emaOfEma = calculateEMASeries(ema, period);

        // DEMA = 2 * EMA - EMA(EMA)
        int lastIndex = ema.length - 1;
        return 2 * ema[lastIndex] - emaOfEma[lastIndex];
    }

    private double[] calculateEMASeries(double[] prices, int period) {
        double[] ema = new double[prices.length];
        double multiplier = 2.0 / (period + 1);

        // 初始 EMA = SMA
        double sum = 0;
        for (int i = 0; i < period; i++) {
            sum += prices[i];
        }
        ema[period - 1] = sum / period;

        // 計算後續 EMA
        for (int i = period; i < prices.length; i++) {
            ema[i] = (prices[i] - ema[i - 1]) * multiplier + ema[i - 1];
        }

        return ema;
    }

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
