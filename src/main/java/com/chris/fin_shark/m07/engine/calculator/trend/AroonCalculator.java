package com.chris.fin_shark.m07.engine.calculator.trend;

import com.chris.fin_shark.m07.engine.calculator.IndicatorCalculator;
import com.chris.fin_shark.m07.engine.model.IndicatorMetadata;
import com.chris.fin_shark.m07.engine.model.PriceSeries;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Aroon（阿隆指標）計算器
 * <p>
 * 計算公式：
 * Aroon Up = ((period - 距離最高價天數) / period) * 100
 * Aroon Down = ((period - 距離最低價天數) / period) * 100
 * Aroon Oscillator = Aroon Up - Aroon Down
 * </p>
 * <p>
 * 解讀：
 * - Aroon Up > 70 且 Aroon Down < 30：強勢上漲
 * - Aroon Down > 70 且 Aroon Up < 30：強勢下跌
 * - 兩者交叉可能預示趨勢反轉
 * </p>
 *
 * @author chris
 * @since 1.0.0
 */
@Component
public class AroonCalculator implements IndicatorCalculator {

    @Override
    public String getName() {
        return "AROON";
    }

    @Override
    public String getCategory() {
        return "TREND";
    }

    @Override
    public IndicatorMetadata getMetadata() {
        return IndicatorMetadata.builder()
                .name("AROON")
                .category("TREND")
                .nameZh("阿隆指標")
                .description("衡量趨勢強度與方向的指標")
                .minDataPoints(26)  // period + 1
                .defaultParams(Map.of("period", 25))
                .priority("P1")
                .build();
    }

    @Override
    public Map<String, Object> calculate(PriceSeries series, Map<String, Object> params) {
        int period = (int) params.getOrDefault("period", 25);

        double[] highPrices = series.getHighArray();
        double[] lowPrices = series.getLowArray();

        if (highPrices.length < period + 1 || lowPrices.length < period + 1) {
            return Map.of();
        }

        // 找出最近 period+1 天內的最高價和最低價位置
        int highestIndex = 0;
        int lowestIndex = 0;
        double highestPrice = Double.MIN_VALUE;
        double lowestPrice = Double.MAX_VALUE;

        int startIndex = highPrices.length - period - 1;
        for (int i = 0; i <= period; i++) {
            int idx = startIndex + i;
            if (highPrices[idx] >= highestPrice) {  // >= 確保取最近的
                highestPrice = highPrices[idx];
                highestIndex = i;
            }
            if (lowPrices[idx] <= lowestPrice) {  // <= 確保取最近的
                lowestPrice = lowPrices[idx];
                lowestIndex = i;
            }
        }

        // 計算距離最高/最低價的天數（從最近一天算起）
        int daysSinceHigh = period - highestIndex;
        int daysSinceLow = period - lowestIndex;

        // 計算 Aroon 指標
        double aroonUp = ((double) (period - daysSinceHigh) / period) * 100;
        double aroonDown = ((double) (period - daysSinceLow) / period) * 100;
        double aroonOsc = aroonUp - aroonDown;

        Map<String, Object> result = new HashMap<>();
        result.put("aroon_up_" + period, round(aroonUp));
        result.put("aroon_down_" + period, round(aroonDown));
        result.put("aroon_osc_" + period, round(aroonOsc));
        result.put("aroon_signal", getSignal(aroonUp, aroonDown));

        return result;
    }

    private String getSignal(double aroonUp, double aroonDown) {
        if (aroonUp > 70 && aroonDown < 30) {
            return "STRONG_UPTREND";
        } else if (aroonDown > 70 && aroonUp < 30) {
            return "STRONG_DOWNTREND";
        } else if (aroonUp > aroonDown) {
            return "UPTREND";
        } else if (aroonDown > aroonUp) {
            return "DOWNTREND";
        } else {
            return "NEUTRAL";
        }
    }

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
