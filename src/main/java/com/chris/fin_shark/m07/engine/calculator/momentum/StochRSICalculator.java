package com.chris.fin_shark.m07.engine.calculator.momentum;

import com.chris.fin_shark.m07.engine.calculator.IndicatorCalculator;
import com.chris.fin_shark.m07.engine.model.IndicatorMetadata;
import com.chris.fin_shark.m07.engine.model.PriceSeries;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Stochastic RSI（隨機相對強弱指標）計算器
 * <p>
 * 計算公式：
 * 1. 先計算 RSI 序列
 * 2. StochRSI = (RSI - RSI_min) / (RSI_max - RSI_min)
 * 3. %K = StochRSI 的 smoothK 期 SMA
 * 4. %D = %K 的 smoothD 期 SMA
 * </p>
 * <p>
 * 解讀：
 * - StochRSI 範圍：0 到 1（或 0 到 100）
 * - > 0.8 (80)：超買
 * - < 0.2 (20)：超賣
 * - %K 上穿 %D：買入信號
 * - %K 下穿 %D：賣出信號
 * </p>
 * <p>
 * TODO: [待確認] Stochastic RSI 有多種參數組合：
 * - RSI 週期（通常 14）
 * - Stochastic 週期（通常 14）
 * - %K 平滑週期（通常 3）
 * - %D 平滑週期（通常 3）
 * - 輸出格式：0-1 或 0-100
 * 目前採用 (14, 14, 3, 3) 的標準參數，輸出 0-100
 * </p>
 *
 * @author chris
 * @since 1.0.0
 */
@Component
public class StochRSICalculator implements IndicatorCalculator {

    @Override
    public String getName() {
        return "STOCHRSI";
    }

    @Override
    public String getCategory() {
        return "MOMENTUM";
    }

    @Override
    public IndicatorMetadata getMetadata() {
        return IndicatorMetadata.builder()
                .name("STOCHRSI")
                .category("MOMENTUM")
                .nameZh("隨機相對強弱指標")
                .description("將 Stochastic 應用於 RSI 的動能指標")
                .minDataPoints(32)  // rsiPeriod + stochPeriod + smoothK + smoothD
                .defaultParams(Map.of(
                        "rsiPeriod", 14,
                        "stochPeriod", 14,
                        "smoothK", 3,
                        "smoothD", 3
                ))
                .priority("P1")
                .build();
    }

    @Override
    public Map<String, Object> calculate(PriceSeries series, Map<String, Object> params) {
        int rsiPeriod = (int) params.getOrDefault("rsiPeriod", 14);
        int stochPeriod = (int) params.getOrDefault("stochPeriod", 14);
        int smoothK = (int) params.getOrDefault("smoothK", 3);
        int smoothD = (int) params.getOrDefault("smoothD", 3);

        double[] closePrices = series.getCloseArray();

        int minRequired = rsiPeriod + stochPeriod + smoothK + smoothD;
        if (closePrices.length < minRequired) {
            return Map.of();
        }

        // 1. 計算 RSI 序列
        double[] rsiValues = calculateRSISeries(closePrices, rsiPeriod);

        // 2. 計算 Stochastic RSI
        double[] stochRSI = calculateStochastic(rsiValues, stochPeriod);

        // 3. 計算 %K（對 StochRSI 做 SMA）
        double[] kValues = calculateSMA(stochRSI, smoothK);

        // 4. 計算 %D（對 %K 做 SMA）
        double[] dValues = calculateSMA(kValues, smoothD);

        // 取最新值
        double k = kValues[kValues.length - 1] * 100;  // 轉換為 0-100
        double d = dValues[dValues.length - 1] * 100;

        Map<String, Object> result = new HashMap<>();
        result.put("stochrsi_k", round(k));
        result.put("stochrsi_d", round(d));
        result.put("stochrsi_signal", getSignal(k, d, kValues, dValues));

        return result;
    }

    /**
     * 計算 RSI 序列
     */
    private double[] calculateRSISeries(double[] prices, int period) {
        int length = prices.length;
        double[] rsi = new double[length];

        // 計算價格變動
        double[] gains = new double[length];
        double[] losses = new double[length];

        for (int i = 1; i < length; i++) {
            double change = prices[i] - prices[i - 1];
            if (change > 0) {
                gains[i] = change;
                losses[i] = 0;
            } else {
                gains[i] = 0;
                losses[i] = Math.abs(change);
            }
        }

        // 計算初始平均
        double avgGain = 0;
        double avgLoss = 0;
        for (int i = 1; i <= period; i++) {
            avgGain += gains[i];
            avgLoss += losses[i];
        }
        avgGain /= period;
        avgLoss /= period;

        // 計算 RSI 序列
        for (int i = 0; i < period; i++) {
            rsi[i] = 50;  // 填充初始值
        }

        if (avgLoss == 0) {
            rsi[period] = 100;
        } else {
            rsi[period] = 100 - (100 / (1 + avgGain / avgLoss));
        }

        // 使用 Wilder's smoothing 計算後續值
        for (int i = period + 1; i < length; i++) {
            avgGain = (avgGain * (period - 1) + gains[i]) / period;
            avgLoss = (avgLoss * (period - 1) + losses[i]) / period;

            if (avgLoss == 0) {
                rsi[i] = 100;
            } else {
                rsi[i] = 100 - (100 / (1 + avgGain / avgLoss));
            }
        }

        return rsi;
    }

    /**
     * 計算 Stochastic（對 RSI 序列）
     */
    private double[] calculateStochastic(double[] rsi, int period) {
        int length = rsi.length;
        double[] stoch = new double[length];

        for (int i = 0; i < period - 1; i++) {
            stoch[i] = 0.5;  // 填充初始值
        }

        for (int i = period - 1; i < length; i++) {
            double max = Double.MIN_VALUE;
            double min = Double.MAX_VALUE;

            for (int j = i - period + 1; j <= i; j++) {
                max = Math.max(max, rsi[j]);
                min = Math.min(min, rsi[j]);
            }

            double range = max - min;
            if (range == 0) {
                stoch[i] = 0.5;
            } else {
                stoch[i] = (rsi[i] - min) / range;
            }
        }

        return stoch;
    }

    /**
     * 計算 SMA
     */
    private double[] calculateSMA(double[] values, int period) {
        int length = values.length;
        double[] sma = new double[length];

        for (int i = 0; i < period - 1; i++) {
            sma[i] = values[i];
        }

        for (int i = period - 1; i < length; i++) {
            double sum = 0;
            for (int j = i - period + 1; j <= i; j++) {
                sum += values[j];
            }
            sma[i] = sum / period;
        }

        return sma;
    }

    private String getSignal(double k, double d, double[] kValues, double[] dValues) {
        // 檢查交叉
        int last = kValues.length - 1;
        if (last < 1) return "NEUTRAL";

        double prevK = kValues[last - 1] * 100;
        double prevD = dValues[last - 1] * 100;

        boolean goldenCross = k > d && prevK <= prevD;
        boolean deathCross = k < d && prevK >= prevD;

        if (goldenCross && k < 20) {
            return "STRONG_BUY";  // 低檔黃金交叉
        } else if (deathCross && k > 80) {
            return "STRONG_SELL";  // 高檔死亡交叉
        } else if (k > 80) {
            return "OVERBOUGHT";
        } else if (k < 20) {
            return "OVERSOLD";
        } else {
            return "NEUTRAL";
        }
    }

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
