package com.chris.fin_shark.m07.engine.calculator.support;

import com.chris.fin_shark.m07.engine.calculator.IndicatorCalculator;
import com.chris.fin_shark.m07.engine.model.IndicatorMetadata;
import com.chris.fin_shark.m07.engine.model.PriceSeries;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Support/Resistance（支撐阻力自動識別）計算器
 * <p>
 * 識別方法：價格密集區域分析
 * 1. 將價格分到固定寬度的區間
 * 2. 統計每個區間的觸及次數
 * 3. 高頻區間即為支撐/阻力區域
 * </p>
 * <p>
 * TODO: 支撐阻力識別有多種方法：
 * - 價格密集區（此實現）
 * - 局部極值點
 * - 成交量分布（Volume Profile）
 * - 機器學習聚類
 * 不同方法適用於不同場景
 * </p>
 * <p>
 * TODO: 區間寬度的選擇影響結果敏感度
 * </p>
 *
 * @author chris
 * @since 1.0.0
 */
@Component
public class SupportResistanceCalculator implements IndicatorCalculator {

    @Override
    public String getName() {
        return "SR_LEVELS";
    }

    @Override
    public String getCategory() {
        return "SUPPORT";
    }

    @Override
    public IndicatorMetadata getMetadata() {
        return IndicatorMetadata.builder()
                .name("SR_LEVELS")
                .category("SUPPORT")
                .nameZh("支撐阻力水平")
                .description("自動識別支撐與阻力區域")
                .minDataPoints(60)
                .defaultParams(Map.of("lookbackPeriod", 60, "numLevels", 3))
                .priority("P2")
                .build();
    }

    @Override
    public Map<String, Object> calculate(PriceSeries series, Map<String, Object> params) {
        int lookbackPeriod = (int) params.getOrDefault("lookbackPeriod", 60);
        int numLevels = (int) params.getOrDefault("numLevels", 3);

        double[] highPrices = series.getHighArray();
        double[] lowPrices = series.getLowArray();
        double[] closePrices = series.getCloseArray();

        Map<String, Object> result = new HashMap<>();

        int length = closePrices.length;
        if (length < lookbackPeriod) {
            return result;
        }

        int startIndex = length - lookbackPeriod;

        // 找出價格範圍
        double minPrice = Double.MAX_VALUE;
        double maxPrice = Double.MIN_VALUE;

        for (int i = startIndex; i < length; i++) {
            minPrice = Math.min(minPrice, lowPrices[i]);
            maxPrice = Math.max(maxPrice, highPrices[i]);
        }

        double priceRange = maxPrice - minPrice;
        if (priceRange == 0) {
            return result;
        }

        // 將價格分到區間（約 20 個區間）
        int numBins = 20;
        double binWidth = priceRange / numBins;
        int[] binCounts = new int[numBins];

        // 統計每個區間的觸及次數
        for (int i = startIndex; i < length; i++) {
            // 高點
            int highBin = getBinIndex(highPrices[i], minPrice, binWidth, numBins);
            binCounts[highBin]++;

            // 低點
            int lowBin = getBinIndex(lowPrices[i], minPrice, binWidth, numBins);
            binCounts[lowBin]++;

            // 收盤價
            int closeBin = getBinIndex(closePrices[i], minPrice, binWidth, numBins);
            binCounts[closeBin]++;
        }

        // 找出高頻區間
        List<double[]> levels = new ArrayList<>();
        for (int i = 0; i < numBins; i++) {
            if (binCounts[i] >= lookbackPeriod * 0.1) {  // 至少 10% 的觸及率
                double levelPrice = minPrice + (i + 0.5) * binWidth;
                levels.add(new double[]{levelPrice, binCounts[i]});
            }
        }

        // 按觸及次數排序
        levels.sort((a, b) -> Double.compare(b[1], a[1]));

        // 取前 N 個水平
        double currentPrice = closePrices[length - 1];
        List<Double> supportLevels = new ArrayList<>();
        List<Double> resistanceLevels = new ArrayList<>();

        for (double[] level : levels) {
            double price = level[0];
            if (price < currentPrice && supportLevels.size() < numLevels) {
                supportLevels.add(price);
            } else if (price > currentPrice && resistanceLevels.size() < numLevels) {
                resistanceLevels.add(price);
            }

            if (supportLevels.size() >= numLevels && resistanceLevels.size() >= numLevels) {
                break;
            }
        }

        // 輸出結果
        result.put("current_price", round(currentPrice));

        for (int i = 0; i < supportLevels.size(); i++) {
            result.put("support_" + (i + 1), round(supportLevels.get(i)));
        }

        for (int i = 0; i < resistanceLevels.size(); i++) {
            result.put("resistance_" + (i + 1), round(resistanceLevels.get(i)));
        }

        // 最近的支撐和阻力
        if (!supportLevels.isEmpty()) {
            double nearestSupport = supportLevels.get(0);
            result.put("nearest_support", round(nearestSupport));
            result.put("support_distance_pct", round((currentPrice - nearestSupport) / currentPrice * 100));
        }

        if (!resistanceLevels.isEmpty()) {
            double nearestResistance = resistanceLevels.get(0);
            result.put("nearest_resistance", round(nearestResistance));
            result.put("resistance_distance_pct", round((nearestResistance - currentPrice) / currentPrice * 100));
        }

        return result;
    }

    private int getBinIndex(double price, double minPrice, double binWidth, int numBins) {
        int bin = (int) ((price - minPrice) / binWidth);
        return Math.max(0, Math.min(numBins - 1, bin));
    }

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
