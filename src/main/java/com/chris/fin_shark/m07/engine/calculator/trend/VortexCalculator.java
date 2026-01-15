package com.chris.fin_shark.m07.engine.calculator.trend;

import com.chris.fin_shark.m07.engine.calculator.IndicatorCalculator;
import com.chris.fin_shark.m07.engine.model.IndicatorMetadata;
import com.chris.fin_shark.m07.engine.model.PriceSeries;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Vortex Indicator（渦旋指標）計算器
 * <p>
 * 計算公式：
 * VM+ = |High - Previous Low|
 * VM- = |Low - Previous High|
 * TR = Max(High - Low, |High - Previous Close|, |Low - Previous Close|)
 * VI+ = Sum(VM+, n) / Sum(TR, n)
 * VI- = Sum(VM-, n) / Sum(TR, n)
 * </p>
 * <p>
 * 特點：識別趨勢方向和趨勢反轉
 * </p>
 *
 * @author chris
 * @since 1.0.0
 */
@Component
public class VortexCalculator implements IndicatorCalculator {

    @Override
    public String getName() {
        return "VORTEX";
    }

    @Override
    public String getCategory() {
        return "TREND";
    }

    @Override
    public IndicatorMetadata getMetadata() {
        return IndicatorMetadata.builder()
                .name("VORTEX")
                .category("TREND")
                .nameZh("渦旋指標")
                .description("識別趨勢方向和反轉的指標")
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
        double[] closePrices = series.getCloseArray();

        Map<String, Object> result = new HashMap<>();

        if (closePrices.length >= period + 1) {
            double[] vortex = calculateVortex(highPrices, lowPrices, closePrices, period);
            result.put("vi_plus", round(vortex[0]));
            result.put("vi_minus", round(vortex[1]));
            result.put("vortex_signal", getSignal(vortex[0], vortex[1]));
        }

        return result;
    }

    private double[] calculateVortex(double[] highs, double[] lows, double[] closes, int period) {
        int length = closes.length;

        // 計算 VM+, VM-, TR 序列
        double[] vmPlus = new double[length - 1];
        double[] vmMinus = new double[length - 1];
        double[] tr = new double[length - 1];

        for (int i = 1; i < length; i++) {
            vmPlus[i - 1] = Math.abs(highs[i] - lows[i - 1]);
            vmMinus[i - 1] = Math.abs(lows[i] - highs[i - 1]);

            double hl = highs[i] - lows[i];
            double hc = Math.abs(highs[i] - closes[i - 1]);
            double lc = Math.abs(lows[i] - closes[i - 1]);
            tr[i - 1] = Math.max(hl, Math.max(hc, lc));
        }

        // 計算 n 日總和
        int startIndex = vmPlus.length - period;
        double sumVMPlus = 0;
        double sumVMMinus = 0;
        double sumTR = 0;

        for (int i = startIndex; i < vmPlus.length; i++) {
            sumVMPlus += vmPlus[i];
            sumVMMinus += vmMinus[i];
            sumTR += tr[i];
        }

        // VI+ = Sum(VM+) / Sum(TR)
        // VI- = Sum(VM-) / Sum(TR)
        double viPlus = sumTR != 0 ? sumVMPlus / sumTR : 0;
        double viMinus = sumTR != 0 ? sumVMMinus / sumTR : 0;

        return new double[]{viPlus, viMinus};
    }

    private String getSignal(double viPlus, double viMinus) {
        if (viPlus > viMinus) {
            return "BULLISH";
        } else if (viMinus > viPlus) {
            return "BEARISH";
        }
        return "NEUTRAL";
    }

    private double round(double value) {
        return Math.round(value * 1000.0) / 1000.0;  // 3 位小數
    }
}
