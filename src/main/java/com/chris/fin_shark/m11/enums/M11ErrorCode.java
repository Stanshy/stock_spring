package com.chris.fin_shark.m11.enums;

import com.chris.fin_shark.common.enums.IErrorCode;
import lombok.Getter;

/**
 * M11 量化策略模組專屬錯誤碼
 * <p>
 * 編碼規則: M11xxx
 * - M1101x: 策略相關
 * - M1102x: 因子相關
 * - M1103x: 執行相關
 * - M1104x: 參數相關
 * </p>
 *
 * @author chris
 * @since 1.0.0
 */
@Getter
public enum M11ErrorCode implements IErrorCode {

    // ========== 策略相關 M1101x ==========

    /**
     * 策略不存在
     */
    M11_STRATEGY_NOT_FOUND("M11011", 404, "Strategy not found"),

    /**
     * 策略定義格式錯誤
     */
    M11_STRATEGY_DEFINITION_INVALID("M11012", 400, "Invalid strategy definition format"),

    /**
     * 策略條件無效
     */
    M11_STRATEGY_CONDITION_INVALID("M11013", 400, "Invalid strategy condition"),

    /**
     * 策略版本衝突
     */
    M11_STRATEGY_VERSION_CONFLICT("M11014", 409, "Strategy version conflict"),

    /**
     * 策略已停用
     */
    M11_STRATEGY_INACTIVE("M11015", 400, "Strategy is inactive"),

    /**
     * 策略名稱已存在
     */
    M11_STRATEGY_NAME_EXISTS("M11016", 409, "Strategy name already exists"),

    /**
     * 策略狀態轉換無效
     */
    M11_STRATEGY_STATUS_TRANSITION_INVALID("M11017", 400, "Invalid strategy status transition"),

    // ========== 因子相關 M1102x ==========

    /**
     * 因子不存在
     */
    M11_FACTOR_NOT_FOUND("M11021", 404, "Factor not found"),

    /**
     * 因子數據不足
     */
    M11_FACTOR_DATA_INSUFFICIENT("M11022", 400, "Insufficient factor data"),

    /**
     * 因子類型不符
     */
    M11_FACTOR_TYPE_MISMATCH("M11023", 400, "Factor type mismatch"),

    /**
     * 因子數據載入失敗
     */
    M11_FACTOR_LOAD_FAILED("M11024", 500, "Failed to load factor data"),

    // ========== 執行相關 M1103x ==========

    /**
     * 策略執行失敗
     */
    M11_EXECUTION_FAILED("M11031", 500, "Strategy execution failed"),

    /**
     * 執行逾時
     */
    M11_EXECUTION_TIMEOUT("M11032", 504, "Strategy execution timeout"),

    /**
     * 執行記錄不存在
     */
    M11_EXECUTION_NOT_FOUND("M11033", 404, "Execution record not found"),

    /**
     * 執行中無法修改
     */
    M11_EXECUTION_IN_PROGRESS("M11034", 409, "Cannot modify while execution in progress"),

    // ========== 參數相關 M1104x ==========

    /**
     * 參數格式錯誤
     */
    M11_PARAM_FORMAT_INVALID("M11041", 400, "Invalid parameter format"),

    /**
     * 日期範圍無效
     */
    M11_DATE_RANGE_INVALID("M11042", 400, "Invalid date range"),

    /**
     * 優化配置無效
     */
    M11_OPTIMIZATION_CONFIG_INVALID("M11043", 400, "Invalid optimization configuration"),

    /**
     * 信號不存在
     */
    M11_SIGNAL_NOT_FOUND("M11044", 404, "Strategy signal not found");

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

    M11ErrorCode(String code, int httpStatus, String defaultMessage) {
        this.code = code;
        this.httpStatus = httpStatus;
        this.defaultMessage = defaultMessage;
    }
}
