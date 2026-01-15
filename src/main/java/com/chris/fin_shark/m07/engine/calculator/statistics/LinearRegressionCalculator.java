package com.chris.fin_shark.m07.engine.calculator.statistics;

import com.chris.fin_shark.m07.engine.calculator.IndicatorCalculator;
import com.chris.fin_shark.m07.engine.model.IndicatorMetadata;
import com.chris.fin_shark.m07.engine.model.PriceSeries;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Linear Regression（線性回歸）計算器
 * <p>
 * 計算公式：
 * y = mx + b（其中 m 是斜率，b 是截距）
 *
 * 斜率（Slope）：
 * m = (n * Σ(xy) - Σx * Σy) / (n * Σ(x²) - (Σx)²)
 *
 * R²（決定係數）：
 * R² = 1 - (SS_res / SS_tot)
 * 其中 SS_res = Σ(y - ŷ)²，SS_tot = Σ(y - ȳ)²
 * </p>
 * <p>
 * 輸出：
 * - linreg_value: 線性回歸預測值（在最後一天的回歸值）
 * - linreg_slope: 斜率（正值為上升趨勢）
 * - linreg_r2: R²（0-1，越接近 1 表示線性關係越強）
 * - linreg_intercept: 截距
 * </p>
 *
 * @author chris
 * @since 1.0.0
 */
@Component
public class LinearRegressionCalculator implements IndicatorCalculator {

    @Override
    public String getName() {
        return "LINREG";
    }

    @Override
    public String getCategory() {
        return "STATISTICS";
    }

    @Override
    public IndicatorMetadata getMetadata() {
        return IndicatorMetadata.builder()
                .name("LINREG")
                .category("STATISTICS")
                .nameZh("線性回歸")
                .description("計算價格的線性回歸趨勢、斜率和 R²")
                .minDataPoints(14)
                .defaultParams(Map.of("period", 14))
                .priority("P1")
                .build();
    }

    @Override
    public Map<String, Object> calculate(PriceSeries series, Map<String, Object> params) {
        int period = (int) params.getOrDefault("period", 14);

        double[] closePrices = series.getCloseArray();

        if (closePrices.length < period) {
            return Map.of();
        }

        // 取最近 period 天的資料
        double[] y = new double[period];
        int startIndex = closePrices.length - period;
        System.arraycopy(closePrices, startIndex, y, 0, period);

        // 計算線性回歸
        LinearRegressionResult result = calculateLinearRegression(y);

        Map<String, Object> output = new HashMap<>();
        output.put("linreg_" + period, round(result.predictedValue));
        output.put("linreg_slope_" + period, round(result.slope));
        output.put("linreg_r2_" + period, round4(result.rSquared));
        output.put("linreg_intercept_" + period, round(result.intercept));
        output.put("linreg_signal", getSignal(result.slope, result.rSquared));

        return output;
    }

    /**
     * 計算線性回歸
     */
    private LinearRegressionResult calculateLinearRegression(double[] y) {
        int n = y.length;

        // x 為 0, 1, 2, ..., n-1
        double sumX = 0;
        double sumY = 0;
        double sumXY = 0;
        double sumX2 = 0;

        for (int i = 0; i < n; i++) {
            sumX += i;
            sumY += y[i];
            sumXY += i * y[i];
            sumX2 += i * i;
        }

        double meanY = sumY / n;

        // 計算斜率和截距
        double denominator = n * sumX2 - sumX * sumX;
        double slope = (n * sumXY - sumX * sumY) / denominator;
        double intercept = (sumY - slope * sumX) / n;

        // 計算 R²
        double ssTot = 0;  // 總變異
        double ssRes = 0;  // 殘差變異

        for (int i = 0; i < n; i++) {
            double predicted = slope * i + intercept;
            ssTot += (y[i] - meanY) * (y[i] - meanY);
            ssRes += (y[i] - predicted) * (y[i] - predicted);
        }

        double rSquared = 1.0;
        if (ssTot > 0) {
            rSquared = 1.0 - (ssRes / ssTot);
        }

        // 預測值（在最後一點的回歸值）
        double predictedValue = slope * (n - 1) + intercept;

        return new LinearRegressionResult(slope, intercept, rSquared, predictedValue);
    }

    private String getSignal(double slope, double rSquared) {
        // R² 太低表示線性關係不強，信號不可靠
        if (rSquared < 0.5) {
            return "WEAK_TREND";  // 弱趨勢/無明顯趨勢
        }

        if (slope > 0 && rSquared >= 0.8) {
            return "STRONG_UPTREND";
        } else if (slope > 0 && rSquared >= 0.5) {
            return "UPTREND";
        } else if (slope < 0 && rSquared >= 0.8) {
            return "STRONG_DOWNTREND";
        } else if (slope < 0 && rSquared >= 0.5) {
            return "DOWNTREND";
        } else {
            return "NEUTRAL";
        }
    }

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    private double round4(double value) {
        return Math.round(value * 10000.0) / 10000.0;
    }

    /**
     * 線性回歸結果
     */
    private static class LinearRegressionResult {
        final double slope;
        final double intercept;
        final double rSquared;
        final double predictedValue;

        LinearRegressionResult(double slope, double intercept, double rSquared, double predictedValue) {
            this.slope = slope;
            this.intercept = intercept;
            this.rSquared = rSquared;
            this.predictedValue = predictedValue;
        }
    }
}
