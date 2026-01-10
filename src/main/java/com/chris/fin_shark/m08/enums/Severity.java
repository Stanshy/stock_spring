package com.chris.fin_shark.m08.enums;

import lombok.Getter;

/**
 * 警示嚴重程度
 * <p>
 * 對應資料庫 CHECK 約束: severity IN ('LOW', 'MEDIUM', 'HIGH', 'CRITICAL')
 * </p>
 *
 * @author chris
 * @since 1.0.0
 */
@Getter
public enum Severity {

    /**
     * 低（輕微異常）
     */
    LOW("LOW", "低", 1, "輕微異常，建議觀察"),

    /**
     * 中（需要注意）
     */
    MEDIUM("MEDIUM", "中", 2, "需要注意，持續追蹤"),

    /**
     * 高（嚴重警告）
     */
    HIGH("HIGH", "高", 3, "嚴重警告，建議處理"),

    /**
     * 極高（緊急）
     */
    CRITICAL("CRITICAL", "極高", 4, "緊急狀況，立即處理");

    /**
     * 資料庫儲存值
     */
    private final String code;

    /**
     * 顯示名稱
     */
    private final String displayName;

    /**
     * 優先級（數字越大越嚴重）
     */
    private final int priority;

    /**
     * 說明
     */
    private final String description;

    Severity(String code, String displayName, int priority, String description) {
        this.code = code;
        this.displayName = displayName;
        this.priority = priority;
        this.description = description;
    }

    /**
     * 根據 code 取得 Enum
     */
    public static Severity fromCode(String code) {
        for (Severity severity : values()) {
            if (severity.code.equals(code)) {
                return severity;
            }
        }
        throw new IllegalArgumentException("Unknown Severity code: " + code);
    }
}
