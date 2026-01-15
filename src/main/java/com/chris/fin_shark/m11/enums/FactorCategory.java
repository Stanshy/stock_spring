package com.chris.fin_shark.m11.enums;

import lombok.Getter;

/**
 * 因子類別列舉
 *
 * @author chris
 * @since 1.0.0
 */
@Getter
public enum FactorCategory {

    /**
     * 技術面因子（來自 M07）
     */
    TECHNICAL("技術面因子", "M07", "RSI、MACD、MA、布林通道等技術指標"),

    /**
     * 基本面因子（來自 M08）
     */
    FUNDAMENTAL("基本面因子", "M08", "PE、PB、ROE、EPS 成長率等財務指標"),

    /**
     * 籌碼面因子（來自 M09）
     */
    CHIP("籌碼面因子", "M09", "法人買賣超、融資融券、籌碼集中度等"),

    /**
     * 價量因子（來自 M06）
     */
    PRICE_VOLUME("價量因子", "M06", "成交量、股價變化、振幅等"),

    /**
     * 衍生因子（M11 自定義）
     */
    DERIVED("衍生因子", "M11", "自定義組合因子");

    private final String displayName;
    private final String sourceModule;
    private final String description;

    FactorCategory(String displayName, String sourceModule, String description) {
        this.displayName = displayName;
        this.sourceModule = sourceModule;
        this.description = description;
    }
}
