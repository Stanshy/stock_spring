package com.chris.fin_shark.m07.engine.calculator.statistics;

import com.chris.fin_shark.m07.engine.calculator.IndicatorCalculator;
import com.chris.fin_shark.m07.engine.model.IndicatorMetadata;
import com.chris.fin_shark.m07.engine.model.PriceSeries;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Z-Score 計算器
 * <p>
 * 計算公式：
 * Z-Score = (Price - Mean) / Standard Deviation
 * </p>
 * <p>
 * 特點：衡量價格偏離均值的程度，用於識別極端值
 * </p>
 *
 * @author chris
 * @since 1.0.0
 */
@Component
public class ZScoreCalculator implements IndicatorCalculator {

    @Override
    public String getName() {
        return "Z_SCORE";
    }

    @Override
    public String getCategory() {
        return "STATISTICS";
    }

    @Override
    public IndicatorMetadata getMetadata() {
        return IndicatorMetadata.builder()
                .name("Z_SCORE")
                .category("STATISTICS")
                .nameZh("Z分數")
                .description("衡量價格偏離均值的標準差數量")
                .minDataPoints(20)
                .defaultParams(Map.of("period", 20))
                .priority("P2")
                .build();
    }

    @Override
    public Map<String, Object> calculate(PriceSeries series, Map<String, Object> params) {
        int period = (int) params.getOrDefault("period", 20);
        double[] closePrices = series.getCloseArray();

        Map<String, Object> result = new HashMap<>();

        if (closePrices.length >= period) {
            double zScore = calculateZScore(closePrices, period);
            result.put("z_score", round(zScore));
            result.put("z_score_signal", getSignal(zScore));
        }

        return result;
    }

    private double calculateZScore(double[] prices, int period) {
        int startIndex = prices.length - period;

        // 計算均值
        double sum = 0;
        for (int i = startIndex; i < prices.length; i++) {
            sum += prices[i];
        }
        double mean = sum / period;

        // 計算標準差
        double sumSquaredDiff = 0;
        for (int i = startIndex; i < prices.length; i++) {
            sumSquaredDiff += Math.pow(prices[i] - mean, 2);
        }
        double stdDev = Math.sqrt(sumSquaredDiff / period);

        // 避免除以零
        if (stdDev == 0) {
            return 0;
        }

        // Z-Score = (當前價格 - 均值) / 標準差
        return (prices[prices.length - 1] - mean) / stdDev;
    }

    private String getSignal(double zScore) {
        if (zScore >= 2) {
            return "EXTREMELY_OVERBOUGHT";
        } else if (zScore >= 1) {
            return "OVERBOUGHT";
        } else if (zScore <= -2) {
            return "EXTREMELY_OVERSOLD";
        } else if (zScore <= -1) {
            return "OVERSOLD";
        }
        return "NEUTRAL";
    }

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
