package com.chris.fin_shark.m07.engine.calculator.support;

import com.chris.fin_shark.m07.engine.calculator.IndicatorCalculator;
import com.chris.fin_shark.m07.engine.model.IndicatorMetadata;
import com.chris.fin_shark.m07.engine.model.PriceSeries;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Fibonacci Extension（費波那契延伸）計算器
 * <p>
 * 計算公式（上漲走勢）：
 * Extension Level = Swing Low + (Swing High - Swing Low) * Extension Ratio
 * </p>
 * <p>
 * 常用延伸比率：
 * - 100%: 1.000
 * - 127.2%: 1.272
 * - 161.8%: 1.618
 * - 200%: 2.000
 * - 261.8%: 2.618
 * </p>
 * <p>
 * TODO: 波段識別邏輯需要更複雜的實現
 * - 此版本使用簡化的期間內最高/最低點
 * - 完整實現需要波浪理論或轉折點檢測
 * </p>
 * <p>
 * TODO: 三波段費波那契延伸（A-B-C 走勢）需要額外參數
 * </p>
 *
 * @author chris
 * @since 1.0.0
 */
@Component
public class FibonacciExtensionCalculator implements IndicatorCalculator {

    private static final double[] EXTENSION_RATIOS = {1.0, 1.272, 1.618, 2.0, 2.618};

    @Override
    public String getName() {
        return "FIB_EXT";
    }

    @Override
    public String getCategory() {
        return "SUPPORT";
    }

    @Override
    public IndicatorMetadata getMetadata() {
        return IndicatorMetadata.builder()
                .name("FIB_EXT")
                .category("SUPPORT")
                .nameZh("費波那契延伸")
                .description("預測價格延伸目標的支撐阻力指標")
                .minDataPoints(50)
                .defaultParams(Map.of("lookbackPeriod", 50))
                .priority("P2")
                .build();
    }

    @Override
    public Map<String, Object> calculate(PriceSeries series, Map<String, Object> params) {
        int lookbackPeriod = (int) params.getOrDefault("lookbackPeriod", 50);

        double[] highPrices = series.getHighArray();
        double[] lowPrices = series.getLowArray();
        double[] closePrices = series.getCloseArray();

        Map<String, Object> result = new HashMap<>();

        if (closePrices.length < lookbackPeriod) {
            return result;
        }

        // 找出回顧期間的最高點和最低點
        int length = closePrices.length;
        int startIndex = length - lookbackPeriod;

        double swingHigh = Double.MIN_VALUE;
        double swingLow = Double.MAX_VALUE;
        int highIndex = startIndex;
        int lowIndex = startIndex;

        for (int i = startIndex; i < length; i++) {
            if (highPrices[i] > swingHigh) {
                swingHigh = highPrices[i];
                highIndex = i;
            }
            if (lowPrices[i] < swingLow) {
                swingLow = lowPrices[i];
                lowIndex = i;
            }
        }

        double range = swingHigh - swingLow;
        result.put("swing_high", round(swingHigh));
        result.put("swing_low", round(swingLow));

        // 判斷趨勢方向（低點在高點之前 = 上漲趨勢）
        boolean isUptrend = lowIndex < highIndex;
        result.put("trend", isUptrend ? "UP" : "DOWN");

        // 計算延伸水平
        if (isUptrend) {
            // 上漲趨勢：從低點向上延伸
            for (double ratio : EXTENSION_RATIOS) {
                double level = swingLow + range * ratio;
                result.put("ext_" + formatRatio(ratio), round(level));
            }
        } else {
            // 下跌趨勢：從高點向下延伸
            for (double ratio : EXTENSION_RATIOS) {
                double level = swingHigh - range * ratio;
                result.put("ext_" + formatRatio(ratio), round(level));
            }
        }

        // 判斷當前價格接近哪個延伸水平
        double currentPrice = closePrices[length - 1];
        result.put("nearest_level", findNearestLevel(currentPrice, swingLow, range, isUptrend));

        return result;
    }

    private String formatRatio(double ratio) {
        return String.format("%.1f", ratio * 100).replace(".", "_");
    }

    /**
     * TODO: 接近度的閾值可能需要根據波動率調整
     */
    private String findNearestLevel(double price, double swingLow, double range, boolean isUptrend) {
        double tolerance = range * 0.02;  // 2% 容差

        for (double ratio : EXTENSION_RATIOS) {
            double level = isUptrend ? swingLow + range * ratio : swingLow + range * (2 - ratio);
            if (Math.abs(price - level) <= tolerance) {
                return formatRatio(ratio) + "%";
            }
        }

        return "NONE";
    }

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
