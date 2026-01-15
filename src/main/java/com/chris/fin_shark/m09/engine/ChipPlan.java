package com.chris.fin_shark.m09.engine;

import com.chris.fin_shark.m09.enums.ChipCategory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * 籌碼計算計劃
 * <p>
 * 定義本次計算要執行的計算器與參數。
 * 與 M07 IndicatorPlan 結構對齊。
 * </p>
 *
 * @author chris
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChipPlan {

    /** 計算計劃（計算器名稱 → 參數） */
    @Builder.Default
    private Map<String, Map<String, Object>> calculators = new HashMap<>();

    /** 優先級（P0, P1, P2...） */
    private String priority;

    /** 是否計算三大法人指標 */
    @Builder.Default
    private boolean includeInstitutional = true;

    /** 是否計算融資融券指標 */
    @Builder.Default
    private boolean includeMargin = true;

    /** 是否計算籌碼集中度 */
    @Builder.Default
    private boolean includeConcentration = false;

    /** 是否計算主力成本 */
    @Builder.Default
    private boolean includeCost = false;

    /** 是否偵測籌碼異常訊號 */
    @Builder.Default
    private boolean includeSignals = true;

    /** 回溯天數（用於統計計算） */
    @Builder.Default
    private int lookbackPeriod = 60;

    /**
     * 新增計算器到計劃
     *
     * @param name   計算器名稱
     * @param params 參數
     * @return this（方便鏈式調用）
     */
    public ChipPlan addCalculator(String name, Map<String, Object> params) {
        if (calculators == null) {
            calculators = new HashMap<>();
        }
        calculators.put(name, params);
        return this;
    }

    /**
     * 取得計算器名稱集合
     */
    public Set<String> getCalculatorNames() {
        return calculators != null ? calculators.keySet() : Set.of();
    }

    /**
     * 是否包含特定類別
     *
     * @param category 類別
     * @return 是否包含
     */
    public boolean includesCategory(ChipCategory category) {
        return switch (category) {
            case INSTITUTIONAL -> includeInstitutional;
            case MARGIN -> includeMargin;
            case CONCENTRATION -> includeConcentration;
            case COST -> includeCost;
            case SIGNAL -> includeSignals;
        };
    }

    // ========== 工廠方法 ==========

    /**
     * 建立預設計劃（等同 P0）
     */
    public static ChipPlan defaultPlan() {
        return p0Plan();
    }

    /**
     * 建立 P0 計劃（核心指標 + 訊號偵測）
     */
    public static ChipPlan p0Plan() {
        return ChipPlan.builder()
                .priority("P0")
                .includeInstitutional(true)
                .includeMargin(true)
                .includeConcentration(false)
                .includeCost(false)
                .includeSignals(true)
                .lookbackPeriod(60)
                .build();
    }

    /**
     * 建立完整計劃（所有指標）
     */
    public static ChipPlan fullPlan() {
        return ChipPlan.builder()
                .priority("ALL")
                .includeInstitutional(true)
                .includeMargin(true)
                .includeConcentration(true)
                .includeCost(true)
                .includeSignals(true)
                .lookbackPeriod(120)
                .build();
    }

    /**
     * 快速建立計劃（指定計算器）
     *
     * @param calculatorNames 計算器名稱
     * @return 計劃
     */
    public static ChipPlan of(String... calculatorNames) {
        ChipPlan plan = new ChipPlan();
        for (String name : calculatorNames) {
            plan.addCalculator(name, Map.of());
        }
        return plan;
    }
}
