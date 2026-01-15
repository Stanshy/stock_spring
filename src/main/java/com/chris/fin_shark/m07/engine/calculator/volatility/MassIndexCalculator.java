package com.chris.fin_shark.m07.engine.calculator.volatility;

import com.chris.fin_shark.m07.engine.calculator.IndicatorCalculator;
import com.chris.fin_shark.m07.engine.model.IndicatorMetadata;
import com.chris.fin_shark.m07.engine.model.PriceSeries;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Mass Index（質量指數）計算器
 * <p>
 * 計算公式：
 * Single EMA = EMA(High - Low, 9)
 * Double EMA = EMA(Single EMA, 9)
 * Mass Index = Sum(Single EMA / Double EMA, 25)
 * </p>
 * <p>
 * 信號：
 * - Reversal Bulge: Mass Index > 27 然後跌破 26.5
 * </p>
 * <p>
 * TODO: 反轉膨脹信號的閾值（27 和 26.5）可能需要根據市場調整
 * </p>
 *
 * @author chris
 * @since 1.0.0
 */
@Component
public class MassIndexCalculator implements IndicatorCalculator {

    @Override
    public String getName() {
        return "MASS_INDEX";
    }

    @Override
    public String getCategory() {
        return "VOLATILITY";
    }

    @Override
    public IndicatorMetadata getMetadata() {
        return IndicatorMetadata.builder()
                .name("MASS_INDEX")
                .category("VOLATILITY")
                .nameZh("質量指數")
                .description("識別價格反轉的波動指標")
                .minDataPoints(40)
                .defaultParams(Map.of("emaPeriod", 9, "sumPeriod", 25))
                .priority("P2")
                .build();
    }

    @Override
    public Map<String, Object> calculate(PriceSeries series, Map<String, Object> params) {
        int emaPeriod = (int) params.getOrDefault("emaPeriod", 9);
        int sumPeriod = (int) params.getOrDefault("sumPeriod", 25);

        double[] highPrices = series.getHighArray();
        double[] lowPrices = series.getLowArray();

        Map<String, Object> result = new HashMap<>();

        if (highPrices.length >= emaPeriod * 2 + sumPeriod) {
            double massIndex = calculateMassIndex(highPrices, lowPrices, emaPeriod, sumPeriod);
            result.put("mass_index", round(massIndex));
            result.put("mass_index_signal", getSignal(massIndex));
        }

        return result;
    }

    private double calculateMassIndex(double[] highs, double[] lows, int emaPeriod, int sumPeriod) {
        int length = highs.length;

        // 計算 High - Low 序列
        double[] range = new double[length];
        for (int i = 0; i < length; i++) {
            range[i] = highs[i] - lows[i];
        }

        // Single EMA
        double[] singleEma = calculateEMASeries(range, emaPeriod);

        // Double EMA (EMA of Single EMA)
        double[] doubleEma = calculateEMASeries(singleEma, emaPeriod);

        // EMA Ratio = Single EMA / Double EMA
        double[] emaRatio = new double[length];
        for (int i = 0; i < length; i++) {
            emaRatio[i] = doubleEma[i] != 0 ? singleEma[i] / doubleEma[i] : 1;
        }

        // Mass Index = Sum of EMA Ratio over sumPeriod
        double sum = 0;
        int startIndex = length - sumPeriod;
        for (int i = startIndex; i < length; i++) {
            sum += emaRatio[i];
        }

        return sum;
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

    /**
     * TODO: 反轉膨脹的閾值可能需要針對台股市場調整
     */
    private String getSignal(double massIndex) {
        if (massIndex > 27) {
            return "BULGE";  // 膨脹中，等待跌破 26.5
        } else if (massIndex > 26.5) {
            return "POTENTIAL_REVERSAL";  // 可能即將反轉
        }
        return "NORMAL";
    }

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
