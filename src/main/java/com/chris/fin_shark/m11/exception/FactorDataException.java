package com.chris.fin_shark.m11.exception;

import com.chris.fin_shark.common.exception.BaseException;
import com.chris.fin_shark.m11.enums.M11ErrorCode;

/**
 * 因子數據例外
 *
 * @author chris
 * @since 1.0.0
 */
public class FactorDataException extends BaseException {

    public FactorDataException(String factorId) {
        super(M11ErrorCode.M11_FACTOR_NOT_FOUND,
                String.format("因子不存在: %s", factorId));
    }

    public FactorDataException(M11ErrorCode errorCode, String message) {
        super(errorCode, message);
    }

    public static FactorDataException notFound(String factorId) {
        return new FactorDataException(factorId);
    }

    public static FactorDataException insufficientData(String factorId, String stockId) {
        return new FactorDataException(
                M11ErrorCode.M11_FACTOR_DATA_INSUFFICIENT,
                String.format("因子數據不足: factor=%s, stock=%s", factorId, stockId));
    }

    public static FactorDataException loadFailed(String message) {
        return new FactorDataException(M11ErrorCode.M11_FACTOR_LOAD_FAILED, message);
    }
}
