package com.chris.fin_shark.m07.engine.calculator.momentum;

import com.chris.fin_shark.m07.engine.calculator.IndicatorCalculator;
import com.chris.fin_shark.m07.engine.model.IndicatorMetadata;
import com.chris.fin_shark.m07.engine.model.PriceSeries;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * CMO（錢德動量震盪指標）計算器
 * <p>
 * 計算公式：
 * CMO = ((Su - Sd) / (Su + Sd)) * 100
 * Su = n 日內上漲幅度總和
 * Sd = n 日內下跌幅度總和（絕對值）
 * </p>
 * <p>
 * 特點：範圍 -100 到 +100，類似 RSI 但不使用平滑
 * </p>
 *
 * @author chris
 * @since 1.0.0
 */
@Component
public class CMOCalculator implements IndicatorCalculator {

    @Override
    public String getName() {
        return "CMO";
    }

    @Override
    public String getCategory() {
        return "MOMENTUM";
    }

    @Override
    public IndicatorMetadata getMetadata() {
        return IndicatorMetadata.builder()
                .name("CMO")
                .category("MOMENTUM")
                .nameZh("錢德動量震盪指標")
                .description("衡量價格動能的震盪指標")
                .minDataPoints(20)
                .defaultParams(Map.of("period", 14))
                .priority("P2")
                .build();
    }

    @Override
    public Map<String, Object> calculate(PriceSeries series, Map<String, Object> params) {
        int period = (int) params.getOrDefault("period", 14);
        double[] closePrices = series.getCloseArray();

        Map<String, Object> result = new HashMap<>();

        if (closePrices.length >= period + 1) {
            double cmo = calculateCMO(closePrices, period);
            result.put("cmo", round(cmo));
            result.put("cmo_signal", getSignal(cmo));
        }

        return result;
    }

    private double calculateCMO(double[] prices, int period) {
        int startIndex = prices.length - period - 1;
        double sumUp = 0;
        double sumDown = 0;

        for (int i = startIndex + 1; i < prices.length; i++) {
            double change = prices[i] - prices[i - 1];
            if (change > 0) {
                sumUp += change;
            } else {
                sumDown += Math.abs(change);
            }
        }

        double total = sumUp + sumDown;
        if (total == 0) {
            return 0;
        }

        return ((sumUp - sumDown) / total) * 100;
    }

    private String getSignal(double cmo) {
        if (cmo >= 50) {
            return "OVERBOUGHT";
        } else if (cmo <= -50) {
            return "OVERSOLD";
        }
        return "NEUTRAL";
    }

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
