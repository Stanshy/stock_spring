package com.chris.fin_shark.m08.enums;

import com.chris.fin_shark.common.enums.IErrorCode;
import lombok.Getter;

/**
 * M08 基本面分析模組專屬錯誤碼
 * <p>
 * 編碼規則: M08xxx
 * - M0801x: 財務指標相關
 * - M0802x: 綜合評分相關
 * - M0803x: 財務警示相關
 * - M0804x: 計算相關
 * - M0805x: Job 相關
 * </p>
 *
 * @author chris
 * @since 1.0.0
 */
@Getter
public enum M08ErrorCode implements IErrorCode {

    // ========== 財務指標相關 M0801x ==========

    /**
     * 財務指標不存在
     */
    M08_INDICATOR_NOT_FOUND("M08011", 404, "Financial indicator not found"),

    /**
     * 無效的指標名稱
     */
    M08_INVALID_INDICATOR_NAME("M08012", 400, "Invalid indicator name"),

    /**
     * 無效的時間範圍
     */
    M08_INVALID_TIME_RANGE("M08013", 400, "Invalid time range"),

    // ========== 綜合評分相關 M0802x ==========

    /**
     * 綜合評分不存在
     */
    M08_SCORE_NOT_FOUND("M08021", 404, "Financial score not found"),

    // ========== 財務警示相關 M0803x ==========

    /**
     * 財務警示不存在
     */
    M08_ALERT_NOT_FOUND("M08031", 404, "Financial alert not found"),

    // ========== 計算相關 M0804x ==========

    /**
     * 財務指標計算失敗
     */
    M08_CALCULATION_FAILED("M08041", 422, "Financial indicator calculation failed"),

    /**
     * 財務資料不完整
     */
    M08_DATA_INCOMPLETE("M08042", 422, "Financial data incomplete"),

    /**
     * 計算資料來源缺失
     */
    M08_DATA_SOURCE_MISSING("M08043", 422, "Required data source missing"),

    // ========== Job 相關 M0805x ==========

    /**
     * Job 已在執行中
     */
    M08_JOB_ALREADY_RUNNING("M08051", 409, "Job is already running"),

    /**
     * Job 執行失敗
     */
    M08_JOB_EXECUTION_FAILED("M08052", 500, "Job execution failed"),

    // ========== 通用錯誤 M0809x ==========

    /**
     * 功能尚未實作
     */
    M08_FEATURE_NOT_IMPLEMENTED("M08091", 501, "Feature not implemented yet");

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
     */
    M08ErrorCode(String code, int httpStatus, String defaultMessage) {
        this.code = code;
        this.httpStatus = httpStatus;
        this.defaultMessage = defaultMessage;
    }
}
