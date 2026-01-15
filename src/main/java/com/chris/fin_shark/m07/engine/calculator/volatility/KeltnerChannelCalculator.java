package com.chris.fin_shark.m07.engine.calculator.volatility;

import com.chris.fin_shark.m07.engine.calculator.IndicatorCalculator;
import com.chris.fin_shark.m07.engine.model.IndicatorMetadata;
import com.chris.fin_shark.m07.engine.model.PriceSeries;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Keltner Channel（肯特納通道）計算器
 * <p>
 * 計算公式：
 * 中軌 = EMA(Close, period)
 * 上軌 = 中軌 + multiplier * ATR(atrPeriod)
 * 下軌 = 中軌 - multiplier * ATR(atrPeriod)
 * </p>
 * <p>
 * 解讀：
 * - 價格突破上軌：強勢上漲
 * - 價格跌破下軌：強勢下跌
 * - 與布林通道相比，Keltner 使用 ATR 而非標準差
 * </p>
 * <p>
 * TODO: [待確認] Keltner Channel 有兩種版本：
 * - 原始版本使用 SMA 和 典型價格範圍
 * - 現代版本使用 EMA 和 ATR
 * 目前採用現代版本（EMA + ATR）
 * </p>
 *
 * @author chris
 * @since 1.0.0
 */
@Component
public class KeltnerChannelCalculator implements IndicatorCalculator {

    @Override
    public String getName() {
        return "KELTNER";
    }

    @Override
    public String getCategory() {
        return "VOLATILITY";
    }

    @Override
    public IndicatorMetadata getMetadata() {
        return IndicatorMetadata.builder()
                .name("KELTNER")
                .category("VOLATILITY")
                .nameZh("肯特納通道")
                .description("基於 EMA 和 ATR 的波動通道")
                .minDataPoints(20)
                .defaultParams(Map.of(
                        "emaPeriod", 20,
                        "atrPeriod", 10,
                        "multiplier", 2.0
                ))
                .priority("P1")
                .build();
    }

    @Override
    public Map<String, Object> calculate(PriceSeries series, Map<String, Object> params) {
        int emaPeriod = (int) params.getOrDefault("emaPeriod", 20);
        int atrPeriod = (int) params.getOrDefault("atrPeriod", 10);
        double multiplier = ((Number) params.getOrDefault("multiplier", 2.0)).doubleValue();

        double[] highPrices = series.getHighArray();
        double[] lowPrices = series.getLowArray();
        double[] closePrices = series.getCloseArray();

        int minRequired = Math.max(emaPeriod, atrPeriod);
        if (closePrices.length < minRequired) {
            return Map.of();
        }

        // 計算 EMA
        double ema = calculateEMA(closePrices, emaPeriod);

        // 計算 ATR
        double atr = calculateATR(highPrices, lowPrices, closePrices, atrPeriod);

        // 計算通道
        double upperBand = ema + multiplier * atr;
        double lowerBand = ema - multiplier * atr;
        double currentClose = closePrices[closePrices.length - 1];

        // 計算價格在通道中的位置
        double bandwidth = upperBand - lowerBand;
        double position = 0;
        if (bandwidth > 0) {
            position = ((currentClose - lowerBand) / bandwidth) * 100;
        }

        Map<String, Object> result = new HashMap<>();
        result.put("keltner_upper", round(upperBand));
        result.put("keltner_middle", round(ema));
        result.put("keltner_lower", round(lowerBand));
        result.put("keltner_bandwidth", round(bandwidth));
        result.put("keltner_position", round(position));
        result.put("keltner_signal", getSignal(currentClose, upperBand, lowerBand, ema));

        return result;
    }

    /**
     * 計算 EMA
     */
    private double calculateEMA(double[] prices, int period) {
        double multiplier = 2.0 / (period + 1);

        // 初始 EMA = 第一個價格的 SMA
        double ema = 0;
        for (int i = 0; i < period; i++) {
            ema += prices[i];
        }
        ema /= period;

        // 計算後續 EMA
        for (int i = period; i < prices.length; i++) {
            ema = (prices[i] - ema) * multiplier + ema;
        }

        return ema;
    }

    /**
     * 計算 ATR
     */
    private double calculateATR(double[] high, double[] low, double[] close, int period) {
        int length = high.length;

        // 計算 True Range 序列
        double[] tr = new double[length];
        tr[0] = high[0] - low[0];

        for (int i = 1; i < length; i++) {
            double hl = high[i] - low[i];
            double hc = Math.abs(high[i] - close[i - 1]);
            double lc = Math.abs(low[i] - close[i - 1]);
            tr[i] = Math.max(hl, Math.max(hc, lc));
        }

        // 計算 ATR（使用 RMA/Wilder's smoothing）
        double atr = 0;
        for (int i = 0; i < period; i++) {
            atr += tr[i];
        }
        atr /= period;

        for (int i = period; i < length; i++) {
            atr = (atr * (period - 1) + tr[i]) / period;
        }

        return atr;
    }

    private String getSignal(double close, double upper, double lower, double middle) {
        if (close > upper) {
            return "BREAKOUT_UP";
        } else if (close < lower) {
            return "BREAKOUT_DOWN";
        } else if (close > middle) {
            return "ABOVE_MIDDLE";
        } else {
            return "BELOW_MIDDLE";
        }
    }

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
