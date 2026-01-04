package com.chris.fin_shark.common.exception;

import com.chris.fin_shark.common.enums.IErrorCode;
import lombok.Getter;

/**
 * 基礎異常類別
 * <p>
 * 所有自訂異常的父類，提供統一的異常結構
 * 支援 Common 通用錯誤碼（ErrorCode）和各模組專屬錯誤碼（M06ErrorCode, M07ErrorCode...）
 * </p>
 *
 * @author chris
 * @since 2025-12-24
 */
@Getter
public class BaseException extends RuntimeException {

    /**
     * 錯誤碼（實作 IErrorCode 介面）
     */
    private final IErrorCode errorCode;

    /**
     * HTTP 狀態碼
     */
    private final Integer httpStatus;

    /**
     * 錯誤詳情（給開發者看的詳細資訊）
     */
    private final String details;

    /**
     * 錯誤欄位（可選，用於參數驗證錯誤）
     */
    private final String field;

    /**
     * 建議的解決方案（可選）
     */
    private final String suggestion;

    /**
     * 建構子 - 最簡版本
     *
     * @param errorCode 錯誤碼
     */
    public BaseException(IErrorCode errorCode) {
        super(errorCode.getDefaultMessage());
        this.errorCode = errorCode;
        this.httpStatus = errorCode.getHttpStatus();
        this.details = null;
        this.field = null;
        this.suggestion = null;
    }

    /**
     * 建構子 - 自訂訊息
     *
     * @param errorCode 錯誤碼
     * @param message   自訂錯誤訊息
     */
    public BaseException(IErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
        this.httpStatus = errorCode.getHttpStatus();
        this.details = null;
        this.field = null;
        this.suggestion = null;
    }

    /**
     * 建構子 - 包含詳情
     *
     * @param errorCode 錯誤碼
     * @param message   錯誤訊息
     * @param details   錯誤詳情
     */
    public BaseException(IErrorCode errorCode, String message, String details) {
        super(message);
        this.errorCode = errorCode;
        this.httpStatus = errorCode.getHttpStatus();
        this.details = details;
        this.field = null;
        this.suggestion = null;
    }

    /**
     * 建構子 - 完整版本
     *
     * @param errorCode  錯誤碼
     * @param message    錯誤訊息
     * @param details    錯誤詳情
     * @param field      錯誤欄位
     * @param suggestion 建議解決方案
     */
    public BaseException(IErrorCode errorCode, String message, String details,
                         String field, String suggestion) {
        super(message);
        this.errorCode = errorCode;
        this.httpStatus = errorCode.getHttpStatus();
        this.details = details;
        this.field = field;
        this.suggestion = suggestion;
    }

    /**
     * 建構子 - 包含原因異常
     *
     * @param errorCode 錯誤碼
     * @param message   錯誤訊息
     * @param cause     原因異常
     */
    public BaseException(IErrorCode errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.httpStatus = errorCode.getHttpStatus();
        this.details = cause != null ? cause.getMessage() : null;
        this.field = null;
        this.suggestion = null;
    }

    /**
     * 建構子 - 完整版本（包含原因異常）
     *
     * @param errorCode  錯誤碼
     * @param message    錯誤訊息
     * @param details    錯誤詳情
     * @param field      錯誤欄位
     * @param suggestion 建議解決方案
     * @param cause      原因異常
     */
    public BaseException(IErrorCode errorCode, String message, String details,
                         String field, String suggestion, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.httpStatus = errorCode.getHttpStatus();
        this.details = details;
        this.field = field;
        this.suggestion = suggestion;
    }

    /**
     * 取得錯誤碼字串
     * <p>
     * 用於 API 回應中的 error_code 欄位
     * </p>
     *
     * @return 錯誤碼字串
     */
    public String getErrorCodeString() {
        return errorCode.getCode();
    }
}
