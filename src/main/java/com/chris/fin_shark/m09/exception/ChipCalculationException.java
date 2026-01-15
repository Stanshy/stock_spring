package com.chris.fin_shark.m09.exception;

import com.chris.fin_shark.common.exception.BusinessException;
import com.chris.fin_shark.m09.enums.M09ErrorCode;

/**
 * 籌碼計算異常
 * <p>
 * 當籌碼計算過程中發生錯誤時拋出此異常
 * HTTP 狀態碼: 500
 * </p>
 *
 * @author chris
 * @since 1.0.0
 */
public class ChipCalculationException extends BusinessException {

    /**
     * 建構子
     *
     * @param message 錯誤訊息
     */
    public ChipCalculationException(String message) {
        super(M09ErrorCode.M09_CALCULATION_FAILED, message);
    }

    /**
     * 建構子（帶原因）
     *
     * @param message 錯誤訊息
     * @param cause   原因
     */
    public ChipCalculationException(String message, Throwable cause) {
        super(M09ErrorCode.M09_CALCULATION_FAILED, message, cause);
    }

    /**
     * 工廠方法 - 資料不足
     *
     * @param stockId      股票代碼
     * @param required     需要的天數
     * @param actual       實際天數
     * @return 異常實例
     */
    public static ChipCalculationException insufficientData(String stockId, int required, int actual) {
        return new ChipCalculationException(
                String.format("Insufficient data for stock '%s': required %d days, actual %d days",
                        stockId, required, actual)
        );
    }

    /**
     * 工廠方法 - 計算器錯誤
     *
     * @param calculatorName 計算器名稱
     * @param cause          原因
     * @return 異常實例
     */
    public static ChipCalculationException calculatorError(String calculatorName, Throwable cause) {
        return new ChipCalculationException(
                String.format("Calculator '%s' failed: %s", calculatorName, cause.getMessage()),
                cause
        );
    }
}
