package com.chris.fin_shark.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 錯誤詳情物件
 *
 * 作為 ErrorResponse 的巢狀物件，包含詳細的錯誤資訊
 *
 * JSON 輸出範例:
 * {
 *   "error_code": "RESOURCE_NOT_FOUND",
 *   "error_type": "CLIENT_ERROR",
 *   "details": "Stock with ID '9999' does not exist",
 *   "field": "stock_id",
 *   "suggestion": "Please check the stock ID and try again"
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
public class ErrorDetail {

    /**
     * 業務錯誤碼
     *
     * 格式: 
     * - 通用錯誤: 00xxx (如 "RESOURCE_NOT_FOUND" 對應 00004)
     * - 模組錯誤: M{模組編號}xxx (如 "M06001", "M07002")
     *
     * JSON 輸出為 snake_case: "error_code"
     */
    @JsonProperty("error_code")
    private String errorCode;

    /**
     * 錯誤類型
     *
     * 可能的值:
     * - CLIENT_ERROR: 客戶端錯誤 (4xx)
     * - SERVER_ERROR: 伺服器錯誤 (5xx)
     * - VALIDATION_ERROR: 參數驗證錯誤
     * - BUSINESS_ERROR: 業務邏輯錯誤
     *
     * JSON 輸出為 snake_case: "error_type"
     */
    @JsonProperty("error_type")
    private String errorType;

    /**
     * 錯誤詳細說明（給開發者看）
     * 例如: "Stock with ID '9999' does not exist"
     */
    private String details;

    /**
     * 錯誤相關的欄位名稱（可選）
     * 例如: "stock_id", "start_date"
     */
    private String field;

    /**
     * 建議的解決方案（可選）
     * 例如: "Please check the stock ID and try again"
     */
    private String suggestion;

    /**
     * 建立錯誤詳情
     */
    public static ErrorDetail of(String errorCode, String errorType, String details) {
        return ErrorDetail.builder()
                .errorCode(errorCode)
                .errorType(errorType)
                .details(details)
                .build();
    }
}
