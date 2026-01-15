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
 * 法人籌碼訊號偵測器
 * <p>
 * 偵測三大法人相關的籌碼異常訊號：
 * - CHIP_SIG_001: 外資大買（foreign_net > avg_20 + 2*std_20）
 * - CHIP_SIG_002: 外資大賣（foreign_net < avg_20 - 2*std_20）
 * - CHIP_SIG_003: 外資連續買超（foreign_continuous_days >= 5）
 * - CHIP_SIG_004: 外資連續賣超（foreign_continuous_days <= -5）
 * - CHIP_SIG_005: 投信大買（trust_net > avg_20 + 2*std_20）
 * - CHIP_SIG_006: 投信連續買超（trust_continuous_days >= 3）
 * - CHIP_SIG_007: 三大法人同買
 * - CHIP_SIG_008: 三大法人同賣
 * </p>
 *
 * @author chris
 * @since 1.0.0
 */
@Component
public class InstitutionalSignalDetector implements ChipCalculator, ChipSignalDetector {

    private static final int LOOKBACK_PERIOD = 20;
    private static final double STD_MULTIPLIER = 2.0;
    private static final int FOREIGN_CONTINUOUS_THRESHOLD = 5;
    private static final int TRUST_CONTINUOUS_THRESHOLD = 3;

    @Override
    public String getName() {
        return "INSTITUTIONAL_SIGNAL";
    }

    @Override
    public ChipCategory getCategory() {
        return ChipCategory.SIGNAL;
    }

    @Override
    public ChipMetadata getMetadata() {
        return ChipMetadata.builder()
                .name("INSTITUTIONAL_SIGNAL")
                .nameZh("法人籌碼訊號偵測")
                .category(ChipCategory.SIGNAL)
                .description("偵測三大法人相關的籌碼異常訊號")
                .minDataDays(LOOKBACK_PERIOD)
                .defaultParams(Map.of("lookback", LOOKBACK_PERIOD, "std_multiplier", STD_MULTIPLIER))
                .priority("P0")
                .build();
    }

    @Override
    public Map<String, Object> calculate(ChipSeries series, Map<String, Object> params) {
        // 執行訊號偵測，將結果轉為 Map 格式
        List<ChipSignal> signals = detect(series, params);

        Map<String, Object> result = new HashMap<>();
        result.put("signals", signals);
        result.put("signal_count", signals.size());

        return result;
    }

