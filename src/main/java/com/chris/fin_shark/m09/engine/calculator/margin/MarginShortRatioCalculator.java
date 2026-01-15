package com.chris.fin_shark.m09.engine.calculator.margin;

import com.chris.fin_shark.m09.engine.calculator.ChipCalculator;
import com.chris.fin_shark.m09.engine.model.ChipMetadata;
import com.chris.fin_shark.m09.engine.model.ChipSeries;
import com.chris.fin_shark.m09.enums.ChipCategory;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 券資比計算器
 * <p>
 * 計算券資比相關指標：
 * - margin_short_ratio: 券資比 = (融券餘額 / 融資餘額) * 100
 * - margin_short_ratio_signal: 券資比訊號（HIGH/NORMAL/LOW）
 * </p>
 * <p>
 * 券資比 > 30% 通常被視為高券資比，表示融券壓力大。
 * </p>
 *
 * @author chris
 * @since 1.0.0
 */
@Component
public class MarginShortRatioCalculator implements ChipCalculator {

    private static final double HIGH_RATIO_THRESHOLD = 30.0;
    private static final double LOW_RATIO_THRESHOLD = 5.0;

    @Override
    public String getName() {
        return "MARGIN_SHORT_RATIO";
    }

    @Override
    public ChipCategory getCategory() {
        return ChipCategory.MARGIN;
    }

    @Override
    public ChipMetadata getMetadata() {
        return ChipMetadata.builder()
                .name("MARGIN_SHORT_RATIO")
                .nameZh("券資比")
                .category(ChipCategory.MARGIN)
                .description("計算融券餘額與融資餘額的比值")
                .minDataDays(1)
                .defaultParams(Map.of("high_threshold", HIGH_RATIO_THRESHOLD))
                .priority("P0")
                .build();
    }

    @Override
    public Map<String, Object> calculate(ChipSeries series, Map<String, Object> params) {
        long[] marginBalance = series.getMarginBalanceArray();
        long[] shortBalance = series.getShortBalanceArray();

        if (marginBalance.length == 0 || shortBalance.length == 0) {
            return Map.of();
        }

        Map<String, Object> result = new HashMap<>();

        int lastIndex = Math.min(marginBalance.length, shortBalance.length) - 1;

        long margin = marginBalance[lastIndex];
        long shortBal = shortBalance[lastIndex];

        // 計算券資比
        double ratio = 0.0;
        if (margin > 0) {
            ratio = ((double) shortBal / margin) * 100;
            ratio = Math.round(ratio * 100.0) / 100.0;
        }

        result.put("margin_short_ratio", ratio);

        // 判斷訊號
        String signal;
        if (ratio >= HIGH_RATIO_THRESHOLD) {
            signal = "HIGH";      // 高券資比，融券壓力大
        } else if (ratio <= LOW_RATIO_THRESHOLD) {
            signal = "LOW";       // 低券資比
        } else {
            signal = "NORMAL";    // 正常範圍
        }
        result.put("margin_short_ratio_signal", signal);

        return result;
    }
}
