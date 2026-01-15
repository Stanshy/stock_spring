package com.chris.fin_shark.m07.engine.calculator.statistics;

import com.chris.fin_shark.m07.engine.calculator.IndicatorCalculator;
import com.chris.fin_shark.m07.engine.model.IndicatorMetadata;
import com.chris.fin_shark.m07.engine.model.PriceSeries;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Hurst Exponent（赫斯特指數）計算器
 * <p>
 * Hurst 指數用於判斷價格序列的性質：
 * - H < 0.5: 均值回歸（反趨勢）
 * - H = 0.5: 隨機漫步
 * - H > 0.5: 趨勢持續（動量）
 * </p>
 * <p>
 * 計算方法：R/S Analysis（重新調整範圍分析）
 * </p>
 * <p>
 * TODO: Hurst 指數有多種計算方法：
 * - R/S 分析（此實現）
 * - DFA (Detrended Fluctuation Analysis)
 * - Variance-time method
 * 不同方法結果可能略有差異
 * </p>
 * <p>
 * TODO: 最小樣本數的選擇會影響結果穩定性
 * </p>
 *
 * @author chris
 * @since 1.0.0
 */
@Component
public class HurstExponentCalculator implements IndicatorCalculator {

    @Override
    public String getName() {
        return "HURST";
    }

    @Override
    public String getCategory() {
        return "STATISTICS";
    }

    @Override
    public IndicatorMetadata getMetadata() {
        return IndicatorMetadata.builder()
                .name("HURST")
                .category("STATISTICS")
                .nameZh("赫斯特指數")
                .description("判斷價格序列是趨勢性還是均值回歸")
                .minDataPoints(100)  // 需要足夠的數據
                .defaultParams(Map.of("minLag", 10, "maxLag", 100))
                .priority("P2")
                .build();
    }

    @Override
    public Map<String, Object> calculate(PriceSeries series, Map<String, Object> params) {
        int minLag = (int) params.getOrDefault("minLag", 10);
        int maxLag = (int) params.getOrDefault("maxLag", 100);

        double[] closePrices = series.getCloseArray();
        Map<String, Object> result = new HashMap<>();

        if (closePrices.length >= maxLag) {
            double hurst = calculateHurst(closePrices, minLag, maxLag);
            result.put("hurst", round(hurst));
            result.put("hurst_signal", getSignal(hurst));
        }

        return result;
    }

    /**
     * 使用簡化的 R/S 分析計算 Hurst 指數
     * TODO: 這是簡化版本，完整實現需要更複雜的對數回歸
     */
    private double calculateHurst(double[] prices, int minLag, int maxLag) {
        int length = prices.length;

        // 計算對數報酬率
        double[] logReturns = new double[length - 1];
        for (int i = 1; i < length; i++) {
            if (prices[i - 1] > 0) {
                logReturns[i - 1] = Math.log(prices[i] / prices[i - 1]);
            } else {
                logReturns[i - 1] = 0;
            }
        }

        // 使用多個時間尺度計算 R/S 並做線性回歸
        int numScales = 0;
        double sumLogN = 0;
        double sumLogRS = 0;
        double sumLogNLogRS = 0;
        double sumLogN2 = 0;

        for (int n = minLag; n <= maxLag; n *= 2) {
            double rs = calculateRS(logReturns, n);
            if (rs > 0) {
                double logN = Math.log(n);
                double logRS = Math.log(rs);

                sumLogN += logN;
                sumLogRS += logRS;
                sumLogNLogRS += logN * logRS;
                sumLogN2 += logN * logN;
                numScales++;
            }
        }

        if (numScales < 2) {
            return 0.5;  // 無法計算時返回隨機漫步
        }

        // 線性回歸求斜率（Hurst 指數）
        double hurst = (numScales * sumLogNLogRS - sumLogN * sumLogRS) /
                       (numScales * sumLogN2 - sumLogN * sumLogN);

        // 限制在合理範圍內
        return Math.max(0, Math.min(1, hurst));
    }

    /**
     * 計算特定時間尺度的 R/S 值
     */
    private double calculateRS(double[] returns, int n) {
        int length = returns.length;
        if (n > length) {
            return 0;
        }

        int numSubseries = length / n;
        if (numSubseries == 0) {
            return 0;
        }

        double totalRS = 0;

        for (int i = 0; i < numSubseries; i++) {
            int start = i * n;

            // 計算子序列的均值
            double mean = 0;
            for (int j = start; j < start + n; j++) {
                mean += returns[j];
            }
            mean /= n;

            // 計算累積偏差和標準差
            double[] cumulativeDeviation = new double[n];
            double sumDeviation = 0;
            double sumSquaredDeviation = 0;

            for (int j = 0; j < n; j++) {
                double deviation = returns[start + j] - mean;
                sumDeviation += deviation;
                cumulativeDeviation[j] = sumDeviation;
                sumSquaredDeviation += deviation * deviation;
            }

            // 範圍 R = max(累積偏差) - min(累積偏差)
            double maxCum = Double.MIN_VALUE;
            double minCum = Double.MAX_VALUE;
            for (double cum : cumulativeDeviation) {
                maxCum = Math.max(maxCum, cum);
                minCum = Math.min(minCum, cum);
            }
            double range = maxCum - minCum;

            // 標準差 S
            double stdDev = Math.sqrt(sumSquaredDeviation / n);

            // R/S
            if (stdDev > 0) {
                totalRS += range / stdDev;
            }
        }

        return totalRS / numSubseries;
    }

    private String getSignal(double hurst) {
        if (hurst > 0.6) {
            return "TRENDING";  // 趨勢市場
        } else if (hurst < 0.4) {
            return "MEAN_REVERTING";  // 均值回歸市場
        }
        return "RANDOM";  // 隨機漫步
    }

    private double round(double value) {
        return Math.round(value * 1000.0) / 1000.0;  // 3 位小數
    }
}
