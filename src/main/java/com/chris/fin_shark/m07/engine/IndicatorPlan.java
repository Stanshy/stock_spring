package com.chris.fin_shark.m07.engine;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

/**
 * 指標計算計劃
 *
 * @author chris
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IndicatorPlan {

    /** 計算計劃（指標名稱 → 參數） */
    @Builder.Default
    private Map<String, Map<String, Object>> indicators = new HashMap<>();

    /** 優先級（P0, P1, P2...） */
    private String priority;

    /**
     * 新增指標到計劃
     */
    public IndicatorPlan addIndicator(String name, Map<String, Object> params) {
        if (indicators == null) {
            indicators = new HashMap<>();
        }
        indicators.put(name, params);
        return this;
    }

    /**
     * 快速建立計劃（使用預設參數）
     */
    public static IndicatorPlan of(String... indicatorNames) {
        IndicatorPlan plan = new IndicatorPlan();
        for (String name : indicatorNames) {
            plan.addIndicator(name, Map.of());
        }
        return plan;
    }
}
