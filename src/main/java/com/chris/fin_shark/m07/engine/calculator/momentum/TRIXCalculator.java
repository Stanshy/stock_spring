package com.chris.fin_shark.m07.engine.calculator.momentum;

import com.chris.fin_shark.m07.engine.calculator.IndicatorCalculator;
import com.chris.fin_shark.m07.engine.model.IndicatorMetadata;
import com.chris.fin_shark.m07.engine.model.PriceSeries;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * TRIX（三重指數平滑移動平均）計算器
 * <p>
 * 計算公式：
 * EMA1 = EMA(Price, period)
 * EMA2 = EMA(EMA1, period)
 * EMA3 = EMA(EMA2, period)
 * TRIX = (EMA3 - EMA3[1]) / EMA3[1] * 100
 * </p>
 * <p>
 * 特點：動能指標，過濾短期波動，適合識別趨勢方向
 * </p>
 *
 * @author chris
 * @since 1.0.0
 */
@Component
public class TRIXCalculator implements IndicatorCalculator {

    @Override
    public String getName() {
        return "TRIX";
    }

    @Override
    public String getCategory() {
        return "MOMENTUM";
    }

    @Override
    public IndicatorMetadata getMetadata() {
        return IndicatorMetadata.builder()
                .name("TRIX")
                .category("MOMENTUM")
                .nameZh("三重指數平滑移動平均")
                .description("過濾短期波動的動能指標")
                .minDataPoints(50)
                .defaultParams(Map.of("period", 15, "signalPeriod", 9))
                .priority("P2")
                .build();
    }

    @Override
    public Map<String, Object> calculate(PriceSeries series, Map<String, Object> params) {
        int period = (int) params.getOrDefault("period", 15);
        int signalPeriod = (int) params.getOrDefault("signalPeriod", 9);

        double[] closePrices = series.getCloseArray();
        Map<String, Object> result = new HashMap<>();

        if (closePrices.length >= period * 3 + signalPeriod) {
            double[] trixSeries = calculateTRIXSeries(closePrices, period);
            int length = trixSeries.length;

            double trix = trixSeries[length - 1];
            result.put("trix", round(trix));

            // 計算信號線 (TRIX 的 EMA)
            double signal = calculateEMA(trixSeries, signalPeriod);
            result.put("trix_signal", round(signal));

            // 判斷交叉信號
            result.put("trix_crossover", getCrossoverSignal(trix, signal));
        }

        return result;
    }

    private double[] calculateTRIXSeries(double[] prices, int period) {
        // 三重 EMA
        double[] ema1 = calculateEMASeries(prices, period);
        double[] ema2 = calculateEMASeries(ema1, period);
        double[] ema3 = calculateEMASeries(ema2, period);

        // 計算 TRIX (百分比變化)
        double[] trix = new double[ema3.length - 1];
        for (int i = 1; i < ema3.length; i++) {
            if (ema3[i - 1] != 0) {
                trix[i - 1] = ((ema3[i] - ema3[i - 1]) / ema3[i - 1]) * 100;
            } else {
                trix[i - 1] = 0;
            }
        }

        return trix;
    }

    private double[] calculateEMASeries(double[] prices, int period) {
        double[] ema = new double[prices.length];
        double multiplier = 2.0 / (period + 1);

        // 初始 EMA = 第一個價格
        ema[0] = prices[0];

        for (int i = 1; i < prices.length; i++) {
            ema[i] = (prices[i] - ema[i - 1]) * multiplier + ema[i - 1];
        }

        return ema;
    }

    private double calculateEMA(double[] values, int period) {
        double multiplier = 2.0 / (period + 1);
        double ema = values[0];

        for (int i = 1; i < values.length; i++) {
            ema = (values[i] - ema) * multiplier + ema;
        }

        return ema;
    }

    private String getCrossoverSignal(double trix, double signal) {
        if (trix > signal && trix > 0) {
            return "BULLISH";
        } else if (trix < signal && trix < 0) {
            return "BEARISH";
        }
        return "NEUTRAL";
    }

    private double round(double value) {
        return Math.round(value * 1000.0) / 1000.0;  // 3 位小數
    }
}
