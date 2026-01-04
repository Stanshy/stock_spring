package com.chris.fin_shark.common.enums;

import lombok.Getter;

/**
 * 通用錯誤碼定義（僅包含 00xxx 系列）
 * <p>
 * 遵守總綱 4.4.4 錯誤碼定義規範
 * 各模組專屬錯誤碼請在各模組的 enums 包中定義
 * </p>
 *
 * 錯誤碼格式:
 * - 通用錯誤: 00xxx (如 00001, 00002)
 * - 模組錯誤: M{模組編號}xxx (如 M06001, M07002) → 在各模組定義
 *
 * @author chris
 * @since 2025-12-24
 */
@Getter
public enum ErrorCode implements IErrorCode {

    // ========================================================================
    // 通用錯誤 (00xxx)
    // ========================================================================

    /**
     * 無效的請求參數
     * HTTP 400
     */
    INVALID_REQUEST("00001", 400, "Invalid request parameters"),

    /**
     * 需要認證
     * HTTP 401
     */
    UNAUTHORIZED("00002", 401, "Authentication required"),

    /**
     * 禁止訪問
     * HTTP 403
     */
    FORBIDDEN("00003", 403, "Access denied"),

    /**
     * 資源不存在
     * HTTP 404
     */
    RESOURCE_NOT_FOUND("00004", 404, "Resource not found"),

    /**
     * 資源衝突（如重複創建）
     * HTTP 409
     */
    CONFLICT("00005", 409, "Resource conflict"),

    /**
     * 內部伺服器錯誤
     * HTTP 500
     */
    INTERNAL_ERROR("00006", 500, "Internal server error"),

    /**
     * 服務暫時不可用
     * HTTP 503
     */
    SERVICE_UNAVAILABLE("00007", 503, "Service unavailable"),

    /**
     * 參數驗證失敗
     * HTTP 400
     */
    VALIDATION_ERROR("00008", 400, "Validation failed"),

    /**
     * 請求次數超過限制
     * HTTP 429
     */
    RATE_LIMIT_EXCEEDED("00009", 429, "Rate limit exceeded"),

    /**
     * 請求格式錯誤
     * HTTP 400
     */
    BAD_REQUEST("00010", 400, "Bad request"),

    /**
     * 不支援的媒體類型
     * HTTP 415
     */
    UNSUPPORTED_MEDIA_TYPE("00011", 415, "Unsupported media type"),

    /**
     * 請求實體過大
     * HTTP 413
     */
    PAYLOAD_TOO_LARGE("00012", 413, "Payload too large");

    // ========================================================================
    // 模組專屬錯誤碼請在各模組定義
    // ========================================================================
    // M06 資料管理模組 → m06.enums.M06ErrorCode
    // M07 技術分析模組 → m07.enums.M07ErrorCode
    // M08 基本面分析模組 → m08.enums.M08ErrorCode
    // M09 籌碼分析模組 → m09.enums.M09ErrorCode
    // M10 技術型態辨識模組 → m10.enums.M10ErrorCode
    // M11 量化策略模組 → m11.enums.M11ErrorCode
    // M12 總經與產業分析模組 → m12.enums.M12ErrorCode
    // M13 信號判斷引擎 → m13.enums.M13ErrorCode
    // M14 選股引擎 → m14.enums.M14ErrorCode
    // M15 警報通知系統 → m15.enums.M15ErrorCode
    // M16 回測系統 → m16.enums.M16ErrorCode
    // M17 風險管理模組 → m17.enums.M17ErrorCode
    // M18 投資組合管理 → m18.enums.M18ErrorCode
    // ========================================================================

    // ========================================================================
    // 欄位定義
    // ========================================================================

    /**
     * 錯誤碼
     * 例如: "00001", "00002"
     */
    private final String code;

    /**
     * HTTP 狀態碼
     * 例如: 400, 404, 500
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
    ErrorCode(String code, int httpStatus, String defaultMessage) {
        this.code = code;
        this.httpStatus = httpStatus;
        this.defaultMessage = defaultMessage;
    }

    /**
     * 根據錯誤碼字串查找 ErrorCode
     *
     * @param code 錯誤碼字串
     * @return ErrorCode 或 null
     */
    public static ErrorCode fromCode(String code) {
        for (ErrorCode errorCode : values()) {
            if (errorCode.code.equals(code)) {
                return errorCode;
            }
        }
        return null;
    }
}