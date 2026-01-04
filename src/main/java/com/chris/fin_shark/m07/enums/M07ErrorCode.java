package com.chris.fin_shark.m07.enums;

import com.chris.fin_shark.common.enums.IErrorCode;
import lombok.Getter;

/**
 * M07 技術分析模組專屬錯誤碼
 * <p>
 * 編碼規則: M07xxx
 * - M0701x: 指標查詢相關
 * - M0702x: 指標計算相關
 * - M0703x: 指標定義相關
 * - M0704x: Job 執行相關
 * </p>
 *
 * @author chris
 * @since 1.0.0
 */
@Getter
public enum M07ErrorCode implements IErrorCode {

    // ========== 指標查詢相關 M0701x ==========

    /**
     * 指標不存在
     */
    M07_INDICATOR_NOT_FOUND("M07011", 404, "Indicator not found"),

    /**
     * 無效的指標名稱
     */
    M07_INVALID_INDICATOR_NAME("M07012", 400, "Invalid indicator name"),

    /**
     * 無效的指標類別
     */
    M07_INVALID_CATEGORY("M07013", 400, "Invalid indicator category"),

    // ========== 指標計算相關 M0702x ==========

    /**
     * 指標計算失敗
     */
    M07_CALCULATION_FAILED("M07021", 500, "Indicator calculation failed"),

    /**
     * 資料不足無法計算
     */
    M07_INSUFFICIENT_DATA("M07022", 422, "Insufficient data for calculation"),

    /**
     * 無效的計算參數
     */
    M07_INVALID_CALCULATION_PARAMS("M07023", 400, "Invalid calculation parameters"),

    // ========== 指標定義相關 M0703x ==========

    /**
     * 指標定義不存在
     */
    M07_DEFINITION_NOT_FOUND("M07031", 404, "Indicator definition not found"),

    /**
     * 指標定義已存在
     */
    M07_DEFINITION_ALREADY_EXISTS("M07032", 409, "Indicator definition already exists"),

    // ========== Job 執行相關 M0704x ==========

    /**
     * Job 執行記錄不存在
     */
    M07_JOB_NOT_FOUND("M07041", 404, "Job execution record not found"),

    /**
     * Job 已在執行中
     */
    M07_JOB_ALREADY_RUNNING("M07042", 409, "Job is already running");

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
    M07ErrorCode(String code, int httpStatus, String defaultMessage) {
        this.code = code;
        this.httpStatus = httpStatus;
        this.defaultMessage = defaultMessage;
    }
}

