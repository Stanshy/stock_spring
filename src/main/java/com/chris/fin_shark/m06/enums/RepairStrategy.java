package com.chris.fin_shark.m06.enums;

import lombok.Getter;

/**
 * 資料補齊策略列舉
 * <p>
 * 功能編號: F-M06-009
 * 功能名稱: 資料補齊機制
 * </p>
 *
 * @author chris
 * @since 1.0.0
 */
@Getter
public enum RepairStrategy {

    /**
     * 自動偵測缺漏日期並補齊
     */
    AUTO_DETECT("AUTO_DETECT", "自動偵測補齊"),

    /**
     * 指定日期範圍強制重新同步
     */
    DATE_RANGE("DATE_RANGE", "日期範圍補齊"),

    /**
     * 針對單一股票補齊
     */
    SINGLE_STOCK("SINGLE_STOCK", "單一股票補齊"),

    /**
     * 全市場資料重新同步
     */
    FULL_MARKET("FULL_MARKET", "全市場補齊");

    /**
     * 策略代碼
     */
    private final String code;

    /**
     * 策略描述
     */
    private final String description;

    RepairStrategy(String code, String description) {
        this.code = code;
        this.description = description;
    }

    /**
     * 根據代碼取得策略
     *
     * @param code 策略代碼
     * @return 策略列舉
     */
    public static RepairStrategy fromCode(String code) {
        for (RepairStrategy strategy : values()) {
            if (strategy.getCode().equals(code)) {
                return strategy;
            }
        }
        throw new IllegalArgumentException("Unknown repair strategy code: " + code);
    }
}
