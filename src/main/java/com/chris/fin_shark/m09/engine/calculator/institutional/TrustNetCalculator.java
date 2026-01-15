package com.chris.fin_shark.m09.engine.calculator.institutional;

import com.chris.fin_shark.m09.engine.calculator.ChipCalculator;
import com.chris.fin_shark.m09.engine.model.ChipMetadata;
import com.chris.fin_shark.m09.engine.model.ChipSeries;
import com.chris.fin_shark.m09.enums.ChipCategory;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 投信買賣超計算器
 * <p>
 * 計算投信買賣超相關指標：
 * - trust_net: 當日投信買賣超
 * - trust_net_ma5: 投信買賣超5日均
 * - trust_net_ma20: 投信買賣超20日均
 * - trust_accumulated_5d: 投信5日累計買賣超
 * </p>
 *
 * @author chris
 * @since 1.0.0
 */
@Component
public class TrustNetCalculator implements ChipCalculator {

    @Override
    public String getName() {
        return "TRUST_NET";
    }

    @Override
    public ChipCategory getCategory() {
        return ChipCategory.INSTITUTIONAL;
    }

    @Override
    public ChipMetadata getMetadata() {
        return ChipMetadata.builder()
                .name("TRUST_NET")
                .nameZh("投信買賣超")
                .category(ChipCategory.INSTITUTIONAL)
                .description("計算投信買賣超及其移動平均")
                .minDataDays(20)
                .defaultParams(Map.of("ma5", 5, "ma20", 20))
                .priority("P0")
                .build();
    }

    @Override
    public Map<String, Object> calculate(ChipSeries series, Map<String, Object> params) {
        long[] trustNet = series.getTrustNetArray();

        if (trustNet.length == 0) {
            return Map.of();
        }

        Map<String, Object> result = new HashMap<>();

        // 當日投信買賣超（最後一筆）
        long latestTrustNet = trustNet[trustNet.length - 1];
        result.put("trust_net", latestTrustNet);

        // 計算移動平均
        if (trustNet.length >= 5) {
            double ma5 = calculateSMA(trustNet, 5);
            result.put("trust_net_ma5", Math.round(ma5 * 100.0) / 100.0);
        }

        if (trustNet.length >= 20) {
            double ma20 = calculateSMA(trustNet, 20);
            result.put("trust_net_ma20", Math.round(ma20 * 100.0) / 100.0);
        }

        // 計算累計買賣超
        if (trustNet.length >= 5) {
            long accumulated5d = calculateSum(trustNet, 5);
            result.put("trust_accumulated_5d", accumulated5d);
        }

        return result;
    }

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
