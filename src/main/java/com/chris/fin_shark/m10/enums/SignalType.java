package com.chris.fin_shark.m10.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 訊號類型列舉
 *
 * @author chris
 * @since 1.0.0
 */
@Getter
@RequiredArgsConstructor
public enum SignalType {

    // 反轉訊號
    BULLISH_REVERSAL("BULLISH_REVERSAL", "看漲反轉", true),
    BEARISH_REVERSAL("BEARISH_REVERSAL", "看跌反轉", false),

    // 持續訊號
    BULLISH_CONTINUATION("BULLISH_CONTINUATION", "看漲持續", true),
    BEARISH_CONTINUATION("BEARISH_CONTINUATION", "看跌持續", false),

    // 中性訊號
    NEUTRAL_REVERSAL("NEUTRAL_REVERSAL", "中性反轉警示", null),
    NEUTRAL("NEUTRAL", "中性", null);

    private final String code;
    private final String nameZh;
    private final Boolean bullish;

    /**
     * 是否為看漲訊號
     */
    public boolean isBullish() {
        return Boolean.TRUE.equals(bullish);
    }

    /**
     * 是否為看跌訊號
     */
    public boolean isBearish() {
        return Boolean.FALSE.equals(bullish);
    }

    /**
     * 是否為中性訊號
     */
    public boolean isNeutral() {
        return bullish == null;
    }

    /**
     * 是否為反轉訊號
     */
    public boolean isReversal() {
        return this == BULLISH_REVERSAL || this == BEARISH_REVERSAL || this == NEUTRAL_REVERSAL;
    }

    /**
     * 是否為持續訊號
     */
    public boolean isContinuation() {
        return this == BULLISH_CONTINUATION || this == BEARISH_CONTINUATION;
    }

    /**
     * 根據代碼查找
     */
    public static SignalType fromCode(String code) {
        for (SignalType type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown signal type: " + code);
    }
}
