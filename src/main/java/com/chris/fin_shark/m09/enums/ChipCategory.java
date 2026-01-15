package com.chris.fin_shark.m09.enums;

import lombok.Getter;

/**
 * 籌碼指標類別枚舉
 * <p>
 * 定義籌碼分析模組中各類計算器的分類
 * </p>
 *
 * @author chris
 * @since 1.0.0
 */
@Getter
public enum ChipCategory {

    /**
     * 三大法人指標
     */
    INSTITUTIONAL("INSTITUTIONAL", "三大法人指標"),

    /**
     * 融資融券指標
     */
    MARGIN("MARGIN", "融資融券指標"),

    /**
     * 籌碼集中度指標
     */
    CONCENTRATION("CONCENTRATION", "籌碼集中度指標"),

    /**
     * 主力成本估算
     */
    COST("COST", "主力成本估算"),

    /**
     * 訊號偵測
     */
    SIGNAL("SIGNAL", "訊號偵測");

    /**
     * 類別代碼
     */
    private final String code;

    /**
     * 類別名稱（中文）
     */
    private final String name;

    /**
     * 建構子
     *
     * @param code 類別代碼
     * @param name 類別名稱
     */
    ChipCategory(String code, String name) {
        this.code = code;
        this.name = name;
    }
}
