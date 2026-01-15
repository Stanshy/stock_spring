package com.chris.fin_shark.m10.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 趨勢方向列舉
 *
 * @author chris
 * @since 1.0.0
 */
@Getter
@RequiredArgsConstructor
public enum TrendDirection {

    /**
     * 上升趨勢 - 連續更高的高點和更高的低點
     */
    UPTREND("UPTREND", "上升趨勢", 1),

    /**
     * 下降趨勢 - 連續更低的高點和更低的低點
     */
    DOWNTREND("DOWNTREND", "下降趨勢", -1),

    /**
     * 盤整/橫盤 - 價格在區間內震盪
     */
    SIDEWAYS("SIDEWAYS", "盤整", 0),

    /**
     * 未知 - 無法判斷趨勢
     */
    UNKNOWN("UNKNOWN", "未知", 0);

    private final String code;
    private final String nameZh;
    private final int direction;

    /**
     * 是否為上升趨勢
     */
    public boolean isUp() {
        return this == UPTREND;
    }

    /**
     * 是否為下降趨勢
     */
    public boolean isDown() {
        return this == DOWNTREND;
    }

    /**
     * 是否為盤整
     */
    public boolean isSideways() {
        return this == SIDEWAYS;
    }

    /**
     * 是否有明確趨勢
     */
    public boolean hasTrend() {
        return this == UPTREND || this == DOWNTREND;
    }

    /**
     * 取得相反方向
     */
    public TrendDirection opposite() {
        return switch (this) {
            case UPTREND -> DOWNTREND;
            case DOWNTREND -> UPTREND;
            default -> this;
        };
    }

    /**
     * 根據代碼查找
     */
    public static TrendDirection fromCode(String code) {
        for (TrendDirection direction : values()) {
            if (direction.getCode().equals(code)) {
                return direction;
            }
        }
        return UNKNOWN;
    }
}
