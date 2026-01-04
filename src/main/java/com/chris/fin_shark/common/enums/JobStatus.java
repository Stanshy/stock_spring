package com.chris.fin_shark.common.enums;

import lombok.Getter;

/**
 * Job 狀態列舉
 *
 * 遵守總綱 4.5.4 Job 狀態機規範
 *
 * 狀態轉換流程:
 * PENDING → RUNNING → SUCCESS/FAILED/CANCELLED
 *
 * @author chris
 * @since 2025-12-24
 */
@Getter
public enum JobStatus {

    /**
     * 等待執行
     * Job 已排程，尚未開始執行
     */
    PENDING("PENDING", "等待執行"),

    /**
     * 執行中
     * Job 正在執行中
     */
    RUNNING("RUNNING", "執行中"),

    /**
     * 執行成功
     * Job 執行完成且成功
     */
    SUCCESS("SUCCESS", "執行成功"),

    /**
     * 執行失敗
     * Job 執行失敗，可能需要重試
     */
    FAILED("FAILED", "執行失敗"),

    /**
     * 已取消
     * Job 被手動取消或系統取消
     */
    CANCELLED("CANCELLED", "已取消");

    /**
     * 狀態代碼
     */
    private final String code;

    /**
     * 狀態描述
     */
    private final String description;

    /**
     * 建構子
     */
    JobStatus(String code, String description) {
        this.code = code;
        this.description = description;
    }

    /**
     * 判斷是否為最終狀態（不可再轉換）
     */
    public boolean isFinalStatus() {
        return this == SUCCESS || this == FAILED || this == CANCELLED;
    }

    /**
     * 判斷是否可以重試
     */
    public boolean canRetry() {
        return this == FAILED;
    }
}
