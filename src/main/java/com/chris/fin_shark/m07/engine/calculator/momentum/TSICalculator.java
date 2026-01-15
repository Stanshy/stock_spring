package com.chris.fin_shark.m07.engine.calculator.momentum;

import com.chris.fin_shark.m07.engine.calculator.IndicatorCalculator;
import com.chris.fin_shark.m07.engine.model.IndicatorMetadata;
import com.chris.fin_shark.m07.engine.model.PriceSeries;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * TSI（真實強度指數）計算器
 * <p>
 * 計算公式：
 * PC = Price Change = Close - Previous Close
 * Double Smoothed PC = EMA(EMA(PC, long), short)
 * Double Smoothed Absolute PC = EMA(EMA(|PC|, long), short)
 * TSI = (Double Smoothed PC / Double Smoothed Absolute PC) * 100
 * </p>
 * <p>
 * 特點：雙重平滑的動量指標，適合識別趨勢方向和強度
 * </p>
 *
 * @author chris
 * @since 1.0.0
 */
@Component
public class TSICalculator implements IndicatorCalculator {

    @Override
    public String getName() {
        return "TSI";
    }

    @Override
    public String getCategory() {
        return "MOMENTUM";
    }

    @Override
    public IndicatorMetadata getMetadata() {
        return IndicatorMetadata.builder()
                .name("TSI")
                .category("MOMENTUM")
                .nameZh("真實強度指數")
                .description("雙重平滑的動量指標")
                .minDataPoints(40)
                .defaultParams(Map.of("longPeriod", 25, "shortPeriod", 13, "signalPeriod", 7))
                .priority("P2")
                .build();
    }

    @Override
    public Map<String, Object> calculate(PriceSeries series, Map<String, Object> params) {
        int longPeriod = (int) params.getOrDefault("longPeriod", 25);
        int shortPeriod = (int) params.getOrDefault("shortPeriod", 13);
        int signalPeriod = (int) params.getOrDefault("signalPeriod", 7);

        double[] closePrices = series.getCloseArray();
        Map<String, Object> result = new HashMap<>();

        if (closePrices.length >= longPeriod + shortPeriod + 1) {
            double[] tsiSeries = calculateTSISeries(closePrices, longPeriod, shortPeriod);
            int length = tsiSeries.length;

            double tsi = tsiSeries[length - 1];
            result.put("tsi", round(tsi));

            // 計算信號線
            if (tsiSeries.length >= signalPeriod) {
                double signal = calculateEMA(tsiSeries, signalPeriod);
                result.put("tsi_signal_line", round(signal));
                result.put("tsi_crossover", tsi > signal ? "BULLISH" : "BEARISH");
            }
        }

        return result;
    }

    private double[] calculateTSISeries(double[] prices, int longPeriod, int shortPeriod) {
        int length = prices.length;

        // 計算價格變動
        double[] priceChange = new double[length - 1];
        double[] absPriceChange = new double[length - 1];

        for (int i = 1; i < length; i++) {
            priceChange[i - 1] = prices[i] - prices[i - 1];
            absPriceChange[i - 1] = Math.abs(priceChange[i - 1]);
        }

        // 雙重平滑價格變動
        double[] ema1PC = calculateEMASeries(priceChange, longPeriod);
        double[] ema2PC = calculateEMASeries(ema1PC, shortPeriod);

        // 雙重平滑絕對價格變動
        double[] ema1AbsPC = calculateEMASeries(absPriceChange, longPeriod);
        double[] ema2AbsPC = calculateEMASeries(ema1AbsPC, shortPeriod);

        // 計算 TSI
        double[] tsi = new double[ema2PC.length];
        for (int i = 0; i < ema2PC.length; i++) {
            if (ema2AbsPC[i] != 0) {
                tsi[i] = (ema2PC[i] / ema2AbsPC[i]) * 100;
            } else {
                tsi[i] = 0;
            }
        }

        return tsi;
    }

    private double[] calculateEMASeries(double[] values, int period) {
        double[] ema = new double[values.length];
        double multiplier = 2.0 / (period + 1);

        ema[0] = values[0];
        for (int i = 1; i < values.length; i++) {
            ema[i] = (values[i] - ema[i - 1]) * multiplier + ema[i - 1];
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

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
