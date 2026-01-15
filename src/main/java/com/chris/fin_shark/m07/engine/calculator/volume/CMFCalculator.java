package com.chris.fin_shark.m07.engine.calculator.volume;

import com.chris.fin_shark.m07.engine.calculator.IndicatorCalculator;
import com.chris.fin_shark.m07.engine.model.IndicatorMetadata;
import com.chris.fin_shark.m07.engine.model.PriceSeries;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * CMF（蔡金資金流量）計算器
 * <p>
 * 計算公式：
 * 1. MFM（Money Flow Multiplier）= ((Close - Low) - (High - Close)) / (High - Low)
 * 2. MFV（Money Flow Volume）= MFM * Volume
 * 3. CMF = Sum(MFV, N) / Sum(Volume, N)
 * </p>
 * <p>
 * 解讀：
 * - CMF 範圍：-1 到 +1
 * - CMF > 0：買盤力道強
 * - CMF < 0：賣盤力道強
 * - CMF > 0.25：強勁買盤
 * - CMF < -0.25：強勁賣盤
 * </p>
 *
 * @author chris
 * @since 1.0.0
 */
@Component
public class CMFCalculator implements IndicatorCalculator {

    @Override
    public String getName() {
        return "CMF";
    }

    @Override
    public String getCategory() {
        return "VOLUME";
    }

    @Override
    public IndicatorMetadata getMetadata() {
        return IndicatorMetadata.builder()
                .name("CMF")
                .category("VOLUME")
                .nameZh("蔡金資金流量")
                .description("衡量一段期間內資金流入流出的指標")
                .minDataPoints(20)
                .defaultParams(Map.of("period", 20))
                .priority("P1")
                .build();
    }

    @Override
    public Map<String, Object> calculate(PriceSeries series, Map<String, Object> params) {
        int period = (int) params.getOrDefault("period", 20);

        double[] highPrices = series.getHighArray();
        double[] lowPrices = series.getLowArray();
        double[] closePrices = series.getCloseArray();
        long[] volumes = series.getVolumeArray();

        if (closePrices.length < period || volumes.length < period) {
            return Map.of();
        }

        // 計算最近 period 天的 CMF
        double mfvSum = 0;
        double volumeSum = 0;

        int startIndex = closePrices.length - period;
        for (int i = startIndex; i < closePrices.length; i++) {
            double mfm = calculateMFM(highPrices[i], lowPrices[i], closePrices[i]);
            mfvSum += mfm * volumes[i];
            volumeSum += volumes[i];
        }

        double cmf = 0;
        if (volumeSum > 0) {
            cmf = mfvSum / volumeSum;
        }

        Map<String, Object> result = new HashMap<>();
        result.put("cmf_" + period, round(cmf));
        result.put("cmf_signal", getSignal(cmf));

        return result;
    }

    /**
     * 計算 MFM（Money Flow Multiplier）
     */
    private double calculateMFM(double high, double low, double close) {
        double range = high - low;
        if (range == 0) {
            return 0;
        }
        return ((close - low) - (high - close)) / range;
    }

    private String getSignal(double cmf) {
        if (cmf > 0.25) {
            return "STRONG_BUYING";
        } else if (cmf > 0.05) {
            return "BUYING";
        } else if (cmf > -0.05) {
            return "NEUTRAL";
        } else if (cmf > -0.25) {
            return "SELLING";
        } else {
            return "STRONG_SELLING";
        }
    }

    private double round(double value) {
        return Math.round(value * 10000.0) / 10000.0;  // CMF 需要更高精度
    }
}
