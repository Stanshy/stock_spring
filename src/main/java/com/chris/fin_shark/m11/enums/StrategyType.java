package com.chris.fin_shark.m11.enums;

import lombok.Getter;

/**
 * 策略類型列舉
 *
 * @author chris
 * @since 1.0.0
 */
@Getter
public enum StrategyType {

    /**
     * 動能策略
     */
    MOMENTUM("動能策略", "基於價格動能與技術指標的策略"),

    /**
     * 價值策略
     */
    VALUE("價值策略", "基於基本面估值的策略"),

    /**
     * 混合策略
     */
    HYBRID("混合策略", "結合多種因子的綜合策略"),

    /**
     * 自訂策略
     */
    CUSTOM("自訂策略", "使用者自訂的策略");

    private final String displayName;
    private final String description;

    StrategyType(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }
}
