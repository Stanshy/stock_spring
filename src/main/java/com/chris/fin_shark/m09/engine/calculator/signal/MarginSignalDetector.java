package com.chris.fin_shark.m09.engine.calculator.signal;

import com.chris.fin_shark.m09.engine.calculator.ChipCalculator;
import com.chris.fin_shark.m09.engine.model.ChipMetadata;
import com.chris.fin_shark.m09.engine.model.ChipSeries;
import com.chris.fin_shark.m09.engine.model.ChipSignal;
import com.chris.fin_shark.m09.enums.ChipCategory;
import com.chris.fin_shark.m09.enums.SignalSeverity;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 融資融券訊號偵測器
 * <p>
 * 偵測融資融券相關的籌碼異常訊號：
 * - CHIP_SIG_009: 融資暴增（margin_change > avg_20 + 3*std_20）
 * - CHIP_SIG_010: 融資斷頭（margin_change < -10% of margin_balance）
 * - CHIP_SIG_011: 融券大增（short_change > avg_20 + 2*std_20）
 * - CHIP_SIG_012: 券資比過高（margin_short_ratio > 30）
 * - CHIP_SIG_013: 融資使用率過高（margin_usage_rate > 80）
 * </p>
 *
 * @author chris
 * @since 1.0.0
 */
@Component
public class MarginSignalDetector implements ChipCalculator, ChipSignalDetector {

    private static final int LOOKBACK_PERIOD = 20;
    private static final double MARGIN_SURGE_STD_MULTIPLIER = 3.0;
    private static final double SHORT_SURGE_STD_MULTIPLIER = 2.0;
    private static final double MARGIN_CUT_THRESHOLD = -0.10;  // -10%
    private static final double HIGH_SHORT_RATIO = 30.0;
    private static final double HIGH_USAGE_RATE = 80.0;

    @Override
    public String getName() {
        return "MARGIN_SIGNAL";
    }

    @Override
    public ChipCategory getCategory() {
        return ChipCategory.SIGNAL;
    }

    @Override
    public ChipMetadata getMetadata() {
        return ChipMetadata.builder()
                .name("MARGIN_SIGNAL")
                .nameZh("融資融券訊號偵測")
                .category(ChipCategory.SIGNAL)
                .description("偵測融資融券相關的籌碼異常訊號")
                .minDataDays(LOOKBACK_PERIOD)
                .defaultParams(Map.of("lookback", LOOKBACK_PERIOD))
                .priority("P0")
                .build();
    }

    @Override
    public Map<String, Object> calculate(ChipSeries series, Map<String, Object> params) {
        List<ChipSignal> signals = detect(series, params);

        Map<String, Object> result = new HashMap<>();
        result.put("margin_signals", signals);
        result.put("margin_signal_count", signals.size());

        return result;
    }

