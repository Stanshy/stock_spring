package com.chris.fin_shark.m09.engine.calculator.margin;

import com.chris.fin_shark.m09.engine.calculator.ChipCalculator;
import com.chris.fin_shark.m09.engine.model.ChipMetadata;
import com.chris.fin_shark.m09.engine.model.ChipSeries;
import com.chris.fin_shark.m09.enums.ChipCategory;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 融券餘額計算器
 * <p>
 * 計算融券餘額相關指標：
 * - short_balance: 當日融券餘額
 * - short_change: 融券增減
 * - short_usage_rate: 融券使用率
 * - short_continuous_days: 融券連續增加天數
 * </p>
 *
 * @author chris
 * @since 1.0.0
 */
@Component
public class ShortBalanceCalculator implements ChipCalculator {

    @Override
    public String getName() {
        return "SHORT_BALANCE";
    }

    @Override
    public ChipCategory getCategory() {
        return ChipCategory.MARGIN;
    }

    @Override
    public ChipMetadata getMetadata() {
        return ChipMetadata.builder()
                .name("SHORT_BALANCE")
                .nameZh("融券餘額")
                .category(ChipCategory.MARGIN)
                .description("計算融券餘額、增減及使用率")
                .minDataDays(5)
                .defaultParams(Map.of())
                .priority("P0")
                .build();
    }

    @Override
    public Map<String, Object> calculate(ChipSeries series, Map<String, Object> params) {
        long[] shortBalance = series.getShortBalanceArray();
        double[] shortUsageRate = series.getMarginUsageRateArray(); // TODO(decision): 是否需要單獨的 shortUsageRate getter

        if (shortBalance.length == 0) {
            return Map.of();
        }

        Map<String, Object> result = new HashMap<>();

        int lastIndex = shortBalance.length - 1;

        // 當日融券餘額
        long latestBalance = shortBalance[lastIndex];
        result.put("short_balance", latestBalance);

        // 融券增減
        if (shortBalance.length >= 2) {
            long change = shortBalance[lastIndex] - shortBalance[lastIndex - 1];
            result.put("short_change", change);

            // 融券連續增加天數
            long[] changes = series.getShortChangeArray();
            int continuousDays = calculateContinuousDays(changes);
            result.put("short_continuous_days", continuousDays);
        }

        return result;
    }

    private int calculateContinuousDays(long[] changes) {
        if (changes.length == 0) {
            return 0;
        }

        int lastIndex = changes.length - 1;
        long lastValue = changes[lastIndex];

        if (lastValue == 0) {
            return 0;
        }

        boolean isIncreasing = lastValue > 0;
        int count = 1;

        for (int i = lastIndex - 1; i >= 0; i--) {
            long value = changes[i];
            if (isIncreasing && value > 0) {
                count++;
            } else if (!isIncreasing && value < 0) {
                count++;
            } else {
                break;
            }
        }

        return isIncreasing ? count : -count;
    }
}
