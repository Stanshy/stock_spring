package com.chris.fin_shark.common.enums;

/**
 * 錯誤碼介面
 * <p>
 * 定義所有錯誤碼必須實作的方法
 * 支援 Common 通用錯誤碼（00xxx）和各模組專屬錯誤碼（M06xxx, M07xxx...）
 * </p>
 *
 * @author Chris
 * @since 1.0.0
 */
public interface IErrorCode {

    /**
     * 取得錯誤碼
     * <p>
     * 格式範例:
     * - 通用錯誤: 00001, 00002
     * - M06 模組: M06011, M06012
     * - M07 模組: M07011, M07012
     * </p>
     *
     * @return 錯誤碼字串
     */
    String getCode();

    /**
     * 取得 HTTP 狀態碼
     * <p>
     * 常用狀態碼:
     * - 400: Bad Request
     * - 404: Not Found
     * - 422: Unprocessable Entity
     * - 500: Internal Server Error
     * </p>
     *
     * @return HTTP 狀態碼
     */
    int getHttpStatus();

    /**
     * 取得預設錯誤訊息
     * <p>
     * 當未提供自訂訊息時使用此預設訊息
     * </p>
     *
     * @return 預設錯誤訊息
     */
    String getDefaultMessage();
}