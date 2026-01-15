package com.chris.fin_shark.m07.engine.calculator.momentum;

import com.chris.fin_shark.m07.engine.calculator.IndicatorCalculator;
import com.chris.fin_shark.m07.engine.model.IndicatorMetadata;
import com.chris.fin_shark.m07.engine.model.PriceSeries;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * CCI（順勢指標）計算器
 * <p>
 * 計算公式：
 * 1. TP（典型價格）= (High + Low + Close) / 3
 * 2. SMA(TP) = TP 的 N 日簡單移動平均
 * 3. MD（平均偏差）= Σ|TP - SMA(TP)| / N
 * 4. CCI = (TP - SMA(TP)) / (0.015 * MD)
 * </p>
 * <p>
 * 解讀：
 * - CCI > +100：超買，可能回調
 * - CCI < -100：超賣，可能反彈
 * - CCI 穿越 +100/-100 可作為買賣信號
 * </p>
 *
 * @author chris
 * @since 1.0.0
 */
@Component
public class CCICalculator implements IndicatorCalculator {

    private static final double CONSTANT = 0.015;

    @Override
    public String getName() {
        return "CCI";
    }

    @Override
    public String getCategory() {
        return "MOMENTUM";
    }

    @Override
    public IndicatorMetadata getMetadata() {
        return IndicatorMetadata.builder()
                .name("CCI")
                .category("MOMENTUM")
                .nameZh("順勢指標")
                .description("衡量價格偏離統計平均的程度")
                .minDataPoints(20)
                .defaultParams(Map.of("period", 20))
                .priority("P1")
                .build();
    }

    @Override
    public Map<String, Object> calculate(PriceSeries series, Map<String, Object> params) {
        int period = (int) params.getOrDefault("period", 20);

        double[] highPrices = series.getHighArray();
        double[] lowPrices = series.getLowArray();
        double[] closePrices = series.getCloseArray();

        if (closePrices.length < period) {
            return Map.of();
        }

        // 計算典型價格（TP）序列
        double[] tp = new double[closePrices.length];
        for (int i = 0; i < closePrices.length; i++) {
            tp[i] = (highPrices[i] + lowPrices[i] + closePrices[i]) / 3;
        }

        // 計算最近 period 天的 TP 平均值
        int startIndex = tp.length - period;
        double tpSum = 0;
        for (int i = startIndex; i < tp.length; i++) {
            tpSum += tp[i];
        }
        double tpSMA = tpSum / period;

        // 計算平均偏差（Mean Deviation）
        double mdSum = 0;
        for (int i = startIndex; i < tp.length; i++) {
            mdSum += Math.abs(tp[i] - tpSMA);
        }
        double md = mdSum / period;

        // 計算 CCI
        double currentTP = tp[tp.length - 1];
        double cci;
        if (md == 0) {
            cci = 0;  // 避免除以零
        } else {
            cci = (currentTP - tpSMA) / (CONSTANT * md);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("cci_" + period, round(cci));
        result.put("cci_signal", getSignal(cci));

        return result;
    }

    private String getSignal(double cci) {
        if (cci > 100) {
            return "OVERBOUGHT";
        } else if (cci < -100) {
            return "OVERSOLD";
        } else {
            return "NEUTRAL";
        }
    }

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
