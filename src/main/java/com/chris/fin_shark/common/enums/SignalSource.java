package com.chris.fin_shark.common.enums;

import lombok.Getter;

/**
 * 信號來源列舉
 *
 * 遵守總綱 4.2.3 Signal Source 規範
 * 標識信號由哪個模組產生
 *
 * @author chris
 * @since 2025-12-24
 */
@Getter
public enum SignalSource {

    /**
     * 技術分析模組 (M07)
     * 例如: MA_GOLDEN_CROSS, RSI_OVERSOLD
     */
    TECHNICAL("TECHNICAL", "技術分析"),

    /**
     * 技術型態辨識模組 (M10)
     * 例如: HAMMER, HEAD_SHOULDER
     */
    PATTERN("PATTERN", "型態辨識"),

    /**
     * 基本面分析模組 (M08)
     * 例如: LOW_PE_RATIO, HIGH_ROE
     */
    FUNDAMENTAL("FUNDAMENTAL", "基本面分析"),

    /**
     * 籌碼分析模組 (M09)
     * 例如: INSTITUTIONAL_BUY
     */
    CHIP("CHIP", "籌碼分析"),

    /**
     * 量化策略模組 (M11)
     * 例如: MOMENTUM_BUY
     */
    STRATEGY("STRATEGY", "量化策略"),

    /**
     * 總經與產業分析模組 (M12)
     * 例如: SECTOR_ROTATION
     */
    MACRO("MACRO", "總經產業"),

    /**
     * 選股引擎 (M14)
     * 例如: TOP_MOMENTUM_STOCKS
     */
    SCREENER("SCREENER", "選股引擎");

    /**
     * 來源代碼
     */
    private final String code;

    /**
     * 來源描述
     */
    private final String description;

    /**
     * 建構子
     */
    SignalSource(String code, String description) {
        this.code = code;
        this.description = description;
    }
}
