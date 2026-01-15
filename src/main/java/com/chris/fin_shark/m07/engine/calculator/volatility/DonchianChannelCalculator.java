package com.chris.fin_shark.m07.engine.calculator.volatility;

import com.chris.fin_shark.m07.engine.calculator.IndicatorCalculator;
import com.chris.fin_shark.m07.engine.model.IndicatorMetadata;
import com.chris.fin_shark.m07.engine.model.PriceSeries;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Donchian Channel（唐奇安通道）計算器
 * <p>
 * 計算公式：
 * 上軌 = 最近 N 天的最高價
 * 下軌 = 最近 N 天的最低價
 * 中軌 = (上軌 + 下軌) / 2
 * </p>
 * <p>
 * 解讀：
 * - 價格突破上軌：強勢買入信號
 * - 價格跌破下軌：強勢賣出信號
 * - 通道寬度反映波動性
 * </p>
 *
 * @author chris
 * @since 1.0.0
 */
@Component
public class DonchianChannelCalculator implements IndicatorCalculator {

    @Override
    public String getName() {
        return "DONCHIAN";
    }

    @Override
    public String getCategory() {
        return "VOLATILITY";
    }

    @Override
    public IndicatorMetadata getMetadata() {
        return IndicatorMetadata.builder()
                .name("DONCHIAN")
                .category("VOLATILITY")
                .nameZh("唐奇安通道")
                .description("基於最高/最低價的突破通道")
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

        if (highPrices.length < period) {
            return Map.of();
        }

        // 找出最近 period 天的最高價和最低價
        double upperBand = Double.MIN_VALUE;
        double lowerBand = Double.MAX_VALUE;

        int startIndex = highPrices.length - period;
        for (int i = startIndex; i < highPrices.length; i++) {
            upperBand = Math.max(upperBand, highPrices[i]);
            lowerBand = Math.min(lowerBand, lowPrices[i]);
        }

        double middleBand = (upperBand + lowerBand) / 2;
        double bandwidth = upperBand - lowerBand;
        double currentClose = closePrices[closePrices.length - 1];

        // 計算價格在通道中的位置（0-100%）
        double position = 0;
        if (bandwidth > 0) {
            position = ((currentClose - lowerBand) / bandwidth) * 100;
        }

        Map<String, Object> result = new HashMap<>();
        result.put("donchian_upper_" + period, round(upperBand));
        result.put("donchian_middle_" + period, round(middleBand));
        result.put("donchian_lower_" + period, round(lowerBand));
        result.put("donchian_bandwidth_" + period, round(bandwidth));
        result.put("donchian_position_" + period, round(position));
        result.put("donchian_signal", getSignal(currentClose, upperBand, lowerBand));

        return result;
    }

    private String getSignal(double close, double upper, double lower) {
        if (close >= upper) {
            return "BREAKOUT_UP";  // 突破上軌
        } else if (close <= lower) {
            return "BREAKOUT_DOWN";  // 跌破下軌
        } else {
            return "IN_CHANNEL";  // 在通道內
        }
    }

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