    @Override
    public List<ChipSignal> detect(ChipSeries series, Map<String, Object> params) {
        List<ChipSignal> signals = new ArrayList<>();

        long[] marginBalance = series.getMarginBalanceArray();
        long[] shortBalance = series.getShortBalanceArray();
        double[] marginUsageRate = series.getMarginUsageRateArray();

        if (marginBalance.length < 2) {
            return signals;
        }

        int lastIndex = marginBalance.length - 1;
        long latestMarginBalance = marginBalance[lastIndex];
        long marginChange = marginBalance[lastIndex] - marginBalance[lastIndex - 1];

        // 計算融資增減的統計數據
        long[] marginChanges = series.getMarginChangeArray();
        double marginChangeAvg = 0;
        double marginChangeStd = 0;

        if (marginChanges.length >= LOOKBACK_PERIOD) {
            marginChangeAvg = calculateAverage(marginChanges, LOOKBACK_PERIOD);
            marginChangeStd = calculateStdDev(marginChanges, LOOKBACK_PERIOD, marginChangeAvg);
        }

        // CHIP_SIG_009: 融資暴增
        if (marginChanges.length >= LOOKBACK_PERIOD) {
            double surgeTreshold = marginChangeAvg + MARGIN_SURGE_STD_MULTIPLIER * marginChangeStd;
            if (marginChange > surgeTreshold && surgeTreshold > 0) {
                signals.add(ChipSignal.marginSignal(
                        "CHIP_SIG_009",
                        "融資暴增",
                        SignalSeverity.HIGH,
                        BigDecimal.valueOf(marginChange),
                        BigDecimal.valueOf(surgeTreshold),
                        String.format("融資增加 %,d 股，超過 20 日平均 3 個標準差", marginChange)
                ));
            }
        }

        // CHIP_SIG_010: 融資斷頭
        if (latestMarginBalance > 0) {
            double changeRatio = (double) marginChange / marginBalance[lastIndex - 1];
            if (changeRatio < MARGIN_CUT_THRESHOLD) {
                signals.add(ChipSignal.marginSignal(
                        "CHIP_SIG_010",
                        "融資斷頭",
                        SignalSeverity.CRITICAL,
                        BigDecimal.valueOf(changeRatio * 100),
                        BigDecimal.valueOf(MARGIN_CUT_THRESHOLD * 100),
                        String.format("融資減少 %.1f%%，超過 10%% 斷頭門檻", Math.abs(changeRatio * 100))
                ));
            }
        }

        // CHIP_SIG_011: 融券大增
        if (shortBalance.length >= LOOKBACK_PERIOD + 1) {
            long[] shortChanges = series.getShortChangeArray();
            double shortChangeAvg = calculateAverage(shortChanges, LOOKBACK_PERIOD);
            double shortChangeStd = calculateStdDev(shortChanges, LOOKBACK_PERIOD, shortChangeAvg);

            long shortChange = shortBalance[lastIndex] - shortBalance[lastIndex - 1];
            double shortSurgeThreshold = shortChangeAvg + SHORT_SURGE_STD_MULTIPLIER * shortChangeStd;

            if (shortChange > shortSurgeThreshold && shortSurgeThreshold > 0) {
                signals.add(ChipSignal.marginSignal(
                        "CHIP_SIG_011",
                        "融券大增",
                        SignalSeverity.MEDIUM,
                        BigDecimal.valueOf(shortChange),
                        BigDecimal.valueOf(shortSurgeThreshold),
                        String.format("融券增加 %,d 股，超過 20 日平均 2 個標準差", shortChange)
                ));
            }
        }

        // CHIP_SIG_012: 券資比過高
        if (shortBalance.length > 0 && latestMarginBalance > 0) {
            long latestShortBalance = shortBalance[Math.min(lastIndex, shortBalance.length - 1)];
            double ratio = ((double) latestShortBalance / latestMarginBalance) * 100;

            if (ratio > HIGH_SHORT_RATIO) {
                signals.add(ChipSignal.marginSignal(
                        "CHIP_SIG_012",
                        "券資比過高",
                        SignalSeverity.HIGH,
                        BigDecimal.valueOf(ratio),
                        BigDecimal.valueOf(HIGH_SHORT_RATIO),
                        String.format("券資比達 %.1f%%，超過 30%% 警戒線", ratio)
                ));
            }
        }

        // CHIP_SIG_013: 融資使用率過高
        if (marginUsageRate.length > 0) {
            double latestUsageRate = marginUsageRate[marginUsageRate.length - 1];

            if (latestUsageRate > HIGH_USAGE_RATE) {
                signals.add(ChipSignal.marginSignal(
                        "CHIP_SIG_013",
                        "融資使用率過高",
                        SignalSeverity.HIGH,
                        BigDecimal.valueOf(latestUsageRate),
                        BigDecimal.valueOf(HIGH_USAGE_RATE),
                        String.format("融資使用率達 %.1f%%，超過 80%% 警戒線", latestUsageRate)
                ));
            }
        }

        return signals;
    }

    private double calculateAverage(long[] data, int period) {
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

    private double calculateStdDev(long[] data, int period, double avg) {
        if (data.length < period) {
            return 0;
        }
        double sumSquares = 0;
        int start = data.length - period;
        for (int i = start; i < data.length; i++) {
            double diff = data[i] - avg;
            sumSquares += diff * diff;
        }
        return Math.sqrt(sumSquares / period);
    }
}
