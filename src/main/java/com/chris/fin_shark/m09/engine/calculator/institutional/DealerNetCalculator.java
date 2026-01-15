package com.chris.fin_shark.m09.engine.calculator.institutional;

import com.chris.fin_shark.m09.engine.calculator.ChipCalculator;
import com.chris.fin_shark.m09.engine.model.ChipMetadata;
import com.chris.fin_shark.m09.engine.model.ChipSeries;
import com.chris.fin_shark.m09.enums.ChipCategory;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 自營商買賣超計算器
 * <p>
 * 計算自營商買賣超及三大法人合計指標：
 * - dealer_net: 當日自營商買賣超
 * - total_net: 三大法人合計買賣超
 * - total_net_ma5: 合計買賣超5日均
 * </p>
 *
 * @author chris
 * @since 1.0.0
 */
@Component
public class DealerNetCalculator implements ChipCalculator {

    @Override
    public String getName() {
        return "DEALER_NET";
    }

    @Override
    public ChipCategory getCategory() {
        return ChipCategory.INSTITUTIONAL;
    }

    @Override
    public ChipMetadata getMetadata() {
        return ChipMetadata.builder()
                .name("DEALER_NET")
                .nameZh("自營商買賣超")
                .category(ChipCategory.INSTITUTIONAL)
                .description("計算自營商買賣超及三大法人合計")
                .minDataDays(5)
                .defaultParams(Map.of())
                .priority("P0")
                .build();
    }

    @Override
    public Map<String, Object> calculate(ChipSeries series, Map<String, Object> params) {
        long[] dealerNet = series.getDealerNetArray();
        long[] totalNet = series.getTotalNetArray();

        if (dealerNet.length == 0) {
            return Map.of();
        }

        Map<String, Object> result = new HashMap<>();

        // 當日自營商買賣超
        long latestDealerNet = dealerNet[dealerNet.length - 1];
        result.put("dealer_net", latestDealerNet);

        // 三大法人合計
        if (totalNet.length > 0) {
            long latestTotalNet = totalNet[totalNet.length - 1];
            result.put("total_net", latestTotalNet);

            // 合計買賣超5日均
            if (totalNet.length >= 5) {
                double ma5 = calculateSMA(totalNet, 5);
                result.put("total_net_ma5", Math.round(ma5 * 100.0) / 100.0);
            }
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
}
