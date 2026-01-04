package com.chris.fin_shark.m07.engine.calculator.trend;

import com.chris.fin_shark.m07.engine.calculator.IndicatorCalculator;
import com.chris.fin_shark.m07.engine.model.IndicatorMetadata;
import com.chris.fin_shark.m07.engine.model.PriceSeries;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 簡單移動平均（MA）計算器
 * <p>
 * 計算公式：MA(n) = (Price[1] + Price[2] + ... + Price[n]) / n
 * </p>
 *
 * @author chris
 * @since 1.0.0
 */
@Component
public class MACalculator implements IndicatorCalculator {

    @Override
    public String getName() {
        return "MA";
    }

    @Override
    public String getCategory() {
        return "TREND";
    }

    @Override
    public IndicatorMetadata getMetadata() {
        return IndicatorMetadata.builder()
                .name("MA")
                .category("TREND")
                .nameZh("簡單移動平均")
                .description("計算N天收盤價的平均值")
                .minDataPoints(5)  // 最少需要5天
                .defaultParams(Map.of("periods", List.of(5, 20, 60)))
                .priority("P0")
                .build();
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, Object> calculate(PriceSeries series, Map<String, Object> params) {
        // 取得參數
        List<Integer> periods = (List<Integer>) params.getOrDefault(
                "periods",
                List.of(5, 20, 60)
        );

        // 取得收盤價
        double[] closePrices = series.getCloseArray();

        // 計算每個週期的 MA
        Map<String, Object> result = new HashMap<>();
        for (Integer period : periods) {
            if (closePrices.length >= period) {
                double ma = calculateMA(closePrices, period);
                result.put("ma" + period, ma);
            }
        }

        return result;
    }

    /**
     * 計算移動平均
     *
     * @param prices 價格陣列
     * @param period 週期
     * @return MA 值
     */
    private double calculateMA(double[] prices, int period) {
        if (prices.length < period) {
            throw new IllegalArgumentException(
                    String.format("資料不足：需要%d天，實際%d天", period, prices.length)
            );
        }

        // 計算最近 period 天的平均
        double sum = 0;
        for (int i = prices.length - period; i < prices.length; i++) {
            sum += prices[i];
        }

        return sum / period;
    }
}
