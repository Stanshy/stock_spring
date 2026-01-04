package com.chris.fin_shark.common.constant;

/**
 * 訊息常量
 *
 * 定義系統中使用的各種訊息文字
 *
 * @author chris
 * @since 2025-12-24
 */
public final class MessageConstants {

    private MessageConstants() {
        throw new UnsupportedOperationException("Constant class cannot be instantiated");
    }

    // ========================================================================
    // 成功訊息
    // ========================================================================

    public static final String SUCCESS = "Success";
    public static final String CREATED_SUCCESSFULLY = "Created successfully";
    public static final String UPDATED_SUCCESSFULLY = "Updated successfully";
    public static final String DELETED_SUCCESSFULLY = "Deleted successfully";
    public static final String OPERATION_SUCCESSFUL = "Operation successful";


    // ========================================================================
    // 查詢相關訊息
    // ========================================================================

    public static final String QUERY_SUCCESS = "Query successful";
    public static final String NO_DATA_FOUND = "No data found";
    public static final String DATA_RETRIEVED = "Data retrieved successfully";


    // ========================================================================
    // 錯誤訊息
    // ========================================================================

    public static final String INTERNAL_ERROR = "Internal server error";
    public static final String INVALID_REQUEST = "Invalid request parameters";
    public static final String RESOURCE_NOT_FOUND = "Resource not found";
    public static final String UNAUTHORIZED = "Authentication required";
    public static final String FORBIDDEN = "Access denied";
    public static final String VALIDATION_FAILED = "Validation failed";


    // ========================================================================
    // 股票相關訊息
    // ========================================================================

    public static final String STOCK_NOT_FOUND = "Stock not found";
    public static final String STOCK_CREATED = "Stock created successfully";
    public static final String STOCK_UPDATED = "Stock updated successfully";
    public static final String STOCK_DELETED = "Stock deleted successfully";

    // TODO: M06 開發時補充更多業務相關訊息


    // ========================================================================
    // Job 相關訊息
    // ========================================================================

    public static final String JOB_STARTED = "Job started";
    public static final String JOB_COMPLETED = "Job completed successfully";
    public static final String JOB_FAILED = "Job execution failed";
    public static final String JOB_CANCELLED = "Job cancelled";


    // ========================================================================
    // 資料同步訊息
    // ========================================================================

    public static final String SYNC_STARTED = "Data sync started";
    public static final String SYNC_COMPLETED = "Data sync completed";
    public static final String SYNC_FAILED = "Data sync failed";


    // ========================================================================
    // 驗證相關訊息
    // ========================================================================

    public static final String VALIDATION_STOCK_ID = "Invalid stock ID format";
    public static final String VALIDATION_DATE_FORMAT = "Invalid date format";
    public static final String VALIDATION_PRICE_RANGE = "Price is out of valid range";
    public static final String VALIDATION_FOUR_PRICE = "Four price relationship validation failed";

    // TODO: 各模組開發時，可以在此補充訊息常量
}
