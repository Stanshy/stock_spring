package com.chris.fin_shark.m08.enums;

import lombok.Getter;

/**
 * 警示類別
 * <p>
 * 對應資料庫 CHECK 約束: alert_category IN 
 * ('EARNINGS_QUALITY', 'DEBT_RISK', 'LIQUIDITY_RISK', 'PROFITABILITY_DECLINE', 'OTHER')
 * </p>
 *
 * @author chris
 * @since 1.0.0
 */
@Getter
public enum AlertCategory {

    /**
     * 盈餘品質異常
     */
    EARNINGS_QUALITY("EARNINGS_QUALITY", "盈餘品質異常",
            "盈餘操縱、應計項目異常、現金流與淨利不符"),

    /**
     * 負債風險
     */
    DEBT_RISK("DEBT_RISK", "負債風險",
            "負債比過高、利息保障倍數不足、流動負債壓力"),

    /**
     * 流動性風險
     */
    LIQUIDITY_RISK("LIQUIDITY_RISK", "流動性風險",
            "流動比率過低、速動比率不足、現金流量不佳"),

    /**
     * 獲利能力衰退
     */
    PROFITABILITY_DECLINE("PROFITABILITY_DECLINE", "獲利能力衰退",
            "ROE下降、毛利率下滑、營業利益率衰退"),

    /**
     * 其他異常
     */
    OTHER("OTHER", "其他異常", "其他財務異常狀況");

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

    AlertCategory(String code, String displayName, String description) {
        this.code = code;
        this.displayName = displayName;
        this.description = description;
    }

    /**
     * 根據 code 取得 Enum
     */
    public static AlertCategory fromCode(String code) {
        for (AlertCategory category : values()) {
            if (category.code.equals(code)) {
                return category;
            }
        }
        throw new IllegalArgumentException("Unknown AlertCategory code: " + code);
    }
}
