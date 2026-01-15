package com.chris.fin_shark.m07.engine.calculator.momentum;

import com.chris.fin_shark.m07.engine.calculator.IndicatorCalculator;
import com.chris.fin_shark.m07.engine.model.IndicatorMetadata;
import com.chris.fin_shark.m07.engine.model.PriceSeries;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * KST（知識確定事項）計算器
 * <p>
 * 計算公式：
 * ROC1 = ROC(10), SMA1 = SMA(ROC1, 10)
 * ROC2 = ROC(15), SMA2 = SMA(ROC2, 10)
 * ROC3 = ROC(20), SMA3 = SMA(ROC3, 10)
 * ROC4 = ROC(30), SMA4 = SMA(ROC4, 15)
 * KST = (SMA1 * 1) + (SMA2 * 2) + (SMA3 * 3) + (SMA4 * 4)
 * Signal = SMA(KST, 9)
 * </p>
 * <p>
 * 特點：結合多個時間框架的 ROC，適合識別長期趨勢
 * </p>
 *
 * @author chris
 * @since 1.0.0
 */
@Component
public class KSTCalculator implements IndicatorCalculator {

    // TODO: 標準參數可能因來源不同而有變化，當前使用最常見的版本
    private static final int[] ROC_PERIODS = {10, 15, 20, 30};
    private static final int[] SMA_PERIODS = {10, 10, 10, 15};
    private static final int[] WEIGHTS = {1, 2, 3, 4};

    @Override
    public String getName() {
        return "KST";
    }

    @Override
    public String getCategory() {
        return "MOMENTUM";
    }

    @Override
    public IndicatorMetadata getMetadata() {
        return IndicatorMetadata.builder()
                .name("KST")
                .category("MOMENTUM")
                .nameZh("知識確定事項")
                .description("結合多時間框架 ROC 的動量指標")
                .minDataPoints(60)
                .defaultParams(Map.of("signalPeriod", 9))
                .priority("P2")
                .build();
    }

    @Override
    public Map<String, Object> calculate(PriceSeries series, Map<String, Object> params) {
        int signalPeriod = (int) params.getOrDefault("signalPeriod", 9);
        double[] closePrices = series.getCloseArray();

        Map<String, Object> result = new HashMap<>();

        // 需要足夠的數據：最長 ROC (30) + 最長 SMA (15) + 信號線 (9)
        if (closePrices.length >= 60) {
            double[] kstSeries = calculateKSTSeries(closePrices);

            if (kstSeries.length > 0) {
                double kst = kstSeries[kstSeries.length - 1];
                result.put("kst", round(kst));

                // 計算信號線
                if (kstSeries.length >= signalPeriod) {
                    double signal = calculateSMA(kstSeries, signalPeriod);
                    result.put("kst_signal", round(signal));
                    result.put("kst_crossover", kst > signal ? "BULLISH" : "BEARISH");
                }
            }
        }

        return result;
    }

    private double[] calculateKSTSeries(double[] prices) {
        int length = prices.length;

        // 計算各 ROC
        double[][] rocSeries = new double[4][];
        for (int i = 0; i < 4; i++) {
            rocSeries[i] = calculateROCSeries(prices, ROC_PERIODS[i]);
        }

        // 找出最短的 ROC 序列長度
        int minLength = Integer.MAX_VALUE;
        for (double[] roc : rocSeries) {
            minLength = Math.min(minLength, roc.length);
        }

        // 計算各 SMA 並組合成 KST
        int kstLength = minLength - Math.max(Math.max(SMA_PERIODS[0], SMA_PERIODS[1]),
                Math.max(SMA_PERIODS[2], SMA_PERIODS[3])) + 1;

        if (kstLength <= 0) {
            return new double[0];
        }

        double[] kst = new double[kstLength];

        for (int i = 0; i < kstLength; i++) {
            double kstValue = 0;
            for (int j = 0; j < 4; j++) {
                // 取對應位置的 SMA
                int rocIndex = rocSeries[j].length - kstLength + i;
                double[] subRoc = getSubArray(rocSeries[j], rocIndex - SMA_PERIODS[j] + 1, SMA_PERIODS[j]);
                double sma = calculateArrayMean(subRoc);
                kstValue += sma * WEIGHTS[j];
            }
            kst[i] = kstValue;
        }

        return kst;
    }

    private double[] calculateROCSeries(double[] prices, int period) {
        int length = prices.length - period;
        if (length <= 0) {
            return new double[0];
        }

        double[] roc = new double[length];
        for (int i = 0; i < length; i++) {
            int current = i + period;
            if (prices[i] != 0) {
                roc[i] = ((prices[current] - prices[i]) / prices[i]) * 100;
            } else {
                roc[i] = 0;
            }
        }

        return roc;
    }

    private double[] getSubArray(double[] array, int start, int length) {
        start = Math.max(0, start);
        int end = Math.min(array.length, start + length);
        double[] result = new double[end - start];
        System.arraycopy(array, start, result, 0, result.length);
        return result;
    }

    private double calculateArrayMean(double[] array) {
        if (array.length == 0) return 0;
        double sum = 0;
        for (double v : array) {
            sum += v;
        }
        return sum / array.length;
    }

    private double calculateSMA(double[] values, int period) {
        int startIndex = values.length - period;
        if (startIndex < 0) startIndex = 0;

        double sum = 0;
        int count = 0;
        for (int i = startIndex; i < values.length; i++) {
            sum += values[i];
            count++;
        }

        return count > 0 ? sum / count : 0;
    }

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
