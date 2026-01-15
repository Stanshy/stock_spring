package com.chris.fin_shark.m11.enums;

import lombok.Getter;

/**
 * 條件運算子列舉
 *
 * @author chris
 * @since 1.0.0
 */
@Getter
public enum ConditionOperator {

    /**
     * 等於
     */
    EQUAL("=", "等於"),

    /**
     * 不等於
     */
    NOT_EQUAL("!=", "不等於"),

    /**
     * 大於
     */
    GREATER_THAN(">", "大於"),

    /**
     * 大於等於
     */
    GREATER_THAN_EQUAL(">=", "大於等於"),

    /**
     * 小於
     */
    LESS_THAN("<", "小於"),

    /**
     * 小於等於
     */
    LESS_THAN_EQUAL("<=", "小於等於"),

    /**
     * 介於（需要 min/max 值）
     */
    BETWEEN("BETWEEN", "介於"),

    /**
     * 包含於（需要陣列值）
     */
    IN("IN", "包含於"),

    /**
     * 向上穿越
     */
    CROSS_ABOVE("CROSS_ABOVE", "向上穿越"),

    /**
     * 向下穿越
     */
    CROSS_BELOW("CROSS_BELOW", "向下穿越");

    private final String symbol;
    private final String displayName;

    ConditionOperator(String symbol, String displayName) {
        this.symbol = symbol;
        this.displayName = displayName;
    }

    /**
     * 根據字串查找運算子
     */
    public static ConditionOperator fromString(String value) {
        for (ConditionOperator op : values()) {
            if (op.name().equalsIgnoreCase(value) || op.symbol.equals(value)) {
                return op;
            }
        }
        throw new IllegalArgumentException("Unknown operator: " + value);
    }
}
