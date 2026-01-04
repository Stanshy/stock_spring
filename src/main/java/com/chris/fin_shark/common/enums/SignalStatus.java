package com.chris.fin_shark.common.enums;

import lombok.Getter;

/**
 * 信號狀態列舉
 *
 * 遵守總綱 4.2.9 信號生命週期規範
 *
 * 狀態轉換流程:
 * ACTIVE → TRIGGERED/EXPIRED/CANCELLED
 *
 * @author chris
 * @since 2025-12-24
 */
@Getter
public enum SignalStatus {

    /**
     * 有效狀態
     * 新生成的信號，有效且未觸發
     */
    ACTIVE("ACTIVE", "有效"),

    /**
     * 已觸發
     * 使用者已執行操作（買入/賣出）
     */
    TRIGGERED("TRIGGERED", "已觸發"),

    /**
     * 已過期
     * 超過 valid_until 時間，信號過期
     */
    EXPIRED("EXPIRED", "已過期"),

    /**
     * 已取消
     * 條件不再滿足，信號取消
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
    SignalStatus(String code, String description) {
        this.code = code;
        this.description = description;
    }

    /**
     * 判斷是否為有效信號
     */
    public boolean isActive() {
        return this == ACTIVE;
    }

    /**
     * 判斷是否為最終狀態
     */
    public boolean isFinalStatus() {
        return this == TRIGGERED || this == EXPIRED || this == CANCELLED;
    }
}
