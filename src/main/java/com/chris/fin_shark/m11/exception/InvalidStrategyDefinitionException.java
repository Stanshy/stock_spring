package com.chris.fin_shark.m11.exception;

import com.chris.fin_shark.common.exception.BaseException;
import com.chris.fin_shark.m11.enums.M11ErrorCode;

/**
 * 無效策略定義例外
 *
 * @author chris
 * @since 1.0.0
 */
public class InvalidStrategyDefinitionException extends BaseException {

    public InvalidStrategyDefinitionException(String message) {
        super(M11ErrorCode.M11_STRATEGY_DEFINITION_INVALID, message);
    }

    public InvalidStrategyDefinitionException(String field, String detail) {
        super(M11ErrorCode.M11_STRATEGY_DEFINITION_INVALID,
                String.format("策略定義無效: %s", detail),
                detail, field, "請檢查策略定義格式");
    }

    public static InvalidStrategyDefinitionException missingField(String field) {
        return new InvalidStrategyDefinitionException(field,
                String.format("缺少必要欄位: %s", field));
    }

    public static InvalidStrategyDefinitionException invalidCondition(String reason) {
        return new InvalidStrategyDefinitionException("conditions", reason);
    }
}
