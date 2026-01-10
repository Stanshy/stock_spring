package com.chris.fin_shark.m08.enums;

import lombok.Getter;

/**
 * 警示狀態
 * <p>
 * 對應資料庫 CHECK 約束: alert_status IN ('ACTIVE', 'RESOLVED', 'IGNORED')
 * </p>
 *
 * @author chris
 * @since 1.0.0
 */
@Getter
public enum AlertStatus {

    /**
     * 活躍中（尚未處理）
     */
    ACTIVE("ACTIVE", "活躍中", "警示尚未處理"),

    /**
     * 已解決
     */
    RESOLVED("RESOLVED", "已解決", "警示已經解決"),

    /**
     * 已忽略
     */
    IGNORED("IGNORED", "已忽略", "警示已被忽略");

    /**
     * 資料庫儲存值
     */
    private final String code;

    /**
     * 顯示名稱
     */
    private final String displayName;

    /**
     * 說明
     */
    private final String description;

    AlertStatus(String code, String displayName, String description) {
        this.code = code;
        this.displayName = displayName;
        this.description = description;
    }

    /**
     * 根據 code 取得 Enum
     */
    public static AlertStatus fromCode(String code) {
        for (AlertStatus status : values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown AlertStatus code: " + code);
    }
}
