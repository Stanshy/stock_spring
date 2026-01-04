package com.chris.fin_shark.m07.engine.calculator.trend;

import com.chris.fin_shark.m07.engine.calculator.IndicatorCalculator;
import com.chris.fin_shark.m07.engine.model.IndicatorMetadata;
import com.chris.fin_shark.m07.engine.model.PriceSeries;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * EMA（指數移動平均）計算器
 * <p>
 * 計算公式：
 * EMA(today) = Price(today) * k + EMA(yesterday) * (1 - k)
 * 其中 k = 2 / (period + 1)
 * </p>
 *
 * @author chris
 * @since 1.0.0
 */
@Component
public class EMACalculator implements IndicatorCalculator {

    @Override
    public String getName() {
        return "EMA";
    }

    @Override
    public String getCategory() {
        return "TREND";
    }

    @Override
    public IndicatorMetadata getMetadata() {
        return IndicatorMetadata.builder()
                .name("EMA")
                .category("TREND")
                .nameZh("指數移動平均")
                .description("給予近期價格更高權重的移動平均")
                .minDataPoints(12)
                .defaultParams(Map.of("periods", List.of(12, 26)))
                .priority("P0")
                .build();
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, Object> calculate(PriceSeries series, Map<String, Object> params) {
        // 取得參數
        List<Integer> periods = (List<Integer>) params.getOrDefault(
                "periods",
                List.of(12, 26)
        );

        // 取得收盤價
        double[] closePrices = series.getCloseArray();

        // 計算每個週期的 EMA
        Map<String, Object> result = new HashMap<>();
        for (Integer period : periods) {
            if (closePrices.length >= period) {
                double ema = calculateEMA(closePrices, period);
                result.put("ema" + period, ema);
            }
        }

        return result;
    }

    /**
     * 計算 EMA
     *
     * @param prices 價格陣列
     * @param period 週期
     * @return EMA 值
     */
    public double calculateEMA(double[] prices, int period) {
        if (prices.length < period) {
            throw new IllegalArgumentException(
                    String.format("資料不足：需要%d天，實際%d天", period, prices.length)
            );
        }

        // 計算平滑係數
        double k = 2.0 / (period + 1);

        // 使用 SMA 作為初始 EMA
        double ema = 0;
        for (int i = 0; i < period; i++) {
            ema += prices[i];
        }
        ema /= period;

        // 計算後續的 EMA
        for (int i = period; i < prices.length; i++) {
            ema = (prices[i] * k) + (ema * (1 - k));
        }

        // 四捨五入到小數點後 2 位
        return Math.round(ema * 100.0) / 100.0;
    }

    /**
     * 計算整個 EMA 序列（MACD 需要）
     *
     * @param prices 價格陣列
     * @param period 週期
     * @return EMA 序列
     */
    public double[] calculateEMASeries(double[] prices, int period) {
        if (prices.length < period) {
            return new double[0];
        }

        double[] emaValues = new double[prices.length];
        double k = 2.0 / (period + 1);

        // 初始 EMA（使用 SMA）
        double ema = 0;
        for (int i = 0; i < period; i++) {
            ema += prices[i];
        }
        ema /= period;
        emaValues[period - 1] = ema;

        // 計算後續的 EMA
        for (int i = period; i < prices.length; i++) {
            ema = (prices[i] * k) + (ema * (1 - k));
            emaValues[i] = ema;
        }

        return emaValues;
    }
}
