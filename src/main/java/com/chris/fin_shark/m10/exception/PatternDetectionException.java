package com.chris.fin_shark.m10.exception;

import com.chris.fin_shark.common.exception.BaseException;
import com.chris.fin_shark.m10.enums.M10ErrorCode;

/**
 * 型態偵測異常
 *
 * @author chris
 * @since 1.0.0
 */
public class PatternDetectionException extends BaseException {

    public PatternDetectionException(M10ErrorCode errorCode) {
        super(errorCode);
    }

    public PatternDetectionException(M10ErrorCode errorCode, String message) {
        super(errorCode, message);
    }

    public PatternDetectionException(M10ErrorCode errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }

    // === 靜態工廠方法 ===

    /**
     * 股票不存在
     */
    public static PatternDetectionException stockNotFound(String stockId) {
        return new PatternDetectionException(
                M10ErrorCode.STOCK_NOT_FOUND,
                String.format("股票代碼 '%s' 不存在", stockId)
        );
    }

    /**
     * 資料不足
     */
    public static PatternDetectionException insufficientData(String stockId, int required, int actual) {
        return new PatternDetectionException(
                M10ErrorCode.INSUFFICIENT_DATA,
                String.format("股票 '%s' 資料不足：需要 %d 天，實際 %d 天", stockId, required, actual)
        );
    }

    /**
     * 日期超出範圍
     */
    public static PatternDetectionException dateOutOfRange(String message) {
        return new PatternDetectionException(M10ErrorCode.DATE_OUT_OF_RANGE, message);
    }

    /**
     * 不支援的型態
     */
    public static PatternDetectionException unsupportedPattern(String patternType) {
        return new PatternDetectionException(
                M10ErrorCode.UNSUPPORTED_PATTERN,
                String.format("不支援的型態類型：%s", patternType)
        );
    }

    /**
     * 引擎錯誤
     */
    public static PatternDetectionException engineError(String message, Throwable cause) {
        return new PatternDetectionException(M10ErrorCode.ENGINE_ERROR, message, cause);
    }

    /**
     * 偵測器不存在
     */
    public static PatternDetectionException detectorNotFound(String detectorName) {
        return new PatternDetectionException(
                M10ErrorCode.DETECTOR_NOT_FOUND,
                String.format("找不到偵測器：%s", detectorName)
        );
    }

    /**
     * 偵測器執行失敗
     */
    public static PatternDetectionException detectorError(String detectorName, Throwable cause) {
        return new PatternDetectionException(
                M10ErrorCode.DETECTOR_ERROR,
                String.format("偵測器 '%s' 執行失敗", detectorName),
                cause
        );
    }

    /**
     * 無效的強度閾值
     */
    public static PatternDetectionException invalidStrengthThreshold(int threshold) {
        return new PatternDetectionException(
                M10ErrorCode.INVALID_STRENGTH_THRESHOLD,
                String.format("型態強度閾值必須在 0-100 之間，實際值：%d", threshold)
        );
    }
}
