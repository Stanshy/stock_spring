package com.chris.fin_shark.common.enums;

import lombok.Getter;

/**
 * 市場類型列舉
 *
 * 台股市場分類
 *
 * @author chris
 * @since 2025-12-24
 */
@Getter
public enum MarketType {

    /**
     * 上市（台灣證券交易所）
     * Taiwan Stock Exchange
     */
    TWSE("TWSE", "上市"),

    /**
     * 上櫃（櫃檯買賣中心）
     * Over The Counter
     */
    OTC("OTC", "上櫃"),

    /**
     * 興櫃（興櫃市場）
     * Emerging Stock Market
     */
    EMERGING("EMERGING", "興櫃");

    /**
     * 市場代碼
     */
    private final String code;

    /**
     * 市場描述
     */
    private final String description;

    /**
     * 建構子
     */
    MarketType(String code, String description) {
        this.code = code;
        this.description = description;
    }
}
