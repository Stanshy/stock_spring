package com.chris.fin_shark.m09.engine.calculator.margin;

import com.chris.fin_shark.m09.engine.calculator.ChipCalculator;
import com.chris.fin_shark.m09.engine.model.ChipMetadata;
import com.chris.fin_shark.m09.engine.model.ChipSeries;
import com.chris.fin_shark.m09.enums.ChipCategory;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 融資餘額計算器
 * <p>
 * 計算融資餘額相關指標：
 * - margin_balance: 當日融資餘額
 * - margin_change: 融資增減
 * - margin_usage_rate: 融資使用率
 * - margin_change_ma5: 融資增減5日均
 * - margin_continuous_days: 融資連續增加天數
 * </p>
 *
 * @author chris
 * @since 1.0.0
 */
@Component
public class MarginBalanceCalculator implements ChipCalculator {

    @Override
    public String getName() {
        return "MARGIN_BALANCE";
    }

    @Override
    public ChipCategory getCategory() {
        return ChipCategory.MARGIN;
    }

    @Override
    public ChipMetadata getMetadata() {
        return ChipMetadata.builder()
                .name("MARGIN_BALANCE")
                .nameZh("融資餘額")
                .category(ChipCategory.MARGIN)
                .description("計算融資餘額、增減及使用率")
                .minDataDays(5)
                .defaultParams(Map.of())
                .priority("P0")
                .build();
    }

    @Override
    public Map<String, Object> calculate(ChipSeries series, Map<String, Object> params) {
        long[] marginBalance = series.getMarginBalanceArray();
        double[] marginUsageRate = series.getMarginUsageRateArray();

        if (marginBalance.length == 0) {
            return Map.of();
        }

        Map<String, Object> result = new HashMap<>();

        int lastIndex = marginBalance.length - 1;

        // 當日融資餘額
        long latestBalance = marginBalance[lastIndex];
        result.put("margin_balance", latestBalance);

        // 融資增減
        if (marginBalance.length >= 2) {
            long change = marginBalance[lastIndex] - marginBalance[lastIndex - 1];
            result.put("margin_change", change);

            // 融資增減5日均
            long[] changes = series.getMarginChangeArray();
            if (changes.length >= 5) {
                double ma5 = calculateSMA(changes, 5);
                result.put("margin_change_ma5", Math.round(ma5 * 100.0) / 100.0);
            }

            // 融資連續增加天數
            int continuousDays = calculateContinuousDays(changes);
            result.put("margin_continuous_days", continuousDays);
        }

        // 融資使用率
        if (marginUsageRate.length > 0) {
            double usageRate = marginUsageRate[marginUsageRate.length - 1];
            result.put("margin_usage_rate", Math.round(usageRate * 100.0) / 100.0);

            // 融資使用率20日均
            if (marginUsageRate.length >= 20) {
                double ma20 = calculateSMADouble(marginUsageRate, 20);
                result.put("margin_usage_rate_ma20", Math.round(ma20 * 100.0) / 100.0);
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

    private double calculateSMADouble(double[] data, int period) {
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
