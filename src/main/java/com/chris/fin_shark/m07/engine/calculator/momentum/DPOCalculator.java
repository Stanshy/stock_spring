package com.chris.fin_shark.m07.engine.calculator.momentum;

import com.chris.fin_shark.m07.engine.calculator.IndicatorCalculator;
import com.chris.fin_shark.m07.engine.model.IndicatorMetadata;
import com.chris.fin_shark.m07.engine.model.PriceSeries;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * DPO（去趨勢價格震盪指標）計算器
 * <p>
 * 計算公式：
 * DPO = Price - SMA(n/2 + 1 日前)
 * </p>
 * <p>
 * 特點：消除長期趨勢，專注於識別價格週期
 * </p>
 *
 * @author chris
 * @since 1.0.0
 */
@Component
public class DPOCalculator implements IndicatorCalculator {

    @Override
    public String getName() {
        return "DPO";
    }

    @Override
    public String getCategory() {
        return "MOMENTUM";
    }

    @Override
    public IndicatorMetadata getMetadata() {
        return IndicatorMetadata.builder()
                .name("DPO")
                .category("MOMENTUM")
                .nameZh("去趨勢價格震盪指標")
                .description("消除長期趨勢以識別價格週期")
                .minDataPoints(30)
                .defaultParams(Map.of("period", 20))
                .priority("P2")
                .build();
    }

    @Override
    public Map<String, Object> calculate(PriceSeries series, Map<String, Object> params) {
        int period = (int) params.getOrDefault("period", 20);
        double[] closePrices = series.getCloseArray();

        Map<String, Object> result = new HashMap<>();

        int displacement = period / 2 + 1;
        if (closePrices.length >= period + displacement) {
            double dpo = calculateDPO(closePrices, period);
            result.put("dpo", round(dpo));
            result.put("dpo_signal", dpo > 0 ? "BULLISH" : "BEARISH");
        }

        return result;
    }

    private double calculateDPO(double[] prices, int period) {
        int displacement = period / 2 + 1;
        int length = prices.length;

        // 計算位移後的 SMA
        int smaEndIndex = length - displacement;
        double sum = 0;
        for (int i = smaEndIndex - period + 1; i <= smaEndIndex; i++) {
            sum += prices[i];
        }
        double sma = sum / period;

        // DPO = 當前價格 - 位移後的 SMA
        return prices[length - 1] - sma;
    }

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