    @Override
    public List<ChipSignal> detect(ChipSeries series, Map<String, Object> params) {
        List<ChipSignal> signals = new ArrayList<>();

        long[] foreignNet = series.getForeignNetArray();
        long[] trustNet = series.getTrustNetArray();
        long[] dealerNet = series.getDealerNetArray();

        if (foreignNet.length < LOOKBACK_PERIOD) {
            return signals;
        }

        int lastIndex = foreignNet.length - 1;
        long latestForeignNet = foreignNet[lastIndex];
        long latestTrustNet = trustNet.length > 0 ? trustNet[lastIndex] : 0;
        long latestDealerNet = dealerNet.length > 0 ? dealerNet[lastIndex] : 0;

        // 計算統計數據
        double foreignAvg = calculateAverage(foreignNet, LOOKBACK_PERIOD);
        double foreignStd = calculateStdDev(foreignNet, LOOKBACK_PERIOD, foreignAvg);
        double trustAvg = calculateAverage(trustNet, LOOKBACK_PERIOD);
        double trustStd = calculateStdDev(trustNet, LOOKBACK_PERIOD, trustAvg);

        // CHIP_SIG_001: 外資大買
        double foreignUpperThreshold = foreignAvg + STD_MULTIPLIER * foreignStd;
        if (latestForeignNet > foreignUpperThreshold) {
            signals.add(ChipSignal.institutionalSignal(
                    "CHIP_SIG_001",
                    "外資大買",
                    SignalSeverity.HIGH,
                    BigDecimal.valueOf(latestForeignNet),
                    BigDecimal.valueOf(foreignUpperThreshold),
                    String.format("外資買超 %,d 股，超過 20 日平均 2 個標準差", latestForeignNet)
            ));
        }

        // CHIP_SIG_002: 外資大賣
        double foreignLowerThreshold = foreignAvg - STD_MULTIPLIER * foreignStd;
        if (latestForeignNet < foreignLowerThreshold) {
            signals.add(ChipSignal.institutionalSignal(
                    "CHIP_SIG_002",
                    "外資大賣",
                    SignalSeverity.HIGH,
                    BigDecimal.valueOf(latestForeignNet),
                    BigDecimal.valueOf(foreignLowerThreshold),
                    String.format("外資賣超 %,d 股，超過 20 日平均 2 個標準差", Math.abs(latestForeignNet))
            ));
        }

        // CHIP_SIG_003 / CHIP_SIG_004: 外資連續買超/賣超
        int foreignContinuous = calculateContinuousDays(foreignNet);
        if (foreignContinuous >= FOREIGN_CONTINUOUS_THRESHOLD) {
            signals.add(ChipSignal.institutionalSignal(
                    "CHIP_SIG_003",
                    "外資連續買超",
                    SignalSeverity.MEDIUM,
                    BigDecimal.valueOf(foreignContinuous),
                    BigDecimal.valueOf(FOREIGN_CONTINUOUS_THRESHOLD),
                    String.format("外資連續 %d 天買超", foreignContinuous)
            ));
        } else if (foreignContinuous <= -FOREIGN_CONTINUOUS_THRESHOLD) {
            signals.add(ChipSignal.institutionalSignal(
                    "CHIP_SIG_004",
                    "外資連續賣超",
                    SignalSeverity.MEDIUM,
                    BigDecimal.valueOf(Math.abs(foreignContinuous)),
                    BigDecimal.valueOf(FOREIGN_CONTINUOUS_THRESHOLD),
                    String.format("外資連續 %d 天賣超", Math.abs(foreignContinuous))
            ));
        }

        // CHIP_SIG_005: 投信大買
        if (trustNet.length >= LOOKBACK_PERIOD) {
            double trustUpperThreshold = trustAvg + STD_MULTIPLIER * trustStd;
            if (latestTrustNet > trustUpperThreshold) {
                signals.add(ChipSignal.institutionalSignal(
                        "CHIP_SIG_005",
                        "投信大買",
                        SignalSeverity.HIGH,
                        BigDecimal.valueOf(latestTrustNet),
                        BigDecimal.valueOf(trustUpperThreshold),
                        String.format("投信買超 %,d 股，超過 20 日平均 2 個標準差", latestTrustNet)
                ));
            }
        }

        // CHIP_SIG_006: 投信連續買超
        if (trustNet.length > 0) {
            int trustContinuous = calculateContinuousDays(trustNet);
            if (trustContinuous >= TRUST_CONTINUOUS_THRESHOLD) {
                signals.add(ChipSignal.institutionalSignal(
                        "CHIP_SIG_006",
                        "投信連續買超",
                        SignalSeverity.MEDIUM,
                        BigDecimal.valueOf(trustContinuous),
                        BigDecimal.valueOf(TRUST_CONTINUOUS_THRESHOLD),
                        String.format("投信連續 %d 天買超", trustContinuous)
                ));
            }
        }

        // CHIP_SIG_007: 三大法人同買
        if (latestForeignNet > 0 && latestTrustNet > 0 && latestDealerNet > 0) {
            signals.add(ChipSignal.institutionalSignal(
                    "CHIP_SIG_007",
                    "三大法人同買",
                    SignalSeverity.MEDIUM,
                    null,
                    null,
                    "外資、投信、自營商皆為買超"
            ));
        }

        // CHIP_SIG_008: 三大法人同賣
        if (latestForeignNet < 0 && latestTrustNet < 0 && latestDealerNet < 0) {
            signals.add(ChipSignal.institutionalSignal(
                    "CHIP_SIG_008",
                    "三大法人同賣",
                    SignalSeverity.HIGH,
                    null,
                    null,
                    "外資、投信、自營商皆為賣超"
            ));
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

    private int calculateContinuousDays(long[] data) {
        if (data.length == 0) {
            return 0;
        }

        int lastIndex = data.length - 1;
        long lastValue = data[lastIndex];

        if (lastValue == 0) {
            return 0;
        }

        boolean isBuying = lastValue > 0;
        int count = 1;

        for (int i = lastIndex - 1; i >= 0; i--) {
            if (isBuying && data[i] > 0) {
                count++;
            } else if (!isBuying && data[i] < 0) {
                count++;
            } else {
                break;
            }
        }

        return isBuying ? count : -count;
    }
}
