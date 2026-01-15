package com.chris.fin_shark.m11.enums;

import lombok.Getter;

/**
 * 策略執行類型列舉
 *
 * @author chris
 * @since 1.0.0
 */
@Getter
public enum ExecutionType {

    /**
     * 排程執行
     */
    SCHEDULED("排程執行", "由 Job 排程自動觸發"),

    /**
     * 手動執行
     */
    MANUAL("手動執行", "由使用者透過 API 手動觸發"),

    /**
     * 回測執行
     */
    BACKTEST("回測執行", "回測系統執行"),

    /**
     * 優化執行
     */
    OPTIMIZATION("優化執行", "參數優化過程中的執行");

    private final String displayName;
    private final String description;

    ExecutionType(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }
}
