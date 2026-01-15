package com.chris.fin_shark.m10.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 型態類別列舉
 *
 * @author chris
 * @since 1.0.0
 */
@Getter
@RequiredArgsConstructor
public enum PatternCategory {

    // K 線型態
    KLINE_SINGLE("KLINE_SINGLE", "單根K線型態", "Single K-Line Pattern"),
    KLINE_DOUBLE("KLINE_DOUBLE", "雙根K線型態", "Double K-Line Pattern"),
    KLINE_TRIPLE("KLINE_TRIPLE", "三根K線型態", "Triple K-Line Pattern"),
    KLINE_MULTI("KLINE_MULTI", "多根K線型態", "Multi K-Line Pattern"),

    // 圖表型態
    CHART_REVERSAL("CHART_REVERSAL", "圖表反轉型態", "Chart Reversal Pattern"),
    CHART_CONTINUATION("CHART_CONTINUATION", "圖表持續型態", "Chart Continuation Pattern"),
    CHART_GAP("CHART_GAP", "缺口型態", "Gap Pattern"),
    CHART_BILATERAL("CHART_BILATERAL", "雙向突破型態", "Bilateral Pattern"),

    // 趨勢型態
    TREND("TREND", "趨勢型態", "Trend Pattern"),

    // 支撐壓力
    SUPPORT_RESISTANCE("SUPPORT_RESISTANCE", "支撐壓力", "Support & Resistance"),

    // 訊號
    SIGNAL("SIGNAL", "訊號", "Signal");

    private final String code;
    private final String nameZh;
    private final String nameEn;

    /**
     * 根據代碼查找類別
     */
    public static PatternCategory fromCode(String code) {
        for (PatternCategory category : values()) {
            if (category.getCode().equals(code)) {
                return category;
            }
        }
        throw new IllegalArgumentException("Unknown pattern category: " + code);
    }

    /**
     * 是否為 K 線型態
     */
    public boolean isKLinePattern() {
        return this == KLINE_SINGLE || this == KLINE_DOUBLE ||
               this == KLINE_TRIPLE || this == KLINE_MULTI;
    }

    /**
     * 是否為圖表型態
     */
    public boolean isChartPattern() {
        return this == CHART_REVERSAL || this == CHART_CONTINUATION ||
               this == CHART_GAP || this == CHART_BILATERAL;
    }
}
