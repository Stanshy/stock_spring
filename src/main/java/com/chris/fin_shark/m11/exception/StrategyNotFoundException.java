package com.chris.fin_shark.m11.exception;

import com.chris.fin_shark.common.exception.BaseException;
import com.chris.fin_shark.m11.enums.M11ErrorCode;

/**
 * 策略不存在例外
 *
 * @author chris
 * @since 1.0.0
 */
public class StrategyNotFoundException extends BaseException {

    public StrategyNotFoundException(String strategyId) {
        super(M11ErrorCode.M11_STRATEGY_NOT_FOUND,
                String.format("策略不存在: %s", strategyId));
    }

    public StrategyNotFoundException(String strategyId, Integer version) {
        super(M11ErrorCode.M11_STRATEGY_NOT_FOUND,
                String.format("策略不存在: %s (version: %d)", strategyId, version));
    }
}
