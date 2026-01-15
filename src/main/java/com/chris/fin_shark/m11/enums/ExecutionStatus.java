package com.chris.fin_shark.m11.enums;

import lombok.Getter;

/**
 * 策略執行狀態列舉
 *
 * @author chris
 * @since 1.0.0
 */
@Getter
public enum ExecutionStatus {

    /**
     * 執行中
     */
    RUNNING("執行中"),

    /**
     * 成功
     */
    SUCCESS("成功"),

    /**
     * 失敗
     */
    FAILED("失敗"),

    /**
     * 已取消
     */
    CANCELLED("已取消");

    private final String displayName;

    ExecutionStatus(String displayName) {
        this.displayName = displayName;
    }
}
