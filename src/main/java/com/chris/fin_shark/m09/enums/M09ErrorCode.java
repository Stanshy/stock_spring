package com.chris.fin_shark.m09.enums;

import com.chris.fin_shark.common.enums.IErrorCode;
import lombok.Getter;

/**
 * M09 籌碼分析模組專屬錯誤碼
 * <p>
 * 編碼規則: M09xxx
 * - M0901x: 籌碼查詢相關
 * - M0902x: 籌碼計算相關
 * - M0903x: 訊號偵測相關
 * - M0904x: Job 執行相關
 * - M0905x: 參數驗證相關
 * </p>
 *
 * @author chris
 * @since 1.0.0
 */
@Getter
public enum M09ErrorCode implements IErrorCode {

    // ========== 籌碼查詢相關 M0901x ==========

    /**
     * 股票代碼不存在
     */
    M09_STOCK_NOT_FOUND("M09011", 404, "Stock not found"),

    /**
     * 籌碼資料不存在
     */
    M09_CHIP_DATA_NOT_FOUND("M09012", 404, "Chip analysis data not found"),

    /**
     * 無效的籌碼類別
     */
    M09_INVALID_CATEGORY("M09013", 400, "Invalid chip category"),

    // ========== 籌碼計算相關 M0902x ==========

    /**
     * 籌碼資料不足，無法計算
     */
    M09_INSUFFICIENT_DATA("M09021", 422, "Insufficient chip data for calculation"),

    /**
     * 籌碼計算失敗
     */
    M09_CALCULATION_FAILED("M09022", 500, "Chip calculation failed"),

    /**
     * 計算日期超出範圍
     */
    M09_DATE_OUT_OF_RANGE("M09023", 400, "Calculation date out of range"),

    /**
     * 不支援的指標類型
     */
    M09_UNSUPPORTED_INDICATOR("M09024", 400, "Unsupported chip indicator type"),

    /**
     * 計算引擎異常
     */
    M09_ENGINE_ERROR("M09025", 500, "Chip calculation engine error"),

    // ========== 訊號偵測相關 M0903x ==========

    /**
     * 訊號不存在
     */
    M09_SIGNAL_NOT_FOUND("M09031", 404, "Chip signal not found"),

    /**
     * 無效的訊號類型
     */
    M09_INVALID_SIGNAL_TYPE("M09032", 400, "Invalid signal type"),

    // ========== Job 執行相關 M0904x ==========

    /**
     * Job 執行記錄不存在
     */
    M09_JOB_NOT_FOUND("M09041", 404, "Job execution record not found"),

    /**
     * Job 已在執行中
     */
    M09_JOB_ALREADY_RUNNING("M09042", 409, "Job is already running"),

    // ========== 參數驗證相關 M0905x ==========

    /**
     * 參數格式錯誤
     */
    M09_INVALID_PARAM_FORMAT("M09051", 400, "Invalid parameter format"),

    /**
     * 日期範圍無效
     */
    M09_INVALID_DATE_RANGE("M09052", 400, "Invalid date range");

    /**
     * 錯誤碼
     */
    private final String code;

    /**
     * HTTP 狀態碼
     */
    private final int httpStatus;

    /**
     * 預設錯誤訊息
     */
    private final String defaultMessage;

    /**
     * 建構子
     *
     * @param code           錯誤碼
     * @param httpStatus     HTTP 狀態碼
     * @param defaultMessage 預設錯誤訊息
     */
    M09ErrorCode(String code, int httpStatus, String defaultMessage) {
        this.code = code;
        this.httpStatus = httpStatus;
        this.defaultMessage = defaultMessage;
    }
}
