package com.chris.fin_shark.m07.engine.calculator.trend;

import com.chris.fin_shark.m07.engine.calculator.IndicatorCalculator;
import com.chris.fin_shark.m07.engine.model.IndicatorMetadata;
import com.chris.fin_shark.m07.engine.model.PriceSeries;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * VWMA（成交量加權移動平均）計算器
 * <p>
 * 計算公式：
 * VWMA = Σ(Price * Volume) / Σ(Volume)
 * </p>
 * <p>
 * 特點：成交量大的日子對均線影響較大
 * </p>
 *
 * @author chris
 * @since 1.0.0
 */
@Component
public class VWMACalculator implements IndicatorCalculator {

    @Override
    public String getName() {
        return "VWMA";
    }

    @Override
    public String getCategory() {
        return "TREND";
    }

    @Override
    public IndicatorMetadata getMetadata() {
        return IndicatorMetadata.builder()
                .name("VWMA")
                .category("TREND")
                .nameZh("成交量加權移動平均")
                .description("以成交量為權重的移動平均線")
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
        long[] volumes = series.getVolumeArray();

        if (volumes.length == 0) {
            return Map.of();  // 無成交量資料
        }

        Map<String, Object> result = new HashMap<>();

        for (Integer period : periods) {
            if (closePrices.length >= period && volumes.length >= period) {
                double vwma = calculateVWMA(closePrices, volumes, period);
                result.put("vwma_" + period, round(vwma));
            }
        }

        return result;
    }

    private double calculateVWMA(double[] prices, long[] volumes, int period) {
        int startIndex = prices.length - period;
        double sumPriceVolume = 0;
        long sumVolume = 0;

        for (int i = startIndex; i < prices.length; i++) {
            sumPriceVolume += prices[i] * volumes[i];
            sumVolume += volumes[i];
        }

        if (sumVolume == 0) {
            // 若成交量為 0，退化為簡單平均
            double sumPrice = 0;
            for (int i = startIndex; i < prices.length; i++) {
                sumPrice += prices[i];
            }
            return sumPrice / period;
        }

        return sumPriceVolume / sumVolume;
    }

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
