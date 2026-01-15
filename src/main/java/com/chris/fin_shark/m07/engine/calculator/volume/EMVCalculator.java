package com.chris.fin_shark.m07.engine.calculator.volume;

import com.chris.fin_shark.m07.engine.calculator.IndicatorCalculator;
import com.chris.fin_shark.m07.engine.model.IndicatorMetadata;
import com.chris.fin_shark.m07.engine.model.PriceSeries;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * EMV（簡易波動指標）計算器
 * <p>
 * 計算公式：
 * Distance Moved = ((High + Low) / 2) - ((Prior High + Prior Low) / 2)
 * Box Ratio = (Volume / scale) / (High - Low)
 * EMV = Distance Moved / Box Ratio
 * EMV SMA = SMA(EMV, period)
 * </p>
 * <p>
 * 特點：衡量價格變動的難易程度
 * </p>
 *
 * @author chris
 * @since 1.0.0
 */
@Component
public class EMVCalculator implements IndicatorCalculator {

    private static final double VOLUME_SCALE = 10000.0;

    @Override
    public String getName() {
        return "EMV";
    }

    @Override
    public String getCategory() {
        return "VOLUME";
    }

    @Override
    public IndicatorMetadata getMetadata() {
        return IndicatorMetadata.builder()
                .name("EMV")
                .category("VOLUME")
                .nameZh("簡易波動指標")
                .description("衡量價格變動的難易程度")
                .minDataPoints(20)
                .defaultParams(Map.of("period", 14))
                .priority("P2")
                .build();
    }

    @Override
    public Map<String, Object> calculate(PriceSeries series, Map<String, Object> params) {
        int period = (int) params.getOrDefault("period", 14);

        double[] highPrices = series.getHighArray();
        double[] lowPrices = series.getLowArray();
        long[] volumes = series.getVolumeArray();

        Map<String, Object> result = new HashMap<>();

        if (volumes.length == 0 || highPrices.length < period + 1) {
            return result;
        }

        double emvSma = calculateEMV(highPrices, lowPrices, volumes, period);
        result.put("emv", round(emvSma));
        result.put("emv_signal", emvSma > 0 ? "BULLISH" : "BEARISH");

        return result;
    }

    private double calculateEMV(double[] highs, double[] lows, long[] volumes, int period) {
        int length = highs.length;
        double[] emv = new double[length - 1];

        for (int i = 1; i < length; i++) {
            double distanceMoved = ((highs[i] + lows[i]) / 2) - ((highs[i - 1] + lows[i - 1]) / 2);

            double highLowDiff = highs[i] - lows[i];
            if (highLowDiff == 0) {
                emv[i - 1] = 0;
            } else {
                double boxRatio = (volumes[i] / VOLUME_SCALE) / highLowDiff;
                emv[i - 1] = boxRatio == 0 ? 0 : distanceMoved / boxRatio;
            }
        }

        // 計算 EMV 的 SMA
        int startIndex = emv.length - period;
        if (startIndex < 0) {
            startIndex = 0;
        }

        double sum = 0;
        int count = 0;
        for (int i = startIndex; i < emv.length; i++) {
            sum += emv[i];
            count++;
        }

        return count > 0 ? sum / count : 0;
    }

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
