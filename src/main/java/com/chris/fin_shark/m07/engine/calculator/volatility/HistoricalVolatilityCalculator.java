package com.chris.fin_shark.m07.engine.calculator.volatility;

import com.chris.fin_shark.m07.engine.calculator.IndicatorCalculator;
import com.chris.fin_shark.m07.engine.model.IndicatorMetadata;
import com.chris.fin_shark.m07.engine.model.PriceSeries;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Historical Volatility（歷史波動率）計算器
 * <p>
 * 計算公式：
 * Daily Return = ln(Close / Previous Close)
 * HV = StdDev(Daily Return, period) * sqrt(252) * 100
 * </p>
 * <p>
 * 特點：年化波動率，用於選擇權定價和風險評估
 * </p>
 *
 * @author chris
 * @since 1.0.0
 */
@Component
public class HistoricalVolatilityCalculator implements IndicatorCalculator {

    private static final int TRADING_DAYS_PER_YEAR = 252;

    @Override
    public String getName() {
        return "HV";
    }

    @Override
    public String getCategory() {
        return "VOLATILITY";
    }

    @Override
    public IndicatorMetadata getMetadata() {
        return IndicatorMetadata.builder()
                .name("HV")
                .category("VOLATILITY")
                .nameZh("歷史波動率")
                .description("年化歷史波動率")
                .minDataPoints(30)
                .defaultParams(Map.of("periods", List.of(10, 20, 30)))
                .priority("P2")
                .build();
    }

    @Override
    public Map<String, Object> calculate(PriceSeries series, Map<String, Object> params) {
        @SuppressWarnings("unchecked")
        List<Integer> periods = (List<Integer>) params.getOrDefault("periods", List.of(10, 20, 30));

        double[] closePrices = series.getCloseArray();
        Map<String, Object> result = new HashMap<>();

        for (Integer period : periods) {
            if (closePrices.length >= period + 1) {
                double hv = calculateHV(closePrices, period);
                result.put("hv_" + period, round(hv));
            }
        }

        // 波動率排名
        if (closePrices.length >= 21) {
            double hv20 = calculateHV(closePrices, 20);
            result.put("volatility_rank", getVolatilityRank(hv20));
        }

        return result;
    }

    private double calculateHV(double[] prices, int period) {
        int length = prices.length;

        // 計算對數報酬率
        double[] logReturns = new double[period];
        for (int i = 0; i < period; i++) {
            int idx = length - period + i;
            if (prices[idx - 1] > 0) {
                logReturns[i] = Math.log(prices[idx] / prices[idx - 1]);
            } else {
                logReturns[i] = 0;
            }
        }

        // 計算標準差
        double mean = 0;
        for (double ret : logReturns) {
            mean += ret;
        }
        mean /= period;

        double sumSquaredDiff = 0;
        for (double ret : logReturns) {
            sumSquaredDiff += Math.pow(ret - mean, 2);
        }
        double stdDev = Math.sqrt(sumSquaredDiff / (period - 1));  // 樣本標準差

        // 年化 (乘以 sqrt(252) 並轉為百分比)
        return stdDev * Math.sqrt(TRADING_DAYS_PER_YEAR) * 100;
    }

    private String getVolatilityRank(double hv) {
        if (hv >= 50) {
            return "VERY_HIGH";
        } else if (hv >= 30) {
            return "HIGH";
        } else if (hv >= 15) {
            return "MODERATE";
        }
        return "LOW";
    }

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
