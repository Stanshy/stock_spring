package com.chris.fin_shark.m09.engine.calculator.institutional;

import com.chris.fin_shark.m09.engine.calculator.ChipCalculator;
import com.chris.fin_shark.m09.engine.model.ChipMetadata;
import com.chris.fin_shark.m09.engine.model.ChipSeries;
import com.chris.fin_shark.m09.enums.ChipCategory;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 連續買超/賣超天數計算器
 * <p>
 * 計算法人連續買超或賣超的天數：
 * - foreign_continuous_days: 外資連續買超天數（負數表示連續賣超）
 * - trust_continuous_days: 投信連續買超天數
 * - institutional_agreement: 法人買賣方向一致性（BULLISH/BEARISH/MIXED）
 * </p>
 *
 * @author chris
 * @since 1.0.0
 */
@Component
public class ContinuousDaysCalculator implements ChipCalculator {

    @Override
    public String getName() {
        return "CONTINUOUS_DAYS";
    }

    @Override
    public ChipCategory getCategory() {
        return ChipCategory.INSTITUTIONAL;
    }

    @Override
    public ChipMetadata getMetadata() {
        return ChipMetadata.builder()
                .name("CONTINUOUS_DAYS")
                .nameZh("連續買賣超天數")
                .category(ChipCategory.INSTITUTIONAL)
                .description("計算法人連續買超或賣超的天數")
                .minDataDays(5)
                .defaultParams(Map.of())
                .priority("P0")
                .build();
    }

    @Override
    public Map<String, Object> calculate(ChipSeries series, Map<String, Object> params) {
        long[] foreignNet = series.getForeignNetArray();
        long[] trustNet = series.getTrustNetArray();
        long[] dealerNet = series.getDealerNetArray();

        if (foreignNet.length == 0) {
            return Map.of();
        }

        Map<String, Object> result = new HashMap<>();

        // 計算外資連續天數
        int foreignContinuous = calculateContinuousDays(foreignNet);
        result.put("foreign_continuous_days", foreignContinuous);

        // 計算投信連續天數
        if (trustNet.length > 0) {
            int trustContinuous = calculateContinuousDays(trustNet);
            result.put("trust_continuous_days", trustContinuous);
        }

        // 計算法人一致性
        if (foreignNet.length > 0 && trustNet.length > 0 && dealerNet.length > 0) {
            String agreement = calculateAgreement(
                    foreignNet[foreignNet.length - 1],
                    trustNet[trustNet.length - 1],
                    dealerNet[dealerNet.length - 1]
            );
            result.put("institutional_agreement", agreement);
        }

        return result;
    }

    /**
     * 計算連續買超/賣超天數
     * <p>
     * 從最後一天往前計算連續同向的天數。
     * 正數表示連續買超，負數表示連續賣超。
     * </p>
     *
     * @param netData 買賣超資料陣列
     * @return 連續天數（正數=買超，負數=賣超）
     */
    private int calculateContinuousDays(long[] netData) {
        if (netData.length == 0) {
            return 0;
        }

        int lastIndex = netData.length - 1;
        long lastValue = netData[lastIndex];

        // 最後一天為 0，視為無連續
        if (lastValue == 0) {
            return 0;
        }

        boolean isBuying = lastValue > 0;
        int count = 1;

        // 從倒數第二天開始往前算
        for (int i = lastIndex - 1; i >= 0; i--) {
            long value = netData[i];

            if (isBuying && value > 0) {
                count++;
            } else if (!isBuying && value < 0) {
                count++;
            } else {
                break;
            }
        }

        // 賣超返回負數
        return isBuying ? count : -count;
    }

    /**
     * 計算法人買賣方向一致性
     *
     * @param foreignNet 外資買賣超
     * @param trustNet   投信買賣超
     * @param dealerNet  自營商買賣超
     * @return 一致性（BULLISH=同買 / BEARISH=同賣 / MIXED=分歧）
     */
    private String calculateAgreement(long foreignNet, long trustNet, long dealerNet) {
        int buyCount = 0;
        int sellCount = 0;

        if (foreignNet > 0) buyCount++;
        else if (foreignNet < 0) sellCount++;

        if (trustNet > 0) buyCount++;
        else if (trustNet < 0) sellCount++;

        if (dealerNet > 0) buyCount++;
        else if (dealerNet < 0) sellCount++;

        if (buyCount == 3) {
            return "BULLISH";   // 三大法人同買
        } else if (sellCount == 3) {
            return "BEARISH";   // 三大法人同賣
        } else {
            return "MIXED";     // 方向分歧
        }
    }
}
