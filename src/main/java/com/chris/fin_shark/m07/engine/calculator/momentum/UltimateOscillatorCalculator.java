package com.chris.fin_shark.m07.engine.calculator.momentum;

import com.chris.fin_shark.m07.engine.calculator.IndicatorCalculator;
import com.chris.fin_shark.m07.engine.model.IndicatorMetadata;
import com.chris.fin_shark.m07.engine.model.PriceSeries;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Ultimate Oscillator（終極震盪指標）計算器
 * <p>
 * 計算公式：
 * BP (Buying Pressure) = Close - Min(Low, Previous Close)
 * TR (True Range) = Max(High, Previous Close) - Min(Low, Previous Close)
 * Average7 = Sum(BP, 7) / Sum(TR, 7)
 * Average14 = Sum(BP, 14) / Sum(TR, 14)
 * Average28 = Sum(BP, 28) / Sum(TR, 28)
 * UO = [(Average7 * 4) + (Average14 * 2) + Average28] / 7 * 100
 * </p>
 * <p>
 * 特點：結合三個時間週期減少假訊號
 * </p>
 *
 * @author chris
 * @since 1.0.0
 */
@Component
public class UltimateOscillatorCalculator implements IndicatorCalculator {

    @Override
    public String getName() {
        return "ULTIMATE_OSC";
    }

    @Override
    public String getCategory() {
        return "MOMENTUM";
    }

    @Override
    public IndicatorMetadata getMetadata() {
        return IndicatorMetadata.builder()
                .name("ULTIMATE_OSC")
                .category("MOMENTUM")
                .nameZh("終極震盪指標")
                .description("結合三個時間週期的動量指標")
                .minDataPoints(30)
                .defaultParams(Map.of("period1", 7, "period2", 14, "period3", 28))
                .priority("P2")
                .build();
    }

    @Override
    public Map<String, Object> calculate(PriceSeries series, Map<String, Object> params) {
        int period1 = (int) params.getOrDefault("period1", 7);
        int period2 = (int) params.getOrDefault("period2", 14);
        int period3 = (int) params.getOrDefault("period3", 28);

        double[] highPrices = series.getHighArray();
        double[] lowPrices = series.getLowArray();
        double[] closePrices = series.getCloseArray();

        Map<String, Object> result = new HashMap<>();

        int minRequired = period3 + 1;
        if (closePrices.length >= minRequired) {
            double uo = calculateUO(highPrices, lowPrices, closePrices, period1, period2, period3);
            result.put("ultimate_osc", round(uo));
            result.put("ultimate_osc_signal", getSignal(uo));
        }

        return result;
    }

    private double calculateUO(double[] highs, double[] lows, double[] closes,
                                int period1, int period2, int period3) {
        int length = closes.length;

        // 計算 BP 和 TR 序列
        double[] bp = new double[length - 1];
        double[] tr = new double[length - 1];

        for (int i = 1; i < length; i++) {
            double prevClose = closes[i - 1];
            double low = lows[i];
            double high = highs[i];
            double close = closes[i];

            bp[i - 1] = close - Math.min(low, prevClose);
            tr[i - 1] = Math.max(high, prevClose) - Math.min(low, prevClose);
        }

        // 計算各週期的平均
        double avg1 = calculateAverage(bp, tr, period1);
        double avg2 = calculateAverage(bp, tr, period2);
        double avg3 = calculateAverage(bp, tr, period3);

        // UO = [(Avg1 * 4) + (Avg2 * 2) + Avg3] / 7 * 100
        return ((avg1 * 4) + (avg2 * 2) + avg3) / 7 * 100;
    }

    private double calculateAverage(double[] bp, double[] tr, int period) {
        int startIndex = bp.length - period;
        if (startIndex < 0) startIndex = 0;

        double sumBP = 0;
        double sumTR = 0;

        for (int i = startIndex; i < bp.length; i++) {
            sumBP += bp[i];
            sumTR += tr[i];
        }

        return sumTR != 0 ? sumBP / sumTR : 0;
    }

    private String getSignal(double uo) {
        if (uo >= 70) {
            return "OVERBOUGHT";
        } else if (uo <= 30) {
            return "OVERSOLD";
        }
        return "NEUTRAL";
    }

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
