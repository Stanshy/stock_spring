package com.chris.fin_shark.common.exception;

import com.chris.fin_shark.common.enums.ErrorCode;
import com.chris.fin_shark.common.enums.IErrorCode;

/**
 * 外部 API 異常
 * <p>
 * 用於調用外部 API 失敗的場景
 * HTTP 500 或 503
 * </p>
 *
 * 使用場景:
 * - Yahoo Finance API 調用失敗
 * - 證交所 API 調用失敗
 * - 公開資訊觀測站調用失敗
 * - API 超時
 * - API 返回錯誤
 *
 * @author chris
 * @since 2025-12-24
 */
public class ExternalApiException extends BaseException {

    /**
     * API 名稱
     */
    private final String apiName;

    /**
     * API URL
     */
    private final String apiUrl;

    /**
     * HTTP 狀態碼（外部 API 返回的）
     */
    private final Integer externalHttpStatus;

    /**
     * 建構子 - 基本版本
     *
     * @param message 錯誤訊息
     * @param apiName API 名稱
     */
    public ExternalApiException(String message, String apiName) {
        super(ErrorCode.INTERNAL_ERROR, message);
        this.apiName = apiName;
        this.apiUrl = null;
        this.externalHttpStatus = null;
    }

    /**
     * 建構子 - 包含 API URL
     *
     * @param errorCode 錯誤碼
     * @param message   錯誤訊息
     * @param apiName   API 名稱
     * @param apiUrl    API URL
     */
    public ExternalApiException(IErrorCode errorCode, String message,
                                String apiName, String apiUrl) {
        super(errorCode, message, "External API call failed: " + apiName);
        this.apiName = apiName;
        this.apiUrl = apiUrl;
        this.externalHttpStatus = null;
    }

    /**
     * 建構子 - 完整版本
     *
     * @param errorCode          錯誤碼
     * @param message            錯誤訊息
     * @param apiName            API 名稱
     * @param apiUrl             API URL
     * @param externalHttpStatus 外部 API HTTP 狀態碼
     * @param cause              原因異常
     */
    public ExternalApiException(IErrorCode errorCode, String message,
                                String apiName, String apiUrl,
                                Integer externalHttpStatus, Throwable cause) {
        super(errorCode, message, cause);
        this.apiName = apiName;
        this.apiUrl = apiUrl;
        this.externalHttpStatus = externalHttpStatus;
    }

    /**
     * 靜態工廠方法 - API 超時
     * <p>
     * 使用 Common 通用錯誤碼
     * 各模組可定義自己的 API 錯誤碼
     * </p>
     *
     * @param apiName API 名稱
     * @param apiUrl  API URL
     * @return ExternalApiException
     */
    public static ExternalApiException apiTimeout(String apiName, String apiUrl) {
        return new ExternalApiException(
                ErrorCode.SERVICE_UNAVAILABLE,
                "External API timeout",
                apiName,
                apiUrl
        );
    }

    /**
     * 靜態工廠方法 - API 調用失敗
     *
     * @param apiName    API 名稱
     * @param apiUrl     API URL
     * @param httpStatus 外部 API HTTP 狀態碼
     * @param cause      原因異常
     * @return ExternalApiException
     */
    public static ExternalApiException apiFailed(String apiName, String apiUrl,
                                                 Integer httpStatus, Throwable cause) {
        return new ExternalApiException(
                ErrorCode.INTERNAL_ERROR,
                "External API call failed",
                apiName,
                apiUrl,
                httpStatus,
                cause
        );
    }

    /**
     * 靜態工廠方法 - API 返回錯誤
     *
     * @param apiName       API 名稱
     * @param apiUrl        API URL
     * @param httpStatus    外部 API HTTP 狀態碼
     * @param errorMessage  外部 API 錯誤訊息
     * @return ExternalApiException
     */
    public static ExternalApiException apiError(String apiName, String apiUrl,
                                                Integer httpStatus, String errorMessage) {
        return new ExternalApiException(
                ErrorCode.INTERNAL_ERROR,
                "External API returned error",
                apiName,
                apiUrl,
                httpStatus,
                null
        );
    }

    // Getters

    /**
     * 取得 API 名稱
     *
     * @return API 名稱
     */
    public String getApiName() {
        return apiName;
    }

    /**
     * 取得 API URL
     *
     * @return API URL
     */
    public String getApiUrl() {
        return apiUrl;
    }

    /**
     * 取得外部 API HTTP 狀態碼
     *
     * @return 外部 API HTTP 狀態碼
     */
    public Integer getExternalHttpStatus() {
        return externalHttpStatus;
    }
}
