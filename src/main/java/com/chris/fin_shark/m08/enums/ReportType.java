package com.chris.fin_shark.m08.enums;

import lombok.Getter;

/**
 * 報表類型
 * <p>
 * 對應資料庫 CHECK 約束: report_type IN ('Q', 'H', 'Y')
 * </p>
 *
 * @author chris
 * @since 1.0.0
 */
@Getter
public enum ReportType {

    /**
     * 季報（Quarterly）
     */
    Q("Q", "季報"),

    /**
     * 半年報（Half-Year）
     */
    H("H", "半年報"),

    /**
     * 年報（Yearly）
     */
    Y("Y", "年報");

    /**
     * 資料庫儲存值
     */
    private final String code;

    /**
     * 顯示名稱
     */
    private final String description;

    ReportType(String code, String description) {
        this.code = code;
        this.description = description;
    }

    /**
     * 根據 code 取得 Enum
     *
     * @param code 資料庫值
     * @return ReportType
     */
    public static ReportType fromCode(String code) {
        for (ReportType type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown ReportType code: " + code);
    }
}
