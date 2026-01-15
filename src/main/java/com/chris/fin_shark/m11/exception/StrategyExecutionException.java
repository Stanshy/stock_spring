package com.chris.fin_shark.m11.exception;

import com.chris.fin_shark.common.exception.BaseException;
import com.chris.fin_shark.m11.enums.M11ErrorCode;

/**
 * 策略執行例外
 *
 * @author chris
 * @since 1.0.0
 */
public class StrategyExecutionException extends BaseException {

    public StrategyExecutionException(String message) {
        super(M11ErrorCode.M11_EXECUTION_FAILED, message);
    }

    public StrategyExecutionException(String message, Throwable cause) {
        super(M11ErrorCode.M11_EXECUTION_FAILED, message, cause);
    }

    public StrategyExecutionException(String strategyId, String message) {
        super(M11ErrorCode.M11_EXECUTION_FAILED,
                String.format("策略執行失敗 [%s]: %s", strategyId, message));
    }
}
