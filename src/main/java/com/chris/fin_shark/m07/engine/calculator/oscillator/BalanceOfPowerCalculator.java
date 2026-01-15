package com.chris.fin_shark.m07.engine.calculator.oscillator;

import com.chris.fin_shark.m07.engine.calculator.IndicatorCalculator;
import com.chris.fin_shark.m07.engine.model.IndicatorMetadata;
import com.chris.fin_shark.m07.engine.model.PriceSeries;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Balance of Power（力量平衡指標）計算器
 * <p>
 * 計算公式：
 * BOP = (Close - Open) / (High - Low)
 * Smoothed BOP = SMA(BOP, period)
 * </p>
 * <p>
 * 特點：衡量買方與賣方的相對力量，範圍在 -1 到 +1 之間
 * </p>
 *
 * @author chris
 * @since 1.0.0
 */
@Component
public class BalanceOfPowerCalculator implements IndicatorCalculator {

    @Override
    public String getName() {
        return "BOP";
    }

    @Override
    public String getCategory() {
        return "OSCILLATOR";
    }

    @Override
    public IndicatorMetadata getMetadata() {
        return IndicatorMetadata.builder()
                .name("BOP")
                .category("OSCILLATOR")
                .nameZh("力量平衡指標")
                .description("衡量買方與賣方的相對力量")
                .minDataPoints(14)
                .defaultParams(Map.of("period", 14))
                .priority("P2")
                .build();
    }

    @Override
    public Map<String, Object> calculate(PriceSeries series, Map<String, Object> params) {
        int period = (int) params.getOrDefault("period", 14);

        double[] openPrices = series.getOpenArray();
        double[] highPrices = series.getHighArray();
        double[] lowPrices = series.getLowArray();
        double[] closePrices = series.getCloseArray();

        Map<String, Object> result = new HashMap<>();

        if (closePrices.length >= period) {
            double smoothedBop = calculateBOP(openPrices, highPrices, lowPrices, closePrices, period);
            result.put("bop", round(smoothedBop));
            result.put("bop_signal", getSignal(smoothedBop));
        }

        return result;
    }

    private double calculateBOP(double[] opens, double[] highs, double[] lows, double[] closes, int period) {
        int length = closes.length;

        // 計算 BOP 序列
        double[] bop = new double[length];
        for (int i = 0; i < length; i++) {
            double range = highs[i] - lows[i];
            if (range == 0) {
                bop[i] = 0;
            } else {
                bop[i] = (closes[i] - opens[i]) / range;
            }
        }

        // 計算 BOP 的 SMA
        int startIndex = length - period;
        double sum = 0;
        for (int i = startIndex; i < length; i++) {
            sum += bop[i];
        }

        return sum / period;
    }

    private String getSignal(double bop) {
        if (bop > 0.3) {
            return "STRONG_BULLISH";
        } else if (bop > 0) {
            return "BULLISH";
        } else if (bop < -0.3) {
            return "STRONG_BEARISH";
        } else if (bop < 0) {
            return "BEARISH";
        }
        return "NEUTRAL";
    }

    private double round(double value) {
        return Math.round(value * 1000.0) / 1000.0;  // 3 位小數
    }
}
