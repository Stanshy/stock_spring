package com.chris.fin_shark.m07.engine.calculator.statistics;

import com.chris.fin_shark.m07.engine.calculator.IndicatorCalculator;
import com.chris.fin_shark.m07.engine.model.IndicatorMetadata;
import com.chris.fin_shark.m07.engine.model.PriceSeries;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 標準差計算器
 * <p>
 * 計算公式：
 * σ = √(Σ(x - μ)² / n)
 * </p>
 * <p>
 * 特點：衡量價格波動的離散程度
 * </p>
 *
 * @author chris
 * @since 1.0.0
 */
@Component
public class StandardDeviationCalculator implements IndicatorCalculator {

    @Override
    public String getName() {
        return "STD_DEV";
    }

    @Override
    public String getCategory() {
        return "STATISTICS";
    }

    @Override
    public IndicatorMetadata getMetadata() {
        return IndicatorMetadata.builder()
                .name("STD_DEV")
                .category("STATISTICS")
                .nameZh("標準差")
                .description("衡量價格波動的離散程度")
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
            if (closePrices.length >= period) {
                double stdDev = calculateStdDev(closePrices, period);
                result.put("std_dev_" + period, round(stdDev));
            }
        }

        // 計算波動率百分比（相對於均值）
        if (closePrices.length >= 20) {
            double stdDev20 = calculateStdDev(closePrices, 20);
            double mean20 = calculateMean(closePrices, 20);
            if (mean20 != 0) {
                double volatilityPct = (stdDev20 / mean20) * 100;
                result.put("volatility_pct", round(volatilityPct));
                result.put("volatility_signal", getVolatilitySignal(volatilityPct));
            }
        }

        return result;
    }

    private double calculateStdDev(double[] prices, int period) {
        int startIndex = prices.length - period;

        // 計算均值
        double mean = calculateMean(prices, period);

        // 計算標準差
        double sumSquaredDiff = 0;
        for (int i = startIndex; i < prices.length; i++) {
            sumSquaredDiff += Math.pow(prices[i] - mean, 2);
        }

        return Math.sqrt(sumSquaredDiff / period);
    }

    private double calculateMean(double[] prices, int period) {
        int startIndex = prices.length - period;
        double sum = 0;
        for (int i = startIndex; i < prices.length; i++) {
            sum += prices[i];
        }
        return sum / period;
    }

    private String getVolatilitySignal(double volatilityPct) {
        if (volatilityPct >= 5) {
            return "HIGH";
        } else if (volatilityPct >= 2) {
            return "MODERATE";
        }
        return "LOW";
    }

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
