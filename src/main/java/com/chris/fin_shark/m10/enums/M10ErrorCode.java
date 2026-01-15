package com.chris.fin_shark.m10.enums;

import com.chris.fin_shark.common.enums.IErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * M10 模組錯誤碼
 *
 * @author chris
 * @since 1.0.0
 */
@Getter
@RequiredArgsConstructor
public enum M10ErrorCode implements IErrorCode {

    // 型態相關錯誤 (M10_PTN_xxx)
    STOCK_NOT_FOUND("M10_PTN_001", 404, "股票代碼不存在"),
    INSUFFICIENT_DATA("M10_PTN_002", 400, "價格資料不足，無法偵測型態"),
    DATE_OUT_OF_RANGE("M10_PTN_003", 400, "分析日期超出範圍"),
    UNSUPPORTED_PATTERN("M10_PTN_004", 400, "不支援的型態類型"),
    ENGINE_ERROR("M10_PTN_005", 500, "偵測引擎異常"),
    INVALID_STRENGTH_THRESHOLD("M10_PTN_006", 400, "型態強度閾值無效"),

    // 參數相關錯誤 (M10_PARAM_xxx)
    INVALID_PARAM("M10_PARAM_001", 400, "參數格式錯誤"),
    INVALID_DATE_RANGE("M10_PARAM_002", 400, "日期範圍無效"),

    // 偵測器相關錯誤 (M10_DET_xxx)
    DETECTOR_NOT_FOUND("M10_DET_001", 500, "找不到偵測器"),
    DETECTOR_ERROR("M10_DET_002", 500, "偵測器執行失敗");

    private final String code;
    private final int httpStatus;
    private final String defaultMessage;

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public int getHttpStatus() {
        return httpStatus;
    }

    @Override
    public String getDefaultMessage() {
        return defaultMessage;
    }

    /**
     * 根據代碼查找錯誤碼
     */
    public static M10ErrorCode fromCode(String code) {
        for (M10ErrorCode errorCode : values()) {
            if (errorCode.getCode().equals(code)) {
                return errorCode;
            }
        }
        throw new IllegalArgumentException("Unknown error code: " + code);
    }
}
