package com.chris.fin_shark.m07.engine.calculator.support;

import com.chris.fin_shark.m07.engine.calculator.IndicatorCalculator;
import com.chris.fin_shark.m07.engine.model.IndicatorMetadata;
import com.chris.fin_shark.m07.engine.model.PriceSeries;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Pivot Points（樞軸點）計算器
 * <p>
 * 計算公式（標準 Pivot）：
 * PP（樞軸點）= (High + Low + Close) / 3
 * R1 = 2 * PP - Low
 * S1 = 2 * PP - High
 * R2 = PP + (High - Low)
 * S2 = PP - (High - Low)
 * R3 = High + 2 * (PP - Low)
 * S3 = Low - 2 * (High - PP)
 * </p>
 * <p>
 * 解讀：
 * - PP 作為當日的主要支撐/壓力參考點
 * - R1, R2, R3 為壓力位
 * - S1, S2, S3 為支撐位
 * </p>
 *
 * @author chris
 * @since 1.0.0
 */
@Component
public class PivotPointsCalculator implements IndicatorCalculator {

    @Override
    public String getName() {
        return "PIVOT";
    }

    @Override
    public String getCategory() {
        return "SUPPORT";
    }

    @Override
    public IndicatorMetadata getMetadata() {
        return IndicatorMetadata.builder()
                .name("PIVOT")
                .category("SUPPORT")
                .nameZh("樞軸點")
                .description("基於前一交易日的支撐壓力水準")
                .minDataPoints(2)
                .defaultParams(Map.of("type", "standard"))
                .priority("P1")
                .build();
    }

    @Override
    public Map<String, Object> calculate(PriceSeries series, Map<String, Object> params) {
        String type = (String) params.getOrDefault("type", "standard");

        double[] highPrices = series.getHighArray();
        double[] lowPrices = series.getLowArray();
        double[] closePrices = series.getCloseArray();

        if (closePrices.length < 2) {
            return Map.of();
        }

        // 使用前一交易日的 HLC 計算今日的 Pivot Points
        int prevIndex = closePrices.length - 2;
        double prevHigh = highPrices[prevIndex];
        double prevLow = lowPrices[prevIndex];
        double prevClose = closePrices[prevIndex];

        Map<String, Object> result;

        switch (type.toLowerCase()) {
            case "fibonacci":
                result = calculateFibonacciPivot(prevHigh, prevLow, prevClose);
                break;
            case "woodie":
                result = calculateWoodiePivot(prevHigh, prevLow, closePrices[closePrices.length - 1]);
                break;
            case "camarilla":
                result = calculateCamarillaPivot(prevHigh, prevLow, prevClose);
                break;
            default:
                result = calculateStandardPivot(prevHigh, prevLow, prevClose);
        }

        // 判斷當前價格位置
        double currentClose = closePrices[closePrices.length - 1];
        double pp = (Double) result.get("pivot_pp");
        result.put("pivot_signal", getSignal(currentClose, result));

        return result;
    }

    /**
     * 標準 Pivot Points
     */
    private Map<String, Object> calculateStandardPivot(double high, double low, double close) {
        double pp = (high + low + close) / 3;
        double range = high - low;

        Map<String, Object> result = new HashMap<>();
        result.put("pivot_pp", round(pp));
        result.put("pivot_r1", round(2 * pp - low));
        result.put("pivot_r2", round(pp + range));
        result.put("pivot_r3", round(high + 2 * (pp - low)));
        result.put("pivot_s1", round(2 * pp - high));
        result.put("pivot_s2", round(pp - range));
        result.put("pivot_s3", round(low - 2 * (high - pp)));

        return result;
    }

    /**
     * Fibonacci Pivot Points
     */
    private Map<String, Object> calculateFibonacciPivot(double high, double low, double close) {
        double pp = (high + low + close) / 3;
        double range = high - low;

        Map<String, Object> result = new HashMap<>();
        result.put("pivot_pp", round(pp));
        result.put("pivot_r1", round(pp + 0.382 * range));
        result.put("pivot_r2", round(pp + 0.618 * range));
        result.put("pivot_r3", round(pp + 1.0 * range));
        result.put("pivot_s1", round(pp - 0.382 * range));
        result.put("pivot_s2", round(pp - 0.618 * range));
        result.put("pivot_s3", round(pp - 1.0 * range));

        return result;
    }

    /**
     * Woodie Pivot Points
     */
    private Map<String, Object> calculateWoodiePivot(double high, double low, double open) {
        double pp = (high + low + 2 * open) / 4;
        double range = high - low;

        Map<String, Object> result = new HashMap<>();
        result.put("pivot_pp", round(pp));
        result.put("pivot_r1", round(2 * pp - low));
        result.put("pivot_r2", round(pp + range));
        result.put("pivot_s1", round(2 * pp - high));
        result.put("pivot_s2", round(pp - range));

        return result;
    }

    /**
     * Camarilla Pivot Points
     */
    private Map<String, Object> calculateCamarillaPivot(double high, double low, double close) {
        double range = high - low;

        Map<String, Object> result = new HashMap<>();
        result.put("pivot_pp", round((high + low + close) / 3));
        result.put("pivot_r1", round(close + range * 1.1 / 12));
        result.put("pivot_r2", round(close + range * 1.1 / 6));
        result.put("pivot_r3", round(close + range * 1.1 / 4));
        result.put("pivot_r4", round(close + range * 1.1 / 2));
        result.put("pivot_s1", round(close - range * 1.1 / 12));
        result.put("pivot_s2", round(close - range * 1.1 / 6));
        result.put("pivot_s3", round(close - range * 1.1 / 4));
        result.put("pivot_s4", round(close - range * 1.1 / 2));

        return result;
    }

    private String getSignal(double currentPrice, Map<String, Object> pivots) {
        double pp = (Double) pivots.get("pivot_pp");
        Double r1 = (Double) pivots.get("pivot_r1");
        Double s1 = (Double) pivots.get("pivot_s1");

        if (r1 != null && currentPrice > r1) {
            return "ABOVE_R1";  // 突破第一壓力
        } else if (s1 != null && currentPrice < s1) {
            return "BELOW_S1";  // 跌破第一支撐
        } else if (currentPrice > pp) {
            return "ABOVE_PP";
        } else if (currentPrice < pp) {
            return "BELOW_PP";
        } else {
            return "AT_PP";
        }
    }

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
