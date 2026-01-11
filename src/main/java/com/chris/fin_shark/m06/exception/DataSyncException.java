package com.chris.fin_shark.m06.exception;

import com.chris.fin_shark.common.exception.BusinessException;
import com.chris.fin_shark.m06.enums.M06ErrorCode;

/**
 * 資料同步異常
 * <p>
 * 當資料同步過程中發生錯誤時拋出此異常
 * 包含外部 API 呼叫失敗、資料解析錯誤等
 * HTTP 狀態碼: 500/502/504
 * </p>
 *
 * @author chris
 * @since 1.0.0
 */
public class DataSyncException extends BusinessException {

    /**
     * 私有建構子
     *
     * @param errorCode M06 錯誤碼
     * @param message   錯誤訊息
     * @param details   詳細錯誤訊息
     * @param field     錯誤欄位
     */
    private DataSyncException(M06ErrorCode errorCode, String message,
                              String details, String field) {
        super(errorCode, message, details, field, "Please check the sync configuration and retry");
    }

    /**
     * 工廠方法 - 外部 API 呼叫失敗
     *
     * @param apiName 外部 API 名稱
     * @param details 詳細錯誤訊息
     * @return 資料同步異常實例
     */
    public static DataSyncException fetchFailed(String apiName, String details) {
        return new DataSyncException(
                M06ErrorCode.M06_DATA_FETCH_FAILED,
                "Failed to fetch data from external API",
                String.format("API: %s, Error: %s", apiName, details),
                "external_api"
        );
    }

    /**
     * 工廠方法 - 資料同步失敗
     *
     * @param dataType 資料類型
     * @param details  詳細錯誤訊息
     * @return 資料同步異常實例
     */
    public static DataSyncException syncFailed(String dataType, String details) {
        return new DataSyncException(
                M06ErrorCode.M06_DATA_SYNC_FAILED,
                "Data synchronization failed",
                String.format("DataType: %s, Error: %s", dataType, details),
                "sync"
        );
    }

    /**
     * 工廠方法 - 外部 API 逾時
     *
     * @param apiName   外部 API 名稱
     * @param timeoutMs 逾時時間（毫秒）
     * @return 資料同步異常實例
     */
    public static DataSyncException apiTimeout(String apiName, long timeoutMs) {
        return new DataSyncException(
                M06ErrorCode.M06_EXTERNAL_API_TIMEOUT,
                "External API timeout",
                String.format("API: %s, Timeout: %dms", apiName, timeoutMs),
                "external_api"
        );
    }

    /**
     * 工廠方法 - 法人資料同步失敗
     *
     * @param stockId 股票代碼
     * @param date    交易日期
     * @param details 詳細錯誤訊息
     * @return 資料同步異常實例
     */
    public static DataSyncException institutionalSyncFailed(String stockId, String date, String details) {
        return new DataSyncException(
                M06ErrorCode.M06_DATA_SYNC_FAILED,
                "Institutional trading data sync failed",
                String.format("Stock: %s, Date: %s, Error: %s", stockId, date, details),
                "institutional_trading"
        );
    }

    /**
     * 工廠方法 - 融資融券資料同步失敗
     *
     * @param stockId 股票代碼
     * @param date    交易日期
     * @param details 詳細錯誤訊息
     * @return 資料同步異常實例
     */
    public static DataSyncException marginSyncFailed(String stockId, String date, String details) {
        return new DataSyncException(
                M06ErrorCode.M06_DATA_SYNC_FAILED,
                "Margin trading data sync failed",
                String.format("Stock: %s, Date: %s, Error: %s", stockId, date, details),
                "margin_trading"
        );
    }

    /**
     * 工廠方法 - 財報資料同步失敗
     *
     * @param stockId 股票代碼
     * @param year    年度
     * @param quarter 季度
     * @param details 詳細錯誤訊息
     * @return 資料同步異常實例
     */
    public static DataSyncException financialSyncFailed(String stockId, int year, int quarter, String details) {
        return new DataSyncException(
                M06ErrorCode.M06_DATA_SYNC_FAILED,
                "Financial statement sync failed",
                String.format("Stock: %s, Period: %dQ%d, Error: %s", stockId, year, quarter, details),
                "financial_statement"
        );
    }
}
