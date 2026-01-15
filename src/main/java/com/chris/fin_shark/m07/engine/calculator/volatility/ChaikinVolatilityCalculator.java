package com.chris.fin_shark.m07.engine.calculator.volatility;

import com.chris.fin_shark.m07.engine.calculator.IndicatorCalculator;
import com.chris.fin_shark.m07.engine.model.IndicatorMetadata;
import com.chris.fin_shark.m07.engine.model.PriceSeries;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Chaikin Volatility（蔡金波動率）計算器
 * <p>
 * 計算公式：
 * HL = High - Low
 * EMA_HL = EMA(HL, period)
 * Chaikin Volatility = ((EMA_HL - EMA_HL[n periods ago]) / EMA_HL[n periods ago]) * 100
 * </p>
 * <p>
 * 特點：衡量高低價差的變化率
 * </p>
 * <p>
 * TODO: ROC 的回顧期通常與 EMA 期數相同，但可獨立設定
 * </p>
 *
 * @author chris
 * @since 1.0.0
 */
@Component
public class ChaikinVolatilityCalculator implements IndicatorCalculator {

    @Override
    public String getName() {
        return "CHAIKIN_VOL";
    }

    @Override
    public String getCategory() {
        return "VOLATILITY";
    }

    @Override
    public IndicatorMetadata getMetadata() {
        return IndicatorMetadata.builder()
                .name("CHAIKIN_VOL")
                .category("VOLATILITY")
                .nameZh("蔡金波動率")
                .description("衡量高低價差變化的波動指標")
                .minDataPoints(30)
                .defaultParams(Map.of("emaPeriod", 10, "rocPeriod", 10))
                .priority("P2")
                .build();
    }

    @Override
    public Map<String, Object> calculate(PriceSeries series, Map<String, Object> params) {
        int emaPeriod = (int) params.getOrDefault("emaPeriod", 10);
        int rocPeriod = (int) params.getOrDefault("rocPeriod", 10);

        double[] highPrices = series.getHighArray();
        double[] lowPrices = series.getLowArray();

        Map<String, Object> result = new HashMap<>();

        if (highPrices.length >= emaPeriod + rocPeriod) {
            double chaikinVol = calculateChaikinVolatility(highPrices, lowPrices, emaPeriod, rocPeriod);
            result.put("chaikin_volatility", round(chaikinVol));
            result.put("chaikin_vol_signal", getSignal(chaikinVol));
        }

        return result;
    }

    private double calculateChaikinVolatility(double[] highs, double[] lows, int emaPeriod, int rocPeriod) {
        int length = highs.length;

        // 計算 High - Low 序列
        double[] hl = new double[length];
        for (int i = 0; i < length; i++) {
            hl[i] = highs[i] - lows[i];
        }

        // 計算 EMA of HL
        double[] emaHL = calculateEMASeries(hl, emaPeriod);

        // 計算 ROC of EMA
        int currentIndex = length - 1;
        int pastIndex = currentIndex - rocPeriod;

        if (emaHL[pastIndex] != 0) {
            return ((emaHL[currentIndex] - emaHL[pastIndex]) / emaHL[pastIndex]) * 100;
        }

        return 0;
    }

    private double[] calculateEMASeries(double[] values, int period) {
        double[] ema = new double[values.length];
        double multiplier = 2.0 / (period + 1);

        ema[0] = values[0];
        for (int i = 1; i < values.length; i++) {
            ema[i] = (values[i] - ema[i - 1]) * multiplier + ema[i - 1];
        }

        return ema;
    }

    private String getSignal(double chaikinVol) {
        if (chaikinVol > 25) {
            return "EXPANDING";  // 波動擴大
        } else if (chaikinVol < -25) {
            return "CONTRACTING";  // 波動收縮
        }
        return "STABLE";
    }

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
