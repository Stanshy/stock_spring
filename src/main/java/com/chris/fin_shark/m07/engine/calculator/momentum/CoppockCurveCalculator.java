package com.chris.fin_shark.m07.engine.calculator.momentum;

import com.chris.fin_shark.m07.engine.calculator.IndicatorCalculator;
import com.chris.fin_shark.m07.engine.model.IndicatorMetadata;
import com.chris.fin_shark.m07.engine.model.PriceSeries;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Coppock Curve（科波克曲線）計算器
 * <p>
 * 計算公式：
 * ROC1 = ROC(11 months) - 通常用 11 期
 * ROC2 = ROC(14 months) - 通常用 14 期
 * Coppock = WMA(ROC1 + ROC2, 10)
 * </p>
 * <p>
 * 特點：長期動量指標，原為月線設計，此處改為日線適用
 * 日線參數：ROC 11/14 改為 110/140，WMA 10 改為 100
 * </p>
 * <p>
 * TODO: 標準參數是月線版本，日線版本的最佳參數可能需要調整
 * </p>
 *
 * @author chris
 * @since 1.0.0
 */
@Component
public class CoppockCurveCalculator implements IndicatorCalculator {

    @Override
    public String getName() {
        return "COPPOCK";
    }

    @Override
    public String getCategory() {
        return "MOMENTUM";
    }

    @Override
    public IndicatorMetadata getMetadata() {
        return IndicatorMetadata.builder()
                .name("COPPOCK")
                .category("MOMENTUM")
                .nameZh("科波克曲線")
                .description("長期動量指標，用於識別主要底部")
                .minDataPoints(160)  // 需要足夠數據
                .defaultParams(Map.of("rocPeriod1", 14, "rocPeriod2", 11, "wmaPeriod", 10))
                .priority("P2")
                .build();
    }

    @Override
    public Map<String, Object> calculate(PriceSeries series, Map<String, Object> params) {
        int rocPeriod1 = (int) params.getOrDefault("rocPeriod1", 14);
        int rocPeriod2 = (int) params.getOrDefault("rocPeriod2", 11);
        int wmaPeriod = (int) params.getOrDefault("wmaPeriod", 10);

        double[] closePrices = series.getCloseArray();
        Map<String, Object> result = new HashMap<>();

        int minRequired = Math.max(rocPeriod1, rocPeriod2) + wmaPeriod;
        if (closePrices.length >= minRequired) {
            double coppock = calculateCoppock(closePrices, rocPeriod1, rocPeriod2, wmaPeriod);
            result.put("coppock", round(coppock));

            // 科波克曲線信號：從負轉正為買入信號
            result.put("coppock_signal", coppock > 0 ? "BULLISH" : "BEARISH");
        }

        return result;
    }

    private double calculateCoppock(double[] prices, int rocPeriod1, int rocPeriod2, int wmaPeriod) {
        int length = prices.length;

        // 計算兩個 ROC 序列的長度
        int minRocPeriod = Math.min(rocPeriod1, rocPeriod2);
        int maxRocPeriod = Math.max(rocPeriod1, rocPeriod2);
        int rocLength = length - maxRocPeriod;

        if (rocLength < wmaPeriod) {
            return 0;
        }

        // 計算 ROC 總和序列
        double[] rocSum = new double[rocLength];
        for (int i = 0; i < rocLength; i++) {
            int idx = i + maxRocPeriod;
            double roc1 = calculateROC(prices, idx, rocPeriod1);
            double roc2 = calculateROC(prices, idx, rocPeriod2);
            rocSum[i] = roc1 + roc2;
        }

        // 計算 WMA
        return calculateWMA(rocSum, wmaPeriod);
    }

    private double calculateROC(double[] prices, int currentIndex, int period) {
        int pastIndex = currentIndex - period;
        if (prices[pastIndex] != 0) {
            return ((prices[currentIndex] - prices[pastIndex]) / prices[pastIndex]) * 100;
        }
        return 0;
    }

    private double calculateWMA(double[] values, int period) {
        int startIndex = values.length - period;
        if (startIndex < 0) startIndex = 0;

        double weightedSum = 0;
        double weightSum = 0;
        int weight = 1;

        for (int i = startIndex; i < values.length; i++) {
            weightedSum += values[i] * weight;
            weightSum += weight;
            weight++;
        }

        return weightSum > 0 ? weightedSum / weightSum : 0;
    }

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
