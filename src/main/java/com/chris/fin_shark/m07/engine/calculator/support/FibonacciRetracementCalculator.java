package com.chris.fin_shark.m07.engine.calculator.support;

import com.chris.fin_shark.m07.engine.calculator.IndicatorCalculator;
import com.chris.fin_shark.m07.engine.model.IndicatorMetadata;
import com.chris.fin_shark.m07.engine.model.PriceSeries;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Fibonacci Retracement（斐波那契回調）計算器
 * <p>
 * 計算公式：
 * 1. 找出指定期間內的最高價和最低價
 * 2. 計算各斐波那契水準：
 *    - 0%, 23.6%, 38.2%, 50%, 61.8%, 78.6%, 100%
 * </p>
 * <p>
 * 解讀：
 * - 在上升趨勢回調時，38.2%、50%、61.8% 是常見的支撐位
 * - 在下降趨勢反彈時，這些水準是常見的壓力位
 * </p>
 * <p>
 * TODO: [待確認] Fibonacci 回調的起點終點選擇有多種方式：
 * - 固定期間（如最近 N 天）
 * - 自動偵測波段高低點
 * - 手動指定區間
 * 目前採用固定期間的簡單實現，自動判斷趨勢方向
 * </p>
 *
 * @author chris
 * @since 1.0.0
 */
@Component
public class FibonacciRetracementCalculator implements IndicatorCalculator {

    // 標準斐波那契回調比率
    private static final double[] FIB_LEVELS = {0.0, 0.236, 0.382, 0.5, 0.618, 0.786, 1.0};

    @Override
    public String getName() {
        return "FIBRETRACEMENT";
    }

    @Override
    public String getCategory() {
        return "SUPPORT";
    }

    @Override
    public IndicatorMetadata getMetadata() {
        return IndicatorMetadata.builder()
                .name("FIBRETRACEMENT")
                .category("SUPPORT")
                .nameZh("斐波那契回調")
                .description("基於斐波那契數列的支撐壓力水準")
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

        // 找出期間內的最高價和最低價及其位置
        double periodHigh = Double.MIN_VALUE;
        double periodLow = Double.MAX_VALUE;
        int highIndex = 0;
        int lowIndex = 0;

        int startIndex = highPrices.length - period;
        for (int i = startIndex; i < highPrices.length; i++) {
            if (highPrices[i] > periodHigh) {
                periodHigh = highPrices[i];
                highIndex = i;
            }
            if (lowPrices[i] < periodLow) {
                periodLow = lowPrices[i];
                lowIndex = i;
            }
        }

        // 判斷趨勢方向（高點在低點之後 = 上升趨勢）
        boolean isUptrend = highIndex > lowIndex;
        double range = periodHigh - periodLow;

        Map<String, Object> result = new HashMap<>();
        result.put("fib_high", round(periodHigh));
        result.put("fib_low", round(periodLow));
        result.put("fib_trend", isUptrend ? "UPTREND" : "DOWNTREND");

        // 計算各斐波那契水準
        if (isUptrend) {
            // 上升趨勢：從低點往高點計算回調
            result.put("fib_0", round(periodHigh));           // 0%（高點）
            result.put("fib_236", round(periodHigh - range * 0.236));
            result.put("fib_382", round(periodHigh - range * 0.382));
            result.put("fib_500", round(periodHigh - range * 0.5));
            result.put("fib_618", round(periodHigh - range * 0.618));
            result.put("fib_786", round(periodHigh - range * 0.786));
            result.put("fib_1000", round(periodLow));         // 100%（低點）
        } else {
            // 下降趨勢：從高點往低點計算反彈
            result.put("fib_0", round(periodLow));            // 0%（低點）
            result.put("fib_236", round(periodLow + range * 0.236));
            result.put("fib_382", round(periodLow + range * 0.382));
            result.put("fib_500", round(periodLow + range * 0.5));
            result.put("fib_618", round(periodLow + range * 0.618));
            result.put("fib_786", round(periodLow + range * 0.786));
            result.put("fib_1000", round(periodHigh));        // 100%（高點）
        }

        // 判斷當前價格所在的斐波那契區間
        double currentClose = closePrices[closePrices.length - 1];
        result.put("fib_signal", getSignal(currentClose, periodHigh, periodLow, range, isUptrend));

        return result;
    }

    private String getSignal(double price, double high, double low, double range, boolean isUptrend) {
        double retraceLevel;

        if (isUptrend) {
            // 計算回調比率
            retraceLevel = (high - price) / range;
        } else {
            // 計算反彈比率
            retraceLevel = (price - low) / range;
        }

        if (retraceLevel <= 0.236) {
            return "STRONG_TREND";       // 趨勢強勁，回調很淺
        } else if (retraceLevel <= 0.382) {
            return "SHALLOW_RETRACE";    // 淺回調
        } else if (retraceLevel <= 0.5) {
            return "MODERATE_RETRACE";   // 中等回調
        } else if (retraceLevel <= 0.618) {
            return "DEEP_RETRACE";       // 深回調
        } else if (retraceLevel <= 0.786) {
            return "VERY_DEEP_RETRACE";  // 非常深回調
        } else {
            return "TREND_REVERSAL";     // 可能趨勢反轉
        }
    }

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
