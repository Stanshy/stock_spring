package com.chris.fin_shark.m07.engine.calculator.momentum;

import com.chris.fin_shark.m07.engine.calculator.IndicatorCalculator;
import com.chris.fin_shark.m07.engine.model.IndicatorMetadata;
import com.chris.fin_shark.m07.engine.model.PriceSeries;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * ROC（變動率）計算器
 * <p>
 * 計算公式：
 * ROC = ((今日收盤價 - N日前收盤價) / N日前收盤價) * 100
 * </p>
 * <p>
 * 解讀：
 * - ROC > 0：價格上漲
 * - ROC < 0：價格下跌
 * - ROC 的絕對值越大，漲跌幅度越大
 * </p>
 *
 * @author chris
 * @since 1.0.0
 */
@Component
public class ROCCalculator implements IndicatorCalculator {

    @Override
    public String getName() {
        return "ROC";
    }

    @Override
    public String getCategory() {
        return "MOMENTUM";
    }

    @Override
    public IndicatorMetadata getMetadata() {
        return IndicatorMetadata.builder()
                .name("ROC")
                .category("MOMENTUM")
                .nameZh("變動率")
                .description("衡量價格變動百分比的動能指標")
                .minDataPoints(13)
                .defaultParams(Map.of("period", 12))
                .priority("P1")
                .build();
    }

    @Override
    public Map<String, Object> calculate(PriceSeries series, Map<String, Object> params) {
        int period = (int) params.getOrDefault("period", 12);

        double[] closePrices = series.getCloseArray();

        if (closePrices.length < period + 1) {
            return Map.of();
        }

        // 計算 ROC
        int lastIndex = closePrices.length - 1;
        double currentPrice = closePrices[lastIndex];
        double pastPrice = closePrices[lastIndex - period];

        if (pastPrice == 0) {
            return Map.of();  // 避免除以零
        }

        double roc = ((currentPrice - pastPrice) / pastPrice) * 100;

        Map<String, Object> result = new HashMap<>();
        result.put("roc_" + period, round(roc));
        result.put("roc_signal", getSignal(roc));

        return result;
    }

    private String getSignal(double roc) {
        if (roc > 10) {
            return "STRONG_BULLISH";
        } else if (roc > 0) {
            return "BULLISH";
        } else if (roc > -10) {
            return "BEARISH";
        } else {
            return "STRONG_BEARISH";
        }
    }

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
