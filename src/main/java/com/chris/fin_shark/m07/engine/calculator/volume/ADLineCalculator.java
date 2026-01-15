package com.chris.fin_shark.m07.engine.calculator.volume;

import com.chris.fin_shark.m07.engine.calculator.IndicatorCalculator;
import com.chris.fin_shark.m07.engine.model.IndicatorMetadata;
import com.chris.fin_shark.m07.engine.model.PriceSeries;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * AD Line（累積/派發線）計算器
 * <p>
 * 計算公式：
 * 1. CLV（Close Location Value）= ((Close - Low) - (High - Close)) / (High - Low)
 * 2. AD = 前日 AD + CLV * Volume
 * </p>
 * <p>
 * 解讀：
 * - AD 上升：買盤力道強
 * - AD 下降：賣盤力道強
 * - AD 與價格背離可能預示趨勢反轉
 * </p>
 *
 * @author chris
 * @since 1.0.0
 */
@Component
public class ADLineCalculator implements IndicatorCalculator {

    @Override
    public String getName() {
        return "AD";
    }

    @Override
    public String getCategory() {
        return "VOLUME";
    }

    @Override
    public IndicatorMetadata getMetadata() {
        return IndicatorMetadata.builder()
                .name("AD")
                .category("VOLUME")
                .nameZh("累積/派發線")
                .description("衡量資金流入流出的成交量指標")
                .minDataPoints(2)
                .defaultParams(Map.of())
                .priority("P1")
                .build();
    }

    @Override
    public Map<String, Object> calculate(PriceSeries series, Map<String, Object> params) {
        double[] highPrices = series.getHighArray();
        double[] lowPrices = series.getLowArray();
        double[] closePrices = series.getCloseArray();
        long[] volumes = series.getVolumeArray();

        if (closePrices.length < 2 || volumes.length < 2) {
            return Map.of();
        }

        // 計算 AD 線
        double[] adLine = calculateADLine(highPrices, lowPrices, closePrices, volumes);

        // 取最新值和前一日值
        int lastIndex = adLine.length - 1;
        double currentAD = adLine[lastIndex];
        double prevAD = adLine[lastIndex - 1];

        // 計算 AD 的變化率
        double adChange = currentAD - prevAD;

        Map<String, Object> result = new HashMap<>();
        result.put("ad", round(currentAD));
        result.put("ad_change", round(adChange));
        result.put("ad_signal", getSignal(adChange, closePrices));

        return result;
    }

    /**
     * 計算 AD 線序列
     */
    private double[] calculateADLine(double[] high, double[] low, double[] close, long[] volume) {
        int length = high.length;
        double[] ad = new double[length];

        ad[0] = calculateCLV(high[0], low[0], close[0]) * volume[0];

        for (int i = 1; i < length; i++) {
            double clv = calculateCLV(high[i], low[i], close[i]);
            ad[i] = ad[i - 1] + clv * volume[i];
        }

        return ad;
    }

    /**
     * 計算 CLV（Close Location Value）
     */
    private double calculateCLV(double high, double low, double close) {
        double range = high - low;
        if (range == 0) {
            return 0;  // 避免除以零
        }
        return ((close - low) - (high - close)) / range;
    }

    private String getSignal(double adChange, double[] closePrices) {
        // 檢查價格變化
        int last = closePrices.length - 1;
        double priceChange = closePrices[last] - closePrices[last - 1];

        // 檢查背離
        if (priceChange > 0 && adChange < 0) {
            return "BEARISH_DIVERGENCE";  // 價漲量縮，負背離
        } else if (priceChange < 0 && adChange > 0) {
            return "BULLISH_DIVERGENCE";  // 價跌量增，正背離
        } else if (adChange > 0) {
            return "ACCUMULATION";  // 累積
        } else if (adChange < 0) {
            return "DISTRIBUTION";  // 派發
        } else {
            return "NEUTRAL";
        }
    }

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
