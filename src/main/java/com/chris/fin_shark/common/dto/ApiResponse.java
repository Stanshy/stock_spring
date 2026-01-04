package com.chris.fin_shark.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;

/**
 * 統一 API 成功回應包裝類
 *
 * 遵守總綱 4.4.2 API Response 統一格式規範
 * 所有 API 成功回應必須使用此類包裝
 *
 * JSON 輸出範例:
 * {
 *   "code": 200,
 *   "message": "Success",
 *   "data": { ... },
 *   "timestamp": "2024-12-22T13:30:00+08:00",
 *   "trace_id": "req_abc123xyz"
 * }
 *
 * @param <T> 回應資料的泛型類型
 * @author chris
 * @since 2025-12-24
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)  // null 值不序列化
public class ApiResponse<T> {

    /**
     * 回應狀態碼（HTTP Status）
     * 成功時為 200, 201, 204 等
     */
    private Integer code;

    /**
     * 回應訊息
     * 例如: "Success", "Created successfully"
     */
    private String message;

    /**
     * 回應資料（泛型）
     * 可以是單一物件、PageResponse 或其他 DTO
     */
    private T data;

    /**
     * 回應時間戳（ISO 8601 格式，包含時區）
     * 格式: 2024-12-22T13:30:00+08:00
     */
    @Builder.Default
    private ZonedDateTime timestamp = ZonedDateTime.now();

    /**
     * 請求追蹤 ID（用於日誌追蹤）
     * 格式: req_xxxxxxx
     * JSON 輸出為 snake_case: "trace_id"
     */
    @JsonProperty("trace_id")
    private String traceId;

    // ==================== 靜態工廠方法 ====================

    /**
     * 成功回應（無資料）
     * HTTP 200
     */
    public static <T> ApiResponse<T> success() {
        return ApiResponse.<T>builder()
                .code(200)
                .message("Success")
                .timestamp(ZonedDateTime.now())
                .build();
    }

    /**
     * 成功回應（包含資料）
     * HTTP 200
     */
    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
                .code(200)
                .message("Success")
                .data(data)
                .timestamp(ZonedDateTime.now())
                .build();
    }

    /**
     * 成功回應（自訂訊息）
     * HTTP 200
     */
    public static <T> ApiResponse<T> success(String message, T data) {
        return ApiResponse.<T>builder()
                .code(200)
                .message(message)
                .data(data)
                .timestamp(ZonedDateTime.now())
                .build();
    }

    /**
     * 建立成功回應
     * HTTP 201
     */
    public static <T> ApiResponse<T> created(T data) {
        return ApiResponse.<T>builder()
                .code(201)
                .message("Created successfully")
                .data(data)
                .timestamp(ZonedDateTime.now())
                .build();
    }

    /**
     * 無內容回應
     * HTTP 204
     */
    public static <T> ApiResponse<T> noContent() {
        return ApiResponse.<T>builder()
                .code(204)
                .message("No content")
                .timestamp(ZonedDateTime.now())
                .build();
    }

    /**
     * 設定追蹤 ID（鏈式調用）
     */
    public ApiResponse<T> withTraceId(String traceId) {
        this.traceId = traceId;
        return this;
    }
}
