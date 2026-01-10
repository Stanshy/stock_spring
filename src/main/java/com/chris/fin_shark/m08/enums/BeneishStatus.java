package com.chris.fin_shark.m08.enums;

import lombok.Getter;

/**
 * Beneish M-Score 狀態
 * <p>
 * 對應資料庫 CHECK 約束: beneish_status IN ('CLEAN', 'WARNING', 'MANIPULATOR')
 * </p>
 *
 * @author chris
 * @since 1.0.0
 */
@Getter
public enum BeneishStatus {

    /**
     * 乾淨（M-Score < -2.22）
     */
    CLEAN("CLEAN", "盈餘品質良好", "無操縱跡象"),

    /**
     * 警告（-2.22 < M-Score < -1.78）
     */
    WARNING("WARNING", "盈餘品質存疑", "需要進一步調查"),

    /**
     * 操縱（M-Score > -1.78）
     */
    MANIPULATOR("MANIPULATOR", "疑似盈餘操縱", "高風險");

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

    BeneishStatus(String code, String displayName, String description) {
        this.code = code;
        this.displayName = displayName;
        this.description = description;
    }

    /**
     * 根據 code 取得 Enum
     */
    public static BeneishStatus fromCode(String code) {
        for (BeneishStatus status : values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown BeneishStatus code: " + code);
    }
}

