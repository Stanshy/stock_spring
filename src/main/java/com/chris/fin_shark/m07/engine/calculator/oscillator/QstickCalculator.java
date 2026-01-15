package com.chris.fin_shark.m07.engine.calculator.oscillator;

import com.chris.fin_shark.m07.engine.calculator.IndicatorCalculator;
import com.chris.fin_shark.m07.engine.model.IndicatorMetadata;
import com.chris.fin_shark.m07.engine.model.PriceSeries;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Qstick 計算器
 * <p>
 * 計算公式：
 * Qstick = SMA(Close - Open, period)
 * </p>
 * <p>
 * 特點：衡量買賣壓力，正值表示買壓較強，負值表示賣壓較強
 * </p>
 *
 * @author chris
 * @since 1.0.0
 */
@Component
public class QstickCalculator implements IndicatorCalculator {

    @Override
    public String getName() {
        return "QSTICK";
    }

    @Override
    public String getCategory() {
        return "OSCILLATOR";
    }

    @Override
    public IndicatorMetadata getMetadata() {
        return IndicatorMetadata.builder()
                .name("QSTICK")
                .category("OSCILLATOR")
                .nameZh("Qstick 指標")
                .description("衡量買賣壓力的震盪指標")
                .minDataPoints(10)
                .defaultParams(Map.of("period", 8))
                .priority("P2")
                .build();
    }

    @Override
    public Map<String, Object> calculate(PriceSeries series, Map<String, Object> params) {
        int period = (int) params.getOrDefault("period", 8);

        double[] openPrices = series.getOpenArray();
        double[] closePrices = series.getCloseArray();

        Map<String, Object> result = new HashMap<>();

        if (closePrices.length >= period) {
            double qstick = calculateQstick(openPrices, closePrices, period);
            result.put("qstick", round(qstick));
            result.put("qstick_signal", getSignal(qstick));
        }

        return result;
    }

    private double calculateQstick(double[] opens, double[] closes, int period) {
        int startIndex = closes.length - period;

        double sum = 0;
        for (int i = startIndex; i < closes.length; i++) {
            sum += closes[i] - opens[i];
        }

        return sum / period;
    }

    private String getSignal(double qstick) {
        if (qstick > 0) {
            return "BULLISH";
        } else if (qstick < 0) {
            return "BEARISH";
        }
        return "NEUTRAL";
    }

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
