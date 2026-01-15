package com.chris.fin_shark.m07.engine.calculator.momentum;

import com.chris.fin_shark.m07.engine.calculator.IndicatorCalculator;
import com.chris.fin_shark.m07.engine.model.IndicatorMetadata;
import com.chris.fin_shark.m07.engine.model.PriceSeries;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Schaff Trend Cycle（STC）計算器
 * <p>
 * 計算公式：
 * 1. 計算 MACD Line
 * 2. 對 MACD Line 做 Stochastic
 * 3. 對結果再做一次 Stochastic (雙重平滑)
 * </p>
 * <p>
 * TODO: STC 的實現版本眾多，參數和平滑方式可能有差異
 * - 有些版本使用 EMA 平滑，有些使用 SMA
 * - Cycle 參數的選擇影響敏感度
 * </p>
 *
 * @author chris
 * @since 1.0.0
 */
@Component
public class SchaffTrendCycleCalculator implements IndicatorCalculator {

    @Override
    public String getName() {
        return "STC";
    }

    @Override
    public String getCategory() {
        return "MOMENTUM";
    }

    @Override
    public IndicatorMetadata getMetadata() {
        return IndicatorMetadata.builder()
                .name("STC")
                .category("MOMENTUM")
                .nameZh("Schaff 趨勢週期")
                .description("結合 MACD 和 Stochastic 的動量指標")
                .minDataPoints(50)
                .defaultParams(Map.of("fastPeriod", 23, "slowPeriod", 50, "cyclePeriod", 10, "smoothFactor", 0.5))
                .priority("P2")
                .build();
    }

    @Override
    public Map<String, Object> calculate(PriceSeries series, Map<String, Object> params) {
        int fastPeriod = (int) params.getOrDefault("fastPeriod", 23);
        int slowPeriod = (int) params.getOrDefault("slowPeriod", 50);
        int cyclePeriod = (int) params.getOrDefault("cyclePeriod", 10);
        double smoothFactor = (double) params.getOrDefault("smoothFactor", 0.5);

        double[] closePrices = series.getCloseArray();
        Map<String, Object> result = new HashMap<>();

        if (closePrices.length >= slowPeriod + cyclePeriod * 2) {
            double stc = calculateSTC(closePrices, fastPeriod, slowPeriod, cyclePeriod, smoothFactor);
            result.put("stc", round(stc));
            result.put("stc_signal", getSignal(stc));
        }

        return result;
    }

    private double calculateSTC(double[] prices, int fastPeriod, int slowPeriod, int cyclePeriod, double smoothFactor) {
        int length = prices.length;

        // 計算 MACD 線序列
        double[] fastEma = calculateEMASeries(prices, fastPeriod);
        double[] slowEma = calculateEMASeries(prices, slowPeriod);

        double[] macdLine = new double[length];
        for (int i = 0; i < length; i++) {
            macdLine[i] = fastEma[i] - slowEma[i];
        }

        // 第一次 Stochastic
        double[] stoch1 = calculateStochasticSeries(macdLine, cyclePeriod, smoothFactor);

        // 第二次 Stochastic
        double[] stc = calculateStochasticSeries(stoch1, cyclePeriod, smoothFactor);

        return stc[stc.length - 1];
    }

    private double[] calculateEMASeries(double[] prices, int period) {
        double[] ema = new double[prices.length];
        double multiplier = 2.0 / (period + 1);

        ema[0] = prices[0];
        for (int i = 1; i < prices.length; i++) {
            ema[i] = (prices[i] - ema[i - 1]) * multiplier + ema[i - 1];
        }

        return ema;
    }

    /**
     * TODO: Stochastic 的平滑方式可能有不同實現
     */
    private double[] calculateStochasticSeries(double[] values, int period, double smoothFactor) {
        int length = values.length;
        double[] result = new double[length];
        double smoothedValue = 0;

        for (int i = 0; i < length; i++) {
            if (i < period - 1) {
                result[i] = 0;
                continue;
            }

            // 找出期間內的最高和最低值
            double highest = Double.MIN_VALUE;
            double lowest = Double.MAX_VALUE;

            for (int j = i - period + 1; j <= i; j++) {
                highest = Math.max(highest, values[j]);
                lowest = Math.min(lowest, values[j]);
            }

            // 計算 %K
            double range = highest - lowest;
            double stochK;
            if (range == 0) {
                stochK = smoothedValue;  // 保持前值
            } else {
                stochK = ((values[i] - lowest) / range) * 100;
            }

            // 平滑處理
            smoothedValue = smoothedValue + smoothFactor * (stochK - smoothedValue);
            result[i] = smoothedValue;
        }

        return result;
    }

    private String getSignal(double stc) {
        if (stc >= 75) {
            return "OVERBOUGHT";
        } else if (stc <= 25) {
            return "OVERSOLD";
        }
        return "NEUTRAL";
    }

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
