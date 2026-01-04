package com.chris.fin_shark.common.enums;

import lombok.Getter;

/**
 * 資料品質等級列舉
 *
 * 遵守總綱 4.7 資料品質規範
 *
 * @author chris
 * @since 2025-12-24
 */
@Getter
public enum DataQualityLevel {

    /**
     * 高品質
     * 資料完整、準確、一致、有效、唯一
     */
    HIGH("HIGH", "高品質", 0),

    /**
     * 中品質
     * 資料基本正確，但有少量問題（如缺少非必填欄位）
     */
    MEDIUM("MEDIUM", "中品質", 1),

    /**
     * 低品質
     * 資料有明顯問題，需人工審核
     */
    LOW("LOW", "低品質", 2),

    /**
     * 無效
     * 資料嚴重錯誤，不可使用
     */
    INVALID("INVALID", "無效", 3);

    /**
     * 等級代碼
     */
    private final String code;

    /**
     * 等級描述
     */
    private final String description;

    /**
     * 嚴重程度（數字越大越嚴重）
     */
    private final Integer severity;

    /**
     * 建構子
     */
    DataQualityLevel(String code, String description, Integer severity) {
        this.code = code;
        this.description = description;
        this.severity = severity;
    }

    /**
     * 判斷是否可用於分析
     */
    public boolean isUsableForAnalysis() {
        return this == HIGH || this == MEDIUM;
    }
}
