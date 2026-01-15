package com.chris.fin_shark.m11.enums;

import lombok.Getter;

import java.util.Set;

/**
 * 策略狀態列舉
 *
 * @author chris
 * @since 1.0.0
 */
@Getter
public enum StrategyStatus {

    /**
     * 草稿狀態
     */
    DRAFT("草稿", Set.of("ACTIVE", "ARCHIVED")),

    /**
     * 啟用狀態
     */
    ACTIVE("啟用", Set.of("INACTIVE", "ARCHIVED")),

    /**
     * 停用狀態
     */
    INACTIVE("停用", Set.of("ACTIVE", "ARCHIVED")),

    /**
     * 已封存狀態（不可轉換）
     */
    ARCHIVED("已封存", Set.of());

    private final String displayName;
    private final Set<String> allowedTransitions;

    StrategyStatus(String displayName, Set<String> allowedTransitions) {
        this.displayName = displayName;
        this.allowedTransitions = allowedTransitions;
    }

    /**
     * 檢查是否可轉換至目標狀態
     */
    public boolean canTransitionTo(StrategyStatus targetStatus) {
        return allowedTransitions.contains(targetStatus.name());
    }
}
