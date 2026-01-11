package com.chris.fin_shark.m06.exception;

import com.chris.fin_shark.common.exception.BusinessException;
import com.chris.fin_shark.m06.enums.M06ErrorCode;

/**
 * 資料補齊異常
 * <p>
 * 當資料補齊過程中發生錯誤時拋出此異常
 * 包含日期範圍無效、補齊策略錯誤等
 * HTTP 狀態碼: 400/500
 * </p>
 *
 * @author chris
 * @since 1.0.0
 */
public class DataRepairException extends BusinessException {

    /**
     * 私有建構子
     *
     * @param errorCode M06 錯誤碼
     * @param message   錯誤訊息
     * @param details   詳細錯誤訊息
     * @param field     錯誤欄位
     */
    private DataRepairException(M06ErrorCode errorCode, String message,
                                String details, String field) {
        super(errorCode, message, details, field, "Please check the repair parameters and retry");
    }

    /**
     * 工廠方法 - 無效的日期範圍
     *
     * @param startDate 開始日期
     * @param endDate   結束日期
     * @return 資料補齊異常實例
     */
    public static DataRepairException invalidDateRange(String startDate, String endDate) {
        return new DataRepairException(
                M06ErrorCode.M06_DATA_VALIDATION_FAILED,
                "Invalid date range for data repair",
                String.format("StartDate: %s, EndDate: %s - Start date must be before end date",
                        startDate, endDate),
                "date_range"
        );
    }

    /**
     * 工廠方法 - 無效的補齊策略
     *
     * @param strategy 補齊策略
     * @return 資料補齊異常實例
     */
    public static DataRepairException invalidStrategy(String strategy) {
        return new DataRepairException(
                M06ErrorCode.M06_DATA_VALIDATION_FAILED,
                "Invalid repair strategy",
                String.format("Strategy: %s is not supported", strategy),
                "strategy"
        );
    }

    /**
     * 工廠方法 - 補齊執行失敗
     *
     * @param dataType 資料類型
     * @param details  詳細錯誤訊息
     * @return 資料補齊異常實例
     */
    public static DataRepairException executionFailed(String dataType, String details) {
        return new DataRepairException(
                M06ErrorCode.M06_DATA_SYNC_FAILED,
                "Data repair execution failed",
                String.format("DataType: %s, Error: %s", dataType, details),
                "repair"
        );
    }

    /**
     * 工廠方法 - 沒有需要補齊的資料
     *
     * @param dataType  資料類型
     * @param startDate 開始日期
     * @param endDate   結束日期
     * @return 資料補齊異常實例
     */
    public static DataRepairException noMissingData(String dataType, String startDate, String endDate) {
        return new DataRepairException(
                M06ErrorCode.M06_DATA_VALIDATION_FAILED,
                "No missing data found",
                String.format("DataType: %s, Range: %s to %s - All data is complete",
                        dataType, startDate, endDate),
                "missing_data"
        );
    }

    /**
     * 工廠方法 - 部分補齊成功
     *
     * @param successCount 成功筆數
     * @param failedCount  失敗筆數
     * @param details      詳細錯誤訊息
     * @return 資料補齊異常實例
     */
    public static DataRepairException partialSuccess(int successCount, int failedCount, String details) {
        return new DataRepairException(
                M06ErrorCode.M06_DATA_SYNC_FAILED,
                "Data repair partially succeeded",
                String.format("Success: %d, Failed: %d, Details: %s",
                        successCount, failedCount, details),
                "repair"
        );
    }
}
