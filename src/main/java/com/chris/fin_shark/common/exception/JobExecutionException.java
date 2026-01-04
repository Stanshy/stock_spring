package com.chris.fin_shark.common.exception;

import com.chris.fin_shark.common.enums.ErrorCode;
import com.chris.fin_shark.common.enums.IErrorCode;

/**
 * Job 執行異常
 * <p>
 * 用於 Job 執行過程中發生錯誤的場景
 * </p>
 *
 * 使用場景:
 * - Job 執行失敗
 * - Job 參數錯誤
 * - Job 超時
 * - Job 被中斷
 *
 * @author chris
 * @since 2025-12-24
 */
public class JobExecutionException extends BaseException {

    /**
     * Job 執行 ID
     */
    private final Long executionId;

    /**
     * Job 名稱
     */
    private final String jobName;

    /**
     * 建構子 - 基本版本
     *
     * @param message 錯誤訊息
     * @param jobName Job 名稱
     */
    public JobExecutionException(String message, String jobName) {
        super(ErrorCode.INTERNAL_ERROR, message);
        this.executionId = null;
        this.jobName = jobName;
    }

    /**
     * 建構子 - 包含執行 ID
     *
     * @param errorCode   錯誤碼
     * @param message     錯誤訊息
     * @param executionId Job 執行 ID
     * @param jobName     Job 名稱
     */
    public JobExecutionException(IErrorCode errorCode, String message,
                                 Long executionId, String jobName) {
        super(errorCode, message, "Job execution failed: " + jobName);
        this.executionId = executionId;
        this.jobName = jobName;
    }

    /**
     * 建構子 - 包含原因異常
     *
     * @param errorCode   錯誤碼
     * @param message     錯誤訊息
     * @param executionId Job 執行 ID
     * @param jobName     Job 名稱
     * @param cause       原因異常
     */
    public JobExecutionException(IErrorCode errorCode, String message,
                                 Long executionId, String jobName, Throwable cause) {
        super(errorCode, message, cause);
        this.executionId = executionId;
        this.jobName = jobName;
    }

    /**
     * 靜態工廠方法 - Job 執行失敗
     * <p>
     * 使用 Common 通用錯誤碼
     * 各模組可定義自己的 Job 錯誤碼
     * </p>
     *
     * @param jobName Job 名稱
     * @param cause   原因異常
     * @return JobExecutionException
     */
    public static JobExecutionException executionFailed(String jobName, Throwable cause) {
        return new JobExecutionException(
                ErrorCode.INTERNAL_ERROR,
                "Job execution failed",
                null,
                jobName,
                cause
        );
    }

    /**
     * 靜態工廠方法 - Job 執行失敗（帶執行 ID）
     *
     * @param jobName     Job 名稱
     * @param executionId Job 執行 ID
     * @param cause       原因異常
     * @return JobExecutionException
     */
    public static JobExecutionException executionFailed(String jobName, Long executionId, Throwable cause) {
        return new JobExecutionException(
                ErrorCode.INTERNAL_ERROR,
                "Job execution failed",
                executionId,
                jobName,
                cause
        );
    }

    // Getters

    /**
     * 取得 Job 執行 ID
     *
     * @return Job 執行 ID
     */
    public Long getExecutionId() {
        return executionId;
    }

    /**
     * 取得 Job 名稱
     *
     * @return Job 名稱
     */
    public String getJobName() {
        return jobName;
    }
}
