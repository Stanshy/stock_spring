package com.chris.fin_shark.common.exception;

import com.chris.fin_shark.common.enums.ErrorCode;
import com.chris.fin_shark.common.enums.IErrorCode;

/**
 * 資料不存在異常
 * <p>
 * 用於查詢資料時找不到對應資料的場景
 * HTTP 404
 * </p>
 *
 * 使用場景:
 * - 根據 ID 查詢資料不存在
 * - 根據條件查詢無結果
 * - 資源已被刪除
 *
 * @author chris
 * @since 2025-12-24
 */
public class DataNotFoundException extends BaseException {

    /**
     * 建構子 - 使用預設錯誤碼
     *
     * @param message 錯誤訊息
     */
    public DataNotFoundException(String message) {
        super(ErrorCode.RESOURCE_NOT_FOUND, message);
    }

    /**
     * 建構子 - 自訂錯誤碼
     *
     * @param errorCode 錯誤碼
     * @param message   錯誤訊息
     */
    public DataNotFoundException(IErrorCode errorCode, String message) {
        super(errorCode, message);
    }

    /**
     * 建構子 - 包含詳情和欄位
     *
     * @param errorCode 錯誤碼
     * @param message   錯誤訊息
     * @param details   錯誤詳情
     * @param field     錯誤欄位
     */
    public DataNotFoundException(IErrorCode errorCode, String message,
                                 String details, String field) {
        super(errorCode, message, details, field, "Please check the " + field + " and try again");
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
    public DataNotFoundException(IErrorCode errorCode, String message, String details,
                                 String field, String suggestion) {
        super(errorCode, message, details, field, suggestion);
    }

    /**
     * 靜態工廠方法 - 通用資源不存在
     * <p>
     * 使用 Common 通用錯誤碼
     * </p>
     *
     * @param resourceType 資源類型（例如: Stock, Order）
     * @param resourceId   資源 ID
     * @return DataNotFoundException
     */
    public static DataNotFoundException notFound(String resourceType, String resourceId) {
        return new DataNotFoundException(
                ErrorCode.RESOURCE_NOT_FOUND,
                resourceType + " not found",
                resourceType + " with ID '" + resourceId + "' does not exist",
                resourceType.toLowerCase() + "_id",
                "Please check the " + resourceType.toLowerCase() + "_id and try again"
        );
    }

    /**
     * 靜態工廠方法 - 股票不存在（使用通用錯誤碼）
     * <p>
     * 注意: M06 模組應定義自己的 StockNotFoundException 使用 M06ErrorCode
     * </p>
     *
     * @param stockId 股票代碼
     * @return DataNotFoundException
     */
    @Deprecated
    public static DataNotFoundException stockNotFound(String stockId) {
        return notFound("Stock", stockId);
    }
}
