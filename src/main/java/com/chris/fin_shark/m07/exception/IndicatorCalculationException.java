package com.chris.fin_shark.m07.exception;

import com.chris.fin_shark.common.exception.BusinessException;
import com.chris.fin_shark.m07.enums.M07ErrorCode;

/**
 * 指標計算異常
 * <p>
 * 當指標計算過程中發生錯誤時拋出此異常
 * HTTP 狀態碼: 500
 * </p>
 *
 * @author chris
 * @since 1.0.0
 */
public class IndicatorCalculationException extends BusinessException {

    /**
     * 建構子
     *
     * @param message 錯誤訊息
     */
    public IndicatorCalculationException(String message) {
        super(M07ErrorCode.M07_CALCULATION_FAILED, message);
    }

    /**
     * 建構子（帶原因）
     *
     * @param message 錯誤訊息
     * @param cause   原因
     */
    public IndicatorCalculationException(String message, Throwable cause) {
        super(M07ErrorCode.M07_CALCULATION_FAILED, message, cause);
    }
}
