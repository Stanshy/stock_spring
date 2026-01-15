package com.chris.fin_shark.m07.engine.calculator.trend;

import com.chris.fin_shark.m07.engine.calculator.IndicatorCalculator;
import com.chris.fin_shark.m07.engine.model.IndicatorMetadata;
import com.chris.fin_shark.m07.engine.model.PriceSeries;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Supertrend（超級趨勢指標）計算器
 * <p>
 * 計算公式：
 * 1. 計算 ATR
 * 2. 基礎上軌 = (High + Low) / 2 + Multiplier * ATR
 * 3. 基礎下軌 = (High + Low) / 2 - Multiplier * ATR
 * 4. 根據趨勢選擇上軌或下軌作為 Supertrend
 * </p>
 * <p>
 * TODO: [待確認] Supertrend 有多種變體：
 * - ATR 週期和乘數的組合選擇（常見：10,3 或 7,3 或 14,2）
 * - 上/下軌的更新邏輯有些版本會做平滑處理
 * - 信號產生的靈敏度調整
 * 目前採用最常見的 (10, 3) 參數和標準邏輯
 * </p>
 *
 * @author chris
 * @since 1.0.0
 */
@Component
public class SupertrendCalculator implements IndicatorCalculator {

    @Override
    public String getName() {
        return "SUPERTREND";
    }

    @Override
    public String getCategory() {
        return "TREND";
    }

    @Override
    public IndicatorMetadata getMetadata() {
        return IndicatorMetadata.builder()
                .name("SUPERTREND")
                .category("TREND")
                .nameZh("超級趨勢")
                .description("結合 ATR 的趨勢追蹤指標")
                .minDataPoints(14)
                .defaultParams(Map.of(
                        "period", 10,
                        "multiplier", 3.0
                ))
                .priority("P1")
                .build();
    }

    @Override
    public Map<String, Object> calculate(PriceSeries series, Map<String, Object> params) {
        int period = (int) params.getOrDefault("period", 10);
        double multiplier = ((Number) params.getOrDefault("multiplier", 3.0)).doubleValue();

        double[] highPrices = series.getHighArray();
        double[] lowPrices = series.getLowArray();
        double[] closePrices = series.getCloseArray();

        if (closePrices.length < period + 1) {
            return Map.of();
        }

        // 計算 ATR
        double[] atr = calculateATR(highPrices, lowPrices, closePrices, period);

        // 計算 Supertrend
        double[] supertrend = calculateSupertrend(highPrices, lowPrices, closePrices, atr, multiplier);

        int lastIndex = supertrend.length - 1;
        double currentSupertrend = supertrend[lastIndex];
        double currentClose = closePrices[closePrices.length - 1];
        boolean isUptrend = currentClose > currentSupertrend;

        Map<String, Object> result = new HashMap<>();
        result.put("supertrend_" + period + "_" + (int) multiplier, round(currentSupertrend));
        result.put("supertrend_trend", isUptrend ? "UPTREND" : "DOWNTREND");
        result.put("supertrend_signal", getSignal(supertrend, closePrices));

        return result;
    }

    /**
     * 計算 ATR（平均真實範圍）
     */
    private double[] calculateATR(double[] high, double[] low, double[] close, int period) {
        int length = high.length;
        double[] tr = new double[length];
        double[] atr = new double[length];

        // 計算 True Range
        tr[0] = high[0] - low[0];
        for (int i = 1; i < length; i++) {
            double hl = high[i] - low[i];
            double hc = Math.abs(high[i] - close[i - 1]);
            double lc = Math.abs(low[i] - close[i - 1]);
            tr[i] = Math.max(hl, Math.max(hc, lc));
        }

        // 計算 ATR（使用 RMA/Wilder's smoothing）
        double sum = 0;
        for (int i = 0; i < period; i++) {
            sum += tr[i];
        }
        atr[period - 1] = sum / period;

        for (int i = period; i < length; i++) {
            atr[i] = (atr[i - 1] * (period - 1) + tr[i]) / period;
        }

        return atr;
    }

    /**
     * 計算 Supertrend
     */
    private double[] calculateSupertrend(double[] high, double[] low, double[] close,
                                          double[] atr, double multiplier) {
        int length = close.length;
        double[] supertrend = new double[length];
        double[] upperBand = new double[length];
        double[] lowerBand = new double[length];
        boolean[] isUptrend = new boolean[length];

        // 初始化
        for (int i = 0; i < length; i++) {
            double midPoint = (high[i] + low[i]) / 2;
            upperBand[i] = midPoint + multiplier * atr[i];
            lowerBand[i] = midPoint - multiplier * atr[i];
        }

        // 設定初始趨勢
        isUptrend[0] = true;
        supertrend[0] = lowerBand[0];

        for (int i = 1; i < length; i++) {
            // 調整上下軌（保持在有利方向）
            if (lowerBand[i] > lowerBand[i - 1] || close[i - 1] < lowerBand[i - 1]) {
                // 保持或更新下軌
            } else {
                lowerBand[i] = lowerBand[i - 1];
            }

            if (upperBand[i] < upperBand[i - 1] || close[i - 1] > upperBand[i - 1]) {
                // 保持或更新上軌
            } else {
                upperBand[i] = upperBand[i - 1];
            }

            // 判斷趨勢
            if (isUptrend[i - 1]) {
                if (close[i] < lowerBand[i]) {
                    isUptrend[i] = false;
                    supertrend[i] = upperBand[i];
                } else {
                    isUptrend[i] = true;
                    supertrend[i] = lowerBand[i];
                }
            } else {
                if (close[i] > upperBand[i]) {
                    isUptrend[i] = true;
                    supertrend[i] = lowerBand[i];
                } else {
                    isUptrend[i] = false;
                    supertrend[i] = upperBand[i];
                }
            }
        }

        return supertrend;
    }

    /**
     * 判斷信號
     */
    private String getSignal(double[] supertrend, double[] close) {
        if (supertrend.length < 2 || close.length < 2) {
            return "NEUTRAL";
        }

        int last = supertrend.length - 1;
        boolean currentAbove = close[last] > supertrend[last];
        boolean prevAbove = close[last - 1] > supertrend[last - 1];

        if (currentAbove && !prevAbove) {
            return "BUY";
        } else if (!currentAbove && prevAbove) {
            return "SELL";
        } else {
            return "HOLD";
        }
    }

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
