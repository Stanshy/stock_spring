package com.chris.fin_shark.m07.engine.calculator.momentum;

import com.chris.fin_shark.m07.engine.calculator.IndicatorCalculator;
import com.chris.fin_shark.m07.engine.model.IndicatorMetadata;
import com.chris.fin_shark.m07.engine.model.PriceSeries;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Momentum（動量指標）計算器
 * <p>
 * 計算公式：
 * Momentum = 今日收盤價 - N日前收盤價
 * </p>
 * <p>
 * 解讀：
 * - Momentum > 0：價格在上漲
 * - Momentum < 0：價格在下跌
 * - Momentum 穿越零線可作為買賣信號
 * </p>
 *
 * @author chris
 * @since 1.0.0
 */
@Component
public class MomentumCalculator implements IndicatorCalculator {

    @Override
    public String getName() {
        return "MOM";
    }

    @Override
    public String getCategory() {
        return "MOMENTUM";
    }

    @Override
    public IndicatorMetadata getMetadata() {
        return IndicatorMetadata.builder()
                .name("MOM")
                .category("MOMENTUM")
                .nameZh("動量指標")
                .description("衡量價格變動絕對值的動能指標")
                .minDataPoints(11)
                .defaultParams(Map.of("period", 10))
                .priority("P1")
                .build();
    }

    @Override
    public Map<String, Object> calculate(PriceSeries series, Map<String, Object> params) {
        int period = (int) params.getOrDefault("period", 10);

        double[] closePrices = series.getCloseArray();

        if (closePrices.length < period + 1) {
            return Map.of();
        }

        // 計算 Momentum
        int lastIndex = closePrices.length - 1;
        double currentPrice = closePrices[lastIndex];
        double pastPrice = closePrices[lastIndex - period];

        double momentum = currentPrice - pastPrice;

        // 計算前一日的 Momentum（用於判斷零線穿越）
        double prevMomentum = closePrices[lastIndex - 1] - closePrices[lastIndex - period - 1];

        Map<String, Object> result = new HashMap<>();
        result.put("mom_" + period, round(momentum));
        result.put("mom_signal", getSignal(momentum, prevMomentum));

        return result;
    }

    private String getSignal(double momentum, double prevMomentum) {
        // 零線穿越信號
        if (momentum > 0 && prevMomentum <= 0) {
            return "BUY";  // 向上穿越零線
        } else if (momentum < 0 && prevMomentum >= 0) {
            return "SELL";  // 向下穿越零線
        } else if (momentum > 0) {
            return "BULLISH";
        } else if (momentum < 0) {
            return "BEARISH";
        } else {
            return "NEUTRAL";
        }
    }

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
