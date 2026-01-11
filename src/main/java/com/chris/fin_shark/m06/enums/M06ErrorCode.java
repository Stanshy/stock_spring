package com.chris.fin_shark.m06.enums;

import com.chris.fin_shark.common.enums.IErrorCode;
import lombok.Getter;

/**
 * M06 資料管理模組專屬錯誤碼
 * <p>
 * 編碼規則: M06xxx
 * - M0601x: 股票相關
 * - M0602x: 股價相關
 * - M0603x: 財報相關
 * - M0604x: 三大法人相關
 * - M0605x: 融資融券相關
 * - M0606x: 交易日曆相關
 * - M0607x: 資料同步相關
 * - M0608x: 資料品質相關
 * </p>
 *
 * @author Chris
 * @since 1.0.0
 */
@Getter
public enum M06ErrorCode implements IErrorCode {

    // ========== 股票相關 M0601x ==========

    /**
     * 股票不存在
     */
    M06_STOCK_NOT_FOUND("M06011", 404, "Stock not found"),

    /**
     * 股票已存在
     */
    M06_STOCK_ALREADY_EXISTS("M06012", 409, "Stock already exists"),

    /**
     * 無效的股票代碼格式
     */
    M06_INVALID_STOCK_ID("M06013", 400, "Invalid stock ID format"),

    // ========== 股價相關 M0602x ==========

    /**
     * 股價資料不存在
     */
    M06_STOCK_PRICE_NOT_FOUND("M06021", 404, "Stock price not found"),

    /**
     * 無效的股價資料
     */
    M06_INVALID_PRICE_DATA("M06022", 422, "Invalid stock price data"),

    /**
     * 股價資料已存在
     */
    M06_PRICE_ALREADY_EXISTS("M06023", 409, "Stock price already exists"),

    /**
     * 四價關係驗證失敗（low <= open,close <= high）
     */
    M06_FOUR_PRICE_VIOLATION("M06024", 422, "Four price relationship violation"),

    // ========== 財報相關 M0603x ==========

    /**
     * 財務報表不存在
     */
    M06_FINANCIAL_STATEMENT_NOT_FOUND("M06031", 404, "Financial statement not found"),

    /**
     * 無效的財務資料
     */
    M06_INVALID_FINANCIAL_DATA("M06032", 422, "Invalid financial data"),

    /**
     * 資產負債表不平衡（Assets ≠ Liabilities + Equity）
     */
    M06_BALANCE_SHEET_NOT_BALANCED("M06033", 422, "Balance sheet equation not satisfied"),

    /**
     * 財報資料解析失敗
     */
    M06_FINANCIAL_PARSE_ERROR("M06034", 500, "Financial statement parse error"),

    /**
     * 無效的財報期間
     */
    M06_FINANCIAL_PERIOD_INVALID("M06035", 400, "Invalid financial period"),

    /**
     * 財報資料不完整
     */
    M06_FINANCIAL_DATA_INCOMPLETE("M06036", 422, "Financial data incomplete"),

    // ========== 三大法人相關 M0604x ==========

    /**
     * 三大法人資料不存在
     */
    M06_INSTITUTIONAL_TRADING_NOT_FOUND("M06041", 404, "Institutional trading data not found"),

    /**
     * 無效的法人交易資料
     */
    M06_INVALID_TRADING_DATA("M06042", 422, "Invalid trading data"),

    /**
     * 法人資料同步失敗
     */
    M06_INSTITUTIONAL_SYNC_FAILED("M06043", 500, "Institutional trading sync failed"),

    /**
     * 法人資料解析失敗
     */
    M06_INSTITUTIONAL_PARSE_ERROR("M06044", 500, "Institutional trading parse error"),

    // ========== 融資融券相關 M0605x ==========

    /**
     * 融資融券資料不存在
     */
    M06_MARGIN_TRADING_NOT_FOUND("M06051", 404, "Margin trading data not found"),

