package com.chris.fin_shark.m06.exception;

import com.chris.fin_shark.common.exception.BusinessException;
import com.chris.fin_shark.m06.enums.M06ErrorCode;

/**
 * 資料品質異常
 * <p>
 * 當資料品質檢核失敗時拋出此異常
 * 包含四價關係驗證、資產負債表平衡等檢核
 * HTTP 狀態碼: 422
 * </p>
 *
 * @author Chris
 * @since 1.0.0
 */
public class DataQualityException extends BusinessException {

    /**
     * 私有建構子
     *
     * @param errorCode  M06 錯誤碼
     * @param message    錯誤訊息
     * @param details    詳細錯誤訊息
     * @param field      錯誤欄位
     */
    private DataQualityException(M06ErrorCode errorCode, String message,
                                 String details, String field) {
        super(errorCode, message, details, field, "Please check the data quality");
    }

    /**
     * 工廠方法 - 四價關係驗證失敗
     * <p>
     * 驗證規則: low_price <= open_price, close_price <= high_price
     * </p>
     *
     * @param stockId 股票代碼
     * @param date    交易日期
     * @param details 詳細錯誤訊息
     * @return 資料品質異常實例
     */
    public static DataQualityException fourPriceViolation(String stockId, String date, String details) {
        return new DataQualityException(
                M06ErrorCode.M06_FOUR_PRICE_VIOLATION,
                "Stock price validation failed",                    // message
                String.format("Stock %s on %s: %s", stockId, date, details),  // details
                "price_data"                                        // field
        );
    }

    /**
     * 工廠方法 - 資產負債表不平衡
     * <p>
     * 驗證規則: total_assets = total_liabilities + equity
     * </p>
     *
     * @param stockId 股票代碼
     * @param year    年份
     * @param quarter 季度
     * @return 資料品質異常實例
     */
    public static DataQualityException balanceSheetNotBalanced(String stockId, int year, int quarter) {
        return new DataQualityException(
                M06ErrorCode.M06_BALANCE_SHEET_NOT_BALANCED,
                "Balance sheet validation failed",                  // message
                String.format("Stock %s %dQ%d: Assets ≠ Liabilities + Equity",
                        stockId, year, quarter),              // details
                "balance_sheet"                                     // field
        );
    }

    /**
     * 工廠方法 - 一般資料驗證失敗
     *
     * @param message 錯誤訊息
     * @param details 詳細錯誤訊息
     * @return 資料品質異常實例
     */
    public static DataQualityException validationFailed(String message, String details) {
        return new DataQualityException(
                M06ErrorCode.M06_DATA_VALIDATION_FAILED,
                message,                                            // message
                details,                                            // details
                "data"                                              // field
        );
    }
}
