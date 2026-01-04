package com.chris.fin_shark.common.exception;

import com.chris.fin_shark.common.enums.ErrorCode;
import com.chris.fin_shark.common.enums.IErrorCode;

/**
 * 資料驗證異常
 * <p>
 * 用於資料品質檢查失敗、業務規則驗證失敗等場景
 * HTTP 422
 * </p>
 *
 * 使用場景:
 * - 資料完整性檢查失敗（如必填欄位為空）
 * - 資料一致性檢查失敗（如四價關係不一致）
 * - 資料有效性檢查失敗（如價格為負數）
 * - 業務規則驗證失敗
 *
 * @author chris
 * @since 2025-12-24
 */
public class DataValidationException extends BaseException {

    /**
     * 建構子 - 使用預設錯誤碼
     *
     * @param message 錯誤訊息
     */
    public DataValidationException(String message) {
        super(ErrorCode.VALIDATION_ERROR, message);
    }

    /**
     * 建構子 - 自訂錯誤碼
     *
     * @param errorCode 錯誤碼
     * @param message   錯誤訊息
     */
    public DataValidationException(IErrorCode errorCode, String message) {
        super(errorCode, message);
    }

    /**
     * 建構子 - 包含詳情
     *
     * @param errorCode 錯誤碼
     * @param message   錯誤訊息
     * @param details   錯誤詳情
     */
    public DataValidationException(IErrorCode errorCode, String message, String details) {
        super(errorCode, message, details);
    }

    /**
     * 建構子 - 包含欄位和建議
     *
     * @param errorCode  錯誤碼
     * @param message    錯誤訊息
     * @param details    錯誤詳情
     * @param field      錯誤欄位
     * @param suggestion 建議解決方案
     */
    public DataValidationException(IErrorCode errorCode, String message, String details,
                                   String field, String suggestion) {
        super(errorCode, message, details, field, suggestion);
    }

    /**
     * 靜態工廠方法 - 通用驗證失敗
     * <p>
     * 使用 Common 通用錯誤碼
     * </p>
     *
     * @param field   驗證失敗的欄位名稱
     * @param message 錯誤訊息
     * @return DataValidationException
     */
    public static DataValidationException validationFailed(String field, String message) {
        return new DataValidationException(
                ErrorCode.VALIDATION_ERROR,
                "Validation failed",
                message,
                field,
                "Please check the " + field + " value and try again"
        );
    }

    /**
     * 靜態工廠方法 - 股價四價關係驗證失敗（使用通用錯誤碼）
     * <p>
     * 注意: M06 模組應定義自己的驗證異常使用 M06ErrorCode
     * </p>
     *
     * @param stockId   股票代碼
     * @param tradeDate 交易日期
     * @param details   詳細錯誤訊息
     * @return DataValidationException
     */
    @Deprecated
    public static DataValidationException fourPriceValidationFailed(
            String stockId, String tradeDate, String details) {
        return new DataValidationException(
                ErrorCode.VALIDATION_ERROR,
                "Stock price validation failed",
                String.format("Stock %s on %s: %s", stockId, tradeDate, details),
                "price_data",
                "Please check the price data consistency"
        );
    }
}
