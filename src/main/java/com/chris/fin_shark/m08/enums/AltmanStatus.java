package com.chris.fin_shark.m08.enums;

import lombok.Getter;

/**
 * Altman Z-Score 狀態
 * <p>
 * 對應資料庫 CHECK 約束: altman_status IN ('SAFE', 'GREY', 'DISTRESS')
 * </p>
 *
 * @author chris
 * @since 1.0.0
 */
@Getter
public enum AltmanStatus {

    /**
     * 安全區（Z-Score > 2.99）
     */
    SAFE("SAFE", "安全區", "破產風險低"),

    /**
     * 灰色區（1.81 < Z-Score < 2.99）
     */
    GREY("GREY", "灰色區", "需要關注"),

    /**
     * 危險區（Z-Score < 1.81）
     */
    DISTRESS("DISTRESS", "危險區", "破產風險高");

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

    AltmanStatus(String code, String displayName, String description) {
        this.code = code;
        this.displayName = displayName;
        this.description = description;
    }

    /**
     * 根據 code 取得 Enum
     */
    public static AltmanStatus fromCode(String code) {
        for (AltmanStatus status : values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown AltmanStatus code: " + code);
    }
}
