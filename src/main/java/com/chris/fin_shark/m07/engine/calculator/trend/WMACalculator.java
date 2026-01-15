package com.chris.fin_shark.m07.engine.calculator.trend;

import com.chris.fin_shark.m07.engine.calculator.IndicatorCalculator;
import com.chris.fin_shark.m07.engine.model.IndicatorMetadata;
import com.chris.fin_shark.m07.engine.model.PriceSeries;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * WMA（加權移動平均）計算器
 * <p>
 * 計算公式：
 * WMA = (P1 * n + P2 * (n-1) + ... + Pn * 1) / (n + (n-1) + ... + 1)
 * 權重：越近期的價格權重越高
 * </p>
 *
 * @author chris
 * @since 1.0.0
 */
@Component
public class WMACalculator implements IndicatorCalculator {

    @Override
    public String getName() {
        return "WMA";
    }

    @Override
    public String getCategory() {
        return "TREND";
    }

    @Override
    public IndicatorMetadata getMetadata() {
        return IndicatorMetadata.builder()
                .name("WMA")
                .category("TREND")
                .nameZh("加權移動平均")
                .description("近期價格賦予較高權重的移動平均線")
                .minDataPoints(10)
                .defaultParams(Map.of("periods", List.of(10, 20)))
                .priority("P1")
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
                double wma = calculateWMA(closePrices, period);
                result.put("wma_" + period, round(wma));
            }
        }

        return result;
    }

    /**
     * 計算 WMA
     *
     * @param prices 價格陣列
     * @param period 週期
     * @return WMA 值
     */
    public double calculateWMA(double[] prices, int period) {
        if (prices.length < period) {
            throw new IllegalArgumentException(
                    String.format("資料不足：需要%d天，實際%d天", period, prices.length));
        }

        double weightedSum = 0;
        double weightSum = 0;

        // 取最近 period 天的資料
        int startIndex = prices.length - period;
        for (int i = 0; i < period; i++) {
            int weight = i + 1;  // 權重從 1 遞增到 period
            weightedSum += prices[startIndex + i] * weight;
            weightSum += weight;
        }

        return weightedSum / weightSum;
    }

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
