package com.chris.fin_shark.m07.engine.calculator.volume;

import com.chris.fin_shark.m07.engine.calculator.IndicatorCalculator;
import com.chris.fin_shark.m07.engine.model.IndicatorMetadata;
import com.chris.fin_shark.m07.engine.model.PriceSeries;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * PVT（價量趨勢指標）計算器
 * <p>
 * 計算公式：
 * PVT = Previous PVT + ((Close - Previous Close) / Previous Close) * Volume
 * </p>
 * <p>
 * 特點：類似 OBV 但考慮價格變動百分比，更精確反映資金流向
 * </p>
 *
 * @author chris
 * @since 1.0.0
 */
@Component
public class PVTCalculator implements IndicatorCalculator {

    @Override
    public String getName() {
        return "PVT";
    }

    @Override
    public String getCategory() {
        return "VOLUME";
    }

    @Override
    public IndicatorMetadata getMetadata() {
        return IndicatorMetadata.builder()
                .name("PVT")
                .category("VOLUME")
                .nameZh("價量趨勢指標")
                .description("結合價格變動百分比與成交量的累積指標")
                .minDataPoints(20)
                .defaultParams(Map.of())
                .priority("P2")
                .build();
    }

    @Override
    public Map<String, Object> calculate(PriceSeries series, Map<String, Object> params) {
        double[] closePrices = series.getCloseArray();
        long[] volumes = series.getVolumeArray();

        Map<String, Object> result = new HashMap<>();

        if (volumes.length == 0 || closePrices.length < 2) {
            return result;
        }

        double[] pvtSeries = calculatePVTSeries(closePrices, volumes);
        int length = pvtSeries.length;

        result.put("pvt", round(pvtSeries[length - 1]));

        // 計算 PVT 趨勢（比較 5 日前）
        if (length > 5) {
            double pvtChange = pvtSeries[length - 1] - pvtSeries[length - 6];
            result.put("pvt_trend", pvtChange > 0 ? "UP" : "DOWN");
        }

        return result;
    }

    private double[] calculatePVTSeries(double[] prices, long[] volumes) {
        int length = prices.length;
        double[] pvt = new double[length];

        pvt[0] = 0;  // 初始值

        for (int i = 1; i < length; i++) {
            if (prices[i - 1] != 0) {
                double priceChange = (prices[i] - prices[i - 1]) / prices[i - 1];
                pvt[i] = pvt[i - 1] + priceChange * volumes[i];
            } else {
                pvt[i] = pvt[i - 1];
            }
        }

        return pvt;
    }

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
