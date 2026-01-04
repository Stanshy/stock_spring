package com.chris.fin_shark.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;

/**
 * 統一錯誤回應類
 *
 * 遵守總綱 4.4.2 API Response 統一格式規範
 * 所有 API 錯誤回應必須使用此類
 *
 * JSON 輸出範例:
 * {
 *   "code": 404,
 *   "message": "Stock not found",
 *   "error": {
 *     "error_code": "RESOURCE_NOT_FOUND",
 *     "error_type": "CLIENT_ERROR",
 *     "details": "Stock with ID '9999' does not exist",
 *     "field": "stock_id",
 *     "suggestion": "Please check the stock ID and try again"
 *   },
 *   "timestamp": "2024-12-22T13:30:00+08:00",
 *   "trace_id": "req_abc123xyz"
 * }
 *
 * @author chris
 * @since 2025-12-24
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {

    /**
     * HTTP 狀態碼
     * 例如: 400, 404, 500
     */
    private Integer code;

    /**
     * 錯誤訊息（簡短說明）
     * 例如: "Stock not found", "Invalid request parameters"
     */
    private String message;

    /**
     * 錯誤詳細資訊（巢狀物件）
     * 包含 error_code, error_type, details 等
     */
    private ErrorDetail error;

    /**
     * 錯誤發生的時間戳（ISO 8601 格式，包含時區）
     * 格式: 2024-12-22T13:30:00+08:00
     */
    @Builder.Default
    private ZonedDateTime timestamp = ZonedDateTime.now();

    /**
     * 請求追蹤 ID
     * 格式: req_xxxxxxx
     * JSON 輸出為 snake_case: "trace_id"
     */
    @JsonProperty("trace_id")
    private String traceId;

    // ==================== 靜態工廠方法 ====================

    /**
     * 建立錯誤回應（基本版本）
     *
     * @param code HTTP 狀態碼
     * @param errorCode 業務錯誤碼
     * @param message 錯誤訊息
     * @return ErrorResponse
     */
    public static ErrorResponse of(Integer code, String errorCode, String message) {
        ErrorDetail errorDetail = ErrorDetail.builder()
                .errorCode(errorCode)
                .errorType(determineErrorType(code))
                .build();

        return ErrorResponse.builder()
                .code(code)
                .message(message)
                .error(errorDetail)
                .timestamp(ZonedDateTime.now())
                .build();
    }

    /**
     * 建立錯誤回應（包含詳細資訊）
     */
    public static ErrorResponse of(Integer code, String errorCode, String message, String details) {
        ErrorDetail errorDetail = ErrorDetail.builder()
                .errorCode(errorCode)
                .errorType(determineErrorType(code))
                .details(details)
                .build();

        return ErrorResponse.builder()
                .code(code)
                .message(message)
                .error(errorDetail)
                .timestamp(ZonedDateTime.now())
                .build();
    }

    /**
     * 建立錯誤回應（完整版本）
     */
    public static ErrorResponse of(Integer code, String errorCode, String message,
                                   String details, String field, String suggestion) {
        ErrorDetail errorDetail = ErrorDetail.builder()
                .errorCode(errorCode)
                .errorType(determineErrorType(code))
                .details(details)
                .field(field)
                .suggestion(suggestion)
                .build();

        return ErrorResponse.builder()
                .code(code)
                .message(message)
                .error(errorDetail)
                .timestamp(ZonedDateTime.now())
                .build();
    }

    /**
     * 設定追蹤 ID（鏈式調用）
     */
    public ErrorResponse withTraceId(String traceId) {
        this.traceId = traceId;
        return this;
    }

    /**
     * 根據 HTTP 狀態碼判斷錯誤類型
     */
    private static String determineErrorType(Integer code) {
        if (code >= 400 && code < 500) {
            return "CLIENT_ERROR";
        } else if (code >= 500) {
            return "SERVER_ERROR";
        }
        return "UNKNOWN_ERROR";
    }
}
