package com.chris.fin_shark.m07.engine.calculator.trend;

import com.chris.fin_shark.m07.engine.calculator.IndicatorCalculator;
import com.chris.fin_shark.m07.engine.model.IndicatorMetadata;
import com.chris.fin_shark.m07.engine.model.PriceSeries;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * HMA（Hull 移動平均）計算器
 * <p>
 * 計算公式：
 * 1. 計算 WMA(n/2)
 * 2. 計算 WMA(n)
 * 3. 計算 rawHMA = 2 * WMA(n/2) - WMA(n)
 * 4. 對 rawHMA 再做 WMA(sqrt(n))
 * </p>
 * <p>
 * TODO: [待確認] HMA 的實現方式有多種變體：
 * - 有些版本對整個序列計算 rawHMA 再做 WMA
 * - 有些版本只計算最新值
 * - sqrt(n) 是否取整數也有不同做法
 * 目前採用業界常見的最新值計算方式
 * </p>
 *
 * @author chris
 * @since 1.0.0
 */
@Component
public class HMACalculator implements IndicatorCalculator {

    private final WMACalculator wmaCalculator;

    public HMACalculator(WMACalculator wmaCalculator) {
        this.wmaCalculator = wmaCalculator;
    }

    @Override
    public String getName() {
        return "HMA";
    }

    @Override
    public String getCategory() {
        return "TREND";
    }

    @Override
    public IndicatorMetadata getMetadata() {
        return IndicatorMetadata.builder()
                .name("HMA")
                .category("TREND")
                .nameZh("Hull 移動平均")
                .description("減少滯後性的平滑移動平均線")
                .minDataPoints(16)
                .defaultParams(Map.of("periods", List.of(9, 16)))
                .priority("P1")
                .build();
    }

    @Override
    public Map<String, Object> calculate(PriceSeries series, Map<String, Object> params) {
        @SuppressWarnings("unchecked")
        List<Integer> periods = (List<Integer>) params.getOrDefault("periods", List.of(9, 16));

        double[] closePrices = series.getCloseArray();
        Map<String, Object> result = new HashMap<>();

        for (Integer period : periods) {
            if (closePrices.length >= period) {
                Double hma = calculateHMA(closePrices, period);
                if (hma != null) {
                    result.put("hma_" + period, round(hma));
                }
            }
        }

        return result;
    }

    /**
     * 計算 HMA
     * <p>
     * TODO: [待確認] 目前的實現是簡化版，只計算最新的 HMA 值
     * 完整版需要對整個序列計算 rawHMA 再做 WMA
     * </p>
     *
     * @param prices 價格陣列
     * @param period 週期
     * @return HMA 值，資料不足時返回 null
     */
    private Double calculateHMA(double[] prices, int period) {
        int halfPeriod = period / 2;
        int sqrtPeriod = (int) Math.round(Math.sqrt(period));

        // 需要足夠的資料來計算 rawHMA 序列，再對其做 WMA
        int requiredLength = period + sqrtPeriod - 1;
        if (prices.length < requiredLength) {
            return null;
        }

        // 計算 rawHMA 序列（需要 sqrtPeriod 個點來做最後的 WMA）
        double[] rawHMA = new double[sqrtPeriod];
        for (int i = 0; i < sqrtPeriod; i++) {
            int endIndex = prices.length - sqrtPeriod + i + 1;
            double[] subPrices = new double[endIndex];
            System.arraycopy(prices, 0, subPrices, 0, endIndex);

            double wmaHalf = wmaCalculator.calculateWMA(subPrices, halfPeriod);
            double wmaFull = wmaCalculator.calculateWMA(subPrices, period);
            rawHMA[i] = 2 * wmaHalf - wmaFull;
        }

        // 對 rawHMA 做 WMA(sqrtPeriod)
        return calculateWMAFromArray(rawHMA, sqrtPeriod);
    }

    private double calculateWMAFromArray(double[] values, int period) {
        double weightedSum = 0;
        double weightSum = 0;

        for (int i = 0; i < period; i++) {
            int weight = i + 1;
            weightedSum += values[i] * weight;
            weightSum += weight;
        }

        return weightedSum / weightSum;
    }

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
