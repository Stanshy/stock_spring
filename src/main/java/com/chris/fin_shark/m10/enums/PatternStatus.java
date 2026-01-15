package com.chris.fin_shark.m10.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 型態狀態列舉
 *
 * @author chris
 * @since 1.0.0
 */
@Getter
@RequiredArgsConstructor
public enum PatternStatus {

    /**
     * 形成中 - 型態尚未完成，正在發展
     */
    FORMING("FORMING", "形成中"),

    /**
     * 已確認 - 型態已完成形成，等待突破
     */
    CONFIRMED("CONFIRMED", "已確認"),

    /**
     * 已完成 - 型態已突破並達到目標
     */
    COMPLETED("COMPLETED", "已完成"),

    /**
     * 失敗 - 型態突破後未達目標
     */
    FAILED("FAILED", "失敗"),

    /**
     * 無效 - 型態被破壞，不再有效
     */
    INVALIDATED("INVALIDATED", "已失效");

    private final String code;
    private final String nameZh;

    /**
     * 是否為最終狀態
     */
    public boolean isFinalStatus() {
        return this == COMPLETED || this == FAILED || this == INVALIDATED;
    }

    /**
     * 是否為活躍狀態
     */
    public boolean isActive() {
        return this == FORMING || this == CONFIRMED;
    }

    /**
     * 根據代碼查找
     */
    public static PatternStatus fromCode(String code) {
        for (PatternStatus status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown pattern status: " + code);
    }
}
