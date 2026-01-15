package com.chris.fin_shark.m07.engine.calculator.volume;

import com.chris.fin_shark.m07.engine.calculator.IndicatorCalculator;
import com.chris.fin_shark.m07.engine.model.IndicatorMetadata;
import com.chris.fin_shark.m07.engine.model.PriceSeries;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * NVI（負量指標）計算器
 * <p>
 * 計算公式：
 * 當 Volume < Previous Volume 時:
 *   NVI = Previous NVI + ((Close - Previous Close) / Previous Close) * Previous NVI
 * 當 Volume >= Previous Volume 時:
 *   NVI = Previous NVI (不變)
 * </p>
 * <p>
 * 特點：追蹤聰明錢（機構）的動向，假設低量日為機構活動
 * </p>
 *
 * @author chris
 * @since 1.0.0
 */
@Component
public class NVICalculator implements IndicatorCalculator {

    @Override
    public String getName() {
        return "NVI";
    }

    @Override
    public String getCategory() {
        return "VOLUME";
    }

    @Override
    public IndicatorMetadata getMetadata() {
        return IndicatorMetadata.builder()
                .name("NVI")
                .category("VOLUME")
                .nameZh("負量指標")
                .description("追蹤低成交量日的價格變動")
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

        double[] nviSeries = calculateNVISeries(closePrices, volumes);
        int length = nviSeries.length;

        double nvi = nviSeries[length - 1];
        result.put("nvi", round(nvi));

        // 計算 NVI 的 EMA (信號線)
        if (length >= signalPeriod) {
            double signal = calculateEMA(nviSeries, signalPeriod);
            result.put("nvi_signal", round(signal));
            result.put("nvi_trend", nvi > signal ? "BULLISH" : "BEARISH");
        }

        return result;
    }

    private double[] calculateNVISeries(double[] prices, long[] volumes) {
        int length = prices.length;
        double[] nvi = new double[length];

        nvi[0] = 1000;  // 起始值

        for (int i = 1; i < length; i++) {
            if (volumes[i] < volumes[i - 1]) {
                // 成交量下降時更新 NVI
                double priceChange = (prices[i] - prices[i - 1]) / prices[i - 1];
                nvi[i] = nvi[i - 1] + priceChange * nvi[i - 1];
            } else {
                // 成交量不變或上升時 NVI 不變
                nvi[i] = nvi[i - 1];
            }
        }

        return nvi;
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
