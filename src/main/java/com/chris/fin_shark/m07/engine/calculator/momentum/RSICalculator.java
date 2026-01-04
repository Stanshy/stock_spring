package com.chris.fin_shark.m07.engine.calculator.momentum;

import com.chris.fin_shark.m07.engine.calculator.IndicatorCalculator;
import com.chris.fin_shark.m07.engine.model.IndicatorMetadata;
import com.chris.fin_shark.m07.engine.model.PriceSeries;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * RSI（相對強弱指標）計算器
 * <p>
 * 計算公式：
 * RS = 平均漲幅 / 平均跌幅
 * RSI = 100 - (100 / (1 + RS))
 * </p>
 *
 * @author chris
 * @since 1.0.0
 */
@Component
public class RSICalculator implements IndicatorCalculator {

    @Override
    public String getName() {
        return "RSI";
    }

    @Override
    public String getCategory() {
        return "MOMENTUM";
    }

    @Override
    public IndicatorMetadata getMetadata() {
        return IndicatorMetadata.builder()
                .name("RSI")
                .category("MOMENTUM")
                .nameZh("相對強弱指標")
                .description("衡量價格動能的超買超賣指標")
                .minDataPoints(15)  // period + 1
                .defaultParams(Map.of("period", 14))
                .priority("P0")
                .build();
    }

    @Override
    public Map<String, Object> calculate(PriceSeries series, Map<String, Object> params) {
        // 取得參數
        int period = (int) params.getOrDefault("period", 14);

        // 取得收盤價
        double[] closePrices = series.getCloseArray();

        if (closePrices.length < period + 1) {
            // 資料不足，返回空結果
            return Map.of();
        }

        // 計算 RSI
        double rsi = calculateRSI(closePrices, period);

        // 返回結果
        Map<String, Object> result = new HashMap<>();
        result.put("rsi_" + period, rsi);
        result.put("rsi_signal", getSignal(rsi));  // 超買/超賣信號

        return result;
    }

    /**
     * 計算 RSI
     *
     * @param prices 價格陣列
     * @param period 週期
     * @return RSI 值
     */
    private double calculateRSI(double[] prices, int period) {
        // 計算價格變動
        double[] changes = new double[prices.length - 1];
        for (int i = 1; i < prices.length; i++) {
            changes[i - 1] = prices[i] - prices[i - 1];
        }

        // 分離漲跌
        double[] gains = new double[changes.length];
        double[] losses = new double[changes.length];
        for (int i = 0; i < changes.length; i++) {
            if (changes[i] > 0) {
                gains[i] = changes[i];
                losses[i] = 0;
            } else {
                gains[i] = 0;
                losses[i] = Math.abs(changes[i]);
            }
        }

        // 計算初始平均漲跌幅（使用 SMA）
        double avgGain = 0;
        double avgLoss = 0;
        for (int i = 0; i < period; i++) {
            avgGain += gains[i];
            avgLoss += losses[i];
        }
        avgGain /= period;
        avgLoss /= period;

        // 使用指數平滑計算後續的平均漲跌幅
        for (int i = period; i < changes.length; i++) {
            avgGain = ((avgGain * (period - 1)) + gains[i]) / period;
            avgLoss = ((avgLoss * (period - 1)) + losses[i]) / period;
        }

        // 計算 RS 和 RSI
        if (avgLoss == 0) {
            return 100.0;  // 沒有下跌，RSI = 100
        }

        double rs = avgGain / avgLoss;
        double rsi = 100.0 - (100.0 / (1.0 + rs));

        // 四捨五入到小數點後 2 位
        return Math.round(rsi * 100.0) / 100.0;
    }

    /**
     * 判斷信號
     *
     * @param rsi RSI 值
     * @return 信號（OVERBOUGHT/OVERSOLD/NEUTRAL）
     */
    private String getSignal(double rsi) {
        if (rsi >= 70) {
            return "OVERBOUGHT";  // 超買
        } else if (rsi <= 30) {
            return "OVERSOLD";    // 超賣
        } else {
            return "NEUTRAL";     // 中性
        }
    }
}
