package com.chris.fin_shark.m07.engine.calculator.trend;

import com.chris.fin_shark.m07.engine.calculator.IndicatorCalculator;
import com.chris.fin_shark.m07.engine.model.IndicatorMetadata;
import com.chris.fin_shark.m07.engine.model.PriceSeries;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Parabolic SAR（拋物線停損轉向指標）計算器
 * <p>
 * 計算公式：
 * SAR(t) = SAR(t-1) + AF * (EP - SAR(t-1))
 * - AF: 加速因子，從 afStart 開始，每次新高/新低增加 afStep，最大值 afMax
 * - EP: 極值點，上升趨勢中為最高價，下降趨勢中為最低價
 * </p>
 * <p>
 * TODO: [待確認] Parabolic SAR 的初始化方式有多種：
 * - 有些版本使用前 N 天的最高/最低價作為初始 EP
 * - 有些版本使用固定的起始方向
 * - SAR 不能超過前兩天的最低/最高價的規則實現也有差異
 * 目前採用 Wilder 原始定義的標準實現
 * </p>
 *
 * @author chris
 * @since 1.0.0
 */
@Component
public class ParabolicSARCalculator implements IndicatorCalculator {

    @Override
    public String getName() {
        return "SAR";
    }

    @Override
    public String getCategory() {
        return "TREND";
    }

    @Override
    public IndicatorMetadata getMetadata() {
        return IndicatorMetadata.builder()
                .name("SAR")
                .category("TREND")
                .nameZh("拋物線停損轉向")
                .description("追蹤趨勢的停損點與反轉信號")
                .minDataPoints(5)
                .defaultParams(Map.of(
                        "afStart", 0.02,
                        "afStep", 0.02,
                        "afMax", 0.2
                ))
                .priority("P1")
                .build();
    }

    @Override
    public Map<String, Object> calculate(PriceSeries series, Map<String, Object> params) {
        double afStart = ((Number) params.getOrDefault("afStart", 0.02)).doubleValue();
        double afStep = ((Number) params.getOrDefault("afStep", 0.02)).doubleValue();
        double afMax = ((Number) params.getOrDefault("afMax", 0.2)).doubleValue();

        double[] highPrices = series.getHighArray();
        double[] lowPrices = series.getLowArray();
        double[] closePrices = series.getCloseArray();

        if (highPrices.length < 5) {
            return Map.of();
        }

        // 計算 SAR 序列
        double[] sarValues = calculateSARSeries(highPrices, lowPrices, afStart, afStep, afMax);

        // 取最新值
        int lastIndex = sarValues.length - 1;
        double sar = sarValues[lastIndex];
        double currentClose = closePrices[closePrices.length - 1];

        // 判斷趨勢
        boolean isUptrend = currentClose > sar;

        Map<String, Object> result = new HashMap<>();
        result.put("sar", round(sar));
        result.put("sar_trend", isUptrend ? "UPTREND" : "DOWNTREND");
        result.put("sar_signal", getSignal(sarValues, closePrices));

        return result;
    }

    /**
     * 計算 SAR 序列
     */
    private double[] calculateSARSeries(double[] high, double[] low,
                                         double afStart, double afStep, double afMax) {
        int length = high.length;
        double[] sar = new double[length];
        boolean[] isUptrend = new boolean[length];

        // 初始化：假設從第 2 天開始，判斷初始趨勢
        // 如果第 1 天收盤價高於第 0 天，假設上升趨勢
        isUptrend[0] = high[1] > high[0];
        isUptrend[1] = isUptrend[0];

        double af = afStart;
        double ep;  // 極值點

        if (isUptrend[0]) {
            sar[0] = low[0];
            sar[1] = low[0];
            ep = high[1];
        } else {
            sar[0] = high[0];
            sar[1] = high[0];
            ep = low[1];
        }

        // 計算後續 SAR
        for (int i = 2; i < length; i++) {
            // 計算新的 SAR
            sar[i] = sar[i - 1] + af * (ep - sar[i - 1]);

            // 檢查是否需要反轉
            if (isUptrend[i - 1]) {
                // 上升趨勢中，SAR 不能高於前兩天的最低價
                sar[i] = Math.min(sar[i], Math.min(low[i - 1], low[i - 2]));

                // 檢查是否被觸及（價格跌破 SAR）
                if (low[i] < sar[i]) {
                    // 反轉為下降趨勢
                    isUptrend[i] = false;
                    sar[i] = ep;  // SAR 設為之前的極值點
                    ep = low[i];
                    af = afStart;
                } else {
                    isUptrend[i] = true;
                    // 更新極值點
                    if (high[i] > ep) {
                        ep = high[i];
                        af = Math.min(af + afStep, afMax);
                    }
                }
            } else {
                // 下降趨勢中，SAR 不能低於前兩天的最高價
                sar[i] = Math.max(sar[i], Math.max(high[i - 1], high[i - 2]));

                // 檢查是否被觸及（價格突破 SAR）
                if (high[i] > sar[i]) {
                    // 反轉為上升趨勢
                    isUptrend[i] = true;
                    sar[i] = ep;  // SAR 設為之前的極值點
                    ep = high[i];
                    af = afStart;
                } else {
                    isUptrend[i] = false;
                    // 更新極值點
                    if (low[i] < ep) {
                        ep = low[i];
                        af = Math.min(af + afStep, afMax);
                    }
                }
            }
        }

        return sar;
    }

    /**
     * 判斷信號（趨勢反轉）
     */
    private String getSignal(double[] sar, double[] close) {
        if (sar.length < 2 || close.length < 2) {
            return "NEUTRAL";
        }

        int last = sar.length - 1;
        boolean currentAbove = close[last] > sar[last];
        boolean prevAbove = close[last - 1] > sar[last - 1];

        if (currentAbove && !prevAbove) {
            return "BUY";  // 向上突破
        } else if (!currentAbove && prevAbove) {
            return "SELL";  // 向下跌破
        } else {
            return "HOLD";
        }
    }

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
