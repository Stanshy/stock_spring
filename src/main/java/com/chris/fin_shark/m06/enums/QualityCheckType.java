package com.chris.fin_shark.m06.enums;

import lombok.Getter;

/**
 * 資料品質檢核類型列舉
 * <p>
 * 功能編號: F-M06-006
 * 功能名稱: 資料品質檢核
 * </p>
 *
 * @author chris
 * @since 1.0.0
 */
@Getter
public enum QualityCheckType {

    /**
     * 完整性檢核 - 檢查資料是否完整
     */
    COMPLETENESS("COMPLETENESS", "完整性檢核"),

    /**
     * 一致性檢核 - 檢查資料是否符合業務規則
     */
    CONSISTENCY("CONSISTENCY", "一致性檢核"),

    /**
     * 時效性檢核 - 檢查資料是否為最新
     */
    TIMELINESS("TIMELINESS", "時效性檢核"),

    /**
     * 準確性檢核 - 檢查資料計算是否正確
     */
    ACCURACY("ACCURACY", "準確性檢核"),

    /**
     * 唯一性檢核 - 檢查是否有重複資料
     */
    UNIQUENESS("UNIQUENESS", "唯一性檢核");

    /**
     * 類型代碼
     */
    private final String code;

    /**
     * 類型描述
     */
    private final String description;

    QualityCheckType(String code, String description) {
        this.code = code;
        this.description = description;
    }

    /**
     * 根據代碼取得類型
     *
     * @param code 類型代碼
     * @return 類型列舉
     */
    public static QualityCheckType fromCode(String code) {
        for (QualityCheckType type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown quality check type code: " + code);
    }
}