    /**
     * 無效的融資融券資料
     */
    M06_INVALID_MARGIN_DATA("M06052", 422, "Invalid margin trading data"),

    /**
     * 融資融券資料同步失敗
     */
    M06_MARGIN_SYNC_FAILED("M06053", 500, "Margin trading sync failed"),

    /**
     * 融資融券資料解析失敗
     */
    M06_MARGIN_PARSE_ERROR("M06054", 500, "Margin trading parse error"),


    // ========== 交易日曆相關 M0606x ==========

    /**
     * 交易日曆資料不存在
     */
    M06_TRADING_CALENDAR_NOT_FOUND("M06061", 404, "Trading calendar not found"),

    /**
     * 非交易日
     */
    M06_NOT_TRADING_DAY("M06062", 400, "Not a trading day"),

    // ========== 資料同步相關 M0607x ==========

    /**
     * 從外部資料源取得資料失敗
     */
    M06_DATA_FETCH_FAILED("M06071", 502, "Failed to fetch data from external source"),

    /**
     * 資料同步失敗
     */
    M06_DATA_SYNC_FAILED("M06072", 500, "Data synchronization failed"),

    /**
     * 外部 API 逾時
     */
    M06_EXTERNAL_API_TIMEOUT("M06073", 504, "External API timeout"),

    // ========== 資料品質相關 M0608x ==========

    /**
     * 資料驗證失敗
     */
    M06_DATA_VALIDATION_FAILED("M06081", 422, "Data validation failed"),

    /**
     * 資料品質檢核失敗
     */
    M06_DATA_QUALITY_CHECK_FAILED("M06082", 422, "Data quality check failed"),

    /**
     * 偵測到重複資料
     */
    M06_DUPLICATE_DATA("M06083", 409, "Duplicate data detected"),

    /**
     * 品質檢核執行失敗
     */
    M06_QUALITY_CHECK_EXECUTION_FAILED("M06084", 500, "Quality check execution failed"),

    /**
     * 無效的品質檢核規則
     */
    M06_QUALITY_RULE_INVALID("M06085", 400, "Invalid quality check rule"),

    /**
     * 品質檢核執行逾時
     */
    M06_QUALITY_CHECK_TIMEOUT("M06086", 504, "Quality check timeout"),


    /**
     * Job 執行記錄不存在
     */
    M06_JOB_EXECUTION_NOT_FOUND("M06091", 404, "Job execution record not found"),

    /**
     * 功能尚未實作
     */
    M06_FEATURE_NOT_IMPLEMENTED("M06092", 501, "Feature not implemented yet"),

    /**
     * Job 已在執行中
     */
    M06_JOB_ALREADY_RUNNING("M06093", 409, "Job is already running"),

    // ========== 資料補齊相關 M0610x（新範圍）==========

    /**
     * 無效的補齊日期範圍
     */
    M06_REPAIR_DATE_RANGE_INVALID("M06101", 400, "Invalid repair date range"),

    /**
     * 無效的補齊策略
     */
    M06_REPAIR_STRATEGY_INVALID("M06102", 400, "Invalid repair strategy"),

    /**
     * 資料補齊執行失敗
     */
    M06_REPAIR_EXECUTION_FAILED("M06103", 500, "Data repair execution failed"),

    /**
     * 沒有需要補齊的資料
     */
    M06_REPAIR_NO_MISSING_DATA("M06104", 200, "No missing data to repair"),

    /**
     * 部分資料補齊成功
     */
    M06_REPAIR_PARTIAL_SUCCESS("M06105", 207, "Partial data repair success");


    /**
     * 錯誤碼
     */
    private final String code;

    /**
     * HTTP 狀態碼
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
    M06ErrorCode(String code, int httpStatus, String defaultMessage) {
        this.code = code;
        this.httpStatus = httpStatus;
        this.defaultMessage = defaultMessage;
    }
}