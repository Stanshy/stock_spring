package com.chris.fin_shark.m09.enums;

import lombok.Getter;

/**
 * 籌碼訊號嚴重度枚舉
 * <p>
 * 定義籌碼異常訊號的嚴重程度等級
 * </p>
 *
 * @author chris
 * @since 1.0.0
 */
@Getter
public enum SignalSeverity {

    /**
     * 嚴重（如：融資斷頭）
     */
    CRITICAL("CRITICAL", "嚴重", 4),

    /**
     * 高（如：外資大買/大賣、融資暴增）
     */
    HIGH("HIGH", "高", 3),

    /**
     * 中（如：外資連續買超、投信連續買超）
     */
    MEDIUM("MEDIUM", "中", 2),

    /**
     * 低（如：一般資訊性訊號）
     */
    LOW("LOW", "低", 1);

    /**
     * 嚴重度代碼
     */
    private final String code;

    /**
     * 嚴重度名稱（中文）
     */
    private final String name;

    /**
     * 嚴重度權重（用於排序）
     */
    private final int weight;

    /**
     * 建構子
     *
     * @param code   嚴重度代碼
     * @param name   嚴重度名稱
     * @param weight 嚴重度權重
     */
    SignalSeverity(String code, String name, int weight) {
        this.code = code;
        this.name = name;
        this.weight = weight;
    }
}
