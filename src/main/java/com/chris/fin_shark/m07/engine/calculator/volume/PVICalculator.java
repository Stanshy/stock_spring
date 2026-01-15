package com.chris.fin_shark.m07.engine.calculator.volume;

import com.chris.fin_shark.m07.engine.calculator.IndicatorCalculator;
import com.chris.fin_shark.m07.engine.model.IndicatorMetadata;
import com.chris.fin_shark.m07.engine.model.PriceSeries;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * PVI（正量指標）計算器
 * <p>
 * 計算公式：
 * 當 Volume > Previous Volume 時:
 *   PVI = Previous PVI + ((Close - Previous Close) / Previous Close) * Previous PVI
 * 當 Volume <= Previous Volume 時:
 *   PVI = Previous PVI (不變)
 * </p>
 * <p>
 * 特點：追蹤散戶資金動向，假設高量日為散戶活動
 * </p>
 *
 * @author chris
 * @since 1.0.0
 */
@Component
public class PVICalculator implements IndicatorCalculator {

    @Override
    public String getName() {
        return "PVI";
    }

    @Override
    public String getCategory() {
        return "VOLUME";
    }

    @Override
    public IndicatorMetadata getMetadata() {
        return IndicatorMetadata.builder()
                .name("PVI")
                .category("VOLUME")
                .nameZh("正量指標")
                .description("追蹤高成交量日的價格變動")
                .minDataPoints(255)  // 需要計算 255 日均線
                .defaultParams(Map.of("signalPeriod", 255))
                .priority("P2")
                .build();
    }

    @Override
    public Map<String, Object> calculate(PriceSeries series, Map<String, Object> params) {
        int signalPeriod = (int) params.getOrDefault("signalPeriod", 255);

        double[] closePrices = series.getCloseArray();
        long[] volumes = series.getVolumeArray();

        Map<String, Object> result = new HashMap<>();

        if (volumes.length == 0 || closePrices.length < signalPeriod) {
            return result;
        }

        double[] pviSeries = calculatePVISeries(closePrices, volumes);
        int length = pviSeries.length;

        double pvi = pviSeries[length - 1];
        result.put("pvi", round(pvi));

        // 計算 PVI 的 EMA (信號線)
        if (length >= signalPeriod) {
            double signal = calculateEMA(pviSeries, signalPeriod);
            result.put("pvi_signal", round(signal));
            result.put("pvi_trend", pvi > signal ? "BULLISH" : "BEARISH");
        }

        return result;
    }

    private double[] calculatePVISeries(double[] prices, long[] volumes) {
        int length = prices.length;
        double[] pvi = new double[length];

        pvi[0] = 1000;  // 起始值

        for (int i = 1; i < length; i++) {
            if (volumes[i] > volumes[i - 1]) {
                // 成交量上升時更新 PVI
                double priceChange = (prices[i] - prices[i - 1]) / prices[i - 1];
                pvi[i] = pvi[i - 1] + priceChange * pvi[i - 1];
            } else {
                // 成交量不變或下降時 PVI 不變
                pvi[i] = pvi[i - 1];
            }
        }

        return pvi;
    }

    private double calculateEMA(double[] values, int period) {
        double multiplier = 2.0 / (period + 1);
        double ema = values[0];

        for (int i = 1; i < values.length; i++) {
            ema = (values[i] - ema) * multiplier + ema;
        }

        return ema;
    }

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
