package com.chris.fin_shark.m07.engine.calculator.volatility;

import com.chris.fin_shark.m07.engine.calculator.IndicatorCalculator;
import com.chris.fin_shark.m07.engine.model.IndicatorMetadata;
import com.chris.fin_shark.m07.engine.model.PriceSeries;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * BBands（布林通道）計算器
 * <p>
 * 計算公式：
 * Middle Band = SMA(20)
 * Upper Band = SMA(20) + (2 * STD)
 * Lower Band = SMA(20) - (2 * STD)
 * </p>
 *
 * @author chris
 * @since 1.0.0
 */
@Component
public class BBandsCalculator implements IndicatorCalculator {

    @Override
    public String getName() {
        return "BBANDS";
    }

    @Override
    public String getCategory() {
        return "VOLATILITY";
    }

    @Override
    public IndicatorMetadata getMetadata() {
        return IndicatorMetadata.builder()
                .name("BBANDS")
                .category("VOLATILITY")
                .nameZh("布林通道")
                .description("價格波動範圍指標")
                .minDataPoints(20)
                .defaultParams(Map.of(
                        "period", 20,
                        "std_dev", 2.0
                ))
                .priority("P0")
                .build();
    }

    @Override
    public Map<String, Object> calculate(PriceSeries series, Map<String, Object> params) {
        // 取得參數
        int period = (int) params.getOrDefault("period", 20);
        double stdDevMultiplier = params.containsKey("std_dev")
                ? ((Number) params.get("std_dev")).doubleValue()
                : 2.0;

        // 取得收盤價
        double[] closePrices = series.getCloseArray();

        if (closePrices.length < period) {
            // 資料不足，返回空結果
            return Map.of();
        }

        // 計算 Middle Band（SMA）
        double middleBand = calculateSMA(closePrices, period);

        // 計算標準差
        double stdDev = calculateStdDev(closePrices, period, middleBand);

        // 計算 Upper/Lower Band
        double upperBand = middleBand + (stdDevMultiplier * stdDev);
        double lowerBand = middleBand - (stdDevMultiplier * stdDev);

        // 計算當前價格位置（%B）
        double currentPrice = closePrices[closePrices.length - 1];
        double percentB = (currentPrice - lowerBand) / (upperBand - lowerBand);

        // 計算頻寬（Bandwidth）
        double bandwidth = (upperBand - lowerBand) / middleBand;

        // 組裝結果
        Map<String, Object> bbandsResult = new HashMap<>();
        bbandsResult.put("upper", round(upperBand));
        bbandsResult.put("middle", round(middleBand));
        bbandsResult.put("lower", round(lowerBand));
        bbandsResult.put("percent_b", round(percentB));
        bbandsResult.put("bandwidth", round(bandwidth));
        bbandsResult.put("signal", getSignal(percentB));

        // 返回
        Map<String, Object> result = new HashMap<>();
        result.put("bbands", bbandsResult);

        return result;
    }

    /**
     * 計算 SMA
     */
    private double calculateSMA(double[] prices, int period) {
        double sum = 0;
        for (int i = prices.length - period; i < prices.length; i++) {
            sum += prices[i];
        }
        return sum / period;
    }

    /**
     * 計算標準差
     */
    private double calculateStdDev(double[] prices, int period, double mean) {
        double sumSquaredDiff = 0;
        for (int i = prices.length - period; i < prices.length; i++) {
            double diff = prices[i] - mean;
            sumSquaredDiff += diff * diff;
        }
        return Math.sqrt(sumSquaredDiff / period);
    }

    /**
     * 判斷信號
     */
    private String getSignal(double percentB) {
        if (percentB > 1.0) {
            return "ABOVE_UPPER";  // 突破上軌
        } else if (percentB < 0.0) {
            return "BELOW_LOWER";  // 跌破下軌
        } else if (percentB > 0.8) {
            return "NEAR_UPPER";   // 接近上軌
        } else if (percentB < 0.2) {
            return "NEAR_LOWER";   // 接近下軌
        } else {
            return "MIDDLE";       // 中間區域
        }
    }

    /**
     * 四捨五入
     */
    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
