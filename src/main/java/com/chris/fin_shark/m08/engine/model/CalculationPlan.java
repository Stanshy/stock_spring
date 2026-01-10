package com.chris.fin_shark.m08.engine.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

/**
 * 計算計劃
 * <p>
 * 定義要計算哪些指標類別
 * </p>
 *
 * @author chris
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CalculationPlan {

    /** 是否計算估值指標 */
    @Builder.Default
    private boolean includeValuation = true;

    /** 是否計算獲利能力指標 */
    @Builder.Default
    private boolean includeProfitability = true;

    /** 是否計算財務結構指標 */
    @Builder.Default
    private boolean includeFinancialStructure = true;

    /** 是否計算償債能力指標 */
    @Builder.Default
    private boolean includeSolvency = true;

    /** 是否計算現金流量指標 */
    @Builder.Default
    private boolean includeCashFlow = true;

    /** 是否計算成長性指標 */
    @Builder.Default
    private boolean includeGrowth = false;

    /** 是否計算股利政策指標 */
    @Builder.Default
    private boolean includeDividend = false;

    /** 是否計算綜合評分 */
    @Builder.Default
    private boolean includeScore = false;

    /** 指定要計算的指標（若不為空，則只計算這些指標） */
    private Set<String> specificIndicators;

    /**
     * 建立預設計劃（P0 核心指標）
     */
    public static CalculationPlan defaultPlan() {
        return CalculationPlan.builder()
                .includeValuation(true)
                .includeProfitability(true)
                .includeFinancialStructure(true)
                .includeSolvency(true)
                .includeCashFlow(true)
                .includeGrowth(false)
                .includeDividend(false)
                .includeScore(false)
                .build();
    }

    /**
     * 建立完整計劃（所有指標）
     */
    public static CalculationPlan fullPlan() {
        return CalculationPlan.builder()
                .includeValuation(true)
                .includeProfitability(true)
                .includeFinancialStructure(true)
                .includeSolvency(true)
                .includeCashFlow(true)
                .includeGrowth(true)
                .includeDividend(true)
                .includeScore(true)
                .build();
    }
}
