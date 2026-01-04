package com.chris.fin_shark.common.enums;

import lombok.Getter;

/**
 * 信號類型列舉
 *
 * 遵守總綱 4.2.1 Signal Contract 規範
 *
 * @author chris
 * @since 2025-12-24
 */
@Getter
public enum SignalType {

    /**
     * 買入信號
     * 建議買入/進場
     */
    BUY("BUY", "買入"),

    /**
     * 賣出信號
     * 建議賣出/出場
     */
    SELL("SELL", "賣出"),

    /**
     * 持有信號
     * 建議繼續持有
     */
    HOLD("HOLD", "持有");

    /**
     * 信號類型代碼
     */
    private final String code;

    /**
     * 信號類型描述
     */
    private final String description;

    /**
     * 建構子
     */
    SignalType(String code, String description) {
        this.code = code;
        this.description = description;
    }
}
