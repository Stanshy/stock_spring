package com.chris.fin_shark.m07.engine.calculator.momentum;

import com.chris.fin_shark.m07.engine.calculator.IndicatorCalculator;
import com.chris.fin_shark.m07.engine.model.IndicatorMetadata;
import com.chris.fin_shark.m07.engine.model.PriceSeries;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * MFI（資金流量指標）計算器
 * <p>
 * 計算公式：
 * 1. TP（典型價格）= (High + Low + Close) / 3
 * 2. Raw Money Flow = TP * Volume
 * 3. 若 TP(今) > TP(昨)，則為 Positive Money Flow
 * 4. 若 TP(今) < TP(昨)，則為 Negative Money Flow
 * 5. Money Ratio = Positive MF / Negative MF
 * 6. MFI = 100 - (100 / (1 + Money Ratio))
 * </p>
 * <p>
 * 解讀：
 * - MFI > 80：超買
 * - MFI < 20：超賣
 * - 類似 RSI，但納入成交量因素
 * </p>
 * <p>
 * TODO: [待確認] MFI 的計算方式有些差異：
 * - 當 TP(今) = TP(昨) 時，有些版本忽略該日，有些歸入上一個方向
 * - 成交量為 0 時的處理方式
 * 目前採用忽略該日的做法
 * </p>
 *
 * @author chris
 * @since 1.0.0
 */
@Component
public class MFICalculator implements IndicatorCalculator {

    @Override
    public String getName() {
        return "MFI";
    }

    @Override
    public String getCategory() {
        return "MOMENTUM";
    }

    @Override
    public IndicatorMetadata getMetadata() {
        return IndicatorMetadata.builder()
                .name("MFI")
                .category("MOMENTUM")
                .nameZh("資金流量指標")
                .description("結合價格和成交量的超買超賣指標")
                .minDataPoints(15)
                .defaultParams(Map.of("period", 14))
                .priority("P1")
                .build();
    }

    @Override
    public Map<String, Object> calculate(PriceSeries series, Map<String, Object> params) {
        int period = (int) params.getOrDefault("period", 14);

        double[] highPrices = series.getHighArray();
        double[] lowPrices = series.getLowArray();
        double[] closePrices = series.getCloseArray();
        long[] volumes = series.getVolumeArray();

        if (closePrices.length < period + 1 || volumes.length < period + 1) {
            return Map.of();
        }

        // 計算典型價格（TP）
        double[] tp = new double[closePrices.length];
        for (int i = 0; i < closePrices.length; i++) {
            tp[i] = (highPrices[i] + lowPrices[i] + closePrices[i]) / 3;
        }

        // 計算最近 period 天的正/負資金流量
        double positiveMF = 0;
        double negativeMF = 0;

        int startIndex = tp.length - period;
        for (int i = startIndex; i < tp.length; i++) {
            double rawMF = tp[i] * volumes[i];

            if (tp[i] > tp[i - 1]) {
                positiveMF += rawMF;
            } else if (tp[i] < tp[i - 1]) {
                negativeMF += rawMF;
            }
            // TP 相等時忽略
        }

        // 計算 MFI
        double mfi;
        if (negativeMF == 0) {
            mfi = 100.0;  // 全部為正資金流量
        } else {
            double moneyRatio = positiveMF / negativeMF;
            mfi = 100.0 - (100.0 / (1.0 + moneyRatio));
        }

        Map<String, Object> result = new HashMap<>();
        result.put("mfi_" + period, round(mfi));
        result.put("mfi_signal", getSignal(mfi));

        return result;
    }

    private String getSignal(double mfi) {
        if (mfi > 80) {
            return "OVERBOUGHT";
        } else if (mfi < 20) {
            return "OVERSOLD";
        } else {
            return "NEUTRAL";
        }
    }

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
