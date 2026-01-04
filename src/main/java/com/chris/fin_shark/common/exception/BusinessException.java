package com.chris.fin_shark.common.exception;

import com.chris.fin_shark.common.enums.IErrorCode;

/**
 * 業務邏輯異常
 * <p>
 * 用於業務規則驗證失敗、業務邏輯錯誤等場景
 * </p>
 *
 * 使用場景:
 * - 業務規則不滿足
 * - 資料狀態不允許執行操作
 * - 業務流程錯誤
 *
 * @author chris
 * @since 2025-12-24
 */
public class BusinessException extends BaseException {

    /**
     * 建構子 - 使用錯誤碼
     *
     * @param errorCode 錯誤碼
     */
    public BusinessException(IErrorCode errorCode) {
        super(errorCode);
    }

    /**
     * 建構子 - 自訂訊息
     *
     * @param errorCode 錯誤碼
     * @param message   錯誤訊息
     */
    public BusinessException(IErrorCode errorCode, String message) {
        super(errorCode, message);
    }

    /**
     * 建構子 - 包含詳情
     *
     * @param errorCode 錯誤碼
     * @param message   錯誤訊息
     * @param details   錯誤詳情
     */
    public BusinessException(IErrorCode errorCode, String message, String details) {
        super(errorCode, message, details);
    }

    /**
     * 建構子 - 包含原因異常
     *
     * @param errorCode 錯誤碼
     * @param message   錯誤訊息
     * @param cause     原因異常
     */
    public BusinessException(IErrorCode errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }

    /**
     * 建構子 - 包含欄位和建議（無 details）
     * <p>
     * 適用於不需要詳細錯誤描述的場景
     * </p>
     *
     * @param errorCode  錯誤碼
     * @param message    錯誤訊息
     * @param field      錯誤欄位
     * @param suggestion 建議解決方案
     */
    public BusinessException(IErrorCode errorCode, String message,
                             String field, String suggestion) {
        super(errorCode, message, null, field, suggestion);
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
    public BusinessException(IErrorCode errorCode, String message, String details,
                             String field, String suggestion) {
        super(errorCode, message, details, field, suggestion);
    }
}
