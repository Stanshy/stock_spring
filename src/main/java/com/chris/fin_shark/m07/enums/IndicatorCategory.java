package com.chris.fin_shark.m07.enums;

import lombok.Getter;

/**
 * 指標類別枚舉
 *
 * @author chris
 * @since 1.0.0
 */
@Getter
public enum IndicatorCategory {

    /**
     * 趨勢指標
     */
    TREND("TREND", "趨勢指標"),

    /**
     * 動能指標
     */
    MOMENTUM("MOMENTUM", "動能指標"),

    /**
     * 波動性指標
     */
    VOLATILITY("VOLATILITY", "波動性指標"),

    /**
     * 成交量指標
     */
    VOLUME("VOLUME", "成交量指標"),

    /**
     * 支撐壓力指標
     */
    SUPPORT_RESISTANCE("SUPPORT_RESISTANCE", "支撐壓力指標"),

    /**
     * 週期指標
     */
    CYCLE("CYCLE", "週期指標"),

    /**
     * 統計指標
     */
    STATISTICAL("STATISTICAL", "統計指標"),

    /**
     * 綜合指標
     */
    COMPOSITE("COMPOSITE", "綜合指標");

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
    IndicatorCategory(String code, String name) {
        this.code = code;
        this.name = name;
    }
}
