package com.chris.fin_shark.m07.enums;

import lombok.Getter;

/**
 * 指標優先級枚舉
 *
 * @author chris
 * @since 1.0.0
 */
@Getter
public enum IndicatorPriority {

    /**
     * P0 基礎組（每日必算）
     */
    P0("P0", "基礎組", "每日必算的常用指標"),

    /**
     * P1 進階組（每日必算）
     */
    P1("P1", "進階組", "每日計算的進階指標"),

    /**
     * P2 專業組（按需計算）
     */
    P2("P2", "專業組", "按需計算的專業指標");

    /**
     * 優先級代碼
     */
    private final String code;

    /**
     * 優先級名稱
     */
    private final String name;

    /**
     * 描述
     */
    private final String description;

    /**
     * 建構子
     *
     * @param code        優先級代碼
     * @param name        優先級名稱
     * @param description 描述
     */
    IndicatorPriority(String code, String name, String description) {
        this.code = code;
        this.name = name;
        this.description = description;
    }
}
