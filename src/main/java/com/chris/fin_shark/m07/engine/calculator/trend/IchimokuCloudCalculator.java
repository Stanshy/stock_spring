package com.chris.fin_shark.m07.engine.calculator.trend;

import com.chris.fin_shark.m07.engine.calculator.IndicatorCalculator;
import com.chris.fin_shark.m07.engine.model.IndicatorMetadata;
import com.chris.fin_shark.m07.engine.model.PriceSeries;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Ichimoku Cloud（一目均衡表）計算器
 * <p>
 * 計算公式：
 * Tenkan-sen (轉換線) = (Highest High + Lowest Low) / 2 over 9 periods
 * Kijun-sen (基準線) = (Highest High + Lowest Low) / 2 over 26 periods
 * Senkou Span A (先行帶 A) = (Tenkan-sen + Kijun-sen) / 2, plotted 26 periods ahead
 * Senkou Span B (先行帶 B) = (Highest High + Lowest Low) / 2 over 52 periods, plotted 26 periods ahead
 * Chikou Span (遲行帶) = Close plotted 26 periods back
 * </p>
 * <p>
 * TODO: 參數可依市場調整
 * - 日本傳統: 9, 26, 52 (基於舊日本股市每週 6 天交易)
 * - 現代調整: 可考慮 7, 22, 44 或 10, 30, 60
 * </p>
 * <p>
 * TODO: 雲帶信號解讀可能因策略不同而有差異
 * </p>
 *
 * @author chris
 * @since 1.0.0
 */
@Component
public class IchimokuCloudCalculator implements IndicatorCalculator {

    @Override
    public String getName() {
        return "ICHIMOKU";
    }

    @Override
    public String getCategory() {
        return "TREND";
    }

    @Override
    public IndicatorMetadata getMetadata() {
        return IndicatorMetadata.builder()
                .name("ICHIMOKU")
                .category("TREND")
                .nameZh("一目均衡表")
                .description("綜合趨勢分析系統")
                .minDataPoints(60)  // 需要 52 期 + 緩衝
                .defaultParams(Map.of("tenkanPeriod", 9, "kijunPeriod", 26, "senkouBPeriod", 52))
                .priority("P2")
                .build();
    }

    @Override
    public Map<String, Object> calculate(PriceSeries series, Map<String, Object> params) {
        int tenkanPeriod = (int) params.getOrDefault("tenkanPeriod", 9);
        int kijunPeriod = (int) params.getOrDefault("kijunPeriod", 26);
        int senkouBPeriod = (int) params.getOrDefault("senkouBPeriod", 52);

        double[] highPrices = series.getHighArray();
        double[] lowPrices = series.getLowArray();
        double[] closePrices = series.getCloseArray();

        Map<String, Object> result = new HashMap<>();

        if (closePrices.length < senkouBPeriod) {
            return result;
        }

        int length = closePrices.length;

        // 計算轉換線 (Tenkan-sen)
        double tenkan = calculateMidpoint(highPrices, lowPrices, tenkanPeriod);
        result.put("tenkan_sen", round(tenkan));

        // 計算基準線 (Kijun-sen)
        double kijun = calculateMidpoint(highPrices, lowPrices, kijunPeriod);
        result.put("kijun_sen", round(kijun));

        // 計算先行帶 A (Senkou Span A) - 當前值，實際使用需位移 26 期
        double senkouA = (tenkan + kijun) / 2;
        result.put("senkou_span_a", round(senkouA));

        // 計算先行帶 B (Senkou Span B) - 當前值，實際使用需位移 26 期
        double senkouB = calculateMidpoint(highPrices, lowPrices, senkouBPeriod);
        result.put("senkou_span_b", round(senkouB));

        // 遲行帶 (Chikou Span) = 當前收盤價 (位移在視覺化時處理)
        result.put("chikou_span", round(closePrices[length - 1]));

        // 雲帶狀態和信號
        double currentPrice = closePrices[length - 1];
        result.put("cloud_status", getCloudStatus(currentPrice, senkouA, senkouB));
        result.put("tk_cross_signal", getTKCrossSignal(tenkan, kijun));

        return result;
    }

    private double calculateMidpoint(double[] highs, double[] lows, int period) {
        int length = highs.length;
        int startIndex = length - period;

        double highestHigh = Double.MIN_VALUE;
        double lowestLow = Double.MAX_VALUE;

        for (int i = startIndex; i < length; i++) {
            highestHigh = Math.max(highestHigh, highs[i]);
            lowestLow = Math.min(lowestLow, lows[i]);
        }

        return (highestHigh + lowestLow) / 2;
    }

    /**
     * TODO: 雲帶狀態判斷可能需要更細緻的邏輯
     * 目前僅判斷價格與雲帶的位置關係
     */
    private String getCloudStatus(double price, double senkouA, double senkouB) {
        double cloudTop = Math.max(senkouA, senkouB);
        double cloudBottom = Math.min(senkouA, senkouB);

        if (price > cloudTop) {
            return "ABOVE_CLOUD";  // 強勢
        } else if (price < cloudBottom) {
            return "BELOW_CLOUD";  // 弱勢
        }
        return "IN_CLOUD";  // 盤整
    }

    /**
     * TODO: TK 交叉信號可搭配其他條件過濾
     */
    private String getTKCrossSignal(double tenkan, double kijun) {
        if (tenkan > kijun) {
            return "BULLISH";  // 轉換線在基準線之上
        } else if (tenkan < kijun) {
            return "BEARISH";  // 轉換線在基準線之下
        }
        return "NEUTRAL";
    }

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
