package com.chris.fin_shark.m06.exception;

import com.chris.fin_shark.common.exception.BusinessException;
import com.chris.fin_shark.m06.enums.M06ErrorCode;

/**
 * 功能未實作異常
 * <p>
 * 當呼叫尚未實作的功能時拋出此異常
 * HTTP 狀態碼: 501 (Not Implemented)
 * </p>
 *
 * 使用場景:
 * - P1/P2 功能尚未開發
 * - 預留的 API 端點
 * - 計劃中的功能
 *
 * @author Chris
 * @since 1.0.0
 */
public class FeatureNotImplementedException extends BusinessException {

    /**
     * 私有建構子
     *
     * @param featureName 功能名稱
     */
    private FeatureNotImplementedException(String featureName) {
        super(
                M06ErrorCode.M06_FEATURE_NOT_IMPLEMENTED,
                String.format("Feature '%s' is not implemented yet", featureName),
                "feature",
                "This feature is planned for future release. Please contact support for more information"
        );
    }

    /**
     * 私有建構子 - 包含計劃版本
     *
     * @param featureName    功能名稱
     * @param plannedVersion 計劃版本
     */
    private FeatureNotImplementedException(String featureName, String plannedVersion) {
        super(
                M06ErrorCode.M06_FEATURE_NOT_IMPLEMENTED,
                String.format("Feature '%s' is not implemented yet (Planned: %s)",
                        featureName, plannedVersion),
                "feature",
                String.format("This feature is planned for version %s. Please contact support for more information",
                        plannedVersion)
        );
    }

    /**
     * 工廠方法 - 建立功能未實作異常
     * <p>
     * 使用範例:
     * <pre>
     * public JobExecutionDTO triggerFinancialSync() {
     *     throw FeatureNotImplementedException.of("財報同步");
     * }
     * </pre>
     * </p>
     *
     * @param featureName 功能名稱
     * @return 功能未實作異常實例
     */
    public static FeatureNotImplementedException of(String featureName) {
        return new FeatureNotImplementedException(featureName);
    }

    /**
     * 工廠方法 - 建立功能未實作異常（包含計劃版本）
     * <p>
     * 使用範例:
     * <pre>
     * public JobExecutionDTO triggerFinancialSync() {
     *     throw FeatureNotImplementedException.of("財報同步", "P1");
     * }
     * </pre>
     * </p>
     *
     * @param featureName    功能名稱
     * @param plannedVersion 計劃版本（例如：P1, v2.0）
     * @return 功能未實作異常實例
     */
    public static FeatureNotImplementedException of(String featureName, String plannedVersion) {
        return new FeatureNotImplementedException(featureName, plannedVersion);
    }
}