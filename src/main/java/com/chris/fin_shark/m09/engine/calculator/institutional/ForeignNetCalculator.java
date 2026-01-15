package com.chris.fin_shark.m09.engine.calculator.institutional;

import com.chris.fin_shark.m09.engine.calculator.ChipCalculator;
import com.chris.fin_shark.m09.engine.model.ChipMetadata;
import com.chris.fin_shark.m09.engine.model.ChipSeries;
import com.chris.fin_shark.m09.enums.ChipCategory;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 外資買賣超計算器
 * <p>
 * 計算外資買賣超相關指標：
 * - foreign_net: 當日外資買賣超
 * - foreign_net_ma5: 外資買賣超5日均
 * - foreign_net_ma20: 外資買賣超20日均
 * - foreign_accumulated_5d: 外資5日累計買賣超
 * - foreign_accumulated_20d: 外資20日累計買賣超
 * </p>
 *
 * @author chris
 * @since 1.0.0
 */
@Component
public class ForeignNetCalculator implements ChipCalculator {

    @Override
    public String getName() {
        return "FOREIGN_NET";
    }

    @Override
    public ChipCategory getCategory() {
        return ChipCategory.INSTITUTIONAL;
    }

    @Override
    public ChipMetadata getMetadata() {
        return ChipMetadata.builder()
                .name("FOREIGN_NET")
                .nameZh("外資買賣超")
                .category(ChipCategory.INSTITUTIONAL)
                .description("計算外資買賣超及其移動平均")
                .minDataDays(20)
                .defaultParams(Map.of("ma5", 5, "ma20", 20))
                .priority("P0")
                .build();
    }

    @Override
    public Map<String, Object> calculate(ChipSeries series, Map<String, Object> params) {
        long[] foreignNet = series.getForeignNetArray();

        if (foreignNet.length == 0) {
            return Map.of();
        }

        Map<String, Object> result = new HashMap<>();

        // 當日外資買賣超（最後一筆）
        long latestForeignNet = foreignNet[foreignNet.length - 1];
        result.put("foreign_net", latestForeignNet);

        // 計算移動平均
        if (foreignNet.length >= 5) {
            double ma5 = calculateSMA(foreignNet, 5);
            result.put("foreign_net_ma5", Math.round(ma5 * 100.0) / 100.0);
        }

        if (foreignNet.length >= 20) {
            double ma20 = calculateSMA(foreignNet, 20);
            result.put("foreign_net_ma20", Math.round(ma20 * 100.0) / 100.0);
        }

        // 計算累計買賣超
        if (foreignNet.length >= 5) {
            long accumulated5d = calculateSum(foreignNet, 5);
            result.put("foreign_accumulated_5d", accumulated5d);
        }

        if (foreignNet.length >= 20) {
            long accumulated20d = calculateSum(foreignNet, 20);
            result.put("foreign_accumulated_20d", accumulated20d);
        }

        return result;
    }

    /**
     * 計算簡單移動平均
     */
    private double calculateSMA(long[] data, int period) {
        if (data.length < period) {
            return 0;
        }

        double sum = 0;
        int start = data.length - period;
        for (int i = start; i < data.length; i++) {
            sum += data[i];
        }
        return sum / period;
    }

    /**
     * 計算累計值
     */
    private long calculateSum(long[] data, int period) {
        if (data.length < period) {
            return 0;
        }

        long sum = 0;
        int start = data.length - period;
        for (int i = start; i < data.length; i++) {
            sum += data[i];
        }
        return sum;
    }
}
